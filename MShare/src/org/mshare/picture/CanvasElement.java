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
	
	public abstract boolean isClicked(int clickX, int clickY);
	
	public void draw(Canvas canvas, Paint paint) {
		long currentTime = System.currentTimeMillis();
		// ʹ�ö���Ч��
		for (int i = 0, len = animations.size(); i < len; i++) {
			CanvasAnimation animation = animations.get(i);
			// ��ҪstartTime����ratio
			float ratio = (float)(currentTime - animation.getStartTime()) / (float)animation.getDuration();
			Log.d(TAG, "ratio : " + ratio + " currentTime : " + currentTime);
			// ׼��ֹͣ
			if (ratio > 1.0f) {
				ratio = 1.0f;
				animation.calcAndDoAnimation(ratio);
				// ֹͣ���ҷ���animation,���ڵ�animation�����ظ�����
				animation.stop();
				animations.remove(i);
			} else {
				animation.calcAndDoAnimation(ratio);
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
		return animations.size() != 0;
	}
	
	public CanvasAnimation addAnimation(CanvasAnimation animation) {
		// �����animation
		animations.add(animation);
		return animation;
	}
	
	// ��������Ҫ������
	public void paint(Canvas canvas, Paint paint) {}
}
