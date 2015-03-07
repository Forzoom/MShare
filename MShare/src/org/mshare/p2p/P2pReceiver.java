package org.mshare.p2p;

import java.util.Collection;
import java.util.Iterator;

import org.mshare.main.MShareApp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.util.Log;

/**
 * ����WIFI_P2P_PEERS_CHANGE_ACTION
 * TODO ��Ҫ�˽���ο���WIFIP2P
 * WIFIP2P��Ҫ�ֶ�����
 * @author HM
 *
 */
public class P2pReceiver extends BroadcastReceiver {

	private static final String TAG = P2pReceiver.class.getSimpleName();
	private WifiP2pManager wpm;
	private Channel channel;
	private Activity activity;
	
	private boolean canDiscover;
	
	public P2pReceiver(WifiP2pManager wpm, Channel channel, Activity activity) {
		this.wpm = wpm;
		this.channel = channel;
		this.activity = activity;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.v(TAG, "received :" + action);
		
		// ��⵱ǰ��WiFiP2P�����
		if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// WIFIP2P����
				Log.v(TAG, "wifi p2p state change enable");
				canDiscover = true;
//				wpm.discoverPeers(channel, new OnDiscoverPeersListener());
			} else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
				// WIFIP2P������
				Log.e(TAG, "wifi p2p state change disable");
			}
		}
		
		// ��⵱ǰpeer�б����仯�����
		// discoverPeers()�����ú󷵻صĽ��,requestPeers()����ý��
		if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
			Log.v(TAG, "peers state change");
			wpm.requestPeers(channel, new OnPeerListListener());
		}
		
		// ��Ϊ���ֻ����API16������²��ܹ�ʹ�ã�������Ҫ����
		if (action.equals(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)) {
			Log.d(TAG, "the discover state change : " + intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -11));
			wpm.requestPeers(channel, new OnPeerListListener());
		}

	}
	
	/**
	 * ���µ�peer�б����ʹ�õ�ʱ��
	 * @author HM
	 *
	 */
	protected class OnPeerListListener implements WifiP2pManager.PeerListListener {

		@Override
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			// TODO �г����е�Peer�б�
			Log.v(TAG, "onPeerListListener");
			Context context = MShareApp.getAppContext();
			Intent intent = new Intent(P2pActivity.ACTION_ON_PEERS_AVAILABLE);
			((P2pActivity)activity).showPeers(peers);
//			context.sendBroadcast(intent);
		}
		
	}

}
