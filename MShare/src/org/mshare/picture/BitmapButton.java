package org.mshare.picture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BitmapButton extends CanvasElement {

	private int x;
	private int y;
	private Bitmap bitmap;

	public BitmapButton(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	@Override
	public boolean isClicked(int clickX, int clickY) {
		return false;
	}

	@Override
	public void paint(Canvas canvas, Paint paint) {
		canvas.drawBitmap(bitmap, x, y, paint);
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
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
}
