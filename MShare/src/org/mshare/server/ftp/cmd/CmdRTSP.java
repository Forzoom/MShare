package org.mshare.server.ftp.cmd;

import android.util.Log;

import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.SessionThread;

/**
 * 用于启动服务器的RTSP模式
 * Created by huangming on 15/3/27.
 */
public class CmdRTSP extends FtpCmd {
	private static final String TAG = CmdRTSP.class.getSimpleName();

	public CmdRTSP(SessionThread sessionThread, String input) {
		super(sessionThread);
	}

	public void run() {
		Log.d(TAG, "executing RTSP");

		sessionThread.startUsingRtsp();
		sessionThread.writeString("211 \r\n");

		// 尝试启动rtsp模式
		Log.d(TAG, "finished RTSP");
	}

}
