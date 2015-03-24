package org.mshare.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.mshare.server.ftp.ServerService;

import android.app.Service;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Environment;
import android.util.Log;

/**
 * TODO �Ƿ���Ҫ��getState��isEnable��ʹ��
 * TODO ��wifip2p���֮�������Ƹ��࣬��ǰ�����WIFIp2p������£���֪��wifi�ܲ��ܴ�using���enable
 * TODO ��stateBarʹ��include���뵽xml�ļ���
 * ��StateController��ΪNewConn���ڲ���,�����о�̬����
 * ��receiver�ƶ�������
 * receiver�в�������state������
 * @author HM
 *
 */
public class StatusController {
	private static final String TAG = StatusController.class.getSimpleName();
	
	// ������״̬
	public static final int STATUS_SERVER_UNKNOWN= 0x0;
	public static final int STATUS_SERVER_STARTING = 0x1;
	public static final int STATUS_SERVER_STARTED = 0x2;
	public static final int STATUS_SERVER_STOPING = 0x4;
	public static final int STATUS_SERVER_STOPPED = 0x8;
	private int serverStatus = STATUS_SERVER_UNKNOWN;
	
	public static final int STATE_WIFI_UNKNOWN = -1;
	public static final int STATE_WIFI_ENABLE = 0;
	public static final int STATE_WIFI_DISABLE = 1;
	public static final int STATE_WIFI_USING = 2;
	private int mWifiState = STATE_WIFI_UNKNOWN; 
	
	public static final int STATE_WIFI_AP_UNKNOWN = -1;
	public static final int STATE_WIFI_AP_ENABLE = 0;
	public static final int STATE_WIFI_AP_DISABLE = 1;
	public static final int STATE_WIFI_AP_USING = 2;
	public static final int STATE_WIFI_AP_UNSUPPORT = 3;
	private int mWifiApState = STATE_WIFI_AP_UNKNOWN;
	
	public static final int STATE_WIFI_P2P_UNKNOWN = -1;
	public static final int STATE_WIFI_P2P_ENABLE = 0;
	public static final int STATE_WIFI_P2P_DISABLE = 1;
	public static final int STATE_WIFI_P2P_USING = 2;
	private int mWifiP2pState = STATE_WIFI_P2P_UNKNOWN;
	
	public static final int STATE_NFC_UNKNOWN = -1;
	public static final int STATE_NFC_ENABLE = 0;
	public static final int STATE_NFC_DISABLE = 1;
	public static final int STATE_NFC_USING = 2;
	private int mNfcState = STATE_NFC_UNKNOWN;
	
	public static final int STATE_EXTERNAL_STORAGE_UNKNOWN = -1;
	public static final int STATE_EXTERNAL_STORAGE_ENABLE = 0;
	public static final int STATE_EXTERNAL_STORAGE_DISABLE = 1;
	public static final int STATE_EXTERNAL_STORAGE_USING = 2;
	private int mSdState = STATE_EXTERNAL_STORAGE_UNKNOWN;
	
	// TODO ʹ�÷��������WifiAp״̬�ı�Ĺ㲥
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;
	
	private StatusCallback callback;
	
	// ����״̬��UI������п���
	private NetworkStatusRecevier networkStateReceiver;
	private ExternalStorageStatusReceiver externalStorageStateReceiver;
	private ServerStatusRecevier serverStatusReceiver;
	
	/**
	 * ״̬�仯�Ļص��ӿ�
	 * @author HM
	 *
	 */
	public interface StatusCallback {
		public void onServerStatusChange(int status);
		public void onWifiStatusChange(int status);
		public void onWifiApStatusChange(int status);
		public void onWifiP2pStatusChange(int status);
		public void onExternalStorageChange(int status);
		// NFC��״̬��̫�˽�
		public void onNfcStatusChange(int state);
	}
	
	/**
	 *  ��ʼ�����е�״̬����
	 */
	public void initial() {
		Log.d(TAG, "statusController initial!");
		
		setServerStatus(getServerStatus());
		setWifiStatus(getWifiStatus());
		setWifiApState(getWifiApState());
		setWifiP2pState(getWifiP2pState());
		setNfcState(getNfcState());
		setExternalStorageState(getExternalStorageState());
	}
	
	/**
	 * ��Ҫ��Activity�е�onStart�е���
	 */
	public void registerReceiver() {
		Context context = MShareApp.getAppContext();
		
		/* ע������� */
		
		// ע��򵥵�BroadcastReceiver���������豸������״���仯�����ܴ��ڰ�ȫ����
		networkStateReceiver = new NetworkStatusRecevier(this);
		
		// ����IntentFilter
		IntentFilter wifiConnectFilter = new IntentFilter();
		wifiConnectFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		wifiConnectFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		// ����WifiAp��״̬
		// ��ЩActionӦ��ʹ�÷��������
		// TODO ѧϰʹ�÷���
		wifiConnectFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
		
		context.registerReceiver(networkStateReceiver, wifiConnectFilter);
		
		/*
		 * ��չ�洢������
		 */
		externalStorageStateReceiver = new ExternalStorageStatusReceiver(this);
		IntentFilter externalStorageFilter = new IntentFilter();
		// TODO ��֪����Ҫ��ӵ�Action��ʲô
		// externalStorageFilter.addAction(action)
		context.registerReceiver(externalStorageStateReceiver, externalStorageFilter);
		
		/*
		 * ������״̬������
		 */
		serverStatusReceiver = new ServerStatusRecevier(this);
		
		IntentFilter serverStatusFilter = new IntentFilter();
		serverStatusFilter.addAction(ServerService.ACTION_STARTED);
		serverStatusFilter.addAction(ServerService.ACTION_FAILEDTOSTART);
		serverStatusFilter.addAction(ServerService.ACTION_STOPPED);
		
		context.registerReceiver(serverStatusReceiver, serverStatusFilter);
	}
	
	/**
	 * ��onStop�е���
	 */
	public void unregisterReceiver() {
		Context context = MShareApp.getAppContext();
		if (networkStateReceiver != null) {
			context.unregisterReceiver(networkStateReceiver);
		}

		if (externalStorageStateReceiver != null) {
			context.unregisterReceiver(externalStorageStateReceiver);
		}
		
		if (serverStatusReceiver != null) {
			context.unregisterReceiver(serverStatusReceiver);
		}
	}
	
	public void setCallback(StatusCallback callback) {
		this.callback = callback;
	}
	
	// ��÷�����״̬
	public int getServerStatus() {
		if (serverStatus == STATUS_SERVER_UNKNOWN) {
			if (ServerService.isRunning()) {
				return STATUS_SERVER_STARTED;
			} else {
				return STATUS_SERVER_STOPPED;
			}
		} else {
			return serverStatus;
		}
	}
	
	// ���÷�����״̬
	public void setServerStatus(int status) {
		Log.d(TAG, "set server status : " + status);
		this.serverStatus  = status;
		
		if (callback != null) {
			callback.onServerStatusChange(status);
		}
	}
	
	public static int getWifiStatus() {
		Context context = MShareApp.getAppContext();
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI) {
			return STATE_WIFI_USING;
		}
		// ���wifi�Ƿ���
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		if (wm.isWifiEnabled()) {
			return STATE_WIFI_ENABLE;
		} else {
			return STATE_WIFI_DISABLE;
		}
	}
	
	public void setWifiStatus(int status) {
		Log.d(TAG, "set WIFI state : " + status);
		if (callback != null) {
			callback.onWifiStatusChange(status);
		}
	}
	
	// ��Ҫ�������ط�����
	// TODO �ؼ������ڵ�WifiP2p���ò鿴�Ƿ��������ã�ֻ�ܹ��ȴ��������Թ���Ľ��
	public static int getWifiP2pState() {
//		Context context = MShareApp.getAppContext();
//		WifiP2pManager wpm = (WifiP2pManager)context.getSystemService(Service.WIFI_P2P_SERVICE);
//		wpm.initialize(srcContext, srcLooper, listener)
		return STATE_WIFI_P2P_DISABLE;
	}
	
	public void setWifiP2pState(int status) {
		Log.d(TAG, "set WIFI_P2P state : " + status);
		if (callback != null) {
			callback.onWifiP2pStatusChange(status);
		}
	}
	
	/**
	 * TODO ��Ҫ����
	 * @return
	 */
	public static int getExternalStorageState() {
		String state = Environment.getExternalStorageState();
		// ��������չ�洢ê�����ӣ��ɶ�д��ʱ�������Ч
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return STATE_EXTERNAL_STORAGE_ENABLE;
		} else {
			return STATE_EXTERNAL_STORAGE_DISABLE;
		}
	}
	
	public void setExternalStorageState(int state) {
		Log.d(TAG, "set ExternalStorage state : " + state);
		if (callback != null) {
			callback.onExternalStorageChange(state);
		}
	}
	
	/**
	 * ���ܲ��Ǻܺã���Ҫ����
	 * @return
	 */
	public static int getWifiApState() {
		Context context = MShareApp.getAppContext();
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		Method isWifiApEnabledMethod = null;
		try {
			// �÷���������û��AP�����������Ҳ�ܹ�����
			// ������Ҫ����setWifiApEnable
			isWifiApEnabledMethod = wm.getClass().getDeclaredMethod("isWifiApEnabled");
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			return STATE_WIFI_AP_UNSUPPORT;
		}
		
		if (isWifiApEnabledMethod != null) {
			int result = STATE_WIFI_AP_DISABLE;
			try {
				if ((Boolean)isWifiApEnabledMethod.invoke(wm)) {
					result = STATE_WIFI_AP_ENABLE;
				} else {
					result = STATE_WIFI_AP_DISABLE;
				}
				Log.d(TAG, "log when get state : " + result);
				// �����˴���Ļ�����֪������ô���ˣ�
			} catch (IllegalAccessException e) {
				Log.e(TAG, "test ap error");
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "test ap error");
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				Log.e(TAG, "test ap error");
				e.printStackTrace();
			}
			return result;
		} else {
			return STATE_WIFI_AP_DISABLE;
		}
	}
	
	public void setWifiApState(int state) {
		Log.d(TAG, "set WIFI_AP state : " + state);
		if (callback != null) {
			callback.onWifiApStatusChange(state);
		}
	}
	
	/**
	 * �����ж�NFC�Ƿ�����USING
	 * @return
	 */
	public static int getNfcState() {
		Context context = MShareApp.getAppContext();
		// ���ڻ���û�а취����Ƿ���NFC֧��
		// �����Log : this device does not have NFC support
		if (NfcAdapter.getDefaultAdapter(context) != null) {
			return STATE_NFC_ENABLE;
		} else {
			return STATE_NFC_DISABLE;
		}
	}
	
	public void setNfcState(int state) {
		Log.d(TAG, "set NFC state : " + state);
		if (callback != null) {
			callback.onNfcStatusChange(state);
		}
	}
	
	/**
	 * ����WifiManager�е�ͬ������
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 */
//	public static boolean isWifiApEnabled() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
//		return false;
//	}
	
}
