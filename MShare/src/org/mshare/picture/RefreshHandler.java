package org.mshare.picture;

import android.os.Handler;
import android.os.Looper;

public class RefreshHandler extends Handler {
	private static final String TAG = RefreshHandler.class.getSimpleName();

	// 判断当前是否在刷新循环中
	private boolean isRefreshLooping = false;

	// 单例模式
	private static RefreshHandler sRefreshHandler;

	private RefreshHandler(Looper looper, Handler.Callback callback) {
		super(looper, callback);
	}

	public static RefreshHandler init(Looper looper, Handler.Callback callback) {
		sRefreshHandler = new RefreshHandler(looper, callback);
		return sRefreshHandler;
	}

	public static RefreshHandler getInstance() {
		// 可能返回的时null
		return sRefreshHandler;
	}

	public boolean isRefreshLooping() {
		return isRefreshLooping;
	}

	public void setRefreshLooping(boolean isRefreshLooping) {
		this.isRefreshLooping = isRefreshLooping;
	}

}
