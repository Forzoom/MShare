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

import java.util.HashMap;
import java.util.Map;

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
    private String mUserName = null;
    private String mPassword = null;
    private String mAttemptPassword = null;
    private boolean userAuthenticated = false;
    public int authFails = 0;
    // ��������Ϊ�ʺŵ�Ȩ�ޣ�ӳ�����û��ļ���
    public static final int PERMISSION_READ = 0x1;
    public static final int PERMISSION_WRITE = 0x2;
    public static final int PERMISSION_EXECUTE = 0x4;// execute��Զ������
    
    public static final String USER_DEFAULT = "default_username";
    
    // Ĭ��ֵӵ�ж�Ȩ��
    private int permission = PERMISSION_READ; 
    // ����������¼�˻���Ȩ��
    private static final int PERMISSION_ANONYMOUS = 0644;
    
    public static final String KEY_ACCOUNT_INFO = "accounts";
    
    private Account(String username, String password) {
    	// TODO username,mPassword���п�����null
    	setUsername(username);
    	this.mPassword = password;
    }
    
    // ��⵱ǰ�Ƿ��ǵ�¼�ɹ���
    // TODO �ؼ������ݿ����޷�����
    public boolean authAttempt() {
		if (!mUserName.equals(AnonymousUsername) && mAttemptPassword != null && mAttemptPassword.equals(mPassword)) {
			Log.d(TAG, "ʹ�÷������˻����Ե�¼");
			userAuthenticated = true;
		} else if (FsSettings.allowAnoymous() && mUserName.equals(AnonymousUsername)) {
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
    
    /**
     * �ھ�̬״̬�£�SharedPreferences���޸Ŀ��ܲ��ᱻ��Ӧ��FTP�Ự�У���Ҫ���Ƽ������Ļص�
     * @param account
     * @return
     */
    public static SharedPreferences getDefaultSharedPreferences() {
    	Context context = MShareApp.getAppContext();
    	return context.getSharedPreferences(FsSettings.getUsername(), Context.MODE_PRIVATE);
    }
    
    // TODO ��Ҫ�����޸��û���������
	public static Account getInstance(String username) {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
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
			String password = sp.getString("password", "");
			if (!password.equals("")) {
				Log.d(TAG, "�����ȷ���˻�");
				return new Account(username, password);
			} else {
				Log.e(TAG, "��Ҫ���˻����벻����");
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
	 * ע���û����û������ܺ�Ĭ���û�����ͻ������Ҳ���ܺ������û�����ͻ
	 * @param username
	 * @param password
	 * @return
	 */
	private static boolean register(String username, String password) {
		Context context = MShareApp.getAppContext();
		if (!username.equals(FsSettings.getUsername()) && !username.equals(AnonymousUsername)) {
			// �����û��ļ�
			SharedPreferences userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
			if (userSp.getString("password", "").equals("")) {
				Editor editor = userSp.edit();
				editor.putString(username, password);
				if (editor.commit()) {
					Log.d(TAG, "Register Success:success");
					// �����˻��ļ����������
					SharedPreferences accountsSp = context.getSharedPreferences(KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
					Editor accountEditor = accountsSp.edit();
					accountEditor.putBoolean(username, true);
					if (accountEditor.commit()) {
						// TODO ��Ҫ������ϸ�����ݣ��ͼ��Ĳ�������
						Log.d(TAG, "Register Success:success");
						return true;
					} else {
						Log.d(TAG, "Register Fail:Fail");
						return false;
					}
				} else {
					Log.d(TAG, "Register Fail:create sharedPreferences fail");
					return false;
				}
			} else {
				Log.e(TAG, "Register Fail:username has already existed");
				return false;
			}
		} else {
			// TODO 
			Log.e(TAG, "try default account and anonymous account");
			return false;
		}
	}
	
	public static void checkDefaultAndAnonymousAccount() {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
		if (accountsSp.getBoolean(AnonymousUsername, false) == false) {
			
			Log.d(TAG, "��ǰ�����˻���Ϣ������");
			Log.d(TAG, "��ʼע�������˻�");
			SharedPreferences anonymousSp = context.getSharedPreferences(AnonymousUsername, Context.MODE_PRIVATE);
			// �����˻���Ϣû�����ú�
			if (anonymousSp.getString("password", "").equals("")) {
				Editor editor = anonymousSp.edit();
				editor.putString("password", AnonymousPassword);
				if (editor.commit()) {
					Log.d(TAG, "�����˻��ļ��ɹ�");
				} else {
					Log.d(TAG, "�����˻��ļ�ʧ��");
				}
			} else {
				Log.d(TAG, "��ǰ�˻��������� :" + anonymousSp.getString("password", ""));
			}
			// TODO �������˻��ļ�ʧ�ܵ�ʱ������Ĳ�����Ӧ��ִ��
			Editor accountEditor = accountsSp.edit();
			accountEditor.putBoolean(AnonymousUsername, true);
			boolean registerResult = accountEditor.commit();
			Log.d(TAG, "����ע�������˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰ�����˻���Ϣ����");
		}
		// ���Ĭ�˻�
		if (accountsSp.getBoolean(FsSettings.getUsername(), false) == false) {
			
			Log.d(TAG, "��ǰĬ���˻���Ϣ������");
			Log.d(TAG, "��ʼע��Ĭ���˻�");
			SharedPreferences defaultSp = context.getSharedPreferences(FsSettings.getUsername(), Context.MODE_PRIVATE);
			// �����˻���Ϣû�����ú�
			if (defaultSp.getString("password", "").equals("")) {
				Editor editor = defaultSp.edit();
				editor.putString("password", FsSettings.getPassword());
				if (editor.commit()) {
					Log.d(TAG, "�����˻��ļ��ɹ�");
				} else {
					Log.d(TAG, "�����˻��ļ�ʧ��");
				}
			} else {
				Log.d(TAG, "��ǰ�˻��������� :" + defaultSp.getString("password", ""));
			}
			// TODO �������˻��ļ�ʧ�ܵ�ʱ������Ĳ�����Ӧ��ִ��
			Editor accountEditor = accountsSp.edit();
			accountEditor.putBoolean(FsSettings.getUsername(), true);
			boolean registerResult = accountEditor.commit();
			Log.d(TAG, "����ע��Ĭ���˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰĬ���˻���Ϣ����");
		}
	}
	
	// �����˶����Զ�����
	public boolean canRead() {
		return (permission & PERMISSION_READ) == PERMISSION_READ;
	}
	
	public boolean canWrite() {
		return (permission & PERMISSION_WRITE) == PERMISSION_WRITE; 
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
	
    public String getUsername() {
        return mUserName;
    }

    public String getPassword() {
    	return mPassword;
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
}
