package org.mshare.server.ftp.cmd;

import android.util.Log;

import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.FtpSessionThread;

/**
 * ����������������RTSPģʽ
 * Created by huangming on 15/3/27.
 */
public class CmdRTSP extends FtpCmd {
	private static final String TAG = CmdRTSP.class.getSimpleName();

	public CmdRTSP(FtpSessionThread sessionThread, String input) {
		super(sessionThread);
	}

	public void run() {
		Log.d(TAG, "executing RTSP");

		sessionThread.startUsingRtsp();
		Log.d(TAG, "is rtsp enabled " + sessionThread.isRtspEnabled());
		sessionThread.writeString("211 \r\n");

		// ��������rtspģʽ
		Log.d(TAG, "finished RTSP");
	}

}
