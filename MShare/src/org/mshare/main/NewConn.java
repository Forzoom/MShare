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

import org.mshare.main.StatusController.StatusCallback;
import org.mshare.nfc.NfcServerActivity;
import org.mshare.scan.ScanActivity;
import org.mshare.p2p.P2pActivity;

/**
 * TODO 当在选择要分享的内容的时候，能不能将我们的应用也加入到其中
 * @author HM
 *
 */
public class NewConn extends Activity implements StatusCallback {
	
	private static final String TAG = NewConn.class.getSimpleName();
	
	// 所有和配置有关的控件
	private ToggleButton ftpSwitch;
	private TextView ftpUsernameView;
	private TextView ftpPasswordView;
	private TextView ftpPortView;
	
	private TextView ftpAddrView;
	// 服务器状态
	private TextView serverStateView;
	// StatusBar
	private RelativeLayout statusBar;
	
	private TextView uploadPathView;
	
	// 总共是6种状态
	private static final int WIFI_STATE_CONNECTED = 0x10;
	private static final int WIFI_STATE_DISCONNECTED = 0x20;
	
	private static final int SERVER_STATE_MASK = 0xf;
	private static final int WIFI_STATE_MASK = 0x30;
	
	private StatusController statusController;
	
	// 没有任何状态
	private int state = 0;
	
	private ServerStatusRecevier serverStatusReceiver;
	
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
		statusBar = (RelativeLayout)findViewById(R.id.state_bar);
		
		// 上传路径
		uploadPathView = (TextView)findViewById(R.id.upload_path);
		
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
		
	}
	
	@Override
	protected void onStart() {
		// TODO 可能需要使用更加安全的BroadcastReceiver注册方式
		super.onStart();
		
		// 设置和初始化状态内容
		statusController = new StatusController();
		statusController.setCallback(this);
		statusController.initial((ViewGroup)findViewById(R.id.state_bar));
		
		// TODO 临时设置的检测当前是否是MOBILE信号，
		// TODO 需要处理的内容太多了，考虑不加入开启AP的功能
		// 并没有AP cannot enable，所以对于isWifiApEnable函数，可以正确的执行,但是对于setWifiApEnabled就会报错
		// TODO 如果启动AP失败了之后，就将其写入配置文件，表明当前设备可能并不支持开启AP

		// 当前上传路径
		uploadPathView.setText(FsSettings.getUpload());

		statusController.registerReceiver();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		statusController.unregisterReceiver();
		if (serverStatusReceiver != null) {
			unregisterReceiver(serverStatusReceiver);
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
				
				startQRCode.putExtra(QRCodeConnectActivity.EXTRA_CONTENT, connectInfo);
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
	 * 启动服务器
	 */
	private void startServer() {
		// 设置新的配置内容
//		statusController.
		sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
	}
	
	/**
	 * 尝试停止服务器
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		
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
			
		} catch (Exception e) {
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

	@Override
	public void onServerStatusChange(int status) {
		// 当服务器群状态变化的时候，需要用来调整背景颜色
		
	}
	
	@Override
	public void onWifiStatusChange(int state) {
		Log.d(TAG, "on wifi state change");
		switch (state) {
		// 表示的是手机不支持WIFI
		case StatusController.STATE_WIFI_DISABLE:
		case StatusController.STATE_WIFI_ENABLE:
			if (FsService.isRunning()) {
				// 尝试关闭服务器
				stopServer();
			}
			ftpAddrView.setText("未知");
			break;
		case StatusController.STATE_WIFI_USING:
			
			// 设置显示的IP地址
			ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
			break;
		}
	}
	
	/**
	 * 这里可能会有代码重复,需要将上面的内容除去
	 */
	@Override
	public void onWifiApStatusChange(int status) {
		Log.d(TAG, "on wifi ap state change");
		// TODO 地址可能并不是这样设置的，所以暂时将这些注释
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
	public void onWifiP2pStatusChange(int status) {
		// TODO Auto-generated method stub
		Log.d(TAG, "on wifi p2p state change");
	}

	@Override
	public void onExternalStorageChange(int status) {
		// TODO 对于扩展存储的变化能够作为响应
		Log.d(TAG, "on external storage state change");
	}

	@Override
	public void onNfcStatusChange(int status) {
		Log.d(TAG, "on nfc state change");
	}
	
}
