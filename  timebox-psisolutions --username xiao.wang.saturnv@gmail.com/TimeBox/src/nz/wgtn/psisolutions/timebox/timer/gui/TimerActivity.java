package nz.wgtn.psisolutions.timebox.timer.gui;

import java.util.ArrayList;

import nz.wgtn.psisolutions.timebox.Constants;
import nz.wgtn.psisolutions.timebox.Debug;
import nz.wgtn.psisolutions.timebox.HelpUtils;
import nz.wgtn.psisolutions.timebox.R;
import nz.wgtn.psisolutions.timebox.Utils;
import nz.wgtn.psisolutions.timebox.preferences.Preferences;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroDbAdapter;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroService;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimer;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimerCallback;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class TimerActivity extends Activity {

	public static final String TAG = "timebox.TimerActivity";

	private final int DIALOG_PAUSE_ID = 1;
	private long timeRemaining;

	private TextView timerDisplay, presetName, presetState, nextPresetLabel;
	private ImageButton pauseResumeButton;
	private ImageButton	cancelButton;
	private ImageButton soundButton;
	private Button nextDropdown;
	private Dialog pauseOverlay;
	private PomodoroPreset preset, nextPreset;
	private ArrayList<PomodoroPreset> presetList;

	private TimerView mTimerView;

	//service stuff
	private PomodoroService.PomoBinder serviceBinder;
	private boolean mIsBound;

	//wake lock
	private PowerManager.WakeLock wakeLock;
	private boolean wakeLockEnabled;

	private ServiceConnection mConnection = new ServiceConnection (){

		@Override
		public void onServiceConnected (ComponentName name, IBinder service){
			serviceBinder = ((PomodoroService.PomoBinder)service);
			mIsBound = true;

			serviceBinder.setPreset(preset);
			serviceBinder.start();
			PomodoroTimer timer = serviceBinder.getTimer();
			timer.attachCallback(new TimerCallback());
			mTimerView.setTimer(timer);

			Debug.d(TAG, "onServiceConnected(): mIsBound = " + mIsBound);
		}

		@Override
		public void onServiceDisconnected (ComponentName name){}
	};
	// service stuff end

	class IncomingHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			setTimerDisplay(0, msg.arg1, msg.arg2);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.timer);
		preset = getIntent().getParcelableExtra(Constants.KEY_POMODORO_PRESET);

		Preferences.reloadPreferences(this); // load preferences

		//fetch components
		timerDisplay = (TextView)findViewById(R.id.timerDisplay);
		presetName = (TextView)findViewById(R.id.preset_name);
		presetName.setText(preset.getPresetName());
		presetName.requestFocus();
		//force focus
		presetName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean focus) {
				if(!focus)
					v.requestFocus();
			}
		});
		presetState = (TextView)findViewById(R.id.preset_state);
		pauseResumeButton = (ImageButton)findViewById(R.id.btn_pause);
		cancelButton = (ImageButton)findViewById(R.id.btn_stop);
		soundButton = (ImageButton)findViewById(R.id.btn_sound);
		validateSoundButton();
		nextPresetLabel = (TextView)findViewById(R.id.next_preset);
		nextDropdown = (Button)findViewById(R.id.next_pomodoro);
		nextDropdown.setText(preset.getPresetName());
		registerForContextMenu(nextDropdown);
		mTimerView = (TimerView)findViewById(R.id.timer_view);

		//Check if wake lock is enabled
		wakeLockEnabled = Preferences.isWakeLockEnabled();
	}

	private void validateSoundButton(){
		if(Preferences.isSilent()){
			soundButton.setImageResource(R.drawable.btn_muted);
		} else{
			soundButton.setImageResource(R.drawable.btn_unmuted);
		}
	}

	public void soundClicked(View view){
		Preferences.toggleSilent();
		validateSoundButton();
	}
	
	public void nextPomodoroClicked(View view){
		view.showContextMenu();
	}
	
	@Override
	public void onCreateContextMenu(final ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		//create menu item click listener
		MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {	
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				for(int i=0; i<menu.size(); i++){
					menu.getItem(i).setChecked(false);
				}
				item.setChecked(true);
				return false;
			}
		};
		//fetch presets
		if(presetList == null)
			fetchPresetList();
		menu.setHeaderTitle(R.string.label_next_pomodoro);
		MenuItem first = menu.add(Menu.NONE, -1, Menu.NONE, preset.getDisplayable()).setCheckable(true);
		first.setOnMenuItemClickListener(listener);
		//populate menu
		for(int i=0; i<presetList.size(); i++){
			PomodoroPreset p = presetList.get(i);
			if(preset.equals(p))
				continue;
			MenuItem item = menu.add(Menu.NONE, i, Menu.NONE, p.getDisplayable()).setCheckable(true);
			if(nextPreset != null && nextPreset.equals(p))
				item.setChecked(true);
			item.setOnMenuItemClickListener(listener);
		}
		if(nextPreset == null)
			menu.getItem(0).setChecked(true);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);
		if(item.getItemId() == -1){
			nextPreset = null;
			nextDropdown.setText(preset.getPresetName());
		} else{
			nextPreset = presetList.get(item.getItemId());
			nextDropdown.setText(nextPreset.getPresetName());
		}
		return result;
	}
	
	private void fetchPresetList(){
		PomodoroDbAdapter adapter = new PomodoroDbAdapter(this);
		adapter.open();
		Cursor presets = adapter.fetchAllPresets();
		presets.moveToFirst();
		//create presets
		presetList = new ArrayList<PomodoroPreset>();
		while(!presets.isAfterLast()){
			presetList.add(new PomodoroPreset(presets));
			presets.moveToNext();
		}
		presets.close();
		adapter.close();
	}

	protected void onStart(){
		super.onStart();
		doBindService();
		if(wakeLockEnabled){
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock =  pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,TAG);
			wakeLock.acquire();
		}
	}

	protected void onStop(){
		super.onStop();
		//doUnbindService();
		if(wakeLockEnabled)
			wakeLock.release();
	}


	public long getTimeRemaining(){
		return timeRemaining;
	}

	//Respond to pause or resume button press
	public void pauseClicked(View v){

		//Stop the currently running timer
		serviceBinder.pause();
		pauseResumeButton.setVisibility(View.INVISIBLE);
		cancelButton.setVisibility(View.INVISIBLE);
		if(pauseOverlay == null)
			pauseOverlay = onCreateDialog(DIALOG_PAUSE_ID);
		pauseOverlay.show();
	}

	public void playClicked(){	
		pauseResumeButton.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);
		pauseOverlay.dismiss();
		serviceBinder.resume();
	}

	//Respond to cancel button press
	public void cancelTimer(View v){
		confirmCancel(false);
	}

	@Override
	public void onBackPressed() {
		confirmCancel(true);
	}

	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch(id){
		case DIALOG_PAUSE_ID:
			dialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
			dialog.setContentView(R.layout.paused_overlay);
			dialog.setCancelable(false);
			ImageButton cancelBtn =(ImageButton) dialog.findViewById(R.id.pause_screen_cancel_btn);
			ImageButton playBtn = (ImageButton)dialog.findViewById(R.id.pause_screen_play_btn);

			cancelBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					confirmCancel(false);
					dialog.dismiss();
				}
			});

			playBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					playClicked();
					dialog.dismiss();
				}
			});
			//enable fade
			LayoutParams params = dialog.getWindow().getAttributes();
			params.flags |= LayoutParams.FLAG_DIM_BEHIND;
			params.dimAmount = 0.8f;
			break;
		default:
			dialog = HelpUtils.getHelpDialog(this, id);
		}

		return dialog;
	}

	private void confirmCancel(boolean showDialog){
		if(showDialog){
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setTitle(R.string.dialog_cancel_title);
			dialog.setMessage(R.string.dialog_cancel_message);
			dialog.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					serviceBinder.cancel();
					doUnbindService();
					finish();
				}
			});
			dialog.setNegativeButton(R.string.button_no, null);
			dialog.show();
		} else{
			serviceBinder.cancel();
			doUnbindService();
			finish();
		}
	}

	public void setTimerDisplay(int hours, int minutes, int seconds){
		timerDisplay.setText(Utils.valueToString(minutes)+":"+Utils.valueToString(seconds));
	}

	//service
	private void doBindService() {
		Intent i = new Intent(this, PomodoroService.class);
		boolean result = bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		Debug.d(TAG, "bind service result: "+result);
	}

	private void doUnbindService() {
		if (mIsBound) {       
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	public PomodoroService.PomoBinder getServiceBinder() {
		return serviceBinder;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about_item:
			showDialog(Constants.HELP_ABOUT);
			return true;
		case R.id.help_item:
			showDialog(Constants.HELP_TIMER);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	private class TimerCallback implements PomodoroTimerCallback{

		@Override
		public void onTimerStateChanged(PomodoroTimer timer) {
			switch(timer.getState()){
			case WORK:
				if(nextPreset != null){//restart pomodoro timer with new preset
					Intent i = new Intent(TimerActivity.this, TimerActivity.class);
					i.putExtra(Constants.KEY_POMODORO_PRESET, nextPreset);
					confirmCancel(false);
					startActivity(i);
				}
				presetState.setText("");
				nextPresetLabel.setVisibility(View.INVISIBLE);
				nextDropdown.setVisibility(View.INVISIBLE);
				break;
			case BREAK:
				presetState.setText(getString(R.string.pomodoro_break));
				nextPresetLabel.setVisibility(View.VISIBLE);
				nextDropdown.setVisibility(View.VISIBLE);
				break;
			case EX_BREAK:
				presetState.setText(getString(R.string.pomodoro_ex_break));
				nextPresetLabel.setVisibility(View.VISIBLE);
				nextDropdown.setVisibility(View.VISIBLE);
				break;
			}
		}

		@Override
		public void onStart(PomodoroTimer timer) {

		}

		@Override
		public void onPause(PomodoroTimer timer) {

		}

		@Override
		public void onResume(PomodoroTimer timer) {

		}

		@Override
		public void onCancel(PomodoroTimer timer) {

		}

		@Override
		public void onTimerTicked(PomodoroTimer timer) {
			setTimerDisplay(timer.getHoursRemaining(), timer.getMinutesRemaining(), timer.getSecondsRemaining());
		}


	}
}


