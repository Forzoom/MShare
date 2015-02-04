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
    // execute永远不开放
    public static final int PERMISSION_EXECUTE = 0010;
    
    public static final String USER_DEFAULT = "default_username";
    
    // 对于写权限来说就是可以使用所有的命令s
    // TODO 需要了解最后一位的0表示的权限含义
    // 默认值拥有读权限
    private int permission = 0644;
    // 对于匿名登录账户的权限
    private static final int PERMISSION_ANONYMOUS = 0644;
    
    private Account(String username, String password) {
    	// TODO username,mPassword仍有可能是null
    	setUsername(username);
    	this.mPassword = password;
    }
    
    // 检测当前是否是登录成功了
    // TODO 关键是内容可能无法返回
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
    
    // TODO 需要考虑修改用户名的事情
	public static Account getInstance(String username) {
		Context context = MShareApp.getAppContext();
		SharedPreferences anonymousSp = context.getSharedPreferences(AnonymousUsername, Context.MODE_PRIVATE);
		// 匿名账户信息没有设置好
		if (anonymousSp.getString("password", "").equals("")) {
			Editor editor = anonymousSp.edit();
			editor.putString("password", AnonymousPassword);
			editor.commit();
		}
		
		// 需要设置默认帐号
		// TODO 这里的默认帐号可能发生改变的情况
		SharedPreferences defaultSp = context.getSharedPreferences(FsSettings.getUsername(), Context.MODE_PRIVATE);
		if (defaultSp.getString("password", "").equals("")) {
			Editor editor = defaultSp.edit();
			editor.putString("password", FsSettings.getPassword());
			editor.commit();
		}
		
		// TODO 需要对账户名做更多的限制
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
			String password = sp.getString("password", "");
			if (password.equals("")) {
				return new Account(username, password);
			} else {
				return null;// 账户不存在
			}
		} else {
			return null;
		}
	}

	/**
	 * 注册用户，用户名不能和默认用户名冲突，而且也不能和匿名用户名冲突
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
	
	// 所有人都可以读内容
	public boolean canRead() {
		return (permission & PERMISSION_READ) == PERMISSION_READ;
	}
	
	public boolean canWrite() {
		return (permission & PERMISSION_WRITE) == PERMISSION_WRITE; 
	}
	// 都不可以执行
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
