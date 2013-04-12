package nz.wgtn.psisolutions.timebox.timer.backend;

public interface PomodoroTimerCallback {
	
	public void onTimerStateChanged(PomodoroTimer timer);
	
	public void onStart(PomodoroTimer timer);
	public void onPause(PomodoroTimer timer);
	public void onResume(PomodoroTimer timer);
	public void onCancel(PomodoroTimer timer);
	
	public void onTimerTicked(PomodoroTimer timer);
}
