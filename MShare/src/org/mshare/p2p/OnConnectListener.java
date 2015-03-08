package org.mshare.p2p;

import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.util.Log;

public class OnConnectListener implements ActionListener {

	private static final String TAG = OnConnectListener.class.getSimpleName();
	
	@Override
	public void onSuccess() {
		Log.d(TAG, "try connect success");
		
		// TODO �����ﳢ�Դ������ݣ�
	}

	@Override
	public void onFailure(int reason) {
		Log.e(TAG, "try connect fail");
	}

}
