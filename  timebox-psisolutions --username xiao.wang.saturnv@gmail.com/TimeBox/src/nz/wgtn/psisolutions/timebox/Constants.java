package nz.wgtn.psisolutions.timebox;

public class Constants {
	//misc constants
	public static final String KEY_POMODORO_PRESET = "timebox.POMODORO_PRESET";
	public static final String KEY_PRESET_ID = "timebox.PRESET_ID";
	public static final String KEY_TURN_SCREEN_ON = "timebox.TURN_SCREEN_ON";
	public static final String KEY_DISABLE_VIS_PREF = "timebox.DISABLE_VIS_PREF";
	public static final int PAUSE_DIALOG = 0x57A71C;
	public static final String URL_APP_VERSION = "http://homepages.ecs.vuw.ac.nz/~wangdavi/timebox_version";
	public static final String URL_APP_MARKET = "market://details?id=nz.wgtn.psisolutions.timebox";
	
	//preference keys
	public static final String PREF_KEY_FIRST_RUN = "timebox.KEY_FIRST_RUN";
	public static final String PREF_KEY_LAST_ALERT_TONE = "timebox.KEY_LAST_ALERT_TONE";
	public static final String PREF_KEY_LAST_DISMISSED_VERSION = "timebox.KEY_LAST_DISMISSED_VERSION";
	
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
	
	//preset defaults
	public static final int DEFAULT_TOTAL = 30,
	 	 DEFAULT_WORK = 25,
	 	 DEFAULT_EX_BREAK = 20,
	 	 DEFAULT_BREAK_CYCLES = 4;
}
