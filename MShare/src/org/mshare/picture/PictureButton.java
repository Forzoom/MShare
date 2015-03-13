package org.mshare.picture;

import org.mshare.main.R;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class PictureButton implements Parcelable {
	private static final String TAG = PictureButton.class.getSimpleName();
	
	private int startRadius;
	private int endRadius;
	private int outerRadius;

	// 需要设置，和Canvas紧密相关，是否需要background
	private int cx;
	private int cy;

	private Paint mPaint;
	
	public PictureButton() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}
	
	// 绘制
	public void draw(Canvas canvas, double ratio, Paint paint) {
		int innerRadius = startRadius - (int)((startRadius - endRadius) * ratio);
		int ringWidth = outerRadius - innerRadius - 2;
		Log.d(TAG, "innerRadius : " + innerRadius + " outerRadius : " + outerRadius + " ringWidth : " + ringWidth);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(ringWidth);
		canvas.drawCircle(cx, cy, (innerRadius + outerRadius) / 2, paint);
	}

	public boolean isClickButton(int clickX, int clickY) {
		Log.d(TAG, "center x:" + cx + " centerY:" + cy + " clickX:" + clickX + " clickY:" + clickY);
		int distanceX = clickX - cx;
		int distanceY = clickY - cy;
		Log.d(TAG, "disX:" + distanceX + " disY:" + distanceY);
		int distance = (int)Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		Log.d(TAG, "distance is :" + distance);
		return distance <= outerRadius;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(startRadius);
		dest.writeInt(endRadius);
		dest.writeInt(outerRadius);
		dest.writeInt(cx);
		dest.writeInt(cy);
	}
	
	public static final Creator<PictureButton> CREATOR = new Creator<PictureButton>() {

		@Override
		public PictureButton createFromParcel(Parcel source) {
			PictureButton pb = new PictureButton();
			
			pb.setStartRadius(source.readInt());
			pb.setEndRadius(source.readInt());
			pb.setOuterRadius(source.readInt());
			pb.setCx(source.readInt());
			pb.setCy(source.readInt());
			
			return pb;
		}

		@Override
		public PictureButton[] newArray(int size) {
			return new PictureButton[size];
		}
		
	};
	

	/**
	 * @return the startRadius
	 */
	public int getStartRadius() {
		return startRadius;
	}

	/**
	 * @param startRadius the startRadius to set
	 */
	public void setStartRadius(int startRadius) {
		this.startRadius = startRadius;
	}

	/**
	 * @return the endRadius
	 */
	public int getEndRadius() {
		return endRadius;
	}

	/**
	 * @param endRadius the endRadius to set
	 */
	public void setEndRadius(int endRadius) {
		this.endRadius = endRadius;
	}

	/**
	 * @return the outerRadius
	 */
	public int getOuterRadius() {
		return outerRadius;
	}

	/**
	 * @param outerRadius the outerRadius to set
	 */
	public void setOuterRadius(int outRadius) {
		this.outerRadius = outRadius;
	}
	
	/**
	 * @return the cx
	 */
	public int getCx() {
		return cx;
	}

	/**
	 * @param cx the cx to set
	 */
	public void setCx(int cx) {
		this.cx = cx;
	}

	/**
	 * @return the cy
	 */
	public int getCy() {
		return cy;
	}

	/**
	 * @param cy the cy to set
	 */
	public void setCy(int cy) {
		this.cy = cy;
	}
}
