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
 * TODO 是否需要将getState和isEnable来使用
 * TODO 当wifip2p完成之后再完善该类，当前如果是WIFIp2p等情况下，不知道wifi能不能从using变成enable
 * TODO 将stateBar使用include引入到xml文件中
 * 将StateController作为NewConn的内部类,可是有静态函数
 * 将receiver移动到这里
 * receiver中不仅仅是state的问题
 * @author HM
 *
 */
public class StatusController {
	private static final String TAG = StatusController.class.getSimpleName();
	
	// 服务器状态
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
	
	// TODO 使用反射来获得WifiAp状态改变的广播
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;
	
	private StatusCallback callback;
	
	// 监听状态对UI界面进行控制
	private NetworkStatusRecevier networkStateReceiver;
	private ExternalStorageStatusReceiver externalStorageStateReceiver;
	private ServerStatusRecevier serverStatusReceiver;
	
	/**
	 * 状态变化的回调接口
	 * @author HM
	 *
	 */
	public interface StatusCallback {
		public void onServerStatusChange(int status);
		public void onWifiStatusChange(int status);
		public void onWifiApStatusChange(int status);
		public void onWifiP2pStatusChange(int status);
		public void onExternalStorageChange(int status);
		// NFC的状态不太了解
		public void onNfcStatusChange(int state);
	}
	
	/**
	 *  初始化所有的状态内容
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
	 * 需要在Activity中的onStart中调用
	 */
	public void registerReceiver() {
		Context context = MShareApp.getAppContext();
		
		/* 注册监听器 */
		
		// 注册简单的BroadcastReceiver用来监听设备的网络状况变化，可能存在安全风险
		networkStateReceiver = new NetworkStatusRecevier(this);
		
		// 设置IntentFilter
		IntentFilter wifiConnectFilter = new IntentFilter();
		wifiConnectFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		wifiConnectFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		// 监听WifiAp的状态
		// 这些Action应该使用反射来获得
		// TODO 学习使用反射
		wifiConnectFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
		
		context.registerReceiver(networkStateReceiver, wifiConnectFilter);
		
		/*
		 * 扩展存储监听器
		 */
		externalStorageStateReceiver = new ExternalStorageStatusReceiver(this);
		IntentFilter externalStorageFilter = new IntentFilter();
		// TODO 不知道需要添加的Action是什么
		// externalStorageFilter.addAction(action)
		context.registerReceiver(externalStorageStateReceiver, externalStorageFilter);
		
		/*
		 * 服务器状态监听器
		 */
		serverStatusReceiver = new ServerStatusRecevier(this);
		
		IntentFilter serverStatusFilter = new IntentFilter();
		serverStatusFilter.addAction(ServerService.ACTION_STARTED);
		serverStatusFilter.addAction(ServerService.ACTION_FAILEDTOSTART);
		serverStatusFilter.addAction(ServerService.ACTION_STOPPED);
		
		context.registerReceiver(serverStatusReceiver, serverStatusFilter);
	}
	
	/**
	 * 在onStop中调用
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
	
	// 获得服务器状态
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
	
	// 设置服务器状态
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
		// 检测wifi是否开启
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
	
	// 需要在其他地方设置
	// TODO 关键是现在的WifiP2p不好查看是否正在启用，只能够等待监听尝试过后的结果
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
	 * TODO 需要修正
	 * @return
	 */
	public static int getExternalStorageState() {
		String state = Environment.getExternalStorageState();
		// 仅仅当扩展存储锚点连接，可读写的时候才算有效
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
	 * 可能不是很好，需要测试
	 * @return
	 */
	public static int getWifiApState() {
		Context context = MShareApp.getAppContext();
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		Method isWifiApEnabledMethod = null;
		try {
			// 该方法可能在没有AP启动的情况下也能够调用
			// 所以需要测试setWifiApEnable
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
				// 出现了错误的话，不知道该怎么办了？
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
	 * 不能判断NFC是否是在USING
	 * @return
	 */
	public static int getNfcState() {
		Context context = MShareApp.getAppContext();
		// 现在基本没有办法检测是否有NFC支持
		// 这里会Log : this device does not have NFC support
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
	 * 调用WifiManager中的同名方法
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
