package org.mshare.picture;

import android.view.animation.Interpolator;

public class BounceInterpolator implements Interpolator{

	@Override
	public float getInterpolation(float input) {
		if (input <= 0.5f) {
			return input * 2;
		} else if (input <= 1.0f) {
			return 2 - input * 2;
		} else {
			return 0;
		}
	}

}
