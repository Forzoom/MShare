package org.mshare.picture;

import android.util.Log;
import android.view.animation.Interpolator;

public class BreatheInterpolator implements Interpolator {
	private static final String TAG = BreatheInterpolator.class.getSimpleName();
	
	// 对应的呼吸开始和呼吸结束的边界
	private float bound;
	
	public BreatheInterpolator() {
		bound = 1.0f / 3.0f;
	}
	
	@Override
	public float getInterpolation(float input) {
		
		// 吸气急促,呼气缓慢
		if (input < bound) {
			double radian = input / bound * Math.PI;
//			Log.d(TAG, "the radian : " + radian + " the result input : " + (-(float)Math.cos(radian) + 1));
			return (-(float)Math.cos(radian) + 1) / 2.0f;
		} else if (input < 1.0f) {
			double radian = (input - bound) / (1.0f - bound) * Math.PI;
//			Log.d(TAG, "the radian : " + radian + " the result input : " + (-(float)Math.cos(Math.PI + radian) + 1) / 2.0f);
			return (-(float)Math.cos(Math.PI + radian) + 1) / 2.0f;
		} else {
			Log.e(TAG, "the input > 1.0");
			return 0;
		}
	}

}
