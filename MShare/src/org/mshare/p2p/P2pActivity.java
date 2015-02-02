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
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;

public class P2pActivity extends Activity {

	private WifiP2pManager wpm;
	private Channel channel;
	private P2pReceiver pr;
	private IntentFilter filter;
	
	public static final String ACTION_ON_PEERS_AVAILABLE = "org.mshare.p2p.ON_PEERS_AVAILABLE";
	
	/**
	 * 通知当peers可以使用的时候
	 * @author HM
	 *
	 */
	private class OnPeersAvailableReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Collection<WifiP2pDevice> devices = peers.getDeviceList();
			Iterator<WifiP2pDevice> iterator = devices.iterator();
			if (iterator.hasNext()) {
				WifiP2pConfig config = new WifiP2pConfig();
				WifiP2pDevice device = iterator.next();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;
				
				wpm.connect(channel, config, new WifiP2pManager.ActionListener() {

					@Override
					public void onSuccess() {
						
					}

					@Override
					public void onFailure(int reason) {
						
					}
					
				});
			}
			
			/*
			Iterator<WifiP2pDevice> iterator = devices.iterator();
			while (iterator.hasNext()) {
				WifiP2pConfig config = new WifiP2pConfig();
				WifiP2pDevice device = iterator.next();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;
			}
			*/
		}
	}
	
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
	
}
