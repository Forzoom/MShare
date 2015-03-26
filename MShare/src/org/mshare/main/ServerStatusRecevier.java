package org.mshare.main;

import org.mshare.server.ftp.ServerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 用于接收服务器状态，并修改UI
 * @author HM
 *
 */
public class ServerStatusRecevier extends BroadcastReceiver {
	private static final String TAG = ServerStatusRecevier.class.getSimpleName();
	
	private StatusController statusController;
	
	public ServerStatusRecevier(StatusController statusController) {
		this.statusController = statusController;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d(TAG, "Recevie action : " + action);
		
		if (statusController != null) {
			if (action.equals(ServerService.ACTION_STARTED)) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STARTED);
				// 可能发送了启动请求，但是启动失败了
			} else if (action.equals(ServerService.ACTION_STOPPED) || action.equals(ServerService.ACTION_FAILEDTOSTART)) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STOPPED);
			}
		}
	}

}
