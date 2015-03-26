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
		
		// ��������ļ�������
		String nickName = PreferenceManager.getDefaultSharedPreferences(context).getString(ServerSettings.KEY_NICKNAME, ServerSettings.VALUE_NICKNAME_DEFAULT);
		
		// �����û�����õ�����»���û�������
				
		sessionThread.writeString("211 " + "" + "/r/n");
		
		Log.d(TAG, "CmdNINA finished");
	}

}
