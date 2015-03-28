package org.mshare.picture;

import android.os.Handler;
import android.os.Looper;

public class RefreshHandler extends Handler {
	private static final String TAG = RefreshHandler.class.getSimpleName();

	// �жϵ�ǰ�Ƿ���ˢ��ѭ����
	private boolean isRefreshLooping = false;

	// ����ģʽ
	private static RefreshHandler sRefreshHandler;

	private RefreshHandler(Looper looper, Handler.Callback callback) {
		super(looper, callback);
	}

	public static RefreshHandler init(Looper looper, Handler.Callback callback) {
		sRefreshHandler = new RefreshHandler(looper, callback);
		return sRefreshHandler;
	}

	public static RefreshHandler getInstance() {
		// ���ܷ��ص�ʱnull
		return sRefreshHandler;
	}

	public boolean isRefreshLooping() {
		return isRefreshLooping;
	}

	public void setRefreshLooping(boolean isRefreshLooping) {
		this.isRefreshLooping = isRefreshLooping;
	}

}
