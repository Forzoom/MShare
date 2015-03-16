package org.mshare.picture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class SettingsButton extends CanvasElement {
	private static final String TAG = SettingsButton.class.getSimpleName();
	
	private int x;
	private int y;
	private Bitmap solidIconBitmap;

	private int alpha;
	
	private AlphaAnimation alphaAnimation;
	
	public SettingsButton(Bitmap bitmap) {
		this.solidIconBitmap = bitmap;
	}
	
	@Override
	public boolean isClicked(int clickX, int clickY) {
		if (solidIconBitmap == null) {
			Log.e(TAG, "solidIconBitmap is null");
			return false;
		}
		
		if (x <= clickX && clickX <= (x + solidIconBitmap.getWidth()) && y <= clickY && clickY <= (y + solidIconBitmap.getHeight())) {
			return true;
		}
		return false;
	}

	@Override
	public void paint(Canvas canvas, Paint paint) {
		paint.setAlpha(alpha);
		canvas.drawBitmap(solidIconBitmap, x, y, paint);
		paint.setAlpha(255);
	}

	public void startAlphaAnimation(int targetAlpha) {
		startAlphaAnimation(targetAlpha, System.currentTimeMillis());
	}
	
	public void startAlphaAnimation(int targetAlpha, long startTime) {
		if (alphaAnimation == null) {
			Log.e(TAG, "alpha animation is null");
			return;
		}
		alphaAnimation.setTargetAlpha(targetAlpha);
		alphaAnimation.start(startTime);
	}
	
	public void stopAlphaAnimation() {
		
	}
	
	public void setAlphaAnimation(AlphaAnimation alphaAnimation) {
		this.alphaAnimation = alphaAnimation;
		addAnimation(alphaAnimation);
	}
	
	public AlphaAnimation getAlphaAnimation() {
		return alphaAnimation;
	}
	
	public class AlphaAnimation extends CanvasAnimation {

		private int targetAlpha;
		
		private int originAlpha = 255;
		
		public AlphaAnimation(CanvasElement owner, int targetAlpha) {
			super(owner);
			this.targetAlpha = targetAlpha;
		}

		public AlphaAnimation(CanvasElement owner) {
			super(owner);
			setInterpolator(new BounceInterpolator());
		}

		@Override
		public void doAnimation(float ratio) {
			alpha = originAlpha + (int)((targetAlpha - originAlpha) * ratio);
		}

		public int getTargetAlpha() {
			return targetAlpha;
		}

		public void setTargetAlpha(int targetAlpha) {
			this.targetAlpha = targetAlpha;
		}

	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public Bitmap getBitmap() {
		return solidIconBitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.solidIconBitmap = bitmap;
	}
}
