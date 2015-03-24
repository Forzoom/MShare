package org.mshare.ftp.server;

import org.mshare.main.MShareApp;

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
		String nickName = PreferenceManager.getDefaultSharedPreferences(context).getString(FtpSettings.KEY_NICKNAME, FtpSettings.VALUE_NICKNAME_DEFAULT);
		
		// �����û�����õ�����»���û�������
				
		sessionThread.writeString("211 " + "" + "/r/n");
		
		Log.d(TAG, "CmdNINA finished");
	}

}
