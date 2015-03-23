package org.mshare.ftp.server;

import android.util.Log;

public class CmdUUID extends FtpCmd implements Runnable {
	private static final String TAG = CmdUUID.class.getSimpleName();
	
	protected String input;
	
	public CmdUUID(SessionThread sessionThread, String input) {
		super(sessionThread);
		this.input = input;
	}

	@Override
	public void run() {
		Log.d(TAG, "UUID executing");
		
		sessionThread.writeString("211 " + FtpSettings.getUUID() + "\r\n");
		
		Log.d(TAG, "UUID finished");
	}

}
