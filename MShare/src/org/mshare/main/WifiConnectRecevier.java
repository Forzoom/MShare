package org.mshare.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * ������ǰ��WIFI״̬�㲥
 * @author HM
 *
 */
public class WifiConnectRecevier extends BroadcastReceiver {

	private static final String TAG = WifiConnectRecevier.class.getSimpleName();
	private OnWifiConnectChangeListener listener;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		// ������״̬�����˸ı�
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			if (listener != null) {
				listener.onWifiConnectChange(MShareUtil.isConnectedUsingWifi());
			}
		}
	}

	public void setListener(OnWifiConnectChangeListener listener) {
		this.listener = listener;
	}
	
	interface OnWifiConnectChangeListener {
		/**
		 * ��ǰwifi״̬�Ƿ���ã�
		 * @param connected
		 */
		void onWifiConnectChange(boolean connected);
	}
}