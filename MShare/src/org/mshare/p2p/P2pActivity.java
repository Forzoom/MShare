package org.mshare.p2p;

import java.util.Collection;
import java.util.Iterator;

import org.mshare.main.ConnectInfo;
import org.mshare.main.R;
import org.mshare.nfc.NfcServerActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * 使用wifip2p不知是否需要用户手动开启wifip2p
 * 
 * 在许多机器上，wifip2p和wifi都合并在一起，wifip2p使用wifi设备，所有要使用wifip2p，要先启动wifi
 * 
 * 需要尝试在二维码扫描中，连接对于的AP或者的wifip2p peer
 * 使用wifip2p已经大致掌握了，但无法测试
 * wifip2p将如何尝试启动
 * 
 * Attention:
 * 在API版本没有到16(4.1)的情况下，stopDiscoverPeer是不可使用的，API14-15的情况下，使用discover可能会造成电量的损耗？不知道在API14-15的情况下该怎么办？
 * 所以暂时将版本限定到API16
 * 
 * TODO 将要尝试wifip2p的传输速度
 * @author HM
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class P2pActivity extends Activity {
	private static final String TAG = P2pActivity.class.getSimpleName();
	
	private WifiP2pManager wpm;
	private Channel channel;
	private P2pReceiver pr;
	private IntentFilter filter;
	
	// 这个是干什么的？
	public static final String ACTION_ON_PEERS_AVAILABLE = "org.mshare.p2p.ON_PEERS_AVAILABLE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO 显示当前的设备的名字
		setContentView(R.layout.p2p);
		
		wpm = (WifiP2pManager)getSystemService(Service.WIFI_P2P_SERVICE);
		// TODO 需要检测channel是否能够被正确地获得，当关闭WiFi Direct的时候，channel能否被正确地获得
		channel = wpm.initialize(this, getMainLooper(), null);

		Log.d(TAG, "get channel : " + channel);
		
		// 启动和停止p2p发现
		Button startButton = (Button)findViewById(R.id.start_discover);
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "discover start");
				wpm.discoverPeers(channel, new OnDiscoverPeersListener());
			}
		});
		
		// API版本是有问题的
		Button stopButton = (Button)findViewById(R.id.stop_discover);
		stopButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "discover stop");
				wpm.stopPeerDiscovery(channel, new OnStopDiscoverPeerListener());
			}
		});
		
		// 尝试连接和断开的速度
		// 需要对CONNECT_CHANGE事件监听来判断连接是否改变
		Button connectButton = (Button)findViewById(R.id.connect);
		connectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "try connect");
//				wpm.connect(channel, config, listener)
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		pr = new P2pReceiver(wpm, channel, this);
		
		filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		// TODO API版本不对
		filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		registerReceiver(pr, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(pr);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "NFC");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// 启动NFC
			Intent startNfc = new Intent(P2pActivity.this, NfcServerActivity.class);
			// 表示所发送内容为服务器信息
			startNfc.putExtra(NfcServerActivity.EXTRA_MESSAGE_TYPE, NfcServerActivity.MESSAGE_SERVER_INFO);
			// TODO 获得当前的ConnectInfo
			ConnectInfo connectInfo = new ConnectInfo("192.168.0.1", "2121", "username", "password");
			startNfc.putExtra(NfcServerActivity.EXTRA_SERVER_INFO, connectInfo);
			
			startActivity(startNfc);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// 不知道使用TargetApi是否好
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void showPeers(WifiP2pDeviceList peers) {
		Collection<WifiP2pDevice> devices = peers.getDeviceList();
		Iterator<WifiP2pDevice> iterator = devices.iterator();
		int count = 0;
		
		while (iterator.hasNext()) {
			count++;
			WifiP2pDevice device = iterator.next();
			
			String address = device.deviceAddress;
			String name = device.deviceName;
			Log.v(TAG, "address " + address);
			Log.v(TAG, "name " + name);
		}

		Log.d(TAG, "will show " + count + " peers");
		wpm.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
			
			@Override
			public void onSuccess() {
				Log.d(TAG, "stop discover success");
			}
			
			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "stop discover fail");
			}
		});
	}
	
}
