package org.mshare.picture;

import org.mshare.main.MShareApp;
import org.mshare.main.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.animation.Interpolator;

public class PictureBackground extends CanvasElement {
	private static final String TAG = PictureBackground.class.getSimpleName();
	
	private int colorBlueDarken;
	private int colorBlueLighten;
	
	private int currentColor;
	
	private static final int DIRECTION_DARK = -1;
	private static final int DIRECTION_LIGHT = 1;
	// 默认明亮
	private int direction = DIRECTION_LIGHT;
	
	private static final int MASK_RED = 0x00ff0000;
	private static final int MASK_GREEN = 0x0000ff00;
	private static final int MASK_BLUE = 0x000000ff;
	
	public PictureBackground() {
		
	}
	
	@Override
	public void paint(Canvas canvas, Paint paint) {
		Log.d(TAG, "color : " + Integer.toHexString(currentColor));
		canvas.drawColor(currentColor);
	}

	// 判断当前是否点击了Button
	// background是不能点击的
	public boolean isClicked(int clickX, int clickY) {
		return false;
	}

	class ColorAnimation extends CanvasAnimation {

		int startColor;
		int endColor;
		
		public ColorAnimation(int startColor, int endColor) {
			this.startColor = startColor;
			this.endColor = endColor;
		}
		
		@Override
		public void doAnimation(float ratio) {
			int color = 0xff000000;
			int startRed = (MASK_RED & startColor), endRed = (MASK_RED & endColor);
			int startGreen = (MASK_GREEN & startColor), endGreen = (MASK_GREEN & endColor);
			int startBlue = (MASK_BLUE & startColor), endBlue = (MASK_BLUE & endColor);
			
			color |= (startRed + ((int)((endRed - startRed) * ratio) & MASK_RED));
			color |= (startGreen + ((int)((endGreen - startGreen) * ratio) & MASK_GREEN));
			color |= (startBlue + ((int)((endBlue - startBlue) * ratio) & MASK_BLUE));
			
			currentColor = color;
		}
		
	}
	
	public CanvasAnimation addColorAnimation(int startColor, int endColor) {
		return addAnimation(new ColorAnimation(startColor, endColor));
	}

	/**
	 * @return the currentColor
	 */
	public int getCurrentColor() {
		return currentColor;
	}

	/**
	 * @param currentColor the currentColor to set
	 */
	public void setCurrentColor(int currentColor) {
		this.currentColor = currentColor;
	}

}
