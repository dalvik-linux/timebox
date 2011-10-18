package nz.wgtn.psisolutions.timebox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class HelpUtils {
	
	public static final String TAG = "timebox.HelpHelper";
	
	/**
	 * Returns a relevant help dialog based on the type.
	 */
	public static Dialog getHelpDialog(Context c, int type){
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setCancelable(true);
		//create the help view
		View helpView = LayoutInflater.from(c).inflate(R.layout.help, null);
		builder.setView(helpView);
		//extract the help text view
		TextView helpText = (TextView)helpView.findViewById(R.id.help_text);
		switch(type){
		case Constants.HELP_ABOUT:
			builder.setTitle(String.format(c.getString(R.string.help_about_title), c.getString(R.string.app_name)));
			helpText.setText(String.format(c.getString(R.string.help_about), c.getString(R.string.app_name)));
			break;
		case Constants.HELP_EDIT_PRESET:
			builder.setTitle(c.getString(R.string.help_create_edit_title));
			helpText.setText(c.getString(R.string.help_create_edit_content));
			break;
		case Constants.HELP_PREFERENCES:
			builder.setTitle(c.getString(R.string.help_preferences_title));
			helpText.setText(c.getString(R.string.help_preferences_content));
			break;
		case Constants.HELP_PRESET_LIST:
			builder.setTitle(c.getString(R.string.help_preset_list_title));
			helpText.setText(c.getString(R.string.help_preset_list_content));
			break;
		case Constants.HELP_TIMER:
			builder.setTitle(c.getString(R.string.help_timer_title));
			helpText.setText(c.getString(R.string.help_timer_content));
			break;
		}
		
		builder.setNeutralButton(R.string.button_close, null);
		
		AlertDialog alert = builder.create();
		return alert;
	}
}
