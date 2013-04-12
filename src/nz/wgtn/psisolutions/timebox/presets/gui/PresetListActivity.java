package nz.wgtn.psisolutions.timebox.presets.gui;

import nz.wgtn.psisolutions.timebox.Constants;
import nz.wgtn.psisolutions.timebox.Debug;
import nz.wgtn.psisolutions.timebox.HelpUtils;
import nz.wgtn.psisolutions.timebox.R;
import nz.wgtn.psisolutions.timebox.Utils;
import nz.wgtn.psisolutions.timebox.preferences.Preferences;
import nz.wgtn.psisolutions.timebox.preferences.PreferencesActivity;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroDbAdapter;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import nz.wgtn.psisolutions.timebox.timer.gui.TimerActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class PresetListActivity extends ListActivity{

	public static final String TAG = "timebox.PresetListActivity";

	private static final int MENU_START = 0, MENU_EDIT = 1, MENU_REMOVE = 2;

	private PomodoroDbAdapter dbAdapter;
	private Cursor presetsCursor;

	private PomodoroPreset lastPreset;

	//activity request codes
	private static final int CREATE_PRESET = 0, EDIT_PRESET = 1;
	
	//request update check
	private boolean updateChecked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.preset_list);
		
		Preferences.reloadPreferences(this); // load preferences

		//open the database
		dbAdapter = new PomodoroDbAdapter(this);

		//show first run dialog
		if(Preferences.isFirstRun()){
			//open the database
			dbAdapter.open();
			//add standard/default Pomodoro preset
			dbAdapter.createPreset(PomodoroPreset.createDefault(this));
			//show about dialog
			HelpUtils.getHelpDialog(this, Constants.HELP_ABOUT).show();
			Preferences.markFirstRun();
		}

		//listen for long-clicks
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//used to populate the list of presets from the database
		populatePresets();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//check for updates
		if(!updateChecked){
			try {
				Utils.checkForUpdates(this, false);
				updateChecked = true;
			} catch (Exception e) {
				//should not happen
				Debug.e(TAG, "onResume ... update check failed!");
			}
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//close the connect to the database when the focus leaves this activity
		dbAdapter.close();
	}

	private void populatePresets(){
		//open the database
		dbAdapter.open();
		//fetch all presets
		presetsCursor = dbAdapter.fetchAllPresets();
		startManagingCursor(presetsCursor);
		//attributes to display
		String[] from = {PomodoroDbAdapter.ATTR_NAME,
				PomodoroDbAdapter.ATTR_WL,
				PomodoroDbAdapter.ATTR_BL};
		int[] to = {R.id.preset_name, R.id.preset_work_length, R.id.preset_break_length};
		//create a cursor adapter to display the presets
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.preset_row, presetsCursor, from, to);
		setListAdapter(adapter);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(v.getId() == android.R.id.list){
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			lastPreset = dbAdapter.fetchPreset(info.id);
			menu.setHeaderTitle(lastPreset.getPresetName());
			//add menu items
			menu.add(Menu.NONE, MENU_START, Menu.NONE, R.string.menu_start_pomodoro);
			menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.menu_edit_preset);
			menu.add(Menu.NONE, MENU_REMOVE, Menu.NONE, R.string.menu_remove_preset);
		} else
			super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(lastPreset == null){
			Debug.w(TAG, "onContextItemSelected... the pressed preset is unknown.");
			return super.onContextItemSelected(item);
		}
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		switch(item.getItemId()){
		case MENU_START:
			Intent i = new Intent(this, TimerActivity.class);
			i.putExtra(Constants.KEY_POMODORO_PRESET, lastPreset);
			startActivity(i);
			return true;
		case MENU_EDIT:
			i = new Intent(this, EditPresetActivity.class);
			i.putExtra(Constants.KEY_POMODORO_PRESET, lastPreset);
			i.putExtra(Constants.KEY_PRESET_ID, info.id);
			startActivityForResult(i, EDIT_PRESET);
			return true;
		case MENU_REMOVE:
			removeClicked(info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void newPresetClicked(View view){
		Intent i = new Intent(this, EditPresetActivity.class);
		startActivityForResult(i, CREATE_PRESET);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != RESULT_OK)
			return;
		//open database just in case
		dbAdapter.open();
		switch(requestCode){
		case CREATE_PRESET:
			PomodoroPreset preset = data.getParcelableExtra(Constants.KEY_POMODORO_PRESET);
			if(dbAdapter.existsPreset(preset)){
				Debug.d(TAG, "onActivityResult... preset already exists: " + preset);
				return;
			}
			Debug.d(TAG, "onActivityResult... creating a new preset: " + preset);
			dbAdapter.createPreset(preset);
			populatePresets();
			break;
		case EDIT_PRESET:
			preset = data.getParcelableExtra(Constants.KEY_POMODORO_PRESET);
			long id = data.getLongExtra(Constants.KEY_PRESET_ID, -1);
			Debug.d(TAG, "onActivityResult... updating a preset[" + id + "]: " + preset);
			dbAdapter.updatePreset(id, preset);
			populatePresets();
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void globalSettingsClicked(View view){
		Intent i = new Intent(this, PreferencesActivity.class);
		startActivity(i);
	}

	public void removeClicked(final long id){
		//ask for confirmation
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(R.string.dialog_remove_title);
		dialog.setMessage(R.string.dialog_remove_message);
		dialog.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {		
			public void onClick(DialogInterface arg0, int arg1) {
				dbAdapter.deletePreset(id);
				populatePresets();
			}
		});
		dialog.setNegativeButton(R.string.button_no, null);
		dialog.show();
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		Intent i = new Intent(this, TimerActivity.class);
		PomodoroPreset preset = dbAdapter.fetchPreset(id);
		if(preset == null)
			Debug.w(TAG, "Could not find preset at id: " + id);
		else{
			Debug.d(TAG, preset.toString()); 
			i.putExtra(Constants.KEY_POMODORO_PRESET, preset);
			startActivity(i);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		d = HelpUtils.getHelpDialog(this, id);
		return d;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about_item:
			showDialog(Constants.HELP_ABOUT);
			return true;
		case R.id.help_item:
			showDialog(Constants.HELP_PRESET_LIST);
			return true;
		case R.id.update_item:
			Utils.checkForUpdates(this, true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_presets, menu);
		return true;
	}

	
}
