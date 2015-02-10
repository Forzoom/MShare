package org.mshare.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * 状态可能并不是很精准
 * ACTION_MEDIA_REMOVED会将状态设置为不可用
 * ACTION_MEDIA_MOUNTED会将状态设置为可用
 * @author HM
 *
 */
public class ExternalStorageStateReceiver extends BroadcastReceiver {

	private OnExternalStorageStateChangeListener listener = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (listener != null) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // 扩展卡被拔出
				listener.onExternalStorageStateChange(false);
			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // 扩展卡可以使用
				listener.onExternalStorageStateChange(true);
			}
		}
	}

	public void setOnExternalStorageStorageStateChangeListener(OnExternalStorageStateChangeListener listener) {
		this.listener = listener;
	}
	
	public interface OnExternalStorageStateChangeListener {
		/**
		 * 只有当值MOUNTED的时候usable为true
		 * @param usable
		 */
		public void onExternalStorageStateChange(boolean usable);
	}
}
