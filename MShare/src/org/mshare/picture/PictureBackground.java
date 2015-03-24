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

    // 当前所绘制的颜色
	private int currentColor;

	private ColorAnimation colorAnimation;
	
	public PictureBackground() {
        setClickable(false);
        colorAnimation = new ColorAnimation();
	}
	
	@Override
	public void paint(Canvas canvas, Paint paint) {
		Log.d(TAG, "PictureBackground color : " + Integer.toHexString(currentColor));
		canvas.drawColor(currentColor);
	}

	// background是不能点击的
	public boolean isClicked(int clickX, int clickY) {
		return false;
	}

	public class ColorAnimation extends CanvasAnimation {

		private int startColor;
		private int endColor;

		@Override
		public void doAnimation(float ratio) {
			currentColor = ColorComputer.computeGradientColor(startColor, endColor, ratio);
		}
		
		public int getStartColor() {
			return startColor;
		}

		public void setStartColor(int startColor) {
			this.startColor = startColor;
		}

		public int getEndColor() {
			return endColor;
		}

		public void setEndColor(int endColor) {
			this.endColor = endColor;
		}

	}

	public void startColorAnimation(int startColor, int endColor, long startTime, int duration) {
        if (colorAnimation == null) {
            Log.e(TAG, "color animation is null, and create a new one");
            colorAnimation = new ColorAnimation();
        }

        colorAnimation.setStartColor(startColor);
        colorAnimation.setEndColor(endColor);
        colorAnimation.setDuration(duration);
        colorAnimation.start(startTime);
	}
	
	// 停止当前的colorAnimation
	public void stopColorAnimation() {
		// 当前Animation可能被执行
		if (colorAnimation != null) {
			colorAnimation.stop();
		}
	}

	public ColorAnimation getColorAnimation() {
		return colorAnimation;
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
