package org.mshare.picture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;;

public class ServerOverviewSurfaceView extends SurfaceView {

	private GestureDetector gestureDetector;
	
	public ServerOverviewSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ServerOverviewSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ServerOverviewSurfaceView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	/**
	 * @param gestureDetector the gestureDetector to set
	 */
	public void setGestureDetector(GestureDetector gestureDetector) {
		this.gestureDetector = gestureDetector;
	}
	
}
