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
 * ������ǰ��WIFI״̬�㲥
 * ������Ҫ������ֻ֧��WIFI�����
 * ��������֧��WIFI
 * @author HM
 *
 */
public class NetworkStateRecevier extends BroadcastReceiver {

	private static final String TAG = NetworkStateRecevier.class.getSimpleName();
	
	private OnRssiChangeListener rcListener;
	
	// Ϊû������ʱ���õ�״̬
	public static final int TYPE_NONE = -1;
	
	public StateController mState;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		// ������״̬�����˸ı�
		// CONNECTIVITY_ACTION ���������������緢���仯��ʱ��
		// RSSI_CHANGE ������WIFI�����ӵ�AP�����仯��ʱ��
		
		// WIFI״̬
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (mState != null) {
				mState.setWifiState(StateController.getWifiState());
			}
		}
		
		// TODO ��ǰ�������������
		if (rcListener != null && action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			// TODO ��֪���ӷ������ж�
		}

		// ��ö�Ӧ��WIFI_AP_STATE_CHANGED_ACTION
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		Field wifiApStateChangeActionField = null;
		try {
			wifiApStateChangeActionField = wm.getClass().getDeclaredField("WIFI_AP_STATE_CHANGED_ACTION");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		// ֻ���ڴ���Action�������
		// ���������̫�鷳
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
		
		// AP״̬
		if (!wifiApStateChangeAction.equals("") && action.equals(wifiApStateChangeAction)) {
			mState.setWifiApState(StateController.getWifiApState());
		}
	}

	interface OnRssiChangeListener {
		/**
		 * �������ӵ�AP�����ı��ʱ��
		 */
		void onRssiChange();
	}
}
