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
 * TODO 当在选择要分享的内容的时候，能不能将我们的应用也加入到其中
 * TODO 尝试用NFC来连接
 * @author HM
 *
 */
public class NewConn extends Activity implements StateCallback {
	
	private static final String TAG = NewConn.class.getSimpleName();
	
	// 所有和配置有关的控件
	private ToggleButton ftpSwitch;
	private TextView ftpUsernameView;
	private TextView ftpPasswordView;
	private TextView ftpPortView;
	
	private TextView ftpAddrView;
	// 服务器状态
	private TextView serverStateView;
	// StateBar
	private RelativeLayout stateBar;
	
	private TextView uploadPathView;
	
	private ToggleButton apTest;
	private TextView ftpApIp;
	
	// 总共是6种状态
	private static final int SERVER_STATE_STARTING = 0x1;
	private static final int SERVER_STATE_STARTED = 0x2;
	private static final int SERVER_STATE_STOPING = 0x4;
	private static final int SERVER_STATE_STOPPED = 0x8;
	private static final int WIFI_STATE_CONNECTED = 0x10;
	private static final int WIFI_STATE_DISCONNECTED = 0x20;
	
	private static final int SERVER_STATE_MASK = 0xf;
	private static final int WIFI_STATE_MASK = 0x30;
	
	private StateController mState;
	
	// 没有任何状态
	private int state = 0;
	
	private ServerStateRecevier serverStateReceiver;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newconn);
		
		// 服务器开关
		ftpSwitch = (ToggleButton) findViewById(R.id.ftp_switch);
		
		// 显示当前IP地址的内容
		ftpAddrView = (TextView) findViewById(R.id.ftp_addr);
		
		// 服务器状态显示
		serverStateView = (TextView)findViewById(R.id.server_state);
		
		// StateBar
		stateBar = (RelativeLayout)findViewById(R.id.state_bar);
		
		// 上传路径
		uploadPathView = (TextView)findViewById(R.id.upload_path);
		
		// 尝试启动AP
		apTest = (ToggleButton)findViewById(R.id.ftp_ap_test);
		// TODO 不知道该怎么对应的IP地址
		ftpApIp = (TextView)findViewById(R.id.ftp_ap_ip);
		
		// 服务器设置显示
		ftpUsernameView = (TextView)findViewById(R.id.ftp_username);
		ftpPasswordView = (TextView)findViewById(R.id.ftp_password);
		ftpPortView = (TextView)findViewById(R.id.ftp_port);
		
		// 设置默认的参数
		ftpUsernameView.setText(FsSettings.getUsername());
		ftpPasswordView.setText(FsSettings.getPassword());
		ftpPortView.setText(String.valueOf(FsSettings.getPort()));
		
		// 设置启动和关闭的监听器
		ftpSwitch.setOnClickListener(new StartStopServerListener());
		apTest.setOnClickListener(new WifiApControlListener());
		
	}
	
	@Override
	protected void onStart() {
		// TODO 可能需要使用更加安全的BroadcastReceiver注册方式
		super.onStart();
		
		// 设置和初始化状态内容
		mState = new StateController();
		mState.setCallback(this);
		mState.initial((ViewGroup)findViewById(R.id.state_bar));
		
		// TODO 临时设置的检测当前是否是MOBILE信号，
		// TODO 需要处理的内容太多了，考虑不加入开启AP的功能
		// 并没有AP cannot enable
		// TODO 如果启动AP失败了之后，就将其写入配置文件，表明当前设备可能并不支持开启AP

		// 先设置当前的服务器状态
		// TODO 如果当前服务器已经启动了，发送启动命令给服务器，服务器也应该做出相应，所以NewConn和服务器之间的状态应该统一
		if (FsService.isRunning()) {
			changeState(SERVER_STATE_STARTED);
		} else {
			changeState(SERVER_STATE_STOPPED);
		}

		// 当前上传路径
		uploadPathView.setText(FsSettings.getUpload());

		mState.registerReceiver();
		/*
		 * 服务器状态监听器
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
		// TODO 需要使用图标来代替文字吗
		
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
				
				// 当不存在服务器时，不能启动二维码扫描，这该怎么办？
				// 这里暂时使用的是默认的IP地址
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
		
		// TODO 在其中添加了关于服务器状态的内容
		
		if (networkState == WIFI_STATE_CONNECTED) {
			switch (serverState) {
				case (SERVER_STATE_STARTING):
					setSwitchChecked(true);
					setSwitchEnable(false);
					
					serverStateView.setText("服务器启动中");
					
					break;
				case (SERVER_STATE_STARTED):
					setSwitchChecked(true);
					setSwitchEnable(true);
					
					serverStateView.setText("服务器已启动");
					
					break;
				case (SERVER_STATE_STOPING):
					setSwitchChecked(false);
					setSwitchEnable(false);
					
					serverStateView.setText("服务器关闭中");
					
					break;
				case (SERVER_STATE_STOPPED):
					setSwitchChecked(false);
					setSwitchEnable(true);
					
					serverStateView.setText("服务器已关闭");
					
					break;
				default:
					break;
			}
		} else if (networkState == WIFI_STATE_DISCONNECTED) {
			// 当没有连接网络的状态下，能否修改服务器状态
			switch(serverState) {
			case (SERVER_STATE_STARTING):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("服务器启动中");
				
				break;
			case (SERVER_STATE_STARTED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("服务器已启动");
				
				break;
			case (SERVER_STATE_STOPING):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("服务器关闭中");
				
				break;
			case (SERVER_STATE_STOPPED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				
				serverStateView.setText("服务器已关闭");
				
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
	}
	
	/**
	 * 尝试停止服务器
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		changeState(SERVER_STATE_STOPING);
	}
	
	/**
	 * 尝试启动Ap
	 * 这样的函数放在这里是不是不大好
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
			apTest.setEnabled(false);
			apTest.setChecked(false);
			Toast.makeText(this, "AP无法启动", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO 添加对应的响应
			apTest.setEnabled(false);
			apTest.setChecked(false);
			Toast.makeText(this, "AP无法启动", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			apTest.setEnabled(false);
			apTest.setChecked(false);
			Toast.makeText(this, "AP无法启动", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
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
	
	/**
	 * 服务器开关监听器
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
	 * 响应Activity中的点击事件，用来启动和关闭WifiAp
	 * @author HM
	 *
	 */
	private class WifiApControlListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (apTest.isChecked()) {
				// 尝试对WIFIAP进行操作
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
			// 当服务器启动时
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
		// 表示的是手机不支持WIFI
		case StateController.STATE_WIFI_DISABLE:
		case StateController.STATE_WIFI_ENABLE:
			changeState(WIFI_STATE_DISCONNECTED);
			if (FsService.isRunning()) {
				// 尝试关闭服务器
				stopServer();
			}
			ftpAddrView.setText("未知");
			break;
		case StateController.STATE_WIFI_USING:
			changeState(WIFI_STATE_CONNECTED);
			// 设置显示的IP地址
			ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
			break;
		}
	}
	
	/**
	 * 这里可能会有代码重复,需要将上面的内容除去
	 */
	@Override
	public void onWifiApStateChange(int state) {
		Log.d(TAG, "on wifi ap state change");
		switch (state) {
		case StateController.STATE_WIFI_AP_UNSUPPORT:
			apTest.setEnabled(false);
			apTest.setChecked(false);
			break;
			// 下面三种情况下有什么需要处理的吗?
			// 下面的不处理apTest的checkd?
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
		// TODO 地址可能并不是这样设置的，所以暂时将这些撤销
		// 设置地址
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
		// TODO 对于扩展存储的变化能够作为响应
		Log.d(TAG, "on external storage state change");
	}

	@Override
	public void onNfcStateChange(int state) {
		Log.d(TAG, "on nfc state change");
	}
}
