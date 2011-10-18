package nz.wgtn.psisolutions.timebox.timer.gui.visualisations;

import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;

public class WipeVisualisation extends AbstractVisualisation {
	
	private PomodoroPreset preset;
	private float wipeOffset = 0;
	private Paint paint;
	
	private static final int MAX_ALPHA = 200, MIN_ALPHA = 100;
	private static final float WIPE_SIZE = 0.2f, WIPE_SPEED = 0.05f;

	public WipeVisualisation(PomodoroTimer timer, Context context) {
		super(timer, context);
		preset = timer.getPomodoro();
		paint = new Paint();
	}

	@Override
	public void drawVisualisation(int timeRemaining, Canvas canvas) {
		PomodoroTimer.TimerState state = timer.getState();
        int length = 0;
        int width = canvas.getWidth();
        int height = canvas.getHeight();
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
        
        float ratio = height / (length * 60f);
        float progress = ratio * timeRemaining;
        
        LinearGradient gradient = null;
        
        if(timer.isPaused()){
        	wipeOffset = 0;
            gradient = constructGradient(startColor, true, height);
        } else{
        	gradient = constructGradient(startColor, false, height);
        	updateWipeOffset(timer.getState() == PomodoroTimer.TimerState.WORK);
        }
        
        paint.setShader(gradient);
        	
        canvas.drawColor(Color.BLACK);
        if(state == PomodoroTimer.TimerState.WORK){
        	canvas.drawRect(0, progress,width,height, paint);
        } else{
        	canvas.drawRect(0,height - progress,width,height, paint);
        }
	}
	
	private LinearGradient constructGradient(int color, boolean flat, int height){
		int colorRGB = (color % 0x01000000);
		int colorHi = colorRGB + (MAX_ALPHA << 24);
		int colorLo = colorRGB + (MIN_ALPHA << 24);
		if(flat){
			LinearGradient gradient = new LinearGradient(0, 0, 0, 
					height, colorLo, colorLo, TileMode.CLAMP);
			return gradient;
		} else{
			LinearGradient gradient = new LinearGradient(
					0, (-WIPE_SIZE)*height, 0, height*(1 + WIPE_SIZE), 
					new int[]{colorLo, colorHi, colorLo}, 
					new float[]{wipeOffset-WIPE_SIZE, 
							wipeOffset, wipeOffset+WIPE_SIZE}, TileMode.CLAMP);
			return gradient;
		}
	}
	
	private void updateWipeOffset(boolean up){
		if(up){
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
