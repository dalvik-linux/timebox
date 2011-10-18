package nz.wgtn.psisolutions.timebox.timer.gui.visualisations;

import nz.wgtn.psisolutions.timebox.R;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimer;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;

public abstract class AbstractVisualisation{
	
	protected PomodoroTimer timer;
	protected Context context;
	protected int workColor, breakColor, exBreakColor;
	
	public AbstractVisualisation(PomodoroTimer timer, Context context) {
		this.context = context;
		setTimer(timer);
		Resources res = context.getResources();
		workColor = res.getColor(R.color.color_work);
		breakColor = res.getColor(R.color.color_break);
		exBreakColor = res.getColor(R.color.color_ex_break);
	}

	/**
	 * Implement actual drawing algorithm here.
	 * @param timeRemaining time remaining in seconds
	 * @param canvas canvas to draw on
	 */
	public abstract void drawVisualisation(int timeRemaining, Canvas canvas);

	public PomodoroTimer getTimer() {
		return timer;
	}

	public void setTimer(PomodoroTimer timer) {
		this.timer = timer;
	}
}
