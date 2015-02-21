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
import org.mshare.ftp.server.FsService.SessionNotifier;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * 原本打算存在一个存放所有Account是否存在的信息，是否有必要有这样的信息呢？
 * 我们有必要对所有的账户进行列表吗？
 * 许多账户可能都是临时的，很有可能，服务器的存在是持久的，但是文件共享因为在手机端，可能更多并不是在一个长时间内容进行使用
 * TODO 需要考虑修改用户名的事情
 * TODO 考虑是否将Account作为AccountFactory的内部类，因为Account不应该能够被随便new出来，在内部类中不知道能不能保证其不被new出来
 * TODO 考虑将文件树的修改操作都在Account中复制一份，并且加上notify,只需要对add和delete文件树进行notify,对于persist并不需要notify
 * TODO 考虑管理员账户中的不一样
 * TODO 考虑将notify直接分出来调用，但是notifier不应该在SharedLinkSystem中被调用了，或者将Notifier改为静态函数
 * 在Account中设置addSharedPath和persist并不合适
 * 可能要将system中的内容移动到Account中,尝试一下，如果全部都放在Account中，会导致耦合度高
 * @author HM
 *
 */
public class Account {
	private static final String TAG = Account.class.getSimpleName();
	
	// 当前账户的用户名和密码
    private String mUserName = null;
    private String mPassword = null;
    // 用户登录尝试的密码
    private String mAttemptPassword = null;
    // 用户是否登录成功
    private boolean userAuthenticated = false;
    // 登录尝试失败的次数
    // TODO 需要将最大次数放在这里？
    public int authFails = 0;
    
    private SharedLinkSystem mSharedLinkSystem;
    // TODO 不知道是否需要修改，这个有用吗
    public static final String USER_DEFAULT = "default_username";
    
    // 默认值拥有读权限,为测试添加写权限
    private int mPermission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    // 对于匿名登录账户的权限,暂时拥有写权限
    private static final int PERMISSION_GUEST = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;

	// 存放在SharedPreferences中的键值
    // TODO 使用public合适吗
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PERMISSION = "permission";
    /**
     * 在user_sp中保存上传路径的键值
     */
    public static final String KEY_UPLOAD = "upload";

    /**
     * 
     */
    private SessionNotifier mNotifier;
    
    /**
     * 总共有多少个Session在使用当前的Account
     */
    public int sessionCount;
    
    // TODO 考虑将Account的内容移动到AccountFactory中，多例模式是怎么弄的？
    public Account(String username, String password) {
    	// TODO username,mPassword仍有可能是null
    	this.mUserName = username;
    	this.mPassword = password;
    	mSharedLinkSystem = new SharedLinkSystem(this);
    }
    
    /**
     * 检测当前使用 {@link #mAttemptPassword} 是否登录成功
     * @return
     */
    public boolean authAttempt() {
		if (!mUserName.equals(AccountFactory.AnonymousUsername) && mAttemptPassword != null && mAttemptPassword.equals(mPassword)) {
			Log.d(TAG, "使用非匿名账户尝试登录");
			userAuthenticated = true;
		} else if (FsSettings.allowAnoymous() && mUserName.equals(AccountFactory.AnonymousUsername)) {
			// 设置权限为匿名账户权限
			mPermission = PERMISSION_GUEST;
			Log.i(TAG, "Guest logged in with password: " + mAttemptPassword);
			userAuthenticated = true;
		} else {
			Log.d(TAG, "尝试登录失败");
			authFails++;
			userAuthenticated = false;
		}
		return userAuthenticated;
    }
    
    /**
     * 获得账户对应的SharedPreferences
     * @return
     */
    public SharedPreferences getSharedPreferences() {
    	Context context = MShareApp.getAppContext();
    	return context.getSharedPreferences(mUserName, Context.MODE_PRIVATE);
    }
    
    /**
     * 修正当前账户 
     * @param path
     * @return
     */
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
	
	/**
	 * 判断当前用户是否拥有任意的读权限，用于执行读相关的FtpCmd
	 * @param account
	 * @param filePermission
	 * @return
	 */
	public static boolean canRead(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_READ_ALL) != Permission.PERMISSION_NONE;
	}
	/**
	 * 判断当前用户是否拥有任意的写权限，用于执行写相关的FtpCmd
	 * @param account
	 * @param filePermission
	 * @return
	 */
	public static boolean canWrite(Account account, int filePermission) {
		return (account.getPermission() & filePermission & Permission.PERMISSION_WRITE_ALL) != Permission.PERMISSION_NONE; 
	}
	
	// TODO 需要函数给SharedLinkSystem调用，当SharedLinkSystem出现变化时，可以通知所有的SessionThread
	// TODO 当所有获得Account的SessionThread都退出的时候，就会将Account从allAccounts中删除
	// 都不可以执行
	public boolean canExecute() {
		return false;
	}
	
	public boolean isLoggedIn() {
		return userAuthenticated;
	}
	
	public boolean isAnonymous() {
		return mUserName.equals(AccountFactory.AnonymousUsername);
	}
	
	public boolean isAdministrator() {
		return mUserName.equals(AccountFactory.AdminUsername);
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
    
    public void setAttemptPassword(String attemptPassword) {
    	mAttemptPassword = attemptPassword;
    }
    
    private void setPermission(int permission) {
    	mPermission = permission;
    }
    
    public int getPermission() {
    	return mPermission;
    }
    
    /**
     * Account拥有SharedLinkSystem
     * 使用public是因为很多的地方，例如FtpCmd中都需要对SharedLinkSystem进行操作
     * @return
     */
    public SharedLinkSystem getSystem() {
    	return mSharedLinkSystem;
    }
    
    /**
     * 这样真的好吗
     * @return
     */
    public SessionNotifier getNotifier() {
    	return mNotifier;
    }
    
    /**
     * 仅仅能用于与Account进行比较
     */
    @Override
    public boolean equals(Object o) {
    	if (!(o instanceof Account)) {
    		return false;
    	}
    	Account account = (Account)o;
    	String username = account.getUsername();
    	String password = account.getPassword();
    	// TODO username和password会是null吗
    	if (username.equals(mUserName) && password.equals(mPassword)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public int getSessionCount() {
    	return sessionCount;
    }
}
