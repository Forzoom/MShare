package org.mshare.picture;

import android.util.Log;
import android.view.animation.Interpolator;

/**
 * ����Animation����Ҫ������
 * @author HM
 *
 */
public abstract class CanvasAnimation {
	private static final String TAG = CanvasAnimation.class.getSimpleName();
	private static int STATUS_RUNNING = 1;
	private static int STATUS_STOP = 2;
	private int status = STATUS_STOP;
	
	private Interpolator interpolator;
	// �����Ŀ�ʼʱ��
	private long startTime;
	
	private int duration = 500;
	
	public void calcAndDoAnimation(float ratio) {
		if (ratio > 1.0f) {
			ratio = 1.0f;
		}
		if (interpolator != null) {
			ratio = interpolator.getInterpolation(ratio);
			Log.d(TAG, "calc ratio : " + ratio);
		}
		doAnimation(ratio);
	}
	
	/**
	 *  ���������ı�����
	 * @param ratio ��ǰ�����Ľ���
	 */
	public abstract void doAnimation(float ratio);
	
	public void start() {
		start(System.currentTimeMillis());
	}
	
	// ʹ��ָ����startTime
	public void start(long startTime) {
		if (status == STATUS_STOP) {
			Log.d(TAG, "animation start");
			// ����Ϊrunning
			status = STATUS_RUNNING;
			onStart();
		} else {
			Log.e(TAG, "already start");
			// need reset
		}
	}
	
	public void onStart() {}
	
	public boolean isStarted() {
		// ������׼��ˢ�µ�ʱ������ˢ��
		return status == STATUS_RUNNING;
	}
	
	public void stop() {
		Log.d(TAG, "animation stop");
		status = STATUS_STOP;
//		reset();
	}
	
	public void onStop() {}
	
	/**
	 * @return the interpolator
	 */
	public Interpolator getInterpolator() {
		return interpolator;
	}

	/**
	 * @param interpolator the interpolator to set
	 */
	public void setInterpolator(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

}
