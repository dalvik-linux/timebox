package nz.wgtn.psisolutions.timebox;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import android.util.Log;

/**
 * 
 * Debugging helper class.
 * Please use this class instead of {@link android.util.Log}.
 * 
 * @author David X Wang [300164091]
 *
 */
@SuppressWarnings("unused") 
public class Debug {
	
	public static final int VERBOSE = 4, DEBUG = 3, INFO = 2, WARN = 1, ERROR = 0, NONE = -1;
	
	public static final int CURRENT_DEBUG_LEVEL = ERROR;
	
	public static final String[] EXCEPTIONS = {};
	public static final HashSet<String> EXCEPTIONS_SET = new HashSet<String>(Arrays.asList(EXCEPTIONS));
	
	// 1 means normal speed
	private static int TIME_ACCELERATOR = 1;
	
	/**
	 * Please only use for debugging.
	 * @param oneMinute the length of one minute in milliseconds
	 */
	public static void setTimeAccelerator(int acc){
		TIME_ACCELERATOR = acc;
	}
	
	public static int getTimeAccelerator(){
		return TIME_ACCELERATOR;
	}

	public static void v(String tag, String message){
		if(CURRENT_DEBUG_LEVEL < VERBOSE)
			if(!EXCEPTIONS_SET.contains(tag))
				return;
		Log.v(tag, message);
	}
	
	public static void d(String tag, String message){
		if(CURRENT_DEBUG_LEVEL < DEBUG)
			if(!EXCEPTIONS_SET.contains(tag))
				return;
		Log.d(tag, message);
	}
	
	public static void i(String tag, String message){
		if(CURRENT_DEBUG_LEVEL < INFO)
			if(!EXCEPTIONS_SET.contains(tag))
				return;
		Log.i(tag, message);
	}
	
	public static void w(String tag, String message){
		if(CURRENT_DEBUG_LEVEL < WARN)
			if(!EXCEPTIONS_SET.contains(tag))
				return;
		Log.w(tag, message);
	}
	
	public static void e(String tag, String message){
		if(CURRENT_DEBUG_LEVEL < ERROR)
			if(!EXCEPTIONS_SET.contains(tag))
				return;
		Log.e(tag, message);
	}
}
