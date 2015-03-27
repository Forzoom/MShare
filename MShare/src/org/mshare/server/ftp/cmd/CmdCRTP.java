package org.mshare.server.ftp.cmd;

import android.util.Log;

import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.SessionThread;

/**
 * 关闭服务器当前的rtsp模式
 * Created by huangming on 15/3/27.
 */
public class CmdCRTP extends FtpCmd {
	private static final String TAG = CmdCRTP.class.getSimpleName();

	public CmdCRTP(SessionThread sessionThread, String input) {
		super(sessionThread);
	}

	@Override
	public void run() {
		Log.d(TAG, "executing CRTP");

		sessionThread.stopRtsp();
		sessionThread.writeString("221 \r\n");

		Log.d(TAG, "finished CRTP");
	}
}
