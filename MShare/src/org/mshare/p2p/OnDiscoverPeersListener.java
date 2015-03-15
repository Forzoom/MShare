package org.mshare.p2p;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


/**
 * 当有新的Peer被发现的时候调用
 * @author HM
 *
 */
public class OnDiscoverPeersListener implements WifiP2pManager.ActionListener {
	private static final String TAG = OnDiscoverPeersListener.class.getSimpleName();
	
	@Override
	public void onSuccess() {
		// 调用requestPeers放在了WIFI_P2P_PEERS的状态发生改变的时候
		Log.v(TAG, "OnDiscoverPeersListener OnSuccess");
	}

	@Override
	public void onFailure(int reason) {
		Log.v(TAG, "OnDiscoverPeersListener OnFailure");
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