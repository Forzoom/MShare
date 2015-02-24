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

	private StateController mState;
	
	public ExternalStorageStateReceiver(StateController state) {
		this.mState = state;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (mState != null) {
			String action = intent.getAction();
			// �޷���using���д���
			if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // ��չ�����γ�
				mState.setExternalStorageState(StateController.STATE_EXTERNAL_STORAGE_DISABLE);
			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // ��չ������ʹ��
				mState.setExternalStorageState(StateController.STATE_EXTERNAL_STORAGE_ENABLE);
			}
		}
	}
}
