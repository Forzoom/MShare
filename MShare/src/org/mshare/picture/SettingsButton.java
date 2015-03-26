package org.mshare.picture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class SettingsButton extends CanvasElement {
	private static final String TAG = SettingsButton.class.getSimpleName();
	
	// padding也在点击的返回之内
	private int paddingLeft;
	private int paddingTop;
	private int paddingRight;
	private int paddingBottom;
	private int x;
	private int y;
	private Bitmap solidIconBitmap;

	private int alpha = 255;
	
	private AlphaAnimation alphaAnimation;
	
	public SettingsButton(Bitmap bitmap) {
		this.solidIconBitmap = bitmap;
		setClickable(true);
	}
	
	@Override
	public boolean isClicked(int clickX, int clickY) {
		if (solidIconBitmap == null) {
			Log.e(TAG, "solidIconBitmap is null");
			return false;
		}
		Log.d(TAG, "the click x : " + clickX + " clickY : " + clickY);
		Log.d(TAG, "l : " + x + " r : " + (x + solidIconBitmap.getWidth()) + " t " + y + " b : " + (y + solidIconBitmap.getHeight()));
		int leftLimit = x - paddingLeft;
		int topLimit = y - paddingTop;
		int rightLimit = x + solidIconBitmap.getWidth() + paddingRight;
		int bottomLimit = y + solidIconBitmap.getHeight() + paddingBottom;
		if (leftLimit <= clickX && clickX <= rightLimit && topLimit <= clickY && clickY <= bottomLimit) {
			return true;
		}
		return false;
	}

	@Override
	public void paint(Canvas canvas, Paint paint) {
		Log.d(TAG, "draw settings button");
		paint.setAlpha(alpha);
		canvas.drawBitmap(solidIconBitmap, x, y, paint);
		paint.setAlpha(255);
	}

    // 当前默认的AlphaAnimation
    public AlphaAnimation getDefaultAlphaAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(this, 223);
		addAnimation(alphaAnimation);
        alphaAnimation.setDuration(300);
        return alphaAnimation;
    }

    public void startAlphaAnimation(int targetAlpha) {
		startAlphaAnimation(targetAlpha, System.currentTimeMillis());
	}
	
	public void startAlphaAnimation(int targetAlpha, long startTime) {
		if (alphaAnimation == null) {
			Log.e(TAG, "alpha animation is null");
            alphaAnimation = getDefaultAlphaAnimation();
			return;
		}
		alphaAnimation.setTargetAlpha(targetAlpha);
		alphaAnimation.start(startTime);
	}
	
	public void stopAlphaAnimation() {
		if (alphaAnimation != null) {
            alphaAnimation.stop();
        }
	}

	public AlphaAnimation getAlphaAnimation() {
//		if (!canRefresh()) {
//            Log.e(TAG, "cannot get animation now!");
//            return null;
//        }
        return alphaAnimation;
	}
	
	// 设置padding
	public void setPadding(int left, int top, int right, int bottom) {
		this.paddingLeft = left;
		this.paddingRight = right;
		this.paddingTop = top;
		this.paddingBottom = bottom;
	}
	
	public class AlphaAnimation extends CanvasAnimation {

		private int targetAlpha;
		
		private int originAlpha = 255;
		
		public AlphaAnimation(CanvasElement owner, int targetAlpha) {
			this.targetAlpha = targetAlpha;
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
