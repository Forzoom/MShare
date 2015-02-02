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
	
	// 所有和配置有关的空间
	private ToggleButton ftpSwitch;
	private TextView ftpUsernameView;
	private TextView ftpPasswordView;
	private TextView ftpPortView;
	
	private TextView ftpAddrView;
	// 服务器状态
	private TextView serverStateView;
	// 网络状态:WIFI/MOBILE
	private TextView networkStateView;
	private TextView ftpApState;
	private ToggleButton ftpApTest;
	private TextView ftpApIp;
	
	// 服务器当前有多少用户连接
	private TextView ftpCurrentSessionsView;
	// 显示服务器允许多少用户连接
	private TextView ftpMaxSessionsView;
	
	// 用于等待完成的等待进度条
	private LinearLayout progressWait;
	
	// 监听状态对UI界面进行控制
	private NetworkStateRecevier networkStateReceiver;
	private ServerStateRecevier serverStateReceiver;
	
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
//		progressWait = (LinearLayout)findViewById(R.id.progress_wait);  
//		TextView msg = (TextView)findViewById(R.id.progress_description);  
//		msg.setText("服务器启动中");
		// TODO 暂时将其设置为不可见
//		progressWait.setVisibility(View.GONE);
		
		// 服务器开关
		ftpSwitch = (ToggleButton) findViewById(R.id.ftp_switch);
		
		// 显示当前地址的内容
		ftpAddrView = (TextView) findViewById(R.id.ftp_addr);
		ftpApState = (TextView)findViewById(R.id.ftp_wifi_ap_state);
		
		serverStateView = (TextView)findViewById(R.id.server_state);
		// 显示当前的网络连接情况
		networkStateView = (TextView)findViewById(R.id.network_state);
		
		// 尝试启动AP
		ftpApTest = (ToggleButton)findViewById(R.id.ftp_ap_test);
		ftpApIp = (TextView)findViewById(R.id.ftp_ap_ip);
		
		// 服务器设置显示
		ftpUsernameView = (TextView)findViewById(R.id.ftp_username);
		ftpPasswordView = (TextView)findViewById(R.id.ftp_password);
		ftpPortView = (TextView)findViewById(R.id.ftp_port);
		
		// 设置默认的参数
		ftpUsernameView.setText(FsSettings.getUsername());
		ftpPasswordView.setText(FsSettings.getPassword());
		ftpPortView.setText(String.valueOf(FsSettings.getPort()));
		
		Log.v(TAG, ((Context)this).toString());
		
		ftpSwitch.setOnClickListener(new StartStopServerListener());
		ftpApTest.setOnClickListener(new WifiApControlListener());
	}
	
	@Override
	protected void onStart() {
		// TODO 可能需要使用更加安全的BroadcastReceiver注册方式
		super.onStart();
		
		// TODO 临时设置的检测当前是否是3G信号，提醒用户，不知道ethernet是否是3G的内容
//		if (MShareUtil.isConnectedUsing(ConnectivityManager.TYPE_ETHERNET)) {
//			Toast.makeText(this, "当前正在使用移动蜂窝信号网络，可能产生流量", Toast.LENGTH_LONG).show();
//		}
		
		// 显示当前所使用的网络
		// TODO 在NetworkStateReceiver中也有类似的代码，是否可以合并?
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Service.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null) {
			String networkTypeName = ni.getTypeName();
			networkStateView.setText(networkTypeName);
		} else {
			String networkTypeName = "NONE";
			networkStateView.setText("NONE");
		}
		
		// 显示当前的AP启动状态
		// TODO 需要处理的内容太多了，可能考虑不加入开启AP的功能
		try {
			boolean wifiApEnabled = isWifiApEnabled();
			ftpApTest.setChecked(wifiApEnabled);
		} catch (IllegalAccessException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP无法启动");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP无法启动");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP无法启动");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			ftpApTest.setEnabled(false);
			ftpApState.setText("AP无法启动");
			e.printStackTrace();
		}
		
		// 先设置当前的状态
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

		// 注册简单的BroadcastReceiver用来监听设备的网络状况变化，可能存在安全风险
		networkStateReceiver = new NetworkStateRecevier();
		NetworkStateChangeListener wccListener = new NetworkStateChangeListener();
		
		// 设置网络变化和WifiAp变化监听器
		networkStateReceiver.setOnNetworkStateChangeListener(wccListener);
		WifiApStateChangeListener wacListener = new WifiApStateChangeListener();
		networkStateReceiver.setOnWifiApStateChangeListener(wacListener);
		
		// 设置IntentFilter
		IntentFilter wifiConnectFilter = new IntentFilter();
		wifiConnectFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		wifiConnectFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		// 监听WifiAp的状态
		wifiConnectFilter.addAction(FsService.WIFI_AP_STATE_CHANGED_ACTION);
		
		registerReceiver(networkStateReceiver, wifiConnectFilter);
		
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
		// TODO 启动一个新的Activity ServerSettingActivity
		
		switch (item.getItemId()) {
			case R.id.menu_set_ftp_server_qrcode:
				Log.v(TAG, "qrcode");
				Intent startQRCode = new Intent();
				startQRCode.setClass(this, QRCodeLogin.class);
				// 写入二维码需要显示的内容
				// 需要和扫描相对应
				// 需要内容:1.ip 2.port 3.username 4.password
				// 需要如何传送这些内容呢?使用字符隔开,在username和password中不允许有空格
				String address = "192.168.137.1";
				String port = "2121";
				String username = "username";
				String password = "password";
				
				// 使用空格分隔
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
	 * 调用WifiManager中的同名方法
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 */
	public static boolean isWifiApEnabled() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		// TODO 根据java反射机制补全其中的内容
		Context context = MShareApp.getAppContext();
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		Method isWifiApEnabledMethod = wm.getClass().getDeclaredMethod("isWifiApEnabled");
		isWifiApEnabledMethod.invoke(wm);
		return false;
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
					setProgressShow(true);
					
					serverStateView.setText("服务器启动中");
					
					break;
				case (SERVER_STATE_STARTED):
					setSwitchChecked(true);
					setSwitchEnable(true);
					setProgressShow(false);
					
					serverStateView.setText("服务器已启动");
					
					break;
				case (SERVER_STATE_STOPING):
					setSwitchChecked(false);
					setSwitchEnable(false);
					setProgressShow(true);
					
					serverStateView.setText("服务器关闭中");
					
					break;
				case (SERVER_STATE_STOPPED):
					setSwitchChecked(false);
					setSwitchEnable(true);
					setProgressShow(false);
					
					serverStateView.setText("服务器已关闭");
					
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
				
				serverStateView.setText("服务器启动中");
				
				break;
			case (SERVER_STATE_STARTED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
				
				serverStateView.setText("服务器已启动");
				
				break;
			case (SERVER_STATE_STOPING):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
				
				serverStateView.setText("服务器关闭中");
				
				break;
			case (SERVER_STATE_STOPPED):
				setSwitchChecked(FsService.isRunning());
				setSwitchEnable(false);
				setProgressShow(false);
				
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
		setProgressShow(true);
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
			
			// 当上面的方法调用完成后，没有发生错误
			// 将状态设置为开启中/关闭中
			if (enable) {
				ftpApState.setText("AP启动中");
			} else {
				ftpApState.setText("AP关闭中");
			}
			
		} catch (NoSuchMethodException e) {
			ftpApState.setText("AP无法启动");
			ftpApTest.setEnabled(false);
			ftpApTest.setChecked(false);
			Toast.makeText(this, "AP无法启动", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO 添加对应的响应
			ftpApState.setText("AP无法启动");
			ftpApTest.setEnabled(false);
			ftpApTest.setChecked(false);
			Toast.makeText(this, "AP无法启动", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			ftpApState.setText("AP无法启动");
			ftpApTest.setEnabled(false);
			ftpApTest.setChecked(false);
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
	
	private void setProgressShow(boolean show) {
		// TODO 暂时设置为不可使用
//		if (show) {
//			progressWait.setVisibility(View.VISIBLE);
//		} else {
//			progressWait.setVisibility(View.GONE);
//		}
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
			if (ftpApTest.isChecked()) {
				// TODO 尝试对WIFIAP进行操作，但是可能这样做会让代码太过分散，所以使用状态来表最好
				setWifiApEnabled(true);
				ftpApTest.setEnabled(false);
			} else {
				setWifiApEnabled(false);
				ftpApTest.setEnabled(false);
			}
		}
		
	}
	
	/**
	 * 网络状态变化监听器
	 * @author HM
	 *
	 */
	private class NetworkStateChangeListener implements OnNetworkStateChangeListener {

		@Override
		public void onNetworkStateChange(String typeName, int type) {
			Log.v(TAG, "接受到网络状态变化广播");
			// 只有在Wifi的情况下才允许启动服务器
			
			networkStateView.setText(typeName);
			
			if (type == ConnectivityManager.TYPE_WIFI) {
				changeState(WIFI_STATE_CONNECTED);
				ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
			} else {
				changeState(WIFI_STATE_DISCONNECTED);
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
			} else {
				changeState(SERVER_STATE_STOPPED);
			}
		}
	}

	/**
	 * 监听WifiAp状态
	 * @author HM
	 *
	 */
	private class WifiApStateChangeListener implements OnWifiApStateChangeListener {

		@Override
		public void onWifiApStateChange(boolean enable) {
			Context context = MShareApp.getAppContext();
			
			// 做出了响应，将ToggleButton设置为可用
			ftpApTest.setEnabled(true);
			
			// 对WifiAp的状态变化做出响应
			if (enable) {
				
				ftpApTest.setChecked(true);
				ftpApState.setText("AP已启动");
				
				// TODO 地址可能并不是这样设置的，所以暂时将这些撤销
				// 设置地址
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
				ftpApState.setText("AP已关闭");
				
				// TODO 暂时不处理IP
				
//				ftpApIp.setText("");
//				ftpApIp.setVisibility(View.VISIBLE);
			}
		}
	}
}
