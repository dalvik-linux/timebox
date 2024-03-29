package nz.wgtn.psisolutions.timebox.presets.backend;

import nz.wgtn.psisolutions.timebox.Constants;
import nz.wgtn.psisolutions.timebox.R;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class PomodoroPreset implements Parcelable{
	
	private int workLength, breakLength, exBreakLength, breakCycles;
	private String presetName;
	
	public static final Parcelable.Creator<PomodoroPreset> CREATOR
		= new Parcelable.Creator<PomodoroPreset>() {

			public PomodoroPreset createFromParcel(Parcel source) {
				return new PomodoroPreset(source);
			}

			public PomodoroPreset[] newArray(int size) {
				return new PomodoroPreset[size];
			}	
	};
	
	/**
	 * Creates the default 25/5 Pomodoro preset.
	 * @param context
	 * @return
	 */
	public static PomodoroPreset createDefault(Context context){
		return new PomodoroPreset(Constants.DEFAULT_WORK,
								  Constants.DEFAULT_TOTAL - Constants.DEFAULT_WORK,
								  Constants.DEFAULT_EX_BREAK,
								  Constants.DEFAULT_BREAK_CYCLES,
								  context.getString(R.string.pomodoro_default));
	}
	
	public PomodoroPreset(int workLength, int breakLength,
			int exBreakLength, int breakCycles, String presetName) {
		this.workLength = workLength;
		this.breakLength = breakLength;
		this.exBreakLength = exBreakLength;
		this.breakCycles = breakCycles;
		this.presetName = presetName;
	}
	
	/**
	 * Empty temp constructor.
	 */
	public PomodoroPreset(){
		
	}
	
	@Override
	public String toString() {
		return "PomodoroPreset [workLength=" + workLength + ", breakLength="
				+ breakLength + ", exBreakLength=" + exBreakLength
				+ ", breakCycles=" + breakCycles + ", presetName=" + presetName
				+ "]";
	}

	public PomodoroPreset(Cursor cursor){
		this.presetName = cursor.getString(cursor.getColumnIndex(PomodoroDbAdapter.ATTR_NAME));
		this.breakCycles = cursor.getInt(cursor.getColumnIndex(PomodoroDbAdapter.ATTR_BC));
		this.exBreakLength = cursor.getInt(cursor.getColumnIndex(PomodoroDbAdapter.ATTR_XBL));
		this.workLength = Integer.parseInt(
				cursor.getString(cursor.getColumnIndex(PomodoroDbAdapter.ATTR_WL)));
		this.breakLength = Integer.parseInt(
				cursor.getString(cursor.getColumnIndex(PomodoroDbAdapter.ATTR_BL)));
	}
	
	private PomodoroPreset(Parcel in){
		this.presetName = in.readString();
		this.workLength = in.readInt();
		this.breakLength = in.readInt();
		this.exBreakLength = in.readInt();
		this.breakCycles = in.readInt();
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(presetName);
		out.writeInt(workLength);
		out.writeInt(breakLength);
		out.writeInt(exBreakLength);
		out.writeInt(breakCycles);
	}

	public int describeContents() {
		return 0;
	}

	public int getWorkLength() {
		return workLength;
	}

	public void setWorkLength(int workLength) {
		this.workLength = workLength;
	}

	public int getBreakLength() {
		return breakLength;
	}

	public void setBreakLength(int breakLength) {
		this.breakLength = breakLength;
	}

	public int getExBreakLength() {
		return exBreakLength;
	}

	public void setExBreakLength(int exBreakLength) {
		this.exBreakLength = exBreakLength;
	}

	public int getBreakCycles() {
		return breakCycles;
	}

	public void setBreakCycles(int breakCycles) {
		this.breakCycles = breakCycles;
	}

	public String getPresetName() {
		return presetName;
	}

	public void setPresetName(String presetName) {
		this.presetName = presetName;
	}
	
	public String getDisplayable(){
		String name = presetName;
		if(name.length() > 15)
			name = name.substring(0, 15) + "...";
		String format = "[%02d/%02d] %s";
		return String.format(format, workLength, breakLength, name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + breakCycles;
		result = prime * result + breakLength;
		result = prime * result + exBreakLength;
		result = prime * result
				+ ((presetName == null) ? 0 : presetName.hashCode());
		result = prime * result + workLength;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PomodoroPreset other = (PomodoroPreset) obj;
		if (breakCycles != other.breakCycles)
			return false;
		if (breakLength != other.breakLength)
			return false;
		if (exBreakLength != other.exBreakLength)
			return false;
		if (presetName == null) {
			if (other.presetName != null)
				return false;
		} else if (!presetName.equals(other.presetName))
			return false;
		if (workLength != other.workLength)
			return false;
		return true;
	}
}
