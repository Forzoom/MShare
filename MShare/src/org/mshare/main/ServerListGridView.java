package org.mshare.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridView;

public class ServerListGridView extends GridView {
	private static final String TAG = ServerListGridView.class.getSimpleName();
	
	private GestureDetector gestureDetector;
	
	public ServerListGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ServerListGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ServerListGridView(Context context) {
		super(context);
	}
	// 为了能够检测到在GridView上发生了onFling事件
	public void setGestureDetector(GestureDetector gestureDetector) {
		this.gestureDetector = gestureDetector;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (gestureDetector != null) {
			gestureDetector.onTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}
	
}
