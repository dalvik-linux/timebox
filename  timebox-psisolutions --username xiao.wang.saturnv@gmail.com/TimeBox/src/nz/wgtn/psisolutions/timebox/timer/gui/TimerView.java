package nz.wgtn.psisolutions.timebox.timer.gui;



import java.util.Timer;
import java.util.TimerTask;

import nz.wgtn.psisolutions.timebox.Constants;
import nz.wgtn.psisolutions.timebox.Debug;
import nz.wgtn.psisolutions.timebox.preferences.Preferences;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimer;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimerCallback;
import nz.wgtn.psisolutions.timebox.timer.gui.visualisations.AbstractVisualisation;
import nz.wgtn.psisolutions.timebox.timer.gui.visualisations.CircularVisualization;
import nz.wgtn.psisolutions.timebox.timer.gui.visualisations.RadialVisualisation;
import nz.wgtn.psisolutions.timebox.timer.gui.visualisations.WipeVisualisation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TimerView extends SurfaceView implements SurfaceHolder.Callback, PomodoroTimerCallback{
	private final String TAG = "timebox.TimerView";
	private final int DELAY = 1000/15;

	/** The animation timer (15fps) */
	private Timer animTimer;
	private SurfaceRepaintTask repaintTask;
	
	private PomodoroTimer pomoTimer;
	
	private AbstractVisualisation visualisation;

	public TimerView(Context context,AttributeSet as) {
		super(context,as);
		
		SurfaceHolder holder = getHolder();
		holder.setFormat(PixelFormat.RGBA_8888);
		holder.addCallback(this);
		
		setFocusable(true);
	}
	
	public void setTimer(PomodoroTimer pTimer){
		pomoTimer = pTimer;
		pomoTimer.attachCallback(this);
		animTimer = new Timer();
		repaintTask = new SurfaceRepaintTask(getHolder());
		switch(Preferences.getVisualisation()){
		case Constants.VISUALISATION_LINEAR_WIPE:
			visualisation = new WipeVisualisation(pomoTimer, this.getContext());
			break;
		case Constants.VISUALISATION_RADIAL_WIPE:
			visualisation = new RadialVisualisation(pomoTimer, this.getContext());
			break;
		case Constants.VISUALISATION_CIRCULAR_WIPE:
			visualisation = new CircularVisualization(pomoTimer, this.getContext());
			break;
		}
		animTimer.schedule(repaintTask, 0, DELAY);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Debug.d(TAG, "visualisations created.");
		if(pomoTimer == null || animTimer != null)
			return;
		pomoTimer.attachCallback(this);
		animTimer = new Timer();
		repaintTask = new SurfaceRepaintTask(getHolder());
		switch(Preferences.getVisualisation()){
		case Constants.VISUALISATION_LINEAR_WIPE:
			visualisation = new WipeVisualisation(pomoTimer, this.getContext());
			break;
		case Constants.VISUALISATION_RADIAL_WIPE:
			visualisation = new RadialVisualisation(pomoTimer, this.getContext());
			break;
		case Constants.VISUALISATION_CIRCULAR_WIPE:
			visualisation = new CircularVisualization(pomoTimer, this.getContext());
			break;
		}
		animTimer.schedule(repaintTask, 0, DELAY);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Debug.d(TAG, "visualisations destroyed.");
		animTimer.cancel();
		animTimer = null;
		pomoTimer.detachCallback(this);
	}


	class SurfaceRepaintTask extends TimerTask{
		private SurfaceHolder surfaceHolder;
		private int timeRemaining;
		
		//Constructor to initialise thread
		public SurfaceRepaintTask(SurfaceHolder surfaceHolder) {
			this.surfaceHolder = surfaceHolder;
			setTimeRemaining(pomoTimer.getHoursRemaining(),
					pomoTimer.getMinutesRemaining(), pomoTimer.getSecondsRemaining());
		}
		
		@Override
		public void run(){
			Canvas c = null;
			
		        try {
		            c = surfaceHolder.lockCanvas(null);
		            if(c == null)
		            	return;
		            visualisation.drawVisualisation(timeRemaining, c);
		        } finally {
		            // do this in a finally so that if an exception is thrown
		            // during the above, we don't leave the Surface in an
		            // inconsistent state
		            if (c != null) {
		                surfaceHolder.unlockCanvasAndPost(c);
		            }
		        }
		}

		public void setTimeRemaining(int hours, int minutes, int seconds) {
			timeRemaining = seconds;
			timeRemaining += minutes * 60;
			timeRemaining += hours * 3600;
			
		}
	}


	public void onTimerStateChanged(PomodoroTimer timer) {
		
	}

	public void onStart(PomodoroTimer timer) {
		
	}

	public void onPause(PomodoroTimer timer) {
		
	}

	public void onResume(PomodoroTimer timer) {
		
	}

	public void onCancel(PomodoroTimer timer) {
		animTimer.cancel();
	}

	public void onTimerTicked(PomodoroTimer timer) {
		repaintTask.setTimeRemaining(timer.getHoursRemaining(),
				timer.getMinutesRemaining(), timer.getSecondsRemaining());
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

}