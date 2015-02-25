/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mshare.ftp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mshare.file.SharedLinkSystem;
import org.mshare.file.SharedLinkSystem.Permission;
import org.mshare.ftp.server.AccountFactory.Token;
import org.mshare.ftp.server.FsService.SessionNotifier;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * ����Ա�˻��е��ļ������б�Ҫ���ڵģ������жϳ־û������Ƿ���ȷ
 * ������Ա�˻������ݳ־û���ʱ����Ҫ�����е��ļ�����������ݣ���Ҫ������
 * ԭ���������һ���������Account�Ƿ���ڵ���Ϣ���Ƿ��б�Ҫ����������Ϣ�أ�
 * �����б�Ҫ�����е��˻������б���
 * ����˻����ܶ�����ʱ�ģ����п��ܣ��������Ĵ����ǳ־õģ������ļ�������Ϊ���ֻ��ˣ����ܸ��ಢ������һ����ʱ�����ݽ���ʹ��
 * ��authת��Ϊʹ��token
 * TODO ��Ҫ�����޸��û���������
 * TODO �����Ƿ�Account��ΪAccountFactory���ڲ��࣬��ΪAccount��Ӧ���ܹ������new���������ڲ����в�֪���ܲ��ܱ�֤�䲻��new����
 * TODO ���ǽ��ļ������޸Ĳ�������Account�и���һ�ݣ����Ҽ���notify,ֻ��Ҫ��add��delete�ļ�������notify,����persist������Ҫnotify
 * TODO ���ǹ���Ա�˻��еĲ�һ��
 * TODO ���ǽ�notifyֱ�ӷֳ������ã�����notifier��Ӧ����SharedLinkSystem�б������ˣ����߽�Notifier��Ϊ��̬����
 * ��Account������addSharedPath��persist��������
 * ����Ҫ��system�е������ƶ���Account��,����һ�£����ȫ��������Account�У��ᵼ����϶ȸ�
 * @author HM
 *
 */
public abstract class Account {
	private static final String TAG = Account.class.getSimpleName();
	
	// ��ǰ�˻����û���������
    private String mUserName = null;
    private String mPassword = null;
    // �û���¼���Ե�����
    private String mAttemptPassword = null;
    // ��¼����ʧ�ܵĴ���
    // TODO ��Ҫ���������������
    public int authFails = 0;
    
    private SharedLinkSystem mSharedLinkSystem;
    // TODO ��֪���Ƿ���Ҫ�޸ģ����������
    public static final String USER_DEFAULT = "default_username";

	// �����SharedPreferences�еļ�ֵ
    // TODO ʹ��public������
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PERMISSION = "permission";
    /**
     * ��user_sp�б����ϴ�·���ļ�ֵ
     */
    public static final String KEY_UPLOAD = "upload";

    /**
     * SessionNotifier��������������Session���Լ������˱仯
     */
    private SessionNotifier mNotifier;
    
    /**
     * �ܹ��ж��ٸ�Session��ʹ�õ�ǰ��Account
     */
    public int sessionCount;
    
    // TODO ���ǽ�Account�������ƶ���AccountFactory�У�����ģʽ����ôŪ�ģ�
    // TODO ����unprepared���������ڽ��ļ����ͷ�
    public Account(String username, String password) {
    	// TODO username,mPassword���п�����null
    	this.mUserName = username;
    	this.mPassword = password;
    	// TODO ����USER��ʱ���ļ����������ˣ���Ϊ����
    	mSharedLinkSystem = new SharedLinkSystem(this);
    	mSharedLinkSystem.prepare();
    }
    
    /**
     * ����˻���Ӧ��SharedPreferences
     * @return
     */
    public SharedPreferences getSharedPreferences() {
    	Context context = MShareApp.getAppContext();
    	return context.getSharedPreferences(mUserName, Context.MODE_PRIVATE);
    }
    
    /**
     * ������ǰ�˻����ϴ�·��
     * @param path
     * @return
     */
	public boolean setUpload(String path) {
		if (path == null || path.equals("")) {
			Log.e(TAG, "invalid upload path");
			return false;
		}
		
		SharedPreferences sp = getSharedPreferences();
		Editor editor = sp.edit();
		editor.putString(KEY_UPLOAD, path);
		return editor.commit();
	}
	
	/**
	 * ��û��ָ����upload·����ʱ�򣬽������˻���Ӧ�����֣���getUsername
	 * @return
	 */
	public String getUpload() {
		SharedPreferences sp = getSharedPreferences();
		return sp.getString(KEY_UPLOAD, FsSettings.getUpload() + File.separator + getUsername());
	}
	
	/**
	 * �жϵ�ǰ�û��Ƿ�ӵ������Ķ�Ȩ�ޣ�����ִ�ж���ص�FtpCmd
	 * @param account
	 * @param filePermission
	 * @return
	 */
	public static boolean canRead(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_READ_ALL) != Permission.PERMISSION_NONE;
	}
	/**
	 * �жϵ�ǰ�û��Ƿ�ӵ�������дȨ�ޣ�����ִ��д��ص�FtpCmd
	 * @param account
	 * @param filePermission
	 * @return
	 */
	public static boolean canWrite(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_WRITE_ALL) != Permission.PERMISSION_NONE; 
	}
	
	// TODO ��Ҫ������SharedLinkSystem���ã���SharedLinkSystem���ֱ仯ʱ������֪ͨ���е�SessionThread
	// TODO �����л��Account��SessionThread���˳���ʱ�򣬾ͻὫAccount��allAccounts��ɾ��
	// ��������ִ��
	public boolean canExecute() {
		return false;
	}
	
	// TODO ��Ҫ�������жϷ���
	public boolean isLoggedIn(Token token) {
		return false;
	}
	
	/**
	 * �жϵ�ǰ���˻��Ƿ��������˻�
	 * @return
	 */
	public abstract boolean isAnonymous();
	
	/**
	 * �жϵ�ǰ�˻��Ƿ�Ϊ����Ա�˻�
	 * @return
	 */
	public abstract boolean isAdministrator();
	
	/**
	 * �жϵ�ǰ�˻��Ƿ�Ϊ��ͨ�û��˻�
	 * @return
	 */
	public abstract boolean isUser();
	
	/**
	 * TODO ��֤���ص����ݲ�Ϊnull
	 * @return �û�������Ϊnull
	 */
    public String getUsername() {
        return mUserName;
    }

    /**
     * TODO ��֤���ص����ݲ�Ϊnull
	 * @return ���룬��Ϊnull
     */
    public String getPassword() {
    	return mPassword;
    }
    
    public void setAttemptPassword(String attemptPassword) {
    	mAttemptPassword = attemptPassword;
    }
    
    /**
     * Ĭ�Ϸ���{@link Permission#PERMISSION_NONE}
     * @return
     */
    public int getPermission() {
    	return Permission.PERMISSION_NONE;
    }
    
    /**
     * Accountӵ��SharedLinkSystem
     * ʹ��public����Ϊ�ܶ�ĵط�������FtpCmd�ж���Ҫ��SharedLinkSystem���в���
     * @return
     */
    public SharedLinkSystem getSystem() {
    	return mSharedLinkSystem;
    }
    
    /**
     * ������ĺ���
     * @return
     */
    public SessionNotifier getNotifier() {
    	return mNotifier;
    }
    
    /**
     * ������������Account���бȽ�
     */
    @Override
    public boolean equals(Object o) {
    	if (!(o instanceof Account)) {
    		return false;
    	}
    	Account account = (Account)o;
    	String username = account.getUsername();
    	String password = account.getPassword();
    	// TODO username��password����null��
    	if (username.equals(mUserName) && password.equals(mPassword)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void registerSession() {
    	sessionCount++;
    }
    
    // TODO �Ƿ���Ҫ�����ﳢ�Ի���Account
    public void unregisterSession() {
    	sessionCount--;
    }
    
    public int getSessionCount() {
    	return sessionCount;
    }
}
