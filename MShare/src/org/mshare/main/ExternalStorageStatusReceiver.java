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
public class ExternalStorageStatusReceiver extends BroadcastReceiver {

	private StatusController mStatus;
	
	public ExternalStorageStatusReceiver(StatusController status) {
		this.mStatus = status;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (mStatus != null) {
			String action = intent.getAction();
			// �޷���using���д���
			if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // ��չ�����γ�
				mStatus.setExternalStorageState(StatusController.STATE_EXTERNAL_STORAGE_DISABLE);
			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // ��չ������ʹ��
				mStatus.setExternalStorageState(StatusController.STATE_EXTERNAL_STORAGE_ENABLE);
			}
		}
	}
}
