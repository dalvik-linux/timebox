package nz.wgtn.psisolutions.timebox.preferences;

import nz.wgtn.psisolutions.timebox.Constants;
import nz.wgtn.psisolutions.timebox.Debug;
import nz.wgtn.psisolutions.timebox.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/**
 * Helper class for global preferences.
 * 
 * @author David X Wang [300164091]
 *
 */
public class Preferences {
	
	public static final String TAG = "timebox.Preferences";
	
	private static SharedPreferences prefs;
	
	private static Resources res;
	
	/**
	 * Reloads current global preferences, this only needs to be called once
	 * before other methods in this class are called.
	 * @param context
	 */
	public static void reloadPreferences(Context context){
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		res = context.getResources();
		Debug.v(TAG, "Preferences loaded");
	}
	
	/**
	 * Checks if this is the first time the application has been run.
	 * This method will continue to return true until markFirstRun is called.
	 * @return true if it is the first run, false otherwise.
	 */
	public static boolean isFirstRun(){
		return getPreference(Constants.PREF_KEY_FIRST_RUN, true);
	}
	
	/**
	 * Flags that this application has already been run previously.
	 */
	public static void markFirstRun(){
		if(prefs == null){
			Debug.e(TAG, "markFirstRun ... Global preferences were never loaded!");
		} else{
			Editor edit = prefs.edit();
			edit.putBoolean(Constants.PREF_KEY_FIRST_RUN, false);
			edit.commit();
		}
	}
	
	/**
	 * Flags that this application has not been run yet.
	 */
	public static void resetFirstRun(){
		if(prefs == null){
			Debug.e(TAG, "resetFirstRun ... Global preferences were never loaded!");
		} else{
			Editor edit = prefs.edit();
			edit.putBoolean(Constants.PREF_KEY_FIRST_RUN, true);
			edit.commit();
		}
	}
	
	/**
	 * Gets an integer which represents the current default visualisation.
	 */
	public static int getVisualisation(){
		return Integer.parseInt(getPreference(res.getString(R.string.pref_key_visualisation),
				res.getString(R.string.pref_def_visualisation)));
	}
	
	/**
	 * Gets an URI string for the currently selected alert tone.
	 */
	public static String getAlertTone(){
		return getPreference(res.getString(R.string.pref_key_alert_tone),
				res.getString(R.string.pref_def_alert_tone));
	}
	
	/**
	 * Returns whether or not the "Sound always on" option is on.
	 */
	public static boolean isForceAudio(){
		return getPreference(res.getString(R.string.pref_key_force_audio),
				Boolean.parseBoolean(res.getString(R.string.pref_def_force_audio)));
	}
	
	/**
	 * Returns whether or not the "LED lights" option is on.
	 */
	public static boolean isLEDEnabled(){
		return getPreference(res.getString(R.string.pref_key_lights),
				Boolean.parseBoolean(res.getString(R.string.pref_def_lights)));
	}
	
	/**
	 * Returns whether or not the "Wake lock" option is on.
	 */
	public static boolean isWakeLockEnabled(){
		return getPreference(res.getString(R.string.pref_key_wake_lock),
				Boolean.parseBoolean(res.getString(R.string.pref_def_wake_lock)));
	}
	
	/**
	 * Returns whether or not the "Silent" option is on.
	 */
	public static boolean isSilent(){
		return getPreference(res.getString(R.string.pref_key_silent),
				Boolean.parseBoolean(res.getString(R.string.pref_def_silent)));
	}
	
	/**
	 * Toggles the "Silent" option.
	 */
	public static void toggleSilent(){
		setSilent(!isSilent());
	}
	
	/**
	 * Explicitly sets the "Silent" option.
	 */
	public static void setSilent(boolean silent){
		if(prefs == null){
			Debug.e(TAG, "setSilent ... Global preferences were never loaded!");
		} else{
			Editor edit = prefs.edit();
			edit.putBoolean(res.getString(R.string.pref_key_silent), silent);
			edit.commit();
		}
	}
	
	private static boolean getPreference(String key, boolean defValue){
		if(prefs == null){
			Debug.e(TAG, "Global preferences were never loaded!");
			return defValue;
		}
		return prefs.getBoolean(key, defValue);
	}
	
	private static String getPreference(String key, String defValue){
		if(prefs == null){
			Debug.e(TAG, "Global preferences were never loaded!");
			return defValue;
		}
		return prefs.getString(key, defValue);
	}
}
