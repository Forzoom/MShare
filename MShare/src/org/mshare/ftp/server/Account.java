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
import java.util.HashMap;
import java.util.Map;

import org.mshare.file.SharedLinkSystem;
import org.mshare.file.SharedLinkSystem.Permission;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * ԭ���������һ���������Account�Ƿ���ڵ���Ϣ���Ƿ��б�Ҫ����������Ϣ�أ�
 * �����б�Ҫ�����е��˻������б���
 * ����˻����ܶ�����ʱ�ģ����п��ܣ��������Ĵ����ǳ־õģ������ļ�������Ϊ���ֻ��ˣ����ܸ��ಢ������һ����ʱ�����ݽ���ʹ��
 * @author HM
 *
 */
public class Account {
	private static final String TAG = Account.class.getSimpleName();
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";
	public static final String AdminUsername = "admin";
	public static final String AdminPassword = "admin";
    private String mUserName = null;
    private String mPassword = null;
    private String mAttemptPassword = null;
    private boolean userAuthenticated = false;
    public int authFails = 0;
    
    public static final int PERMISSION_NONE = 0;
    
    public static final String USER_DEFAULT = "default_username";
    
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PERMISSION = "mPermission";
    
    // Ĭ��ֵӵ�ж�Ȩ��,Ϊ�������дȨ��
    private int mPermission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    // ����������¼�˻���Ȩ��
    private static final int PERMISSION_ANONYMOUS = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    
    public static final String SP_KEY_ACCOUNT_INFO = "accounts";
    /**
     * ��sp�б����ϴ�·����λ��
     */
    public static final String KEY_UPLOAD = "upload";
    
    public static Account adminAccount = new Account(Account.AdminUsername, Account.AdminPassword);
    
    private Account(String username, String password) {
    	// TODO username,mPassword���п�����null
    	setUsername(username);
    	this.mPassword = password;
    }
    
    /**
     * ��⵱ǰ�Ƿ��ǵ�¼�ɹ�
     * TODO ���ǽ��˻�Ȩ������������
     * @return
     */
    public boolean authAttempt() {
		if (!mUserName.equals(AnonymousUsername) && mAttemptPassword != null && mAttemptPassword.equals(mPassword)) {
			Log.d(TAG, "ʹ�÷������˻����Ե�¼");
			userAuthenticated = true;
		} else if (FsSettings.allowAnoymous() && mUserName.equals(AnonymousUsername)) {
			// ����Ȩ��Ϊ�����˻�Ȩ��
			mPermission = PERMISSION_ANONYMOUS;
			Log.i(TAG, "Guest logged in with password: " + mAttemptPassword);
			userAuthenticated = true;
		} else {
			Log.d(TAG, "���Ե�¼ʧ��");
			authFails++;
			userAuthenticated = false;
		}
		return userAuthenticated;
    }
    
    public SharedPreferences getSharedPreferences() {
    	Context context = MShareApp.getAppContext();
    	return context.getSharedPreferences(mUserName, Context.MODE_PRIVATE);
    }
    
    // TODO ��Ҫ�����޸��û���������
	public static Account getInstance(String username) {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(SP_KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// ���account�е�����
		if (accountsSp.getBoolean(username, false) == false) {
			Log.e(TAG, "account info ��û�и��ʺŵ�����");
			return null;
		}
		
		if (username.equals(AnonymousUsername)) {
			Log.d(TAG, "��ǰ�����˻����Ե�¼");
		} else if (username.equals(FsSettings.getUsername())) {
			Log.d(TAG, "��ǰĬ���˻����Ե�¼");
		}
		
		// TODO ��Ҫ���˻��������������
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
			String password = sp.getString(KEY_PASSWORD, "");
			if (!password.equals("")) {
				Log.d(TAG, "�����ȷ���˻�");
				return new Account(username, password);
			} else {
				Log.e(TAG, "��Ҫ���˻����벻����" + password);
				return null;// �˻�������
			}
		} else {
			// �û�������ȷ
			Log.e(TAG, "������û������Ϸ�");
			return null;
		}
	}

	/**
	 * ��Ҫ��check��ʹ��register�������ļ�
	 * ʹ��֮ǰ��Ӧ����⣬��ע���û����û������ܺ�Ĭ���û��������û��Լ�����Ա�˻���ͻ
	 * ����ͨ���ú���ע�������Ĭ���˻��������˻�
	 * 
	 * @param username
	 * @param password
	 * @param mPermission 
	 * @return
	 */
	private static boolean register(String username, String password, int permission) {
		Log.d(TAG, "��ʼע���˻�,�û���:" + username + " ����:" + password);
		boolean createUserSuccess = false;
		Context context = MShareApp.getAppContext();
		// �����û��ļ�
		SharedPreferences userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		if (userSp.getString(KEY_PASSWORD, "").equals("")) {
			Editor editor = userSp.edit();
			editor.putString(KEY_PASSWORD, password);
			// ��û��ָ��Ȩ��ʱ����ʹ����ͨ�˻��Ķ�дȨ��
			editor.putInt(KEY_PERMISSION, permission == PERMISSION_NONE ? Permission.PERMISSION_READ | Permission.PERMISSION_WRITE: permission);
			createUserSuccess = editor.commit();
		} else {
			Log.e(TAG, "Register Fail:username has already existed");
			return false;
		}
		// �������û��ļ�ʧ��
		if (!createUserSuccess) {
			Log.e(TAG, "Register Fail:create sharedPreferences fail");
			return false;
		}
		
		// ��accountInfo���������
		SharedPreferences accountsSp = context.getSharedPreferences(SP_KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
		Editor accountEditor = accountsSp.edit();
		accountEditor.putBoolean(username, true);
		if (accountEditor.commit()) {
			Log.d(TAG, "Register Success:success");
			return true;
		} else {
			Log.e(TAG, "Register Fail:Fail");
			return false;
		}			
	}
	
	/**
	 * ��Ĭ���˻��������˻������ڵ�ʱ��ʹ��register����ע��,������Ա�˻�
	 * ��������ʵ���Ȩ��
	 */
	public static void checkReservedAccount() {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(SP_KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// ��������˻�
		if (accountsSp.getBoolean(AnonymousUsername, false) == false) {
			
			Log.d(TAG, "��ǰ�����˻���Ϣ������");
			Log.d(TAG, "��ʼע�������˻�");
			int permission = Permission.PERMISSION_READ_GUEST;
			boolean registerResult = register(AnonymousUsername, AnonymousPassword, permission);
			Log.d(TAG, "����ע�������˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰ�����˻���Ϣ����");
		}
		
		// ���Ĭ�˻�
		if (accountsSp.getBoolean(FsSettings.getUsername(), false) == false) {
			
			Log.d(TAG, "��ǰĬ���˻���Ϣ������");
			Log.d(TAG, "��ʼע��Ĭ���˻�");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = register(FsSettings.getUsername(), FsSettings.getPassword(), permission);
			Log.d(TAG, "����ע��Ĭ���˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰĬ���˻���Ϣ����");
		}
		
		// ������Ա�˻�
		if (accountsSp.getBoolean(AdminUsername, false)) {
			Log.d(TAG, "��ǰ����Ա�˻���Ϣ������");
			Log.d(TAG, "��ʼע�����Ա�˻�");
			int permission = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN;
			boolean registerResult = register(AdminUsername, AdminPassword, permission);
			Log.d(TAG, "����ע�����Ա�˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰ����Ա�˻���Ϣ����");
		}
	}
	
	public boolean setUpload(String path) {
		if (path == null || path.equals("")) {
			Log.e(TAG, "��Ч���ϴ�·��");
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
	
	// �����жϵ�ǰ�û��ܷ�Զ�Ӧ�ļ����в���
	public static boolean canRead(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_READ_ALL) != PERMISSION_NONE;
	}
	
	public static boolean canWrite(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_WRITE_ALL) != PERMISSION_NONE; 
	}
	
	// ��������ִ��
	public boolean canExecute() {
		return false;
	}
	
	public boolean isLoggedIn() {
		return userAuthenticated;
	}
	
	public boolean isAnonymous() {
		return mUserName.equals(AnonymousUsername);
	}
	
	public boolean isAdministrator() {
		return mUserName.equals(AdminUsername);
	}
	
    public String getUsername() {
        return mUserName;
    }

    public String getPassword() {
    	return mPassword;
    }
    
    public boolean isDefaultAccount() {
    	return mUserName.equals(FsSettings.getUsername());
    }
    
    public void setUsername(String username) {
        mUserName = username;
    }

    public void setPassword(String password) {
    	mPassword = password;
    }
    
    public void setAttemptPassword(String attemptPassword) {
    	mAttemptPassword = attemptPassword;
    }
    
    private void setPermission(int permission) {
    	mPermission = permission;
    }
    
    public int getPermission() {
    	return mPermission;
    }
}
