package org.mshare.main;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View.OnClickListener;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.ToggleButton;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import org.mshare.main.WifiConnectRecevier.OnWifiConnectChangeListener;
import org.mshare.main.ServerStateRecevier.OnServerStateChangeListener;

public class NewConn extends Activity {
	
	private static final String TAG = NewConn.class.getSimpleName();
	
	// ���к������йصĿռ�
	private ToggleButton ftpSwitch;
	private TextView ftpUsername;
	private TextView ftpPassword;
	private TextView ftpPort;
	
	private TextView ftpaddr;
	private TextView connhint;
	
	// ���ڵȴ���ɵĵȴ�������
	private LinearLayout progressWait;
	
	// ����״̬��UI������п���
	private WifiConnectRecevier wifiConnectReceiver;
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
	
	private boolean isServerRunning = false;
	
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newconn);
		
		// ���õȴ������� 
		progressWait = (LinearLayout)findViewById(R.id.progress_wait);  
		TextView msg = (TextView)findViewById(R.id.progress_description);  
		msg.setText("������������");  
		
		// ����������
		ftpSwitch = (ToggleButton) findViewById(R.id.ftpswitch);
		ftpaddr = (TextView) findViewById(R.id.ftpaddr);
		connhint = (TextView) findViewById(R.id.connhint);
		
		// ������������ʾ
		ftpUsername = (TextView)findViewById(R.id.ftp_username);
		ftpPassword = (TextView)findViewById(R.id.ftp_password);
		ftpPort = (TextView)findViewById(R.id.ftp_port);
		
		// ����Ĭ�ϵĲ���
		ftpUsername.setText(FsSettings.getUsername());
		ftpPassword.setText(FsSettings.getPassword());
		ftpPort.setText(String.valueOf(FsSettings.getPort()));
		
		Log.v(TAG, ((Context)this).toString());
		
		ftpSwitch.setOnClickListener(new OnStartStopServerListener());
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
		wifiConnectReceiver = new WifiConnectRecevier();
		WifiConectionChangeListener wccListener = new WifiConectionChangeListener();
		// ���ü�����
		wifiConnectReceiver.setListener(wccListener);
		
		// ����IntentFilter
		IntentFilter wifiConnectFilter = new IntentFilter();
		wifiConnectFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		wifiConnectFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		
		registerReceiver(wifiConnectReceiver, wifiConnectFilter);
		
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
		if (wifiConnectReceiver != null) {
			unregisterReceiver(wifiConnectReceiver);
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
		
		switch (state) {
			case (SERVER_STATE_STARTING | WIFI_STATE_CONNECTED):
				setSwitchChecked(true);
				setSwitchEnable(false);
				setSettingChanged(false);
				setProgressShow(true);
				break;
			case (SERVER_STATE_STARTED | WIFI_STATE_CONNECTED):
				setSwitchChecked(true);
				setSwitchEnable(true);
				setSettingChanged(false);
				setProgressShow(false);
				break;
			case (SERVER_STATE_STOPING | WIFI_STATE_CONNECTED):
				setSwitchChecked(false);
				setSwitchEnable(false);
				setSettingChanged(false);
				setProgressShow(true);
				break;
			case (SERVER_STATE_STOPPED | WIFI_STATE_CONNECTED):
				setSwitchChecked(false);
				setSwitchEnable(true);
				setSettingChanged(true);
				setProgressShow(false);
				break;
			case (SERVER_STATE_STARTING | WIFI_STATE_DISCONNECTED):
			case (SERVER_STATE_STARTED | WIFI_STATE_DISCONNECTED):
			case (SERVER_STATE_STOPING | WIFI_STATE_DISCONNECTED):
			case (SERVER_STATE_STOPPED | WIFI_STATE_DISCONNECTED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setSettingChanged(false);
				setProgressShow(false);
			default:
				break;
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
	
	/**
	 * �Ƿ�����Ķ�����������
	 */
	private void setSettingChanged(boolean canChange) {
		ftpUsername.setEnabled(canChange);
		ftpPassword.setEnabled(canChange);
		ftpPort.setEnabled(canChange);
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
	private class OnStartStopServerListener implements View.OnClickListener {
		
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

	/**
	 * wifi״̬�仯������
	 * @author HM
	 *
	 */
	private class WifiConectionChangeListener implements OnWifiConnectChangeListener {

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
}
