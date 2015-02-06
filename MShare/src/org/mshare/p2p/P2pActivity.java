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
 * ʹ��wifip2p��֪�Ƿ���Ҫ�û��ֶ�����wifip2p
 * ��Ҫ�����ڶ�ά��ɨ���У����Ӷ��ڵ�AP���ߵ�wifip2p peer
 * ʹ��wifip2p�Ѿ����������ˣ����޷�����
 * wifip2p����γ�������
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
		// TODO ��ʾ��ǰ���豸������
		setContentView(R.layout.p2p);
		
		wpm = (WifiP2pManager)getSystemService(Service.WIFI_P2P_SERVICE);
		// TODO ��Ҫ���channel�Ƿ��ܹ�����ȷ�ػ�ã����ر�WiFi Direct��ʱ��channel�ܷ���ȷ�ػ��
		channel = wpm.initialize(this, getMainLooper(), null);
		pr = new P2pReceiver(wpm, channel, this);
		
		filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		// TODO API�汾����
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
