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
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ToggleButton;
import android.widget.TextView;

import org.mshare.main.WifiApStateReceiver.OnWifiApStateChangeListener;
import org.mshare.main.WifiStateRecevier.OnWifiStateChangeListener;
import org.mshare.main.ServerStateRecevier.OnServerStateChangeListener;

public class NewConn extends Activity {
	
	private static final String TAG = NewConn.class.getSimpleName();
	
	// ���к������йصĿռ�
	private Switch ftpSwitch;
	private TextView ftpUsername;
	private TextView ftpPassword;
	private TextView ftpPort;
	
	private TextView ftpaddr;
	private TextView connhint;
	private TextView ftpApState;
	private ToggleButton ftpApTest;
	private TextView ftpApIp;
	
	// ���ڵȴ���ɵĵȴ�������
	private LinearLayout progressWait;
	
	// ����״̬��UI������п���
	private WifiStateRecevier wifiStateReceiver;
	private ServerStateRecevier serverStateReceiver;
	private WifiApStateReceiver wifiApStateReceiver;
	
	// �ܹ���6��״̬
	private static final int SERVER_STATE_STARTING = 0x1;
	private static final int SERVER_STATE_STARTED = 0x2;
	private static final int SERVER_STATE_STOPING = 0x4;
	private static final int SERVER_STATE_STOPPED = 0x8;
	private static final int WIFI_STATE_CONNECTED = 0x10;
	private static final int WIFI_STATE_DISCONNECTED = 0x20;
	
	private static final int SERVER_STATE_MASK = 0xf;
	private static final int WIFI_STATE_MASK = 0x30;
	
	// û���κ�״̬
	private int state = 0;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newconn);
		
		// ���õȴ������� 
		progressWait = (LinearLayout)findViewById(R.id.progress_wait);  
		TextView msg = (TextView)findViewById(R.id.progress_description);  
		msg.setText("������������");  
		
		// ����������
		ftpSwitch = (Switch) findViewById(R.id.ftp_switch);
		ftpaddr = (TextView) findViewById(R.id.ftpaddr);
		connhint = (TextView) findViewById(R.id.connhint);
		ftpApState = (TextView)findViewById(R.id.ftp_wifi_ap_state);
		// ��������AP
		ftpApTest = (ToggleButton)findViewById(R.id.ftp_ap_test);
		ftpApIp = (TextView)findViewById(R.id.ftp_ap_ip);
		
		// ������������ʾ
		ftpUsername = (TextView)findViewById(R.id.ftp_username);
		ftpPassword = (TextView)findViewById(R.id.ftp_password);
		ftpPort = (TextView)findViewById(R.id.ftp_port);
		
		// ����Ĭ�ϵĲ���
		ftpUsername.setText(FsSettings.getUsername());
		ftpPassword.setText(FsSettings.getPassword());
		ftpPort.setText(String.valueOf(FsSettings.getPort()));
		
		Log.v(TAG, ((Context)this).toString());
		
		ftpSwitch.setOnClickListener(new StartStopServerListener());
		ftpApTest.setOnClickListener(new WifiApControlListener());
	}
	
	@Override
	protected void onStart() {
		// TODO ������Ҫʹ�ø��Ӱ�ȫ��BroadcastReceiverע�᷽ʽ
		super.onStart();
		
		// �����õ�ǰ��״̬
		// TODO �����ü�����
		changeState(SERVER_STATE_STOPPED);
		if (MShareUtil.isConnectedUsingWifi()) {
			changeState(WIFI_STATE_CONNECTED);
			ftpaddr.setText(FsService.getLocalInetAddress().getHostAddress());
		} else {
			changeState(WIFI_STATE_DISCONNECTED);
			if (FsService.isRunning()) {
				stopServer();
			}
		}
		
		// �򵥵�BroadcastReceiver�����ܴ��ڰ�ȫ����
		wifiStateReceiver = new WifiStateRecevier();
		WifiConectionChangeListener wccListener = new WifiConectionChangeListener();
		// ���ü�����
		wifiStateReceiver.setListener(wccListener);
		
		// ����IntentFilter
		IntentFilter wifiConnectFilter = new IntentFilter();
		wifiConnectFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		wifiConnectFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		
		registerReceiver(wifiStateReceiver, wifiConnectFilter);
		
		/*
		 * WifiAp״̬������
		 */
		wifiApStateReceiver = new WifiApStateReceiver();
		WifiApStateChangeListener wascListener = new WifiApStateChangeListener();
		wifiApStateReceiver.setOnWifiApStateChangeListener(wascListener);
		
		IntentFilter wifiApStateFilter = new IntentFilter();
		wifiApStateFilter.addAction(FsService.WIFI_AP_STATE_CHANGED_ACTION);
		
		registerReceiver(wifiApStateReceiver, wifiApStateFilter);
		
		/*
		 * ������״̬������
		 */
		serverStateReceiver = new ServerStateRecevier();
		ServerStateChangeListener sscListener = new ServerStateChangeListener();
		serverStateReceiver.setListener(sscListener);
		
		IntentFilter serverStateFilter = new IntentFilter();
		serverStateFilter.addAction(FsService.ACTION_STARTED);
		serverStateFilter.addAction(FsService.ACTION_FAILEDTOSTART);
		serverStateFilter.addAction(FsService.ACTION_STOPPED);
		
		registerReceiver(serverStateReceiver, serverStateFilter);
	}
	
	@Override
	protected void onStop() {
		// TODO ���ע��
		super.onStop();
		if (wifiStateReceiver != null) {
			unregisterReceiver(wifiStateReceiver);
		}
		
		if (wifiApStateReceiver != null) {
			unregisterReceiver(wifiApStateReceiver);
		}
		
		if (serverStateReceiver != null) {
			unregisterReceiver(serverStateReceiver);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO ��Ӷ�ά������
		
		MenuInflater infalter = getMenuInflater();
		infalter.inflate(R.menu.ftp_new_conn, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO ����һ���µ�Activity ServerSettingActivity
		
		switch (item.getItemId()) {
			case R.id.menu_set_ftp_server_qrcode:
				Log.v(TAG, "qrcode");
				Intent startQRCode = new Intent();
				startQRCode.setClass(this, QRCodeLogin.class);
				startActivity(startQRCode);
				break;
			case R.id.menu_set_ftp_server_setting:
				Log.v(TAG, "setting");
				Intent startSetting = new Intent();
				startSetting.setClass(this, ServerSettingActivity.class);
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
		
		if (networkState == WIFI_STATE_CONNECTED) {
			switch (serverState) {
				case (SERVER_STATE_STARTING):
					setSwitchChecked(true);
					setSwitchEnable(false);
					setProgressShow(true);
					break;
				case (SERVER_STATE_STARTED):
					setSwitchChecked(true);
					setSwitchEnable(true);
					setProgressShow(false);
					break;
				case (SERVER_STATE_STOPING):
					setSwitchChecked(false);
					setSwitchEnable(false);
					setProgressShow(true);
					break;
				case (SERVER_STATE_STOPPED):
					setSwitchChecked(false);
					setSwitchEnable(true);
					setProgressShow(false);
					break;
				default:
					break;
			}
		} else if (networkState == WIFI_STATE_DISCONNECTED) {
			switch(serverState) {
			case (SERVER_STATE_STARTING):
			case (SERVER_STATE_STARTED):
			case (SERVER_STATE_STOPING):
			case (SERVER_STATE_STOPPED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
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
		setHintText("���ڳ�������������");
	}
	
	/**
	 * ����ֹͣ������
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		setProgressShow(true);
		setHintText("���ڳ���ֹͣ������");
	}
	
	/**
	 * ��������WifiAp
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
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void setHintText(String hint) {
		connhint.setText(hint);
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
	
	private void setProgressShow(boolean show) {
		if (show) {
			progressWait.setVisibility(View.VISIBLE);
		} else {
			progressWait.setVisibility(View.GONE);
		}
	}
	
	/**
	 * ���������ؼ�����
	 * @author HM
	 *
	 */
	private class StartStopServerListener implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (ftpSwitch.isChecked()) {
				startServer();
			} else {
				stopServer();
			}
		}
	}

	private class WifiApControlListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (ftpApTest.isChecked()) {
				setWifiApEnabled(true);
			} else {
				setWifiApEnabled(false);
			}
		}
		
	}
	
	/**
	 * wifi״̬�仯������
	 * @author HM
	 *
	 */
	private class WifiConectionChangeListener implements OnWifiStateChangeListener {

		@Override
		public void onWifiConnectChange(boolean connected) {
			// TODO Auto-generated method stub
			// ֻ����Wifi������²���������������
			if (connected) {
				changeState(WIFI_STATE_CONNECTED);
				setHintText("��ǰWIFI������");
				ftpaddr.setText(FsService.getLocalInetAddress().getHostAddress());
			} else {
				changeState(WIFI_STATE_DISCONNECTED);
				setHintText("��ǰWIFIδ����");
				if (FsService.isRunning()) {
					// ���Թرշ�����
					stopServer();
				}
			}
		}
		
	}
	
	private class ServerStateChangeListener implements OnServerStateChangeListener {

		@Override
		public void onServerStateChange(boolean start) {
			// TODO Auto-generated method stub
			// ������������ʱ
			// ���ܵ��˷��������Խ�Progress����Ϊ���ɼ�
			if (start) {
				changeState(SERVER_STATE_STARTED);
				setHintText("�������Ѿ�����");
			} else {
				changeState(SERVER_STATE_STOPPED);
				setHintText("�������Ѿ�ֹͣ");
			}
		}
	}
	
	private class WifiApStateChangeListener implements OnWifiApStateChangeListener {

		@Override
		public void onWifiApStateChange(boolean enable) {
			Context context = MShareApp.getAppContext();
			if (enable) {
				ftpApState.setText("��ǰAp����");
				byte[] address = FsService.getLocalInetAddress().getAddress();
				String addressStr = "";
				for (int i = 0, len = address.length; i < len; i++) {
					byte b = address[i];
					addressStr += String.valueOf(((int)b + 256)) + " ";
				}
				ftpApIp.setText(addressStr);
				ftpApIp.setVisibility(View.VISIBLE);
			} else {
				ftpApState.setText("��ǰAp������");
				ftpApIp.setText("");
				ftpApIp.setVisibility(View.VISIBLE);
			}
			
		}
	}
}
