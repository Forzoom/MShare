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
	// ��ǰ������������
	private static int STATUS_RUNNING = 1;
	// ��ǰ����ֹͣ
	private static int STATUS_STOP = 2;
	// ��ǰ����״̬
	private int status = STATUS_STOP;
	// ������
	private Interpolator interpolator;
	public long START_TIME_UNSET = -1;
	// �����Ŀ�ʼʱ��
	private long startTime = START_TIME_UNSET;

	public static int DURATION_UNSET = -1;
	// ����ִ��ʱ��
	private int duration = DURATION_UNSET;
	
	private CanvasElement owner;
	
	public static final int REPEAT_MODE_ONCE = 0;
	public static final int REPEAT_MODE_INFINITE = 1;
	
	private int repeatMode = REPEAT_MODE_ONCE;
	
	public CanvasAnimation(CanvasElement owner) {
		this.owner= owner; 
	}
	
	/**
	 * �ڵ���doAnimation֮ǰʹ��interpolator���б���
	 * @param ratio
	 */
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
	
	// ������start��ʱ������ΪstartTime
	public void start() {
		start(System.currentTimeMillis());
	}
	
	// ʹ��ָ����startTime
	public void start(long startTime) {
		if (status == STATUS_STOP) {
			Log.d(TAG, "set animation status and start");
			status = STATUS_RUNNING;
			setStartTime(startTime);
			onStart();
		} else {
			// ����ô��?
			Log.e(TAG, "already start");
		}
	}
	// ��Ӧstart()
	public void onStart() {}
	
	public boolean isStarted() {
		// ������׼��ˢ�µ�ʱ������ˢ��
		return status == STATUS_RUNNING;
	}
	
	// stop��CanvasElement�б�remove��ŵ��ã��Ա�֤stop���õ�ʱ��Animation���������κ���
	public void stop() {
		Log.d(TAG, "animation stop");
		status = STATUS_STOP;
		onStop();
	}
	// ��Ӧstop(),Animation����һ���µ�Aniamtion
	public void onStop() {}
	
	// ����Animation��startTime��duration,û��������������Animation���޷�����
	// �ؼ����޷���list������
	public void reset() {
		repeatMode = REPEAT_MODE_ONCE;
		status = STATUS_STOP;
		startTime = START_TIME_UNSET;
		duration = DURATION_UNSET;
	}
	
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

	public int getRepeatMode() {
		return repeatMode;
	}

	public void setRepeatMode(int repeatMode) {
		this.repeatMode = repeatMode;
	}

}
