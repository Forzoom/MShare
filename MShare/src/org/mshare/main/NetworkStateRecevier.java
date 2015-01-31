package org.mshare.main;

import org.mshare.ftp.server.FsService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * 监听当前的WIFI状态广播
 * @author HM
 *
 */
public class NetworkStateRecevier extends BroadcastReceiver {

	private static final String TAG = NetworkStateRecevier.class.getSimpleName();
	
	private OnNetworkStateChangeListener nscListener;
	private OnRssiChangeListener rcListener;
	private OnWifiApStateChangeListener wascListener;
	
	// 为没有连接时设置的状态
	public static final int TYPE_NONE = -1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		// 当连接状态发生了改变
		// CONNECTIVITY_ACTION 发生在连出的网络发生变化的时候
		// RSSI_CHANGE 发生在WIFI所连接的AP发生变化的时候
		
		// ConnectivityManager.TYPE_WIFI
		
		// 连出网络发生改变
		if (nscListener != null && action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			
			if (ni != null) {
				int type = ni.getType();
				String typeName = ni.getTypeName();
				
				Log.v(TAG, "networkType : " + typeName + " type : " + type);
				nscListener.onNetworkStateChange(typeName, type);
			} else {
				nscListener.onNetworkStateChange("NONE", TYPE_NONE);
			}
		}
		
		// TODO 当前不处理意外情况
		if (rcListener != null && action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			// TODO 告知连接发生了中断
		}
		
		// TODO 接收WifiAp的改变情况
		if (action.equals(FsService.WIFI_AP_STATE_CHANGED_ACTION)) {
			int state = intent.getIntExtra(FsService.EXTRA_WIFI_AP_STATE, FsService.WIFI_AP_STATE_FAILED);
			switch(state) {
				case FsService.WIFI_AP_STATE_DISABLED:
				case FsService.WIFI_AP_STATE_DISABLING:
				case FsService.WIFI_AP_STATE_FAILED:
					wascListener.onWifiApStateChange(false);
					break;
				case FsService.WIFI_AP_STATE_ENABLED:
				case FsService.WIFI_AP_STATE_ENABLING:
					wascListener.onWifiApStateChange(true);
					break;
			}
		}
	}

	public void setOnNetworkStateChangeListener(OnNetworkStateChangeListener l) {
		this.nscListener = l;
	}
	
	public void setOnWifiApStateChangeListener(OnWifiApStateChangeListener l) {
		this.wascListener = l;
	}
	
	interface OnNetworkStateChangeListener {
		/**
		 * 当前网络状态
		 * @param type 当前的网络状态 Connectivity.TYPE_WIFI
		 */
		void onNetworkStateChange(String typeName, int type);
	}
	interface OnRssiChangeListener {
		/**
		 * 当所连接的AP发生改变的时候
		 */
		void onRssiChange();
	}
	interface OnWifiApStateChangeListener {
		/**
		 * 检测当前wifiAp的状态是否启动
		 * @param enable
		 */
		public void onWifiApStateChange(boolean enable);
	}
}
