package org.mshare.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * 监听当前的WIFI状态广播
 * @author HM
 *
 */
public class WifiStateRecevier extends BroadcastReceiver {

	private static final String TAG = WifiStateRecevier.class.getSimpleName();
	private OnWifiStateChangeListener listener;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		// 当连接状态发生了改变
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			if (listener != null) {
				listener.onWifiConnectChange(MShareUtil.isConnectedUsingWifi());
			}
		}
	}

	public void setListener(OnWifiStateChangeListener listener) {
		this.listener = listener;
	}
	
	interface OnWifiStateChangeListener {
		/**
		 * 当前wifi状态是否可用，
		 * @param connected
		 */
		void onWifiConnectChange(boolean connected);
	}
}
