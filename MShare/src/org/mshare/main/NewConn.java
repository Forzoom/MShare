package org.mshare.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.TextView;

import org.mshare.main.ServerStateRecevier.OnServerStateChangeListener;
import org.mshare.main.StateController.StateCallback;
import org.mshare.nfc.NfcServerActivity;
import org.mshare.scan.ScanActivity;
import org.mshare.p2p.P2pActivity;

/**
 * TODO ����ѡ��Ҫ��������ݵ�ʱ���ܲ��ܽ����ǵ�Ӧ��Ҳ���뵽����
 * TODO ������NFC������
 * @author HM
 *
 */
public class NewConn extends Activity implements StateCallback {
	
	private static final String TAG = NewConn.class.getSimpleName();
	
	// ���к������йصĿؼ�
	private ToggleButton ftpSwitch;
	private TextView ftpUsernameView;
	private TextView ftpPasswordView;
	private TextView ftpPortView;
	
	private TextView ftpAddrView;
	// ������״̬
	private TextView serverStateView;
	// StateBar
	private RelativeLayout stateBar;
	
	private TextView uploadPathView;
	
	private ToggleButton apTest;
	private TextView ftpApIp;
	
	// �ܹ���6��״̬
	private static final int SERVER_STATE_STARTING = 0x1;
	private static final int SERVER_STATE_STARTED = 0x2;
	private static final int SERVER_STATE_STOPING = 0x4;
	private static final int SERVER_STATE_STOPPED = 0x8;
	private static final int WIFI_STATE_CONNECTED = 0x10;
	private static final int WIFI_STATE_DISCONNECTED = 0x20;
	
	private static final int SERVER_STATE_MASK = 0xf;
	private static final int WIFI_STATE_MASK = 0x30;
	
	private StateController mState;
	
	// û���κ�״̬
	private int state = 0;
	
	private ServerStateRecevier serverStateReceiver;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newconn);
		
		// ����������
		ftpSwitch = (ToggleButton) findViewById(R.id.ftp_switch);
		
		// ��ʾ��ǰIP��ַ������
		ftpAddrView = (TextView) findViewById(R.id.ftp_addr);
		
		// ������״̬��ʾ
		serverStateView = (TextView)findViewById(R.id.server_state);
		
		// StateBar
		stateBar = (RelativeLayout)findViewById(R.id.state_bar);
		
		// �ϴ�·��
		uploadPathView = (TextView)findViewById(R.id.upload_path);
		
		// ��������AP
		apTest = (ToggleButton)findViewById(R.id.ftp_ap_test);
		// TODO ��֪������ô��Ӧ��IP��ַ
		ftpApIp = (TextView)findViewById(R.id.ftp_ap_ip);
		
		// ������������ʾ
		ftpUsernameView = (TextView)findViewById(R.id.ftp_username);
		ftpPasswordView = (TextView)findViewById(R.id.ftp_password);
		ftpPortView = (TextView)findViewById(R.id.ftp_port);
		
		// ����Ĭ�ϵĲ���
		ftpUsernameView.setText(FsSettings.getUsername());
		ftpPasswordView.setText(FsSettings.getPassword());
		ftpPortView.setText(String.valueOf(FsSettings.getPort()));
		
		// ���������͹رյļ�����
		ftpSwitch.setOnClickListener(new StartStopServerListener());
		apTest.setOnClickListener(new WifiApControlListener());
		
	}
	
	@Override
	protected void onStart() {
		// TODO ������Ҫʹ�ø��Ӱ�ȫ��BroadcastReceiverע�᷽ʽ
		super.onStart();
		
		// ���úͳ�ʼ��״̬����
		mState = new StateController();
		mState.setCallback(this);
		mState.initial((ViewGroup)findViewById(R.id.state_bar));
		
		// TODO ��ʱ���õļ�⵱ǰ�Ƿ���MOBILE�źţ�
		// TODO ��Ҫ���������̫���ˣ����ǲ����뿪��AP�Ĺ���
		// ��û��AP cannot enable
		// TODO �������APʧ����֮�󣬾ͽ���д�������ļ���������ǰ�豸���ܲ���֧�ֿ���AP

		// �����õ�ǰ�ķ�����״̬
		// TODO �����ǰ�������Ѿ������ˣ����������������������������ҲӦ��������Ӧ������NewConn�ͷ�����֮���״̬Ӧ��ͳһ
		if (FsService.isRunning()) {
			changeState(SERVER_STATE_STARTED);
		} else {
			changeState(SERVER_STATE_STOPPED);
		}

		// ��ǰ�ϴ�·��
		uploadPathView.setText(FsSettings.getUpload());

		mState.registerReceiver();
		/*
		 * ������״̬������
		 */
		serverStateReceiver = new ServerStateRecevier();
		ServerStateChangeListener ssclistener = new ServerStateChangeListener();
		serverStateReceiver.setListener(ssclistener);
		
		IntentFilter serverStateFilter = new IntentFilter();
		serverStateFilter.addAction(FsService.ACTION_STARTED);
		serverStateFilter.addAction(FsService.ACTION_FAILEDTOSTART);
		serverStateFilter.addAction(FsService.ACTION_STOPPED);
		
		registerReceiver(serverStateReceiver, serverStateFilter);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mState.unregisterReceiver();
		if (serverStateReceiver != null) {
			unregisterReceiver(serverStateReceiver);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater infalter = getMenuInflater();
		infalter.inflate(R.menu.ftp_new_conn, menu);	
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO ��Ҫʹ��ͼ��������������
		
		switch (item.getItemId()) {
			case R.id.menu_nfc_connect:
				Log.v(TAG, "option menu nfc");
				Intent startNfc = new Intent(this, NfcServerActivity.class);
				startActivity(startNfc);
				break;
			case R.id.menu_set_ftp_p2p:
				Log.v(TAG, "P2P");
				Intent startP2p = new Intent();
				startP2p.setClass(this, P2pActivity.class);
				startActivity(startP2p);
				break;
			case R.id.menu_set_ftp_server_qrcode:
				Log.v(TAG, "option menu qrcode");
				Intent startQRCode = new Intent(this, QRCodeConnectActivity.class);
				
				// �������ڷ�����ʱ������������ά��ɨ�裬�����ô�죿
				// ������ʱʹ�õ���Ĭ�ϵ�IP��ַ
//				FsService.getLocalInetAddress().
				String host = "192.168.137.1";
				String port = "2121";
				String username = "username";
				String password = "password";
				ConnectInfo connectInfo = new ConnectInfo(host, port, username, password);
				
				startQRCode.putExtra(QRCodeConnectActivity.EXTRA_CONTENT, connectInfo.toString());
				startActivity(startQRCode);
				break;
			case R.id.menu_set_ftp_server_setting:
				Log.v(TAG, "option menu setting");
				Intent startSetting = new Intent(this, ServerSettingActivity.class);
				startActivity(startSetting);
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * �����е�״̬�����ı�
	 * ������״̬:1.����2.������3.ֹͣ4.ֹͣ��
	 * wifi״̬1.���ϣ�û����
	 * �ܹ�8��״̬�����ϴ�һ��״̬�������һ��״̬���ܹ���Ҫ�Ĵ���̫�̫࣬��
	 */
	private void changeState(int s) {
		if ((s & SERVER_STATE_MASK) != 0) {// �ı�server״̬
			state = state & (~SERVER_STATE_MASK) | s; 
		} else {
			state = state & (~WIFI_STATE_MASK) | s;
		}
		
		int networkState = state & WIFI_STATE_MASK;
		int serverState = state & SERVER_STATE_MASK;
		
		// TODO ����������˹��ڷ�����״̬������
		
		if (networkState == WIFI_STATE_CONNECTED) {
			switch (serverState) {
				case (SERVER_STATE_STARTING):
					setSwitchChecked(true);
					setSwitchEnable(false);
					
					serverStateView.setText("������������");
					
					break;
				case (SERVER_STATE_STARTED):
					setSwitchChecked(true);
					setSwitchEnable(true);
					
					serverStateView.setText("������������");
					
					break;
				case (SERVER_STATE_STOPING):
					setSwitchChecked(false);
					setSwitchEnable(false);
					
					serverStateView.setText("�������ر���");
					
					break;
				case (SERVER_STATE_STOPPED):
					setSwitchChecked(false);
					setSwitchEnable(true);
					
					serverStateView.setText("�������ѹر�");
					
					break;
				default:
					break;
			}
		} else if (networkState == WIFI_STATE_DISCONNECTED) {
			// ��û�����������״̬�£��ܷ��޸ķ�����״̬
			switch(serverState) {
			case (SERVER_STATE_STARTING):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("������������");
				
				break;
			case (SERVER_STATE_STARTED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("������������");
				
				break;
			case (SERVER_STATE_STOPING):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("�������ر���");
				
				break;
			case (SERVER_STATE_STOPPED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("�������ѹر�");
				
				break;
			}
		}
		
	}
	
	/**
	 * ����������
	 */
	private void startServer() {
		// �����µ���������
		sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
		changeState(SERVER_STATE_STARTING);
	}
	
	/**
	 * ����ֹͣ������
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		changeState(SERVER_STATE_STOPING);
	}
	
	/**
	 * ��������Ap
	 * �����ĺ������������ǲ��ǲ����
	 */
	private void setWifiApEnabled(boolean enable) {
		// TODO ��Ҫ�˽�������java���������
		WifiManager wm = (WifiManager)getSystemService(Service.WIFI_SERVICE);
		
		try {
			// ���ڻ��WifiConfiguration
			Method getWifiApConfigurationMethod = wm.getClass().getDeclaredMethod("getWifiApConfiguration");
			WifiConfiguration config = (WifiConfiguration)getWifiApConfigurationMethod.invoke(wm);
			
			Method setWifiApEnabledMethod = wm.getClass().getDeclaredMethod("setWifiApEnabled");
			setWifiApEnabledMethod.invoke(wm, config, enable);
			
		} catch (NoSuchMethodException e) {
			apTest.setEnabled(false);
			apTest.setChecked(false);
			Toast.makeText(this, "AP�޷�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO ��Ӷ�Ӧ����Ӧ
			apTest.setEnabled(false);
			apTest.setChecked(false);
			Toast.makeText(this, "AP�޷�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			apTest.setEnabled(false);
			apTest.setChecked(false);
			Toast.makeText(this, "AP�޷�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	/**
	 * ���÷����������Ƿ����
	 */
	private void setSwitchEnable(boolean b) {
		ftpSwitch.setEnabled(b);
	}
	/**
	 * ���÷�������������ʾ���ǿ����ǹ�
	 */
	private void setSwitchChecked(boolean b) {
		ftpSwitch.setChecked(b);
	}
	
	/**
	 * ���������ؼ�����
	 * @author HM
	 *
	 */
	private class StartStopServerListener implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			if (ftpSwitch.isChecked()) {
				startServer();
			} else {
				stopServer();
			}
		}
	}

	/**
	 * ��ӦActivity�еĵ���¼������������͹ر�WifiAp
	 * @author HM
	 *
	 */
	private class WifiApControlListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (apTest.isChecked()) {
				// ���Զ�WIFIAP���в���
				setWifiApEnabled(true);
			} else {
				setWifiApEnabled(false);
			}
			apTest.setEnabled(false);
		}
	}
	
	private class ServerStateChangeListener implements OnServerStateChangeListener {

		@Override
		public void onServerStateChange(boolean start) {
			// ������������ʱ
			if (start) {
				changeState(SERVER_STATE_STARTED);
			} else {
				changeState(SERVER_STATE_STOPPED);
			}
		}
	}

	@Override
	public void onWifiStateChange(int state) {
		Log.d(TAG, "on wifi state change");
		switch (state) {
		// ��ʾ�����ֻ���֧��WIFI
		case StateController.STATE_WIFI_DISABLE:
		case StateController.STATE_WIFI_ENABLE:
			changeState(WIFI_STATE_DISCONNECTED);
			if (FsService.isRunning()) {
				// ���Թرշ�����
				stopServer();
			}
			ftpAddrView.setText("δ֪");
			break;
		case StateController.STATE_WIFI_USING:
			changeState(WIFI_STATE_CONNECTED);
			// ������ʾ��IP��ַ
			ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
			break;
		}
	}
	
	/**
	 * ������ܻ��д����ظ�,��Ҫ����������ݳ�ȥ
	 */
	@Override
	public void onWifiApStateChange(int state) {
		Log.d(TAG, "on wifi ap state change");
		switch (state) {
		case StateController.STATE_WIFI_AP_UNSUPPORT:
			apTest.setEnabled(false);
			apTest.setChecked(false);
			break;
			// ���������������ʲô��Ҫ�������?
			// ����Ĳ�����apTest��checkd?
		case StateController.STATE_WIFI_AP_ENABLE:
			apTest.setEnabled(true);
			apTest.setChecked(true);
			break;
		case StateController.STATE_WIFI_AP_DISABLE:
			apTest.setEnabled(true);
			apTest.setChecked(false);
			break;
		case StateController.STATE_WIFI_AP_USING:
			apTest.setEnabled(true);
			apTest.setChecked(true);
			break;
		}
		// TODO ��ַ���ܲ������������õģ�������ʱ����Щ����
		// ���õ�ַ
//		byte[] address = FsService.getLocalInetAddress().getAddress();
//		String addressStr = "";
//		for (int i = 0, len = address.length; i < len; i++) {
//			byte b = address[i];
//			addressStr += String.valueOf(((int)b + 256)) + " ";
//		}
//		ftpApIp.setText(addressStr);
//		ftpApIp.setVisibility(View.VISIBLE);
	}

	@Override
	public void onWifiP2pStateChange(int state) {
		// TODO Auto-generated method stub
		Log.d(TAG, "on wifi p2p state change");
	}

	@Override
	public void onExternalStorageChange(int state) {
		// TODO ������չ�洢�ı仯�ܹ���Ϊ��Ӧ
		Log.d(TAG, "on external storage state change");
	}

	@Override
	public void onNfcStateChange(int state) {
		Log.d(TAG, "on nfc state change");
	}
}
