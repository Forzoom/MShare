package org.mshare.picture;

import android.util.Log;
import android.view.animation.Interpolator;

public class BreatheInterpolator implements Interpolator {
	private static final String TAG = BreatheInterpolator.class.getSimpleName();
	
	private float bound;
	
	public BreatheInterpolator() {
		bound = 1.0f / 3.0f;
	}
	
	@Override
	public float getInterpolation(float input) {
		
		// 吸气急促,呼气缓慢
		if (input < bound) {
			
			double radian = input / bound * Math.PI;
			Log.d(TAG, "the radian : " + radian + " the result input : " + ((float)Math.cos(radian) + 1));
			return (float)Math.cos(radian) + 1;
		} else if (input < 1.0f) {
			double radian = (input - bound) / (1.0f - bound) * Math.PI;
			Log.d(TAG, "the radian : " + radian + " the result input : " + ((float)Math.cos(radian) + 1));
			return (float)Math.cos(Math.PI + radian) + 1;
		} else {
			// 对于其他情况，没有更好的方法
			Log.d(TAG, "the input > 1.0");
			return 0;
		}
	}

}
