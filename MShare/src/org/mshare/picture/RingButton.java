package org.mshare.picture;

import org.mshare.main.MShareApp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RingButton extends CanvasElement implements Parcelable {
	private static final String TAG = RingButton.class.getSimpleName();
	
	private int innerRadius;
	private int outerRadius;
	private Point center;
	private int ringColor;
    // ��������Ч��
	private BounceAnimation bounceAnimation;
    // ��������Ч��
	private BreatheAnimation breatheAnimation;
	
	public RingButton() {
		setClickable(true);
	}
	
	@Override
	public void paint(Canvas canvas, Paint paint) {
		int ringWidth = outerRadius - innerRadius;
		paint.setColor(ringColor);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(ringWidth);
		canvas.drawCircle(center.x, center.y, (innerRadius + outerRadius) / 2, paint);
	}
	
	// �жϵ�ǰ�Ƿ�����Button
    @Override
	public boolean isClicked(int clickX, int clickY) {
		int distanceX = clickX - center.x;
		int distanceY = clickY - center.y;
		int distance = (int)Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		return distance <= outerRadius;
	}

    /**
     * �������������ʹ��System.currentTimeMillis()
     * @param newInnerRadius
     */
	public void startBounceAnimation(int newInnerRadius) {
		startBounceAnimation(newInnerRadius, System.currentTimeMillis());
	}

    /**
     * �������������ʹ��ָ����ʱ��
     * @param newInnerRadius
     * @param startTime
     */
	public void startBounceAnimation(int newInnerRadius, long startTime) {
		if (bounceAnimation == null) {
			Log.d(TAG, "bounce animation is null");
            bounceAnimation = new BounceAnimation(this, newInnerRadius);
			return;
		}
		bounceAnimation.setTargetInnerRadius(newInnerRadius);
		Log.d(TAG, "bounce inner radius : " + newInnerRadius + " startTime : " + startTime);
		bounceAnimation.start(startTime);
	}

    /**
     * �����ǰ�����������Ļ�����ֹͣ��ǰ����������
     */
	public void stopBounceAnimation() {
		if (bounceAnimation != null) {
			bounceAnimation.stop();
		}
	}

    /**
     * ������������
     * @param newOuterRadius
     */
	public void startBreatheAnimation(int newOuterRadius) {
		startBreatheAnimation(newOuterRadius, System.currentTimeMillis());
	}

    /**
     * ������������
     * @param targetOuterRadius
     * @param startTime
     */
	public void startBreatheAnimation(int targetOuterRadius, long startTime) {
		if (breatheAnimation == null) {
			Log.d(TAG, "breathe animation is null");
            breatheAnimation = new BreatheAnimation(this, targetOuterRadius);
			return;
		}
		breatheAnimation.setTargetOuterRadius(targetOuterRadius);
		Log.d(TAG, "bounce outer radius : " + targetOuterRadius + " startTime : " + startTime);
		breatheAnimation.start(startTime);
	}

    /**
     * ֹͣ��ǰ�ĺ�������
     */
	public void stopBreatheAnimation() {
		if (breatheAnimation != null) {
			breatheAnimation.stop();
		}
	}

	public BounceAnimation getBounceAnimation() {
        // surfaceû�д�����ʱ�򣬽����ܶ�animation���в���
        if (!canRefresh()) {
            return null;
        }
		return bounceAnimation;
	}

	public BreatheAnimation getBreatheAnimation() {
        if (!canRefresh()) {
            return null;
        }
		return breatheAnimation;
	}

	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(innerRadius);
		dest.writeInt(outerRadius);
		dest.writeInt(center.x);
		dest.writeInt(center.y);
		dest.writeInt(ringColor);
	}
	
	public static final Creator<RingButton> CREATOR = new Creator<RingButton>() {

		@Override
		public RingButton createFromParcel(Parcel source) {
			RingButton rb = new RingButton();
            rb.setRadius(source.readInt(), source.readInt());
			rb.setCenter(new Point(source.readInt(), source.readInt()));
			rb.setRingColor(source.readInt());
			return rb;
		}

		@Override
		public RingButton[] newArray(int size) {
			return new RingButton[size];
		}
		
	};

    /**
     * ��������
     */
	public class BreatheAnimation extends CanvasAnimation {

		private int targetOuterRadius;
		private int originOuterRadius;
		
		public BreatheAnimation(CanvasElement owner, int targetOuterRadius) {
			super(owner);
			this.targetOuterRadius = targetOuterRadius;
			this.originOuterRadius = outerRadius;
			setInterpolator(new BreatheInterpolator());
		}
		
		@Override
		public void doAnimation(float ratio) {
			outerRadius = originOuterRadius + (int)((targetOuterRadius - originOuterRadius) * ratio);
		}

		public int getTargetOuterRadius() {
			return targetOuterRadius;
		}

		public void setTargetOuterRadius(int targetOuterRadius) {
			this.targetOuterRadius = targetOuterRadius;
		}

	}

    /**
     * ��������
     */
	public class BounceAnimation extends CanvasAnimation {

		int targetInnerRadius;
		int originInnerRadius;
		
		public BounceAnimation(CanvasElement owner, int targetInnerRadius) {
			super(owner);
			this.targetInnerRadius = targetInnerRadius;
			this.originInnerRadius = innerRadius;
			setInterpolator(new BounceInterpolator());
		}
		
		@Override
		public void doAnimation(float ratio) {
			innerRadius = originInnerRadius + (int)((targetInnerRadius - originInnerRadius) * ratio);
		}

		public int getTargetInnerRadius() {
			return targetInnerRadius;
		}

		public void setTargetInnerRadius(int targetInnerRadius) {
			this.targetInnerRadius = targetInnerRadius;
		}

		@Override
		public void onStart() {
			setClickable(false);
			super.onStart();
		}
		
		@Override
		public void onStop() {
			setClickable(true);
			super.onStop();
		}
	}

    /**
     * ����Բ���뾶��ص�����
     * @param innerRadius
     * @param outerRadius
     */
	public void setRadius(int innerRadius, int outerRadius) {
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        int ringWidth = outerRadius - innerRadius;
        bounceAnimation = new BounceAnimation(this, innerRadius - ringWidth);
        breatheAnimation = new BreatheAnimation(this, outerRadius + ringWidth);
	}

    public int getInnerRadius() {
        return innerRadius;
    }

	public int getOuterRadius() {
		return outerRadius;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public int getRingColor() {
		return ringColor;
	}

	public void setRingColor(int ringColor) {
		this.ringColor = ringColor;
	}

}
