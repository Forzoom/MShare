package org.mshare.picture;

import org.mshare.main.MShareApp;
import org.mshare.main.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.Bitmap.Config;

public class CircleAvater extends CanvasElement {

	private int cx;
	private int cy;
	private int radius;
	private Bitmap avater;

	@Override
	public boolean isClicked(int clickX, int clickY) {
		// ²»¿Éµã»÷
		return false;
	}

	@Override
	public void paint(Canvas canvas, Paint paint) {
		canvas.drawBitmap(avater, cx - avater.getWidth() / 2, cy - avater.getHeight() / 2, paint);	
	}

	
	public int getCx() {
		return cx;
	}

	public void setCx(int cx) {
		this.cx = cx;
	}

	public int getCy() {
		return cy;
	}

	public void setCy(int cy) {
		this.cy = cy;
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
