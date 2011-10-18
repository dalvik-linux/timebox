package nz.wgtn.psisolutions.timebox;

public class Utils {
	
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
}
