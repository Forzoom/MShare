package org.mshare.picture;

import org.mshare.main.MShareApp;
import org.mshare.main.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class CircleAvater extends CanvasElement {
	private static final String TAG = CircleAvater.class.getSimpleName();

	private Point center;
	private int radius;
	private Bitmap avater;

	@Override
	public boolean isClicked(int clickX, int clickY) {
		// ���ɵ��
		return false;
	}

	@Override
	public void paint(Canvas canvas, Paint paint) {
        if (center != null) {
//			Log.d(TAG, "draw circle avater in center.x :" + center.x + " center.y :" + center.y);
            canvas.drawBitmap(avater, center.x - radius, center.y - radius, paint);
        }

	}

	public Point getCenter() {
		return center;
	}
    // �ڴ�����ʱ���������center
	public void setCenter(Point center) {
		this.center = center;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	public Bitmap getAvater() {
		return avater;
	}

	public void setAvater(Bitmap avater) {
		this.avater = avater;
	}

}
