package org.mshare.main;

import org.mshare.ftp.server.FsService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.view.View;
/**
 * 监听WifiAp的状态变化
 * @author HM
 *
 */
public class WifiApStateReceiver extends BroadcastReceiver {

	private OnWifiApStateChangeListener listener;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (listener != null) {
			String action = intent.getAction();
			
			// TODO 使用反射获得WifiAp状态变化的广播
			
			if (action.equals(FsService.WIFI_AP_STATE_CHANGED_ACTION)) {
				int state = intent.getIntExtra(FsService.EXTRA_WIFI_AP_STATE, FsService.WIFI_AP_STATE_FAILED);
				switch(state) {
					case FsService.WIFI_AP_STATE_DISABLED:
					case FsService.WIFI_AP_STATE_DISABLING:
					case FsService.WIFI_AP_STATE_FAILED:
						listener.onWifiApStateChange(false);
						break;
					case FsService.WIFI_AP_STATE_ENABLED:
					case FsService.WIFI_AP_STATE_ENABLING:
						listener.onWifiApStateChange(false);
						break;
				}
			}
		}
	}

	public void setOnWifiApStateChangeListener(OnWifiApStateChangeListener listener) {
		this.listener = listener;
	}
	
	interface OnWifiApStateChangeListener {
		/**
		 * 检测当前wifiAp的状态是否可用
		 * @param enable
		 */
		public void onWifiApStateChange(boolean enable);
	}
}
