package nz.wgtn.psisolutions.timebox;

public class Constants {
	public static final String KEY_POMODORO_PRESET = "timebox.POMODORO_PRESET";
	public static final String KEY_PRESET_ID = "timebox.PRESET_ID";
	
	//preference keys
	public static final String PREF_KEY_FIRST_RUN = "timebox.KEY_FIRST_RUN";
	public static final String PREF_KEY_LAST_ALERT_TONE = "timebox.KEY_LAST_ALERT_TONE";
	
	//visualisations
	public static final int VISUALISATION_LINEAR_WIPE = 0;
	public static final int VISUALISATION_RADIAL_WIPE = 1;
	public static final int VISUALISATION_CIRCULAR_WIPE = 2;

	//help keys
	public static final int HELP_ABOUT = 0xAB007;
	public static final int HELP_PRESET_LIST = 0x1157;
	public static final int HELP_EDIT_PRESET = 0xED17;
	public static final int HELP_PREFERENCES = 0xC06;
	public static final int HELP_TIMER = 0xC10CC;
	public static final int PAUSE_DIALOG = 0x57A71C;
	
	//preset defaults
	public static final int DEFAULT_TOTAL = 30,
	 	 DEFAULT_WORK = 20,
	 	 DEFAULT_EX_BREAK = 20,
	 	 DEFAULT_BREAK_CYCLES = 4;
}
