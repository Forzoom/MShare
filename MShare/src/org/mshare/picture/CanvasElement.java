package org.mshare.picture;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.animation.Interpolator;

public abstract class CanvasElement  {
	private static final String TAG = CanvasElement.class.getSimpleName();
	
	// 用于保存所有的动画效果
	private ArrayList<CanvasAnimation> animations = new ArrayList<CanvasAnimation>();
	// 当Animation不存在的时候所应该设置的index
	public static final int ANIMATION_INDEX_NONE = -1;
	
	public abstract boolean isClicked(int clickX, int clickY);
	
	public void draw(Canvas canvas, Paint paint) {
		long currentTime = System.currentTimeMillis();
		// 使用动画效果
		for (int i = 0, len = animations.size(); i < len; i++) {
			CanvasAnimation animation = animations.get(i);
			
			if (animation.isStarted()) {
				// 需要startTime调整ratio
				float ratio = (float)(currentTime - animation.getStartTime()) / (float)animation.getDuration();
				Log.d(TAG, "ratio : " + ratio + " currentTime : " + currentTime + " startTime : " + animation.getStartTime());
				// 准备停止
				if (ratio > 1.0f) {
					ratio = 1.0f;
					animation.calcAndDoAnimation(ratio);
					animation.stop();
					if (animation.getRepeatMode() == CanvasAnimation.REPEAT_MODE_INFINITE) {
						// 直接启动下次动画
						animation.start(currentTime);
					}
				} else {
					animation.calcAndDoAnimation(ratio);
				}
			}
		}
		
		// 做完所有的动画效果之后
		paint(canvas, paint);
	}
	
	/**
	 * 当有Animation的时候
	 * @return
	 */
	public boolean hasAnimation() {
		for (int i = 0, len = animations.size(); i < len; i++) {
			if (animations.get(i).isStarted()) {
				return true;
			}
		}
		return false;
	}
	
	// 
	public void addAnimation(CanvasAnimation animation) {
		animations.add(animation);
	}
	
	// 移除aniamtion
	public void removeAnimation(int index) {
		animations.remove(index);
	}
	
	// 绘制所需要的内容
	public void paint(Canvas canvas, Paint paint) {}
}
