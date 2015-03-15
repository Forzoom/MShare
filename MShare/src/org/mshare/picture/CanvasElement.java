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
	
	public abstract boolean isClicked(int clickX, int clickY);
	
	public void draw(Canvas canvas, Paint paint) {
		long currentTime = System.currentTimeMillis();
		// 使用动画效果
		for (int i = 0, len = animations.size(); i < len; i++) {
			CanvasAnimation animation = animations.get(i);
			// 需要startTime调整ratio
			float ratio = (float)(currentTime - animation.getStartTime()) / (float)animation.getDuration();
			Log.d(TAG, "ratio : " + ratio + " currentTime : " + currentTime);
			// 准备停止
			if (ratio > 1.0f) {
				ratio = 1.0f;
				animation.calcAndDoAnimation(ratio);
				// 停止并且废弃animation,现在的animation不能重复利用
				animation.stop();
				animations.remove(i);
			} else {
				animation.calcAndDoAnimation(ratio);
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
		return animations.size() != 0;
	}
	
	public CanvasAnimation addAnimation(CanvasAnimation animation) {
		// 简单添加animation
		animations.add(animation);
		return animation;
	}
	
	// 绘制所需要的内容
	public void paint(Canvas canvas, Paint paint) {}
}
