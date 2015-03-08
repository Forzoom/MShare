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

	private StatusController mState;
	
	public ExternalStorageStateReceiver(StatusController state) {
		this.mState = state;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (mState != null) {
			String action = intent.getAction();
			// 无法对using进行处理
			if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // 扩展卡被拔出
				mState.setExternalStorageState(StatusController.STATE_EXTERNAL_STORAGE_DISABLE);
			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // 扩展卡可以使用
				mState.setExternalStorageState(StatusController.STATE_EXTERNAL_STORAGE_ENABLE);
			}
		}
	}
}
