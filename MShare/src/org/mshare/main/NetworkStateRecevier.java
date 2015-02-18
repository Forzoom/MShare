package org.mshare.main;

import java.lang.reflect.Field;

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
 * 可能需要更改至只支持WIFI的情况
 * 仅仅用于支持WIFI
 * @author HM
 *
 */
public class NetworkStateRecevier extends BroadcastReceiver {

	private static final String TAG = NetworkStateRecevier.class.getSimpleName();
	
	private OnRssiChangeListener rcListener;
	
	// 为没有连接时设置的状态
	public static final int TYPE_NONE = -1;
	
	public StateController mState;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		// 当连接状态发生了改变
		// CONNECTIVITY_ACTION 发生在连出的网络发生变化的时候
		// RSSI_CHANGE 发生在WIFI所连接的AP发生变化的时候
		
		// WIFI状态
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (mState != null) {
				mState.setWifiState(StateController.getWifiState());
			}
		}
		
		// TODO 当前不处理意外情况
		if (rcListener != null && action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			// TODO 告知连接发生了中断
		}

		// 获得对应的WIFI_AP_STATE_CHANGED_ACTION
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		Field wifiApStateChangeActionField = null;
		try {
			wifiApStateChangeActionField = wm.getClass().getDeclaredField("WIFI_AP_STATE_CHANGED_ACTION");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		// 只有在存在Action的情况下
		// 出错的问题太麻烦
		String wifiApStateChangeAction = "";
		if (wifiApStateChangeActionField != null) {
			try {
				wifiApStateChangeAction = (String)wifiApStateChangeActionField.get(wm);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		
		// AP状态
		if (!wifiApStateChangeAction.equals("") && action.equals(wifiApStateChangeAction)) {
			mState.setWifiApState(StateController.getWifiApState());
		}
	}

	interface OnRssiChangeListener {
		/**
		 * 当所连接的AP发生改变的时候
		 */
		void onRssiChange();
	}
}
