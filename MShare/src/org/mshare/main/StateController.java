package org.mshare.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.ServerStateRecevier.OnServerStateChangeListener;

import android.app.Service;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * ��Ҫ�����м��������������
 * TODO �Ƿ���Ҫ��getState��isEnable��ʹ��
 * ��StateController��ΪNewConn���ڲ���,�����о�̬����
 * ��receiver�ƶ�������
 * receiver�в�������state������
 * @author HM
 *
 */
public class StateController {

	private static final String TAG = StateController.class.getSimpleName();
	
	// ���е���ɫ
	private int colorDisable = -1;
	private int colorEnable = -1;
	private int colorUsing = -1;
	
	// ����״̬:��ǰ����ʹ��ʲô����������ݴ���
	private TextView wifiStateView;
	private TextView apStateView;
	private TextView p2pStateView;
	// ��ʾNFC��״̬
	private TextView nfcStateView;
	// ��չ�洢״̬ TODO ��Ҫָ���Ƿ����
	private TextView sdStateView;
	
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
	
	private StateCallback callback;
	
	// ����״̬��UI������п���
	private NetworkStateRecevier networkStateReceiver;
	private ServerStateRecevier serverStateReceiver;
	private ExternalStorageStateReceiver externalStorageStateReceiver;
	
	public interface StateCallback {
		public void onWifiStateChange(int state);
		public void onWifiApStateChange(int state);
		public void onWifiP2pStateChange(int state);
		public void onExternalStorageChange(int state);
		// NFC��״̬��̫�˽�
		public void onNfcStateChange(int state);
	}
	
	// ��ʼ�����е�״̬����
	public void initial(ViewGroup container) {
		// ��ɫ
		Resources resources = container.getResources();
		colorDisable = resources.getColor(R.color.state_disable);
		colorEnable = resources.getColor(R.color.state_enable);
		colorUsing = resources.getColor(R.color.state_using);
		
		// д�����ǲ��ǲ���
		wifiStateView = (TextView)container.findViewById(R.id.wifi_state);
		// ��ǰ���ݴ�������
		wifiStateView = (TextView)container.findViewById(R.id.wifi_state);
		apStateView = (TextView)container.findViewById(R.id.ap_state);
		p2pStateView = (TextView)container.findViewById(R.id.wifip2p_state);
		// NFC״̬
		nfcStateView = (TextView)container.findViewById(R.id.nfc_state);
		// ��չ�洢״̬
		sdStateView = (TextView)container.findViewById(R.id.sd_state);
		
		// ������ɫĬ��Ϊdisable
		setWifiState(STATE_WIFI_DISABLE);
		setWifiApState(getWifiApState());
		setWifiP2pState(STATE_WIFI_P2P_DISABLE);
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
		networkStateReceiver = new NetworkStateRecevier();
		
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
		 * ������״̬������
		 */
		serverStateReceiver = new ServerStateRecevier();
		
		IntentFilter serverStateFilter = new IntentFilter();
		serverStateFilter.addAction(FsService.ACTION_STARTED);
		serverStateFilter.addAction(FsService.ACTION_FAILEDTOSTART);
		serverStateFilter.addAction(FsService.ACTION_STOPPED);
		
		context.registerReceiver(serverStateReceiver, serverStateFilter);
		
		/*
		 * ��չ�洢������
		 */
		externalStorageStateReceiver = new ExternalStorageStateReceiver(this);
		IntentFilter externalStorageFilter = new IntentFilter();
		// TODO ��֪����Ҫ���ӵ�Action��ʲô
		// externalStorageFilter.addAction(action)
		context.registerReceiver(externalStorageStateReceiver, externalStorageFilter);
	}
	
	/**
	 * ��onStop�е���
	 */
	public void unregisterReceiver() {
		Context context = MShareApp.getAppContext();
		if (networkStateReceiver != null) {
			context.unregisterReceiver(networkStateReceiver);
		}
		
		if (serverStateReceiver != null) {
			context.unregisterReceiver(serverStateReceiver);
		}
		
		if (externalStorageStateReceiver != null) {
			context.unregisterReceiver(externalStorageStateReceiver);
		}
	}
	
	public void setCallback(StateCallback callback) {
		this.callback = callback;
	}
	
	public static int getWifiState() {
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
	
	public void setWifiState(int state) {
		Log.d(TAG, "set WIFI state : " + state);
		if (wifiStateView != null) {
			switch (state) {
			case STATE_WIFI_ENABLE:
				wifiStateView.setTextColor(colorEnable);
				break;
			case STATE_WIFI_DISABLE:
				wifiStateView.setTextColor(colorDisable);
				break;
			case STATE_WIFI_USING:
				wifiStateView.setTextColor(colorUsing);
				break;
			}
		}
	}
	
	// ��Ҫ�������ط�����
	// TODO �ؼ������ڵ�WifiP2p���ò鿴�Ƿ��������ã�ֻ�ܹ��ȴ��������Թ���Ľ��
	public static boolean getWifiP2pState() {
//		Context context = MShareApp.getAppContext();
//		WifiP2pManager wpm = (WifiP2pManager)context.getSystemService(Service.WIFI_P2P_SERVICE);
//		wpm.initialize(srcContext, srcLooper, listener)
		return false;
	}
	
	public void setWifiP2pState(int state) {
		Log.d(TAG, "set WIFI_P2P state : " + state);
		switch (state) {
		case STATE_WIFI_P2P_ENABLE:
			p2pStateView.setTextColor(colorEnable);
			break;
		case STATE_WIFI_P2P_DISABLE:
			p2pStateView.setTextColor(colorDisable);
			break;
		case STATE_WIFI_P2P_USING:
			p2pStateView.setTextColor(colorUsing);
			break;
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
		switch (state) {
		case STATE_EXTERNAL_STORAGE_ENABLE:
			sdStateView.setTextColor(colorEnable);
			break;
		case STATE_EXTERNAL_STORAGE_DISABLE:
			sdStateView.setTextColor(colorDisable);
			break;
		case STATE_EXTERNAL_STORAGE_USING:
			sdStateView.setTextColor(colorUsing);
			break;
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
		switch (state) {
		case STATE_WIFI_AP_ENABLE:
			apStateView.setTextColor(colorEnable);
			break;
		case STATE_WIFI_AP_DISABLE:
			apStateView.setTextColor(colorDisable);
			break;
		case STATE_WIFI_AP_USING:
			apStateView.setTextColor(colorUsing);
			break;
		}
		if (callback != null) {
			callback.onWifiApStateChange(state);
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
		switch (state) {
		case STATE_NFC_ENABLE:
			nfcStateView.setTextColor(colorEnable);
			break;
		case STATE_NFC_DISABLE:
			nfcStateView.setTextColor(colorDisable);
			break;
		case STATE_NFC_USING:
			nfcStateView.setTextColor(colorUsing);
			break;
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