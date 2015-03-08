package org.mshare.p2p;

import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.util.Log;

public class OnConnectListener implements ActionListener {

	private static final String TAG = OnConnectListener.class.getSimpleName();
	
	@Override
	public void onSuccess() {
		Log.d(TAG, "try connect success");
		
		// TODO 在这里尝试传输数据？
	}

	@Override
	public void onFailure(int reason) {
		Log.e(TAG, "try connect fail");
	}

}
