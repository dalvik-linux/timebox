package nz.wgtn.psisolutions.timebox.presets.gui;

import nz.wgtn.psisolutions.timebox.Constants;
import nz.wgtn.psisolutions.timebox.HelpUtils;
import nz.wgtn.psisolutions.timebox.R;
import nz.wgtn.psisolutions.timebox.Utils;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class EditPresetActivity extends Activity implements OnSeekBarChangeListener{

	public static final String TAG = "timebox.EditPresetActivity";
	
	private SeekBar totalLength, workLength, exBreakLength;
	private Button exBreakCycles;
	private String[] exBreakCycleValues;
	
	private PomodoroPreset preset;
	
	private static final double OFFSET_NUMBER = 0.16, OFFSET_END = 0.08;
	
	private double workBreakRatio, workExBreakRatio;
	
	private int minWorkLength, maxWorkLength, minTotal, maxTotal = 60, maxExBreakLength;
	
	private TextView totalLengthLabel, workLengthLabel, breakLengthLabel, exBreakLengthLabel;
	private EditText presetName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_preset);
		
		//setup seekbars
		totalLength = (SeekBar) findViewById(R.id.total_length);
		totalLength.setOnSeekBarChangeListener(this);
		workLength = (SeekBar)findViewById(R.id.work_length);
		workLength.setOnSeekBarChangeListener(this);
		exBreakLength = (SeekBar)findViewById(R.id.ex_break_length);
		exBreakLength.setOnSeekBarChangeListener(this);
		
		//setup dropdown button
		exBreakCycles = (Button)findViewById(R.id.ex_break_cycles);
		registerForContextMenu(exBreakCycles);
		exBreakCycleValues = getResources().getStringArray(R.array.extended_break_entries);
		
		//fetch number displays
		totalLengthLabel = (TextView)findViewById(R.id.lbl_total_length);
		workLengthLabel = (TextView)findViewById(R.id.lbl_work_break_length);
		breakLengthLabel = (TextView)findViewById(R.id.lbl_break_length);
		exBreakLengthLabel = (TextView)findViewById(R.id.lbl_ex_break_length);
		
		//fetch preset name textbox
		presetName = (EditText)findViewById(R.id.preset_name);
		
		//set defaults or initial value
		preset = getIntent().getParcelableExtra(Constants.KEY_POMODORO_PRESET);
		validateLengths();
		if(preset == null){//create new preset
			setTitle(R.string.activity_create_preset);
			totalLength.setProgress(Constants.DEFAULT_TOTAL);
			workBreakRatio = (double)Constants.DEFAULT_WORK/(double)Constants.DEFAULT_TOTAL;
			workLength.setProgress(Constants.DEFAULT_WORK);
			exBreakLength.setProgress(Constants.DEFAULT_EX_BREAK);
			updateExBreakCyclesDropdown(Constants.DEFAULT_BREAK_CYCLES);
		} else{//edit preset
			setTitle(R.string.activity_edit_preset);
			presetName.setText(preset.getPresetName());
			totalLength.setProgress(preset.getWorkLength() + preset.getBreakLength());
			workBreakRatio = (double)preset.getWorkLength()/(double)totalLength.getProgress();
			workLength.setProgress(preset.getWorkLength());
			exBreakLength.setProgress(preset.getExBreakLength());
			updateExBreakCyclesDropdown(preset.getBreakCycles());
		}
	}
	
	private void updateExBreakCyclesDropdown(int cycles){
		exBreakCycles.setText(exBreakCycleValues[cycles]);
		exBreakCycles.setTag(cycles);
	}
	
	public void confirmClicked(View view){
		Intent data = new Intent();
		if(preset == null)
			preset = new PomodoroPreset();
		//save settings
		if(!validatePresetName())
			return;
		preset.setPresetName(presetName.getText().toString());
		preset.setWorkLength(workLength.getProgress());
		preset.setBreakLength(totalLength.getProgress() - preset.getWorkLength());
		preset.setBreakCycles((Integer)exBreakCycles.getTag());
		preset.setExBreakLength(exBreakLength.getProgress());
		data.putExtra(Constants.KEY_POMODORO_PRESET, preset);
		data.putExtra(Constants.KEY_PRESET_ID, getIntent().getLongExtra(Constants.KEY_PRESET_ID, -1));
		setResult(RESULT_OK, data);
		finish();
	}
	
	public void exBreakCyclesClicked(View view){
		view.showContextMenu();
	}
	
	@Override
	public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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
		//create extended break cycles menu
		menu.setHeaderTitle(R.string.label_extended_break_cycles);
		for(int i=0; i<exBreakCycleValues.length; i++){
			String value = exBreakCycleValues[i];
			MenuItem item = menu.add(Menu.NONE, i, Menu.NONE, value);
			//update checkboxes
			item.setCheckable(true);
			if(i==(Integer)exBreakCycles.getTag())
				item.setChecked(true);
			else
				item.setChecked(false);
			item.setOnMenuItemClickListener(listener);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);
		int cycles = item.getItemId();
		updateExBreakCyclesDropdown(cycles);
		return result;
	}
	
	private boolean validatePresetName(){
		String name = presetName.getText().toString();
		if(TextUtils.isEmpty(name)){
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setTitle(R.string.dialog_noname_title);
			dialog.setMessage(R.string.dialog_noname_message);
			dialog.setPositiveButton(R.string.button_ok, null);
			dialog.show();
			return false;
		}
		return true;
	}
	
	public void cancelClicked(View view){
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch(seekBar.getId()){
		case R.id.total_length: //total length bar changed
			if(progress < minTotal){
				seekBar.setProgress(minTotal);
			} else if(progress > maxTotal){
				seekBar.setProgress(maxTotal);
			} else{
				totalLengthLabel.setText(Utils.valueToString(progress));
				validateRatios(progress);
			}
			break;
		case R.id.work_length: //work break ratio bar changed
			if(progress < minWorkLength){
				seekBar.setProgress(minWorkLength);
			} else if(progress > maxWorkLength){
				seekBar.setProgress(maxWorkLength);
			} else{
				workLengthLabel.setText(Utils.valueToString(progress));
				breakLengthLabel.setText(Utils.valueToString(workLength.getMax() - progress));
				if(fromUser)
					workBreakRatio = (double)progress/(double)workLength.getMax();
			}
			break;
		case R.id.ex_break_length: //extended break length bar changed
			if(progress < minTotal){
				seekBar.setProgress(minTotal);
			} else if(progress > maxExBreakLength){
				seekBar.setProgress(maxExBreakLength);
			} else{
				exBreakLengthLabel.setText(Utils.valueToString(progress));
				workExBreakRatio = (double)progress/(double)workLength.getMax();
			}
			break;
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(seekBar.getId() == R.id.total_length)
			validateRatios(seekBar.getProgress());
	}	
	
	private void validateRatios(int total){
		//work out min and max values and adjusts current ratio
		int max = (int)Math.round(total * (1 - OFFSET_NUMBER));
		int min = (int)Math.round(total * OFFSET_NUMBER);
		int newVal = (int)Math.round(workBreakRatio * total);
		workLength.setMax(total);
		workLength.setProgress(newVal);
		maxWorkLength = max;
		minWorkLength = min;
		maxExBreakLength = total;
		
		newVal = (int)Math.round(workExBreakRatio * total);
		exBreakLength.setProgress(newVal);
	}
	
	private void validateLengths(){
		//work out min and max values to keep bars within bounds
		int max = (int)Math.round(maxTotal * (1 + OFFSET_END));
		int min = (int)Math.round(maxTotal * OFFSET_NUMBER);
		minTotal = min;
		totalLength.setMax(max);
		exBreakLength.setMax(max);
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
			showDialog(Constants.HELP_EDIT_PRESET);
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
}
