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

public class Account {
	private static final String TAG = Account.class.getSimpleName();
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";
    private String mUserName = null;
    private String mPassword = null;
    private String mAttemptPassword = null;
    private boolean userAuthenticated = false;
    public int authFails = 0;
    public static final int PERMISSION_READ = 0040;
    public static final int PERMISSION_WRITE = 0020;
    // execute��Զ������
    public static final int PERMISSION_EXECUTE = 0010;
    
    public static final String USER_DEFAULT = "default_username";
    
    // ����дȨ����˵���ǿ���ʹ�����е�����s
    // TODO ��Ҫ�˽����һλ��0��ʾ��Ȩ�޺���
    // Ĭ��ֵӵ�ж�Ȩ��
    private int permission = 0644;
    // ����������¼�˻���Ȩ��
    private static final int PERMISSION_ANONYMOUS = 0644;
    
    private Account(String username, String password) {
    	// TODO username,mPassword���п�����null
    	setUsername(username);
    	this.mPassword = password;
    }
    
    // ��⵱ǰ�Ƿ��ǵ�¼�ɹ���
    // TODO �ؼ������ݿ����޷�����
    public boolean authAttempt() {
		if (!mUserName.equals(AnonymousUsername) && mAttemptPassword != null && mAttemptPassword.equals(mPassword)) {
			userAuthenticated = true;
		} else if (FsSettings.allowAnoymous() && mUserName.equals(AnonymousUsername)) {
			Log.i(TAG, "Guest logged in with password: " + mAttemptPassword);
			userAuthenticated = true;
		} else {
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
		SharedPreferences anonymousSp = context.getSharedPreferences(AnonymousUsername, Context.MODE_PRIVATE);
		// �����˻���Ϣû�����ú�
		if (anonymousSp.getString("password", "").equals("")) {
			Editor editor = anonymousSp.edit();
			editor.putString("password", AnonymousPassword);
			editor.commit();
		}
		
		// ��Ҫ����Ĭ���ʺ�
		// TODO �����Ĭ���ʺſ��ܷ����ı�����
		SharedPreferences defaultSp = context.getSharedPreferences(FsSettings.getUsername(), Context.MODE_PRIVATE);
		if (defaultSp.getString("password", "").equals("")) {
			Editor editor = defaultSp.edit();
			editor.putString("password", FsSettings.getPassword());
			editor.commit();
		}
		
		// TODO ��Ҫ���˻��������������
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
			String password = sp.getString("password", "");
			if (password.equals("")) {
				return new Account(username, password);
			} else {
				return null;// �˻�������
			}
		} else {
			return null;
		}
	}

	/**
	 * ע���û����û������ܺ�Ĭ���û�����ͻ������Ҳ���ܺ������û�����ͻ
	 * @param username
	 * @param password
	 * @return
	 */
	private static boolean register(String username, String password) {
		if (!username.equals(FsSettings.getUsername()) && !username.equals(AnonymousUsername)) {
			Context context = MShareApp.getAppContext();
			SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
			if (sp.getString("password", "").equals("")) {
				Editor editor = sp.edit();
				editor.putString(username, password);
				return editor.commit();
			} else {
				Log.e(TAG, "username has already existed");
				return false;
			}
		} else {
			Log.e(TAG, "username has already existed");
			return false;
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
