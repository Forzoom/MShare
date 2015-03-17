package org.mshare.ftp.server;

import android.util.Log;

public class CmdNINA extends FtpCmd {
	private static final String TAG = CmdNINA.class.getSimpleName();
	
	public CmdNINA(SessionThread sessionThread) {
		super(sessionThread);
	}

	@Override
	public void run() {
		Log.d(TAG, "CmdNINA executing");
		sessionThread.writeString("211 " + "" + "/r/n");
		Log.d(TAG, "CmdNINA finished");
	}

}
