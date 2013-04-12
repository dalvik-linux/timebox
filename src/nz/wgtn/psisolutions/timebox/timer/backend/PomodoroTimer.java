package nz.wgtn.psisolutions.timebox.timer.backend;

import java.util.ArrayList;
import java.util.List;

import nz.wgtn.psisolutions.timebox.Debug;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import android.os.CountDownTimer;

/**
 * Encapsulates the logic behind a Pomodoro timer
 * 
 * @author David X Wang [300164091]
 *
 */
public class PomodoroTimer {
	
	public static final String TAG = "timebox.PomodoroTimer";
	
	private PomodoroPreset preset;
	private PomodoroCountDown timer;
	private List<PomodoroTimerCallback> callbacks;
	
	//states
	private int currentCycle = 0;
	private int totalCycles = 0;
	private boolean paused = false;
	private TimerState timerState = TimerState.NONE;
	public enum TimerState{WORK, BREAK, EX_BREAK, NONE};
	
	private static final int UPDATE_INTERVAL = 1000;
	private static final int ONE_MINUTE = 60000;
	
	public PomodoroTimer(PomodoroPreset preset, PomodoroTimerCallback callback){
		this.preset = preset;
		callbacks = new ArrayList<PomodoroTimerCallback>();
		if(callback != null)
			attachCallback(callback);
	}
	
	public void attachCallback(PomodoroTimerCallback callback){
		if(callback != null)
			callbacks.add(callback);
	}
	
	public void detachCallback(PomodoroTimerCallback callback){
		if(callback != null)
			callbacks.remove(callback);
	}
	
	public PomodoroPreset getPomodoro(){
		return preset;
	}
	
	/**
	 * Starts or restarts the Pomodoro timer.
	 */
	public void start(){
		startWork(false);
		for(PomodoroTimerCallback callback : callbacks)
			callback.onStart(this);
	}
	
	/**
	 * Pauses the current Pomodoro timer if it is running.
	 * @return true if the timer was paused, false if it wasn't running.
	 */
	public boolean pause(){
		if(timer == null || paused)
			return false;
		else{
			timer.cancel();
			paused = true;
			for(PomodoroTimerCallback callback : callbacks)
				callback.onPause(this);
			return true;
		}
	}
	
	/**
	 * Resumes the current Pomodoro timer if it is paused.
	 * @return true if the timer was resumed, false if it wasn't paused.
	 */
	public boolean resume(){
		if(timer == null || !paused)
			return false;
		else{
			timer = new PomodoroCountDown(timer);
			timer.start();
			paused = false;
			for(PomodoroTimerCallback callback : callbacks)
				callback.onResume(this);
			return true;
		}
	}
	
	/**
	 * Cancels the current Pomodoro timer if it is active.
	 * @return true if the timer was canceled, false if it wasn't ever started or active.
	 */
	public boolean cancel(){
		if(timer == null)
			return false;
		else{
			timer.cancel();
			timer = null;
			changeState(TimerState.NONE);
			for(PomodoroTimerCallback callback : callbacks)
				callback.onCancel(this);
			return true;
		}
	}
	
	public boolean isRunning(){
		return timer != null && !paused;
	}
	
	public boolean isPaused(){
		return timer != null && paused;
	}
	
	public TimerState getState(){
		return timerState;
	}
	
	public int getHoursRemaining(){
		if(timer == null) return -1;
		return (int)(timer.timeRemaining / 3600000) % 24;
	}
	
	public int getMinutesRemaining(){
		if(timer == null) return -1;
		return (int)(timer.timeRemaining / 60000) % 60;
	}
	
	public int getSecondsRemaining(){
		if(timer == null) return -1;
		return (int)(timer.timeRemaining / 1000) % 60;
	}
	
	public int getCurrentCycle(){
		return currentCycle;
	}
	
	public int getTotalCycles(){
		return totalCycles;
	}
	
	private void nextState(){
		switch(timerState){
		case WORK:
			startBreak();
			break;
		case BREAK:
			if(currentCycle == preset.getBreakCycles()){
				startExBreak();
			} else
				startWork(true);
			break;
		case EX_BREAK:
			currentCycle = 0;
			startWork(true);
			break;
		}
	}
	
	private void startWork(boolean notify){
		if(timer != null)
			timer.cancel();
		long timeRemaining = preset.getWorkLength()*ONE_MINUTE;
		timer = new PomodoroCountDown(timeRemaining);
		timer.start();
		paused = false;
		if(notify)
			changeState(TimerState.WORK);
		else
			timerState = TimerState.WORK;
		currentCycle++;
		totalCycles++;
	}
	
	private void startBreak(){
		if(timer != null)
			timer.cancel();
		long timeRemaining = preset.getBreakLength()*ONE_MINUTE;
		timer = new PomodoroCountDown(timeRemaining);
		timer.start();
		paused = false;
		changeState(TimerState.BREAK);
	}
	
	private void startExBreak(){
		if(timer != null)
			timer.cancel();
		long timeRemaining = preset.getExBreakLength()*ONE_MINUTE;
		timer = new PomodoroCountDown(timeRemaining);
		timer.start();
		paused = false;
		changeState(TimerState.EX_BREAK);
	}
	
	private void changeState(TimerState newState){
		timerState = newState;
		for(PomodoroTimerCallback callback : callbacks)
			callback.onTimerStateChanged(this);
	}
	
	private class PomodoroCountDown extends CountDownTimer{
		
		long timeRemaining;

		public PomodoroCountDown(long millisInFuture) {
			super(millisInFuture/Debug.getTimeAccelerator(), UPDATE_INTERVAL);
			timeRemaining = millisInFuture;
		}
		
		public PomodoroCountDown(PomodoroCountDown prevTimer){
			super(prevTimer.timeRemaining/Debug.getTimeAccelerator(), UPDATE_INTERVAL);
			timeRemaining = prevTimer.timeRemaining;
		}

		@Override
		public void onFinish() {
			nextState();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			timeRemaining = millisUntilFinished * Debug.getTimeAccelerator();
			for(PomodoroTimerCallback callback : callbacks)
				callback.onTimerTicked(PomodoroTimer.this);
			Debug.v(TAG, "onTick() ... " + timeRemaining);
		}
	}
}
