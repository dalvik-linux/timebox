package nz.wgtn.psisolutions.timebox.preferences;

import nz.wgtn.psisolutions.timebox.Constants;
import nz.wgtn.psisolutions.timebox.HelpUtils;
import nz.wgtn.psisolutions.timebox.R;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PreferencesActivity extends PreferenceActivity {
	
	public static final String TAG = "timebox.PreferencesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Preference vis = findPreference(getString(R.string.pref_key_visualisation));
		if(getIntent().getBooleanExtra(Constants.KEY_DISABLE_VIS_PREF, false))
			vis.setEnabled(false);
		else
			vis.setEnabled(true);
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
			showDialog(Constants.HELP_PREFERENCES);
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
