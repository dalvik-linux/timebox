package nz.wgtn.psisolutions.timebox.presets.backend;

import nz.wgtn.psisolutions.timebox.Debug;
import nz.wgtn.psisolutions.timebox.Utils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class PomodoroDbAdapter{

	public static final String TAG = "timebox.PomodoroDbAdapter";
	
	//table attributes
	public static final String KEY_ID = "_id",
	ATTR_NAME = "name",
	ATTR_WL = "work_length",
	ATTR_BL = "break_length",
	ATTR_XBL = "extended_break_length",
	ATTR_BC = "break_cycles";

	private PomodoroDbHelper dbHelper;
	private SQLiteDatabase database;

	//database attributes
	private static final String DATABASE_NAME = "timebox";
	private static final String DATABASE_TABLE = "pomodoro_preset";
	private static final int DATABASE_VERSION = 1;

	//database creation statement
	private static final String DATABASE_CREATE = 
		"create table " + DATABASE_TABLE + " ("
		+ KEY_ID + " integer primary key autoincrement, "
		+ ATTR_NAME + " text not null, "
		+ ATTR_WL + " text not null, "
		+ ATTR_BL + " text not null, "
		+ ATTR_XBL + " integer not null, "
		+ ATTR_BC + " integer not null);";

	private Context context;

	public PomodoroDbAdapter(Context context){
		this.context = context;
	}

	/**
	 * Opens the TimeBox database or creates it if it can not be opened.
	 * If it fails to create, throws an exception.
	 */
	public void open() throws SQLException {
		if(database != null && database.isOpen()){
			Debug.d(TAG, "open() ... no action taken, database already opened.");
			return;
		}
		dbHelper = new PomodoroDbHelper(context);
		database = dbHelper.getWritableDatabase();
		Debug.d(TAG, "open() ... database opened.");
	}
	
	/**
	 * Needs to called at some point, or else there will be a resource leak.
	 */
	public void close(){
		if(database == null || !database.isOpen()){
			Debug.d(TAG, "close() ... no action taken, database already closed.");
			return;
		}
		database.close();
		Debug.d(TAG, "close() ... database closed.");
	}

	/**
	 * Creates a new Pomodoro preset using the input provided.
	 * @param preset
	 * @return the id of the new row or -1 if there was a failure.
	 */
	public long createPreset(PomodoroPreset preset){
		ContentValues values = new ContentValues();
		values.put(ATTR_WL, Utils.valueToString(preset.getWorkLength()));
		values.put(ATTR_BL, Utils.valueToString(preset.getBreakLength()));
		values.put(ATTR_BC, preset.getBreakCycles());
		values.put(ATTR_XBL, preset.getExBreakLength());
		values.put(ATTR_NAME, preset.getPresetName());

		Debug.d(TAG, "createPreset... " + values);

		return database.insert(DATABASE_TABLE, null, values);
	}

	/**
	 * Delete the Pomodoro preset with the given ID
	 * 
	 * @param id id of preset to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deletePreset(long id) {
		return database.delete(DATABASE_TABLE, KEY_ID + "=" + id, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all presets in the database
	 * 
	 * @return Cursor over all presets
	 */
	public Cursor fetchAllPresets() {
		return database.query(DATABASE_TABLE, 
				new String[] {KEY_ID, ATTR_NAME, ATTR_WL, ATTR_BL, ATTR_BC, ATTR_XBL}, 
				null, null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the preset that matches the given id
	 * 
	 * @param id id of note to retrieve
	 * @return PomodoroPreset, if found, otherwise null
	 * @throws SQLException if preset could not be found/retrieved
	 */
	public PomodoroPreset fetchPreset(long id) throws SQLException {

		Cursor cursor =
			database.query(true, DATABASE_TABLE, 
					new String[] {KEY_ID, ATTR_NAME, ATTR_WL, ATTR_BL, ATTR_BC, ATTR_XBL},
					KEY_ID + "=" + id, null,
					null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		} else
			return null;
		return new PomodoroPreset(cursor);
	}


	public boolean existsPreset(PomodoroPreset preset){
		String name = preset.getPresetName();
		String wl = Utils.valueToString(preset.getWorkLength());
		String bl = Utils.valueToString(preset.getBreakLength());
		int xbl = preset.getExBreakLength();
		int bc = preset.getBreakCycles();

		String[] cols = {ATTR_NAME};
		String where =
					ATTR_NAME + "='" + name + "' AND " +
					ATTR_WL+ "='" + wl + "' AND " +
					ATTR_BL+ "='" + bl + "' AND " +
					ATTR_XBL+ "=" + xbl + " AND " +
					ATTR_BC+ "=" + bc;
		
		Debug.d(TAG, "existsPreset() ... with query: " + where);
		
		Cursor result = database.query(DATABASE_TABLE, 
				cols, 
				where, 
				null, 
				null, 
				null, 
				null);
		
		boolean exists =  (result.getCount() > 0);
		result.close();
		return exists;
	}

	/**
	 * Update the preset using the details provided. The preset to be updated is
	 * specified using the id.
	 * @param id id of preset to update
	 * @param preset preset details to be written
	 * @return true if the preset was successfully updated, false otherwise
	 */
	public boolean updatePreset(long id, PomodoroPreset preset) {
		ContentValues values = new ContentValues();
		values.put(ATTR_WL, Utils.valueToString(preset.getWorkLength()));
		values.put(ATTR_BL, Utils.valueToString(preset.getBreakLength()));
		values.put(ATTR_BC, preset.getBreakCycles());
		values.put(ATTR_XBL, preset.getExBreakLength());
		values.put(ATTR_NAME, preset.getPresetName());

		return database.update(DATABASE_TABLE, values, KEY_ID + "=" + id, null) > 0;
	}




	/*
	 * Database helper class
	 */


	private static class PomodoroDbHelper extends SQLiteOpenHelper {

		public static final String TAG = "timebox.PomodoroDbHelper";

		public PomodoroDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Debug.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}

}
