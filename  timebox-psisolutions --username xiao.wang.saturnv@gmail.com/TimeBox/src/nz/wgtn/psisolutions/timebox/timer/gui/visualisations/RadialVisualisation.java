package nz.wgtn.psisolutions.timebox.timer.gui.visualisations;

import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import nz.wgtn.psisolutions.timebox.timer.backend.PomodoroTimer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;

public class RadialVisualisation extends AbstractVisualisation {
	
	private PomodoroPreset preset;
	private float wipeOffset = 0;
	private Paint paint;
	
	private static final int MAX_ALPHA = 200, MIN_ALPHA = 100;
	private static final float WIPE_SIZE = 0.1f, WIPE_SPEED = 0.025f;
	
	public RadialVisualisation(PomodoroTimer timer, Context context) {
		super(timer, context);
		preset = timer.getPomodoro();
		paint = new Paint();
		paint.setAntiAlias(true);
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
        
        SweepGradient gradient = null;
        
        if(timer.isPaused()){
        	wipeOffset = 0;
            gradient = constructGradient(startColor, true, width, height);
        } else{
        	gradient = constructGradient(startColor, false, width, height);
        	updateWipeOffset();
        }
        
        paint.setShader(gradient);
        	
        canvas.drawColor(Color.BLACK);
        if(state == PomodoroTimer.TimerState.WORK){
        	canvas.drawArc(new RectF(new Rect(0-width, 0-height, width+width, height+height)), 
        			0, (float) 360 - (360 * (progress / height)), true, paint);
        } else{
        	canvas.drawArc(new RectF(new Rect(0-width, 0-height, width+width, height+height)), 
        			(float) 360 - (360 * (progress / height)), (float) 360 - (360 * ((height-progress) / height)), true, paint);
        }

	}

	private SweepGradient constructGradient(int color, boolean flat, int width, int height){
		int colorRGB = (color % 0x01000000);
		int colorHi = colorRGB + (MAX_ALPHA << 24);
		int colorLo = colorRGB + (MIN_ALPHA << 24);
		
		SweepGradient gradient;
		
		if(flat){
			gradient = new SweepGradient(width/2f, height/2f, colorLo, colorLo);
			return gradient;
		} else{
			float posLo = wipeOffset - WIPE_SIZE;
			float posMid = wipeOffset;
			float posHi = wipeOffset + WIPE_SIZE;
			gradient = new SweepGradient(width/2f, height/2f, new int[]{colorLo, colorHi, colorLo},
		 			new float[]{posLo, posMid, posHi});
			return gradient;
		}
		
	}
	
	private void updateWipeOffset(){
		wipeOffset += WIPE_SPEED;
		if(wipeOffset >= 1)
			wipeOffset = 0;
	}
}
