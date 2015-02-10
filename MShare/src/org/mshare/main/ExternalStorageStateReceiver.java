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

	private OnExternalStorageStateChangeListener listener = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (listener != null) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // ��չ�����γ�
				listener.onExternalStorageStateChange(false);
			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // ��չ������ʹ��
				listener.onExternalStorageStateChange(true);
			}
		}
	}

	public void setOnExternalStorageStorageStateChangeListener(OnExternalStorageStateChangeListener listener) {
		this.listener = listener;
	}
	
	public interface OnExternalStorageStateChangeListener {
		/**
		 * ֻ�е�ֵMOUNTED��ʱ��usableΪtrue
		 * @param usable
		 */
		public void onExternalStorageStateChange(boolean usable);
	}
}
