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
 * ������ǰ��WIFI״̬�㲥
 * @author HM
 *
 */
public class NetworkStateRecevier extends BroadcastReceiver {

	private static final String TAG = NetworkStateRecevier.class.getSimpleName();
	
	private OnNetworkStateChangeListener nscListener;
	private OnRssiChangeListener rcListener;
	private OnWifiApStateChangeListener wascListener;
	
	// Ϊû������ʱ���õ�״̬
	public static final int TYPE_NONE = -1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		// ������״̬�����˸ı�
		// CONNECTIVITY_ACTION ���������������緢���仯��ʱ��
		// RSSI_CHANGE ������WIFI�����ӵ�AP�����仯��ʱ��
		
		// ConnectivityManager.TYPE_WIFI
		
		// �������緢���ı�
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
		
		// TODO ��ǰ�������������
		if (rcListener != null && action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			// TODO ��֪���ӷ������ж�
		}
		
		// TODO ����WifiAp�ĸı����
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
		 * ��ǰ����״̬
		 * @param type ��ǰ������״̬ Connectivity.TYPE_WIFI
		 */
		void onNetworkStateChange(String typeName, int type);
	}
	interface OnRssiChangeListener {
		/**
		 * �������ӵ�AP�����ı��ʱ��
		 */
		void onRssiChange();
	}
	interface OnWifiApStateChangeListener {
		/**
		 * ��⵱ǰwifiAp��״̬�Ƿ�����
		 * @param enable
		 */
		public void onWifiApStateChange(boolean enable);
	}
}
