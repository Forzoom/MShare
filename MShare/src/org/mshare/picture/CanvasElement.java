package org.mshare.picture;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.animation.Interpolator;

public abstract class CanvasElement  {
	private static final String TAG = CanvasElement.class.getSimpleName();
	
	// ���ڱ������еĶ���Ч��
	private ArrayList<CanvasAnimation> animations = new ArrayList<CanvasAnimation>();
	// ��Animation�����ڵ�ʱ����Ӧ�����õ�index
	public static final int ANIMATION_INDEX_NONE = -1;
	
	public abstract boolean isClicked(int clickX, int clickY);
	
	public void draw(Canvas canvas, Paint paint) {
		long currentTime = System.currentTimeMillis();
		// ʹ�ö���Ч��
		for (int i = 0, len = animations.size(); i < len; i++) {
			CanvasAnimation animation = animations.get(i);
			
			if (animation.isStarted()) {
				// ��ҪstartTime����ratio
				float ratio = (float)(currentTime - animation.getStartTime()) / (float)animation.getDuration();
				Log.d(TAG, "ratio : " + ratio + " currentTime : " + currentTime + " startTime : " + animation.getStartTime());
				// ׼��ֹͣ
				if (ratio > 1.0f) {
					ratio = 1.0f;
					animation.calcAndDoAnimation(ratio);
					animation.stop();
					if (animation.getRepeatMode() == CanvasAnimation.REPEAT_MODE_INFINITE) {
						// ֱ�������´ζ���
						animation.start(currentTime);
					}
				} else {
					animation.calcAndDoAnimation(ratio);
				}
			}
		}
		
		// �������еĶ���Ч��֮��
		paint(canvas, paint);
	}
	
	/**
	 * ����Animation��ʱ��
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
	
	// �Ƴ�aniamtion
	public void removeAnimation(int index) {
		animations.remove(index);
	}
	
	// ��������Ҫ������
	public void paint(Canvas canvas, Paint paint) {}
}
