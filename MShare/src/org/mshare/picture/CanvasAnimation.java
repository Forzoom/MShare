package org.mshare.picture;

import android.util.Log;
import android.view.animation.Interpolator;

/**
 * 包括Animation所需要的内容
 * @author HM
 *
 */
public abstract class CanvasAnimation {
	private static final String TAG = CanvasAnimation.class.getSimpleName();
	// 当前动画正在运行
	private static int STATUS_RUNNING = 1;
	// 当前动画停止
	private static int STATUS_STOP = 2;
	// 当前动画状态
	private int status = STATUS_STOP;
	// 变速器
	private Interpolator interpolator;
	public long START_TIME_UNSET = -1;
	// 动画的开始时间
	private long startTime = START_TIME_UNSET;

	public static int DURATION_UNSET = -1;
	// 动画执行时间
	private int duration = DURATION_UNSET;
	
	private CanvasElement owner;
	
	public static final int REPEAT_MODE_ONCE = 0;
	public static final int REPEAT_MODE_INFINITE = 1;
	
	private int repeatMode = REPEAT_MODE_ONCE;
	
	public CanvasAnimation(CanvasElement owner) {
		this.owner= owner; 
	}
	
	/**
	 * 在调用doAnimation之前使用interpolator进行变速
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
	 *  调用用来改变内容
	 * @param ratio 当前动画的进度
	 */
	public abstract void doAnimation(float ratio);
	
	// 将调用start的时间设置为startTime
	public void start() {
		start(System.currentTimeMillis());
	}
	
	// 使用指定的startTime
	public void start(long startTime) {
		if (status == STATUS_STOP) {
			Log.d(TAG, "set animation status and start");
			status = STATUS_RUNNING;
			setStartTime(startTime);
			onStart();
		} else {
			// 该怎么办?
			Log.e(TAG, "already start");
		}
	}
	// 对应start()
	public void onStart() {}
	
	public boolean isStarted() {
		// 当正在准备刷新的时候，运行刷新
		return status == STATUS_RUNNING;
	}
	
	// stop在CanvasElement中被remove后才调用，以保证stop调用的时候，Animation不再属于任何人
	public void stop() {
		Log.d(TAG, "animation stop");
		status = STATUS_STOP;
		onStop();
	}
	// 对应stop(),Animation就像一个新的Aniamtion
	public void onStop() {}
	
	// 重置Animation的startTime和duration,没有这两个变量，Animation将无法操作
	// 关键是无法从list中消除
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
