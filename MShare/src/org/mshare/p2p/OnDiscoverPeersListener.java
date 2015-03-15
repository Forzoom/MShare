package org.mshare.p2p;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


/**
 * �����µ�Peer�����ֵ�ʱ�����
 * @author HM
 *
 */
public class OnDiscoverPeersListener implements WifiP2pManager.ActionListener {
	private static final String TAG = OnDiscoverPeersListener.class.getSimpleName();
	
	@Override
	public void onSuccess() {
		// ����requestPeers������WIFI_P2P_PEERS��״̬�����ı��ʱ��
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