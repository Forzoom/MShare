package org.mshare.server.ftp;

import org.mshare.server.ServerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RequestStartStopReceiver extends BroadcastReceiver {

    static final String TAG = RequestStartStopReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received: " + intent.getAction());

        // TODO: analog code as in ServerPreferenceActivity.start/stopServer(), refactor
        try {
            if (intent.getAction().equals(ServerService.ACTION_START_FTPSERVER)) {
                Intent serverService = new Intent(context, ServerService.class);
                if (!ServerService.isRunning()) {
                	// ΪʲôҪ��������ʾ��չ�洢�������أ�
//                    warnIfNoExternalStorage();
                    context.startService(serverService);
                } else {
                	// TODO ��ʱ���������sendBroadcast����
                	context.sendBroadcast(new Intent(ServerService.ACTION_STARTED));
                }
            } else if (intent.getAction().equals(ServerService.ACTION_STOP_FTPSERVER)) {
            	// TODO ����������ж�
            	if (ServerService.isRunning()) {
            		Intent serverService = new Intent(context, ServerService.class);
                    context.stopService(serverService);
            	} else {
            		// ����ӵ����ݣ�û�в���,��˵Ҳ�ǽ���Ϊ�˱���
            		context.sendBroadcast(new Intent(ServerService.ACTION_STOPPED));
            	}
                
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start/stop on intent " + e.getMessage());
        }
    }
}
