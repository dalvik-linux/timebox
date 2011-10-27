package nz.wgtn.psisolutions.timebox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import nz.wgtn.psisolutions.timebox.preferences.Preferences;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;

public class Utils {
	
	public static final String TAG = "timebox.Utils";
	
	/**
	 * Zero-pads single digit integers.
	 */
	public static String valueToString(int value){
		//adds a zero if less than 10
		if(value >= 10)
			return String.valueOf(value);
		else
			return "0" + value;
	}
	
	private static boolean updatesChecked = false;
	
	/**
	 * Checks for application updates in a background
	 * thread and alerts the user if an update is found.
	 * @param forceShowDialog show a dialog even if there are no updates available 
	 * or updates have already been dismissed.
	 */
	public static void checkForUpdates(final Context context, final boolean forceShowDialog){
		if(updatesChecked && !forceShowDialog)//method will only run once per restart
			return;
		else if(!updatesChecked)
			updatesChecked = true;
		
		Debug.i(TAG, "Checking for updates ...");
		
		//create async task to check for updates in a background thread
		AsyncTask<Void, Void, String> checkUpdates = new AsyncTask<Void, Void, String>() {
			private int newVersionCode;
			private boolean networkError = false;
			@Override
			protected String doInBackground(Void... params) {
				try {
					URL versionUrl = new URL(Constants.URL_APP_VERSION);
					URLConnection conn = versionUrl.openConnection();
					//read version code and name
					Scanner scanVersion = new Scanner(conn.getInputStream());
					newVersionCode = scanVersion.nextInt();
					//check if new version is available
					if(getVersionCode(context) >= newVersionCode)
						return null;
					//check for previous dismissal
					if(!forceShowDialog && Preferences.getLastDismissedVersion() == newVersionCode)
						return null;
					//return new version name
					String newVersionName = scanVersion.next();
					scanVersion.close();
					return newVersionName;
				} catch (MalformedURLException e) {
					Debug.e(TAG, "checkForUpdates ... URL malformed: " + e);
					networkError = true;
					return null;
				} catch (IOException e) {
					Debug.e(TAG, "checkForUpdates ... can not connect to version URL: " + e);
					networkError = true;
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(String newVersionName) {
				super.onPostExecute(newVersionName);
				if(!forceShowDialog && newVersionName == null){//no update found or no internet connection
					Debug.d(TAG, "No update found.");
					return;
				}
				//create update dialog
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				if(newVersionName == null){//no updates or network error
					if(networkError){
						Debug.i(TAG, "Network error, no update found.");
						dialog.setIcon(android.R.drawable.ic_dialog_alert);
						dialog.setTitle(context.getString(R.string.dialog_update_error_title));
						dialog.setMessage(context.getString(R.string.dialog_update_error_message));
					} else{
						Debug.i(TAG, "No new updates found.");
						dialog.setIcon(android.R.drawable.ic_dialog_info);
						dialog.setTitle(context.getString(R.string.dialog_no_update_title));
						dialog.setMessage(context.getString(R.string.dialog_no_update_message));
					}
					dialog.setNeutralButton(R.string.button_ok, null);
				} else{//new update available
					Debug.i(TAG, "New update available.");
					dialog.setIcon(android.R.drawable.ic_dialog_info);
					dialog.setTitle(context.getString(R.string.dialog_update_title));
					dialog.setMessage(String.format(context.getString(R.string.dialog_update_message), 
							getVersionName(context), newVersionName));
					//set up buttons
					//"Yes" button
					dialog.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
						//go to market
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_APP_MARKET));
							context.startActivity(i);
						}
					});
					//"Later" button
					dialog.setNegativeButton(R.string.button_later, new DialogInterface.OnClickListener() {
						//does nothing (may change in the future)
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					});
					//"Dismiss" button
					dialog.setNeutralButton(R.string.button_dismiss, new DialogInterface.OnClickListener() {
						//records that this update has been dismissed
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Preferences.setLastDismissedVersion(newVersionCode);
						}
					});
				}
				//show dialog
				try {
					dialog.show();
				} catch (Exception e) {
					Debug.e(TAG, "checkForUpdates ... could not display dialog: " + e);
				}
			}
		};
		
		//begin!
		checkUpdates.execute((Void)null);
	}
	
	public static int getVersionCode(Context context){
		try {
			return context.getPackageManager().
					getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Debug.e(TAG, "getVersionCode ... Could not found package: " + e);
			return -1;
		}
	}
	
	public static String getVersionName(Context context){
		try {
			return context.getPackageManager().
					getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Debug.e(TAG, "getVersionName ... Could not found package: " + e);
			return null;
		}
	}
}
