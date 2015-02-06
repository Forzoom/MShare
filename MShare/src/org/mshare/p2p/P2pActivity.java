package org.mshare.p2p;

import java.util.Collection;
import java.util.Iterator;

import org.mshare.main.R;

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
import android.os.Bundle;
import android.util.Log;

/**
 * 使用wifip2p不知是否需要用户手动开启wifip2p
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
	
	public static final String ACTION_ON_PEERS_AVAILABLE = "org.mshare.p2p.ON_PEERS_AVAILABLE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO 显示当前的设备的名字
		setContentView(R.layout.p2p);
		
		wpm = (WifiP2pManager)getSystemService(Service.WIFI_P2P_SERVICE);
		// TODO 需要检测channel是否能够被正确地获得，当关闭WiFi Direct的时候，channel能否被正确地获得
		channel = wpm.initialize(this, getMainLooper(), null);
		pr = new P2pReceiver(wpm, channel, this);
		
		filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		// TODO API版本不对
		filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(pr, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(pr);
	}
	
	public void showPeers(WifiP2pDeviceList peers) {
		Collection<WifiP2pDevice> devices = peers.getDeviceList();
		Iterator<WifiP2pDevice> iterator = devices.iterator();
		
		while (iterator.hasNext()) {
			WifiP2pDevice device = iterator.next();
			String address = device.deviceAddress;
			String name = device.deviceName;
			Log.v(TAG, "address " + address);
			Log.v(TAG, "name " + name);
		}
		
	}
	
}
