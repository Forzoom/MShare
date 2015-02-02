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
import android.util.Log;

public class Account {
	private static final String TAG = Account.class.getSimpleName();
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";
    private String mUserName = null;
    private String mPassword = null;
    private String mAttemptPassword = null;
    private boolean userAuthenticated = false;
    private int authFails = 0;
    private static HashMap<String, String> accounts = new HashMap<String, String>(); 
    
    private Account(String username, String password) {
    	// TODO username,mPassword仍有可能是null
    	setUsername(username);
    	this.mPassword = password;
    }
    
    // 检测当前是否是登录成功了
    // TODO 关键是内容可能无法返回
    public boolean authAttempt() {
    	if (getUsername() != null && getPassword() != null) {
    		// 当前账户是存在的，现在只要检测密码是否正确就可以了
    		if (!mUserName.equals(AnonymousUsername)) {
    			if (mAttemptPassword == null && mAttemptPassword.equals(mPassword)) {
    				return true;
    			} else {
    				return false;
    			}
    		} else if (FsSettings.allowAnoymous() && mUserName.equals(AnonymousUsername)) {
    			Log.i(TAG, "Guest logged in with password: " + mAttemptPassword);
    			return true;
    		} else {
    			return false;
    		}   
    	} else {
    		return false;
    	}
    }
    
	public static Account getInstance(String username) {
		if (username != null) {
			Context context = MShareApp.getAppContext();
			SharedPreferences sp = context.getSharedPreferences("accounts", Context.MODE_PRIVATE);
			String password = sp.getString(username, "");
			if (password != "") {
				return new Account(username, password);
			} else {
				return null;// 账户不存在
			}
		} else {
			return null;
		}
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
