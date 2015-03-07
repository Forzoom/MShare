package org.mshare.p2p;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class OnStopDiscoverPeerListener implements WifiP2pManager.ActionListener {

	private static final String TAG = OnStopDiscoverPeerListener.class.getSimpleName();
	
	@Override
	public void onSuccess() {
		Log.d(TAG, "discover stop success");
	}

	@Override
	public void onFailure(int reason) {
		Log.e(TAG, "discover stop fail : reason : " + reason);
	}

}
