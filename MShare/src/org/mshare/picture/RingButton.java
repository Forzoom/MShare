package org.mshare.picture;

import org.mshare.main.MShareApp;
import org.mshare.main.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.animation.Interpolator;

public class RingButton extends CanvasElement implements Parcelable {
	private static final String TAG = RingButton.class.getSimpleName();
	
	private int innerRadius;
	private int outerRadius;
	private int cx;
	private int cy;
	private int ringColor;

	public RingButton() {
		Context context = MShareApp.getAppContext();
		ringColor = context.getResources().getColor(R.color.Color_White);
	}
	
	@Override
	public void paint(Canvas canvas, Paint paint) {
		int ringWidth = outerRadius - innerRadius;
		Log.d(TAG, "innerRadius : " + innerRadius + " outerRadius : " + outerRadius + " ringWidth : " + ringWidth);
		paint.setColor(ringColor);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(ringWidth);
		canvas.drawCircle(cx, cy, (innerRadius + outerRadius) / 2, paint);
	}
	
	// 判断当前是否点击了Button
	public boolean isClicked(int clickX, int clickY) {
		Log.d(TAG, "center x:" + cx + " centerY:" + cy + " clickX:" + clickX + " clickY:" + clickY);
		int distanceX = clickX - cx;
		int distanceY = clickY - cy;
		Log.d(TAG, "disX:" + distanceX + " disY:" + distanceY);
		int distance = (int)Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		Log.d(TAG, "distance is :" + distance);
		return distance <= outerRadius;
	}
	
	public CanvasAnimation addBounceAnimation(int newInnerRadius) {
		// 简单添加，不应该同时有两个animation，但是当前是否有BounceAnimation又不能判断，是不是可以有一个describeContent在调用的时候返回?
		return addAnimation(new BounceAnimation(newInnerRadius));
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(innerRadius);
		dest.writeInt(outerRadius);
		dest.writeInt(cx);
		dest.writeInt(cy);
		dest.writeInt(ringColor);
	}
	
	public static final Creator<RingButton> CREATOR = new Creator<RingButton>() {

		@Override
		public RingButton createFromParcel(Parcel source) {
			RingButton pb = new RingButton();
			
			pb.setInnerRadius(source.readInt());
			pb.setOuterRadius(source.readInt());
			pb.setCx(source.readInt());
			pb.setCy(source.readInt());
			pb.setRingColor(source.readInt());
			return pb;
		}

		@Override
		public RingButton[] newArray(int size) {
			return new RingButton[size];
		}
		
	};
	
	class BounceAnimation extends CanvasAnimation {

		int targetInnerRadius;
		int originInnerRadius;
		
		public BounceAnimation(int targetInnerRadius) {
			this.targetInnerRadius = targetInnerRadius;
			this.originInnerRadius = innerRadius;
			setInterpolator(new BounceInterpolator());
		}
		
		@Override
		public void doAnimation(float ratio) {
			innerRadius = originInnerRadius + (int)((targetInnerRadius - originInnerRadius) * ratio);
		}
		
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

	/**
	 * @return the innerRadius
	 */
	public int getInnerRadius() {
		return innerRadius;
	}

	/**
	 * @param innerRadius the innerRadius to set
	 */
	public void setInnerRadius(int innerRadius) {
		this.innerRadius = innerRadius;
	}

	/**
	 * @return the ringColor
	 */
	public int getRingColor() {
		return ringColor;
	}

	/**
	 * @param ringColor the ringColor to set
	 */
	public void setRingColor(int ringColor) {
		this.ringColor = ringColor;
	}

}
