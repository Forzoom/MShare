package org.mshare.server.ftp.cmd;

import org.mshare.main.MShareApp;
import org.mshare.server.ServerSettings;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.SessionThread;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

public class CmdNINA extends FtpCmd {
	private static final String TAG = CmdNINA.class.getSimpleName();
	
	public CmdNINA(SessionThread sessionThread) {
		super(sessionThread);
	}

	@Override
	public void run() {
		Log.d(TAG, "CmdNINA executing");
		
		Context context = MShareApp.getAppContext();
		
		// 获得配置文件的内容
		String nickName = PreferenceManager.getDefaultSharedPreferences(context).getString(ServerSettings.KEY_NICKNAME, ServerSettings.VALUE_NICKNAME_DEFAULT);
		
		// 如何在没有设置的情况下获得用户的名称
				
		sessionThread.writeString("211 " + "" + "/r/n");
		
		Log.d(TAG, "CmdNINA finished");
	}

}
