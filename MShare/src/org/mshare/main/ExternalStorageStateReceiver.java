package org.mshare.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * ״̬���ܲ����Ǻܾ�׼
 * ACTION_MEDIA_REMOVED�Ὣ״̬����Ϊ������
 * ACTION_MEDIA_MOUNTED�Ὣ״̬����Ϊ����
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
			// �޷���using���д���
			if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // ��չ�����γ�
				mState.setExternalStorageState(StatusController.STATE_EXTERNAL_STORAGE_DISABLE);
			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // ��չ������ʹ��
				mState.setExternalStorageState(StatusController.STATE_EXTERNAL_STORAGE_ENABLE);
			}
		}
	}
}
