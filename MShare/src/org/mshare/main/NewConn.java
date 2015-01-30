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
	
	// 所有和配置有关的空间
	private Switch ftpSwitch;
	private TextView ftpUsername;
	private TextView ftpPassword;
	private TextView ftpPort;
	
	private TextView ftpaddr;
	private TextView connhint;
	private TextView ftpApState;
	private ToggleButton ftpApTest;
	private TextView ftpApIp;
	
	// 用于等待完成的等待进度条
	private LinearLayout progressWait;
	
	// 监听状态对UI界面进行控制
	private WifiStateRecevier wifiStateReceiver;
	private ServerStateRecevier serverStateReceiver;
	private WifiApStateReceiver wifiApStateReceiver;
	
	// 总共是6种状态
	private static final int SERVER_STATE_STARTING = 0x1;
	private static final int SERVER_STATE_STARTED = 0x2;
	private static final int SERVER_STATE_STOPING = 0x4;
	private static final int SERVER_STATE_STOPPED = 0x8;
	private static final int WIFI_STATE_CONNECTED = 0x10;
	private static final int WIFI_STATE_DISCONNECTED = 0x20;
	
	private static final int SERVER_STATE_MASK = 0xf;
	private static final int WIFI_STATE_MASK = 0x30;
	
	// 没有任何状态
	private int state = 0;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newconn);
		
		// 设置等待滚动条 
		progressWait = (LinearLayout)findViewById(R.id.progress_wait);  
		TextView msg = (TextView)findViewById(R.id.progress_description);  
		msg.setText("服务器启动中");  
		
		// 服务器开关
		ftpSwitch = (Switch) findViewById(R.id.ftp_switch);
		ftpaddr = (TextView) findViewById(R.id.ftpaddr);
		connhint = (TextView) findViewById(R.id.connhint);
		ftpApState = (TextView)findViewById(R.id.ftp_wifi_ap_state);
		// 尝试启动AP
		ftpApTest = (ToggleButton)findViewById(R.id.ftp_ap_test);
		ftpApIp = (TextView)findViewById(R.id.ftp_ap_ip);
		
		// 服务器设置显示
		ftpUsername = (TextView)findViewById(R.id.ftp_username);
		ftpPassword = (TextView)findViewById(R.id.ftp_password);
		ftpPort = (TextView)findViewById(R.id.ftp_port);
		
		// 设置默认的参数
		ftpUsername.setText(FsSettings.getUsername());
		ftpPassword.setText(FsSettings.getPassword());
		ftpPort.setText(String.valueOf(FsSettings.getPort()));
		
		Log.v(TAG, ((Context)this).toString());
		
		ftpSwitch.setOnClickListener(new StartStopServerListener());
		ftpApTest.setOnClickListener(new WifiApControlListener());
	}
	
	@Override
	protected void onStart() {
		// TODO 可能需要使用更加安全的BroadcastReceiver注册方式
		super.onStart();
		
		// 先设置当前的状态
		// TODO 先设置监听器
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
		
		// 简单的BroadcastReceiver，可能存在安全风险
		wifiStateReceiver = new WifiStateRecevier();
		WifiConectionChangeListener wccListener = new WifiConectionChangeListener();
		// 设置监听器
		wifiStateReceiver.setListener(wccListener);
		
		// 设置IntentFilter
		IntentFilter wifiConnectFilter = new IntentFilter();
		wifiConnectFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		wifiConnectFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		
		registerReceiver(wifiStateReceiver, wifiConnectFilter);
		
		/*
		 * WifiAp状态监听器
		 */
		wifiApStateReceiver = new WifiApStateReceiver();
		WifiApStateChangeListener wascListener = new WifiApStateChangeListener();
		wifiApStateReceiver.setOnWifiApStateChangeListener(wascListener);
		
		IntentFilter wifiApStateFilter = new IntentFilter();
		wifiApStateFilter.addAction(FsService.WIFI_AP_STATE_CHANGED_ACTION);
		
		registerReceiver(wifiApStateReceiver, wifiApStateFilter);
		
		/*
		 * 服务器状态监听器
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
		// TODO 解除注册
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
		// TODO 添加二维码启动
		
		MenuInflater infalter = getMenuInflater();
		infalter.inflate(R.menu.ftp_new_conn, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO 启动一个新的Activity ServerSettingActivity
		
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
	 * 当其中的状态发生改变
	 * 服务器状态:1.启动2.启动中3.停止4.停止中
	 * wifi状态1.连上，没连上
	 * 总共8中状态，加上从一个状态变成另外一种状态，总共需要的代码太多，太乱
	 */
	private void changeState(int s) {
		if ((s & SERVER_STATE_MASK) != 0) {// 改变server状态
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
	 * 启动服务器
	 */
	private void startServer() {
		// 设置新的配置内容
		sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
		changeState(SERVER_STATE_STARTING);
		setHintText("正在尝试启动服务器");
	}
	
	/**
	 * 尝试停止服务器
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		setProgressShow(true);
		setHintText("正在尝试停止服务器");
	}
	
	/**
	 * 尝试启动WifiAp
	 */
	private void setWifiApEnabled(boolean enable) {
		// TODO 需要了解更多关于java反射的内容
		WifiManager wm = (WifiManager)getSystemService(Service.WIFI_SERVICE);
		
		
		
		try {
			// 用于获得WifiConfiguration
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
	 * 设置服务器开关是否可用
	 */
	private void setSwitchEnable(boolean b) {
		ftpSwitch.setEnabled(b);
	}
	/**
	 * 设置服务器开关上显示的是开还是关
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
	 * 服务器开关监听器
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
	 * wifi状态变化监听器
	 * @author HM
	 *
	 */
	private class WifiConectionChangeListener implements OnWifiStateChangeListener {

		@Override
		public void onWifiConnectChange(boolean connected) {
			// TODO Auto-generated method stub
			// 只有在Wifi的情况下才允许启动服务器
			if (connected) {
				changeState(WIFI_STATE_CONNECTED);
				setHintText("当前WIFI连接中");
				ftpaddr.setText(FsService.getLocalInetAddress().getHostAddress());
			} else {
				changeState(WIFI_STATE_DISCONNECTED);
				setHintText("当前WIFI未连接");
				if (FsService.isRunning()) {
					// 尝试关闭服务器
					stopServer();
				}
			}
		}
		
	}
	
	private class ServerStateChangeListener implements OnServerStateChangeListener {

		@Override
		public void onServerStateChange(boolean start) {
			// TODO Auto-generated method stub
			// 当服务器启动时
			// 接受到了反馈，所以将Progress设置为不可见
			if (start) {
				changeState(SERVER_STATE_STARTED);
				setHintText("服务器已经启动");
			} else {
				changeState(SERVER_STATE_STOPPED);
				setHintText("服务器已经停止");
			}
		}
	}
	
	private class WifiApStateChangeListener implements OnWifiApStateChangeListener {

		@Override
		public void onWifiApStateChange(boolean enable) {
			Context context = MShareApp.getAppContext();
			if (enable) {
				ftpApState.setText("当前Ap可用");
				byte[] address = FsService.getLocalInetAddress().getAddress();
				String addressStr = "";
				for (int i = 0, len = address.length; i < len; i++) {
					byte b = address[i];
					addressStr += String.valueOf(((int)b + 256)) + " ";
				}
				ftpApIp.setText(addressStr);
				ftpApIp.setVisibility(View.VISIBLE);
			} else {
				ftpApState.setText("当前Ap不可用");
				ftpApIp.setText("");
				ftpApIp.setVisibility(View.VISIBLE);
			}
			
		}
	}
}
