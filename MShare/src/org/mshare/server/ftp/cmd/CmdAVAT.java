package org.mshare.server.ftp.cmd;

import android.util.Log;

import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.FtpSessionThread;

/**
 * Created by huangming on 15/3/28.
 */
public class CmdAVAT extends FtpCmd {
	private static final String TAG = CmdAVAT.class.getSimpleName();

	public CmdAVAT(FtpSessionThread sessionThread, String input) {
		super(sessionThread);
	}

	@Override
	public void run() {
		Log.d(TAG, "AVAT executing");

		// 发送信息确认准备发送的内容
		if (sessionThread.startUsingDataSocket()) {
			Log.e(TAG, "AVAT start use data socket");
		} else {
			sessionThread.writeString("425 error opening socket\r\n");
			Log.e(TAG, "error in startUsingDataSocket");
		}

		sessionThread.writeString("150 send avater\r\n");
//		Bitmap bitmap
//		sessionThread.sendViaDataSocket()

		Log.d(TAG, "AVAT finished");
	}
}
