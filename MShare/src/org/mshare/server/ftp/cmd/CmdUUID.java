package org.mshare.server.ftp.cmd;

import android.util.Log;

import org.mshare.server.ftp.FtpCmd;
import org.mshare.preference.ServerSettings;
import org.mshare.server.ftp.SessionThread;

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
		
		sessionThread.writeString("211 " + ServerSettings.getUUID() + "\r\n");
		
		Log.d(TAG, "UUID finished");
	}

}
