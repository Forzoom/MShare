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
 * TODO ����ѡ��Ҫ��������ݵ�ʱ���ܲ��ܽ����ǵ�Ӧ��Ҳ���뵽����
 * @author HM
 *
 */
public class NewConn extends Activity {
	
	private static final String TAG = NewConn.class.getSimpleName();
	
	// ���к������йصĿؼ�
	private ToggleButton ftpSwitch;
	private TextView ftpUsernameView;
	private TextView ftpPasswordView;
	private TextView ftpPortView;
	
	private TextView ftpAddrView;
	// ������״̬
	private TextView serverStateView;
	
	private TextView uploadPathView;
	
	// �ܹ���6��״̬
	private static final int WIFI_STATE_CONNECTED = 0x10;
	private static final int WIFI_STATE_DISCONNECTED = 0x20;
	
	private static final int SERVER_STATE_MASK = 0xf;
	private static final int WIFI_STATE_MASK = 0x30;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newconn);
		
		// ����������
		ftpSwitch = (ToggleButton) findViewById(R.id.ftp_switch);
		
		// ��ʾ��ǰIP��ַ������
		ftpAddrView = (TextView) findViewById(R.id.ftp_addr);
		
		// ������״̬��ʾ
		serverStateView = (TextView)findViewById(R.id.server_state);
		
		// �ϴ�·��
		uploadPathView = (TextView)findViewById(R.id.upload_path);
		
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
	 * ����������
	 */
	private void startServer() {
		// �����µ���������
//		statusController.
		sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
	}
	
	/**
	 * ����ֹͣ������
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		
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
			
		} catch (Exception e) {
			Toast.makeText(this, "AP�޷�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
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
			if (ftpSwitch.isChecked()) {
				startServer();
			} else {
				stopServer();
			}
		}
	}
}
