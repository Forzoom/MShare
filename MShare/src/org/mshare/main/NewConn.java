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
import android.net.NetworkInfo;
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
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.TextView;

import org.mshare.main.NetworkStateRecevier.OnWifiApStateChangeListener;
import org.mshare.main.NetworkStateRecevier.OnNetworkStateChangeListener;
import org.mshare.main.ServerStateRecevier.OnServerStateChangeListener;

public class NewConn extends Activity {
	
	private static final String TAG = NewConn.class.getSimpleName();
	
	// ���к������йصĿռ�
	private ToggleButton ftpSwitch;
	private TextView ftpUsernameView;
	private TextView ftpPasswordView;
	private TextView ftpPortView;
	
	private TextView ftpAddrView;
	// ������״̬
	private TextView serverStateView;
	// ����״̬:WIFI/MOBILE
	private TextView networkStateView;
	private TextView ftpApState;
	private ToggleButton ftpApTest;
	private TextView ftpApIp;
	
	// ��������ǰ�ж����û�����
	private TextView ftpCurrentSessionsView;
	// ��ʾ��������������û�����
	private TextView ftpMaxSessionsView;
	
	// ���ڵȴ���ɵĵȴ�������
	private LinearLayout progressWait;
	
	// ����״̬��UI������п���
	private NetworkStateRecevier networkStateReceiver;
	private ServerStateRecevier serverStateReceiver;
	
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
//		progressWait = (LinearLayout)findViewById(R.id.progress_wait);  
//		TextView msg = (TextView)findViewById(R.id.progress_description);  
//		msg.setText("������������");
		// TODO ��ʱ��������Ϊ���ɼ�
//		progressWait.setVisibility(View.GONE);
		
		// ����������
		ftpSwitch = (ToggleButton) findViewById(R.id.ftp_switch);
		
		// ��ʾ��ǰ��ַ������
		ftpAddrView = (TextView) findViewById(R.id.ftp_addr);
		ftpApState = (TextView)findViewById(R.id.ftp_wifi_ap_state);
		
		serverStateView = (TextView)findViewById(R.id.server_state);
		// ��ʾ��ǰ�������������
		networkStateView = (TextView)findViewById(R.id.network_state);
		
		// ��������AP
		ftpApTest = (ToggleButton)findViewById(R.id.ftp_ap_test);
		ftpApIp = (TextView)findViewById(R.id.ftp_ap_ip);
		
		// ������������ʾ
		ftpUsernameView = (TextView)findViewById(R.id.ftp_username);
		ftpPasswordView = (TextView)findViewById(R.id.ftp_password);
		ftpPortView = (TextView)findViewById(R.id.ftp_port);
		
		// ����Ĭ�ϵĲ���
		ftpUsernameView.setText(FsSettings.getUsername());
		ftpPasswordView.setText(FsSettings.getPassword());
		ftpPortView.setText(String.valueOf(FsSettings.getPort()));
		
		Log.v(TAG, ((Context)this).toString());
		
		ftpSwitch.setOnClickListener(new StartStopServerListener());
		ftpApTest.setOnClickListener(new WifiApControlListener());
	}
	
	@Override
	protected void onStart() {
		// TODO ������Ҫʹ�ø��Ӱ�ȫ��BroadcastReceiverע�᷽ʽ
		super.onStart();
		
		// TODO ��ʱ���õļ�⵱ǰ�Ƿ���3G�źţ������û�����֪��ethernet�Ƿ���3G������
//		if (MShareUtil.isConnectedUsing(ConnectivityManager.TYPE_ETHERNET)) {
//			Toast.makeText(this, "��ǰ����ʹ���ƶ������ź����磬���ܲ�������", Toast.LENGTH_LONG).show();
//		}
		
		// ��ʾ��ǰ��ʹ�õ�����
		// TODO ��NetworkStateReceiver��Ҳ�����ƵĴ��룬�Ƿ���Ժϲ�?
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Service.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null) {
			String networkTypeName = ni.getTypeName();
			networkStateView.setText(networkTypeName);
		} else {
			String networkTypeName = "NONE";
			networkStateView.setText("NONE");
		}
		
		// ��ʾ��ǰ��AP����״̬
		// TODO ��Ҫ���������̫���ˣ����ܿ��ǲ����뿪��AP�Ĺ���
		try {
			boolean wifiApEnabled = isWifiApEnabled();
			ftpApTest.setChecked(wifiApEnabled);
		} catch (IllegalAccessException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP�޷�����");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP�޷�����");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP�޷�����");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP�޷�����");
			e.printStackTrace();
		}
		
		// �����õ�ǰ��״̬
		changeState(SERVER_STATE_STOPPED);
		if (MShareUtil.isConnectedUsing(MShareUtil.WIFI)) {
			changeState(WIFI_STATE_CONNECTED);
			ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
		} else {
			changeState(WIFI_STATE_DISCONNECTED);
			if (FsService.isRunning()) {
				stopServer();
			}
		}

		// ע��򵥵�BroadcastReceiver���������豸������״���仯�����ܴ��ڰ�ȫ����
		networkStateReceiver = new NetworkStateRecevier();
		NetworkStateChangeListener wccListener = new NetworkStateChangeListener();
		
		// ��������仯��WifiAp�仯������
		networkStateReceiver.setOnNetworkStateChangeListener(wccListener);
		WifiApStateChangeListener wacListener = new WifiApStateChangeListener();
		networkStateReceiver.setOnWifiApStateChangeListener(wacListener);
		
		// ����IntentFilter
		IntentFilter wifiConnectFilter = new IntentFilter();
		wifiConnectFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		wifiConnectFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		// ����WifiAp��״̬
		wifiConnectFilter.addAction(FsService.WIFI_AP_STATE_CHANGED_ACTION);
		
		registerReceiver(networkStateReceiver, wifiConnectFilter);
		
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
		super.onStop();
		if (networkStateReceiver != null) {
			unregisterReceiver(networkStateReceiver);
		}
		
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
		// TODO ����һ���µ�Activity ServerSettingActivity
		
		switch (item.getItemId()) {
			case R.id.menu_set_ftp_server_qrcode:
				Log.v(TAG, "qrcode");
				Intent startQRCode = new Intent();
				startQRCode.setClass(this, QRCodeLogin.class);
				// д���ά����Ҫ��ʾ������
				// ��Ҫ��ɨ�����Ӧ
				// ��Ҫ����:1.ip 2.port 3.username 4.password
				// ��Ҫ��δ�����Щ������?ʹ���ַ�����,��username��password�в������пո�
				String address = "192.168.137.1";
				String port = "2121";
				String username = "username";
				String password = "password";
				
				// ʹ�ÿո�ָ�
				String content = address + " " + port + " " + username + " " + password;
				
				startQRCode.putExtra(QRCodeLogin.EXTRA_CONTENT, content);
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
	 * ����WifiManager�е�ͬ������
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 */
	public static boolean isWifiApEnabled() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		// TODO ����java������Ʋ�ȫ���е�����
		Context context = MShareApp.getAppContext();
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		Method isWifiApEnabledMethod = wm.getClass().getDeclaredMethod("isWifiApEnabled");
		isWifiApEnabledMethod.invoke(wm);
		return false;
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
					setProgressShow(true);
					
					serverStateView.setText("������������");
					
					break;
				case (SERVER_STATE_STARTED):
					setSwitchChecked(true);
					setSwitchEnable(true);
					setProgressShow(false);
					
					serverStateView.setText("������������");
					
					break;
				case (SERVER_STATE_STOPING):
					setSwitchChecked(false);
					setSwitchEnable(false);
					setProgressShow(true);
					
					serverStateView.setText("�������ر���");
					
					break;
				case (SERVER_STATE_STOPPED):
					setSwitchChecked(false);
					setSwitchEnable(true);
					setProgressShow(false);
					
					serverStateView.setText("�������ѹر�");
					
					break;
				default:
					break;
			}
		} else if (networkState == WIFI_STATE_DISCONNECTED) {
			switch(serverState) {
			case (SERVER_STATE_STARTING):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
				
				serverStateView.setText("������������");
				
				break;
			case (SERVER_STATE_STARTED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
				
				serverStateView.setText("������������");
				
				break;
			case (SERVER_STATE_STOPING):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
				
				serverStateView.setText("�������ر���");
				
				break;
			case (SERVER_STATE_STOPPED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
				
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
		setProgressShow(true);
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
			
			// ������ķ���������ɺ�û�з�������
			// ��״̬����Ϊ������/�ر���
			if (enable) {
				ftpApState.setText("AP������");
			} else {
				ftpApState.setText("AP�ر���");
			}
			
		} catch (NoSuchMethodException e) {
			ftpApState.setText("AP�޷�����");
			ftpApTest.setEnabled(false);
			ftpApTest.setChecked(false);
			Toast.makeText(this, "AP�޷�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO ��Ӷ�Ӧ����Ӧ
			ftpApState.setText("AP�޷�����");
			ftpApTest.setEnabled(false);
			ftpApTest.setChecked(false);
			Toast.makeText(this, "AP�޷�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			ftpApState.setText("AP�޷�����");
			ftpApTest.setEnabled(false);
			ftpApTest.setChecked(false);
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
	
	private void setProgressShow(boolean show) {
		// TODO ��ʱ����Ϊ����ʹ��
//		if (show) {
//			progressWait.setVisibility(View.VISIBLE);
//		} else {
//			progressWait.setVisibility(View.GONE);
//		}
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
			if (ftpApTest.isChecked()) {
				// TODO ���Զ�WIFIAP���в��������ǿ������������ô���̫����ɢ������ʹ��״̬�������
				setWifiApEnabled(true);
				ftpApTest.setEnabled(false);
			} else {
				setWifiApEnabled(false);
				ftpApTest.setEnabled(false);
			}
		}
		
	}
	
	/**
	 * ����״̬�仯������
	 * @author HM
	 *
	 */
	private class NetworkStateChangeListener implements OnNetworkStateChangeListener {

		@Override
		public void onNetworkStateChange(String typeName, int type) {
			Log.v(TAG, "���ܵ�����״̬�仯�㲥");
			// ֻ����Wifi������²���������������
			
			networkStateView.setText(typeName);
			
			if (type == ConnectivityManager.TYPE_WIFI) {
				changeState(WIFI_STATE_CONNECTED);
				ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
			} else {
				changeState(WIFI_STATE_DISCONNECTED);
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
			} else {
				changeState(SERVER_STATE_STOPPED);
			}
		}
	}

	/**
	 * ����WifiAp״̬
	 * @author HM
	 *
	 */
	private class WifiApStateChangeListener implements OnWifiApStateChangeListener {

		@Override
		public void onWifiApStateChange(boolean enable) {
			Context context = MShareApp.getAppContext();
			
			// ��������Ӧ����ToggleButton����Ϊ����
			ftpApTest.setEnabled(true);
			
			// ��WifiAp��״̬�仯������Ӧ
			if (enable) {
				
				ftpApTest.setChecked(true);
				ftpApState.setText("AP������");
				
				// TODO ��ַ���ܲ������������õģ�������ʱ����Щ����
				// ���õ�ַ
//				byte[] address = FsService.getLocalInetAddress().getAddress();
//				String addressStr = "";
//				for (int i = 0, len = address.length; i < len; i++) {
//					byte b = address[i];
//					addressStr += String.valueOf(((int)b + 256)) + " ";
//				}
//				ftpApIp.setText(addressStr);
//				ftpApIp.setVisibility(View.VISIBLE);
			} else {
				
				ftpApTest.setChecked(false);
				ftpApState.setText("AP�ѹر�");
				
				// TODO ��ʱ������IP
				
//				ftpApIp.setText("");
//				ftpApIp.setVisibility(View.VISIBLE);
			}
		}
	}
}
