package org.mshare.main;

import org.mshare.ftp.server.FsService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * ���ڽ��շ�����״̬�����޸�UI
 * @author HM
 *
 */
public class ServerStateRecevier extends BroadcastReceiver {

	private static final String TAG = ServerStateRecevier.class.getSimpleName();
	private OnServerStateChangeListener listener;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		if (listener != null) {
			if (action.equals(FsService.ACTION_STARTED)) {
				listener.onServerStateChange(true);
			} else if (action.equals(FsService.ACTION_STOPPED) || action.equals(FsService.ACTION_FAILEDTOSTART)) {
				listener.onServerStateChange(false);
			}
		}
	}

	public void setListener(OnServerStateChangeListener listener) {
		this.listener = listener;
	}
	
	interface OnServerStateChangeListener {
		/**
		 * ����������ʾ�������������ͽ���
		 * @param start
		 */
		void onServerStateChange(boolean start);
	}
}
