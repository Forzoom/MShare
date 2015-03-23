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

public class CircleAvater extends CanvasElement {

	private Point center;
	private int radius;
	private Bitmap avater;

	@Override
	public boolean isClicked(int clickX, int clickY) {
		// 不可点击
		return false;
	}

	@Override
	public void paint(Canvas canvas, Paint paint) {
        if (center != null) {
            canvas.drawBitmap(avater, center.x - radius, center.y - radius, paint);
        }

	}

	public Point getCenter() {
		return center;
	}
    // 在创建的时候最好设置center
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
