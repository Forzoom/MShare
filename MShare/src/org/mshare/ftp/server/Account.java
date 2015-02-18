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
 * 原本打算存在一个存放所有Account是否存在的信息，是否有必要有这样的信息呢？
 * 我们有必要对所有的账户进行列表吗？
 * 许多账户可能都是临时的，很有可能，服务器的存在是持久的，但是文件共享因为在手机端，可能更多并不是在一个长时间内容进行使用
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
    
    // 默认值拥有读权限,为测试添加写权限
    private int mPermission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    // 对于匿名登录账户的权限
    private static final int PERMISSION_ANONYMOUS = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    
    public static final String SP_KEY_ACCOUNT_INFO = "accounts";
    /**
     * 在sp中保存上传路径的位置
     */
    public static final String KEY_UPLOAD = "upload";
    
    public static Account adminAccount = new Account(Account.AdminUsername, Account.AdminPassword);
    
    private Account(String username, String password) {
    	// TODO username,mPassword仍有可能是null
    	setUsername(username);
    	this.mPassword = password;
    }
    
    /**
     * 检测当前是否是登录成功
     * TODO 考虑将账户权限在哪里设置
     * @return
     */
    public boolean authAttempt() {
		if (!mUserName.equals(AnonymousUsername) && mAttemptPassword != null && mAttemptPassword.equals(mPassword)) {
			Log.d(TAG, "使用非匿名账户尝试登录");
			userAuthenticated = true;
		} else if (FsSettings.allowAnoymous() && mUserName.equals(AnonymousUsername)) {
			// 设置权限为匿名账户权限
			mPermission = PERMISSION_ANONYMOUS;
			Log.i(TAG, "Guest logged in with password: " + mAttemptPassword);
			userAuthenticated = true;
		} else {
			Log.d(TAG, "尝试登录失败");
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
		SharedPreferences accountsSp = context.getSharedPreferences(SP_KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// 检测account中的内容
		if (accountsSp.getBoolean(username, false) == false) {
			Log.e(TAG, "account info 中没有该帐号的内容");
			return null;
		}
		
		if (username.equals(AnonymousUsername)) {
			Log.d(TAG, "当前匿名账户尝试登录");
		} else if (username.equals(FsSettings.getUsername())) {
			Log.d(TAG, "当前默认账户尝试登录");
		}
		
		// TODO 需要对账户名做更多的限制
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
			String password = sp.getString(KEY_PASSWORD, "");
			if (!password.equals("")) {
				Log.d(TAG, "获得正确的账户");
				return new Account(username, password);
			} else {
				Log.e(TAG, "需要的账户密码不存在" + password);
				return null;// 账户不存在
			}
		} else {
			// 用户名不正确
			Log.e(TAG, "请求的用户名不合法");
			return null;
		}
	}

	/**
	 * 需要在check中使用register来创建文件
	 * 使用之前，应当检测，所注册用户的用户名不能和默认用户、匿名用户以及管理员账户冲突
	 * 允许通过该函数注册和生成默认账户和匿名账户
	 * 
	 * @param username
	 * @param password
	 * @param mPermission 
	 * @return
	 */
	private static boolean register(String username, String password, int permission) {
		Log.d(TAG, "开始注册账户,用户名:" + username + " 密码:" + password);
		boolean createUserSuccess = false;
		Context context = MShareApp.getAppContext();
		// 创建用户文件
		SharedPreferences userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		if (userSp.getString(KEY_PASSWORD, "").equals("")) {
			Editor editor = userSp.edit();
			editor.putString(KEY_PASSWORD, password);
			// 当没有指定权限时，将使用普通账户的读写权限
			editor.putInt(KEY_PERMISSION, permission == PERMISSION_NONE ? Permission.PERMISSION_READ | Permission.PERMISSION_WRITE: permission);
			createUserSuccess = editor.commit();
		} else {
			Log.e(TAG, "Register Fail:username has already existed");
			return false;
		}
		// 当创建用户文件失败
		if (!createUserSuccess) {
			Log.e(TAG, "Register Fail:create sharedPreferences fail");
			return false;
		}
		
		// 向accountInfo中添加内容
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
	 * 当默认账户和匿名账户不存在的时候，使用register函数注册,检测管理员账户
	 * 并添加了适当的权限
	 */
	public static void checkReservedAccount() {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(SP_KEY_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// 检测匿名账户
		if (accountsSp.getBoolean(AnonymousUsername, false) == false) {
			
			Log.d(TAG, "当前匿名账户信息不存在");
			Log.d(TAG, "开始注册匿名账户");
			int permission = Permission.PERMISSION_READ_GUEST;
			boolean registerResult = register(AnonymousUsername, AnonymousPassword, permission);
			Log.d(TAG, "结束注册匿名账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前匿名账户信息存在");
		}
		
		// 检测默账户
		if (accountsSp.getBoolean(FsSettings.getUsername(), false) == false) {
			
			Log.d(TAG, "当前默认账户信息不存在");
			Log.d(TAG, "开始注册默认账户");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = register(FsSettings.getUsername(), FsSettings.getPassword(), permission);
			Log.d(TAG, "结束注册默认账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前默认账户信息存在");
		}
		
		// 检测管理员账户
		if (accountsSp.getBoolean(AdminUsername, false)) {
			Log.d(TAG, "当前管理员账户信息不存在");
			Log.d(TAG, "开始注册管理员账户");
			int permission = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN;
			boolean registerResult = register(AdminUsername, AdminPassword, permission);
			Log.d(TAG, "结束注册管理员账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前管理员账户信息存在");
		}
	}
	
	public boolean setUpload(String path) {
		if (path == null || path.equals("")) {
			Log.e(TAG, "无效的上传路径");
			return false;
		}
		
		SharedPreferences sp = getSharedPreferences();
		Editor editor = sp.edit();
		editor.putString(KEY_UPLOAD, path);
		return editor.commit();
	}
	
	/**
	 * 当没有指定的upload路径的时候，将返回账户对应的名字，即getUsername
	 * @return
	 */
	public String getUpload() {
		SharedPreferences sp = getSharedPreferences();
		return sp.getString(KEY_UPLOAD, FsSettings.getUpload() + File.separator + getUsername());
	}
	
	// 用于判断当前用户能否对对应文件进行操作
	public static boolean canRead(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_READ_ALL) != PERMISSION_NONE;
	}
	
	public static boolean canWrite(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_WRITE_ALL) != PERMISSION_NONE; 
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
