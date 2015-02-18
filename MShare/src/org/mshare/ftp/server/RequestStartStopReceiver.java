package org.mshare.ftp.server;

import org.mshare.main.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.util.Log;

public class RequestStartStopReceiver extends BroadcastReceiver {

    static final String TAG = RequestStartStopReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received: " + intent.getAction());

        // TODO: analog code as in ServerPreferenceActivity.start/stopServer(), refactor
        try {
            if (intent.getAction().equals(FsService.ACTION_START_FTPSERVER)) {
                Intent serverService = new Intent(context, FsService.class);
                if (!FsService.isRunning()) {
                	// 为什么要在这里提示扩展存储不可用呢？
//                    warnIfNoExternalStorage();
                    context.startService(serverService);
                } else {
                	// TODO 临时在这里添加sendBroadcast内容
                	context.sendBroadcast(new Intent(FsService.ACTION_STARTED));
                }
            } else if (intent.getAction().equals(FsService.ACTION_STOP_FTPSERVER)) {
            	// TODO 添加运行中判断
            	if (FsService.isRunning()) {
            		Intent serverService = new Intent(context, FsService.class);
                    context.stopService(serverService);
            	} else {
            		// 新添加的内容，没有测试,再说也是仅仅为了保险
            		context.sendBroadcast(new Intent(FsService.ACTION_STOPPED));
            	}
                
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start/stop on intent " + e.getMessage());
        }
    }
}
