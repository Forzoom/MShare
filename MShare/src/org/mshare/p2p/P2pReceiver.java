package org.mshare.p2p;

import java.util.Collection;
import java.util.Iterator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

/**
 * ����WIFI_P2P_PEERS_CHANGE_ACTION
 * TODO ��Ҫ�˽���ο���WIFIP2P
 * WIFIP2P��Ҫ�ֶ�����
 * @author HM
 *
 */
public class P2pReceiver extends BroadcastReceiver {

	private WifiP2pManager wpm;
	private Channel channel;
	private Activity activity;
	
	public P2pReceiver(WifiP2pManager wpm, Channel channel, Activity activity) {
		this.wpm = wpm;
		this.channel = channel;
		this.activity = activity;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		// ��⵱ǰ��WiFiP2P�����
		if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// WIFIP2P����
			} else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
				// WIFIP2P������
			}
		}
		
		// ��⵱ǰpeer�б����仯�����
		// discoverPeers()�����ú󷵻صĽ��,requestPeers()����ý��
		if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
			wpm.discoverPeers(channel, new OnDiscoverPeersListener());
		}

	}
	
	private void error() {
		
	}
	
	private void busy() {
		
	}
	
	private void p2pUnsupported() {
		
	}
	
	private class OnDiscoverPeersListener implements WifiP2pManager.ActionListener {

		@Override
		public void onSuccess() {
			wpm.requestPeers(channel, new OnPeerListListener());
		}

		@Override
		public void onFailure(int reason) {
			switch (reason) {
			case WifiP2pManager.ERROR:
				error();
				break;
			case WifiP2pManager.BUSY:
				busy();
				break;
			case WifiP2pManager.P2P_UNSUPPORTED:
				p2pUnsupported();
				break;
			}
		}
		
	}
	
	private class OnPeerListListener implements WifiP2pManager.PeerListListener {

		@Override
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			// TODO �г����е�Peer�б�
			
			Collection<WifiP2pDevice> devices = peers.getDeviceList();
			Iterator<WifiP2pDevice> iterator = devices.iterator();
		}
		
	}

}
