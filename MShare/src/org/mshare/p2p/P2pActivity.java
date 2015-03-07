package org.mshare.p2p;

import java.util.Collection;
import java.util.Iterator;

import org.mshare.main.R;

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
 * @author HM
 *
 */
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
		
		Button discoverButton = (Button)findViewById(R.id.discover);
		discoverButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "discover start");
				wpm.discoverPeers(channel, new OnDiscoverPeersListener());
			}
		});
		
		Button wakeButton = (Button)findViewById(R.id.wake);
		wakeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "wake start");
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
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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
	
}
