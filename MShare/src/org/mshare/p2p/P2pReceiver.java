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
 * 接收WIFI_P2P_PEERS_CHANGE_ACTION
 * TODO 需要了解如何开启WIFIP2P
 * WIFIP2P需要手动开启
 * @author HM
 *
 */
public class P2pReceiver extends BroadcastReceiver {

	private static final String TAG = P2pReceiver.class.getSimpleName();
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
		
		// 检测当前的WiFiP2P的情况
		if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// WIFIP2P可用
				wpm.discoverPeers(channel, new OnDiscoverPeersListener());
			} else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
				// WIFIP2P不可用
			}
		}
		
		// 检测当前peer列表发生变化的情况
		// discoverPeers()被调用后返回的结果,requestPeers()来获得结果
		if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
			wpm.requestPeers(channel, new OnPeerListListener());
		}

	}
	
	/**
	 * 当有新的Peer被发现的时候调用
	 * @author HM
	 *
	 */
	private class OnDiscoverPeersListener implements WifiP2pManager.ActionListener {

		@Override
		public void onSuccess() {
			// 调用requestPeers放在了WIFI_P2P_PEERS的状态发生改变的时候
		}

		@Override
		public void onFailure(int reason) {
			switch (reason) {
			case WifiP2pManager.ERROR:
				Log.e(TAG, "error");
				break;
			case WifiP2pManager.BUSY:
				Log.e(TAG, "busy");
				break;
			case WifiP2pManager.P2P_UNSUPPORTED:
				Log.e(TAG, "p2p_unsupported");
				break;
			}
		}
		
	}
	
	/**
	 * 当新的peer列表可以使用的时候
	 * @author HM
	 *
	 */
	private class OnPeerListListener implements WifiP2pManager.PeerListListener {

		@Override
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			// TODO 列出所有的Peer列表
			
			Context context = MShareApp.getAppContext();
			Intent intent = new Intent(P2pActivity.ACTION_ON_PEERS_AVAILABLE);
			Bundle b = new Bundle();
//			intent.put
//			context.sendBroadcast(intent);
		}
		
	}

}
