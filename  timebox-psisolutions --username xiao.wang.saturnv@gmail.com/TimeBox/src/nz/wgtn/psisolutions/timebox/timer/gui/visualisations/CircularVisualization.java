package nz.wgtn.psisolutions.timebox.timer.gui.visualisations;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;

public class CircularVisualization extends AbstractVisualisation{
	private PomodoroPreset preset;
	private float wipeOffset = 0;
	private Paint paint;

	private static final int MAX_ALPHA = 200, MIN_ALPHA = 100;
	private static final float WIPE_SIZE = 0.2f, WIPE_SPEED = 0.025f, RADIUS_RATIO = 0.65f;

	public CircularVisualization(PomodoroTimer timer, Context context) {
		super(timer, context);
		preset = timer.getPomodoro();
		paint = new Paint();
		paint.setAntiAlias(true);
	}

	@Override
	public void drawVisualisation(int timeRemaining, Canvas canvas) {
		PomodoroTimer.TimerState state = timer.getState();
		float radius = 1;
		//float maxRadius = canvas.getHeight() + canvas.getWidth()/2;
		int length = 0;
		int startColor = 0;
		//determine total length for current timer state
		switch(state){
		case WORK:
			length = preset.getWorkLength();
			startColor = workColor;
			break;
		case BREAK:
			length = preset.getBreakLength();
			startColor = breakColor;
			break;
		case EX_BREAK:
			length = preset.getExBreakLength();
			startColor = exBreakColor;
			break;
		}

		if(length == 0)
			return;
 
		float timeElapsed = (length * 60f) - timeRemaining;
		
		float completionRatio = ((state == PomodoroTimer.TimerState.WORK ? timeElapsed : timeRemaining) / (length * 60f));
		radius = (float)(canvas.getHeight()/1.7) * completionRatio + 1; 
		if(state == PomodoroTimer.TimerState.WORK) 
			radius++; 
		else 
			radius--;
		
		RadialGradient gradient = null;

		if(timer.isPaused()){
			wipeOffset = 0;
			gradient = constructGradient(startColor, true, 1, canvas.getWidth()/2,
					canvas.getHeight()/2);
		} else{
			gradient = constructGradient(startColor, false, canvas.getHeight() * RADIUS_RATIO * (completionRatio * 0.8f + 0.2f), canvas.getWidth()/2,
					canvas.getHeight()/2);
			updateWipeOffset(timer.getState() == PomodoroTimer.TimerState.WORK);
		}
		

		paint.setShader(gradient);

		canvas.drawColor(Color.BLACK);

		canvas.drawCircle(canvas.getWidth()/2 ,canvas.getHeight()/2, radius, paint);

	}

	private RadialGradient constructGradient(int color, boolean flat, float radius, int startX, int startY){
		int colorRGB = (color % 0x01000000);
		int colorHi = colorRGB + (MAX_ALPHA << 24);
		int colorLo = colorRGB + (MIN_ALPHA << 24);

		RadialGradient gradient;
		if(radius < 1)
			radius = 1;
		if(flat){
			gradient = new RadialGradient(0, 0, 
					radius, colorLo, colorLo, TileMode.CLAMP);
			return gradient;
		} else{
			gradient = new RadialGradient(
					startX, startY, radius, 
					//(-WIPE_SIZE)*startX, startY, -height*(WIPE_SIZE),
					new int[]{colorLo, colorHi, colorLo}, 
					new float[]{wipeOffset-WIPE_SIZE, 
							wipeOffset, wipeOffset+WIPE_SIZE}, TileMode.CLAMP);
			return gradient;
		}

	}

	private void updateWipeOffset(boolean up){
		if(!up){
			wipeOffset -= WIPE_SPEED;
			if(wipeOffset < 0)
				wipeOffset = 1;
		} else{
			wipeOffset += WIPE_SPEED;
			if(wipeOffset > 1)
				wipeOffset = 0;
		}
	}

}
