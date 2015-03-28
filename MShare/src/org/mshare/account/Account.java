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

package org.mshare.account;

import java.io.File;

import org.mshare.file.share.SharedLinkStorage;
import org.mshare.file.share.SharedLinkSystem;
import org.mshare.file.share.SharedLinkSystem.Permission;
import org.mshare.preference.ServerSettings;

import android.util.Log;

/**
 * TODO 使用HASH值作为SP的键
 * 管理员账户中的文件树是有必要存在的，用以判断持久化内容是否正确
 * 当管理员账户的内容持久化的时候，需要向所有的文件树中添加内容，需要这样做
 * 原本打算存在一个存放所有Account是否存在的信息，是否有必要有这样的信息呢？
 * 我们有必要对所有的账户进行列表吗？
 * 许多账户可能都是临时的，很有可能，服务器的存在是持久的，但是文件共享因为在手机端，可能更多并不是在一个长时间内容进行使用
 * 将auth转变为使用token
 * 
 * 因为Session没有办法获得Account对象，所以canRead和canWrite对于filePermission的判断在Token中执行
 * 
 * TODO 需要考虑修改用户名的事情
 * TODO 考虑是否将Account作为AccountFactory的内部类，因为Account不应该能够被随便new出来，在内部类中不知道能不能保证其不被new出来
 * TODO 考虑将文件树的修改操作都在Account中复制一份，并且加上notify,只需要对add和delete文件树进行notify,对于persist并不需要notify
 * TODO 考虑管理员账户中的不一样
 * TODO 考虑将notify直接分出来调用，但是notifier不应该在SharedLinkSystem中被调用了，或者将Notifier改为静态函数
 * 在Account中设置addSharedPath和persist并不合适
 * 可能要将system中的内容移动到Account中,尝试一下，如果全部都放在Account中，会导致耦合度高
 * 
 * 需要调用{@link #prepare()}才能让文件树生成
 * 
 * @author HM
 *
 */
public abstract class Account {
	private static final String TAG = Account.class.getSimpleName();
	
	// 当前账户的用户名和密码
    private String mUserName = null;
    private String mPassword = null;
    
    // 共享层文件树
    private SharedLinkSystem mSharedLinkSystem;
    // 共享层存储
    private SharedLinkStorage mSharedLinkStorage;
    
	// 存放在SharedPreferences中的键值
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PERMISSION = "permission";
    public static final String KEY_UPLOAD = "upload";
    
    /**
     * 使用当前Account的Session数量
     */
    public int tokenCount;
    
    // TODO 考虑unprepared函数，用于将文件树释放
    public Account(String username, String password) {
    	this.mUserName = username != null ? username : "";
    	this.mPassword = password;
    	// TODO 调用USER的时候文件树就生成了，改为调用
    }
    
    /**
     * 在准备文件树SharedLinkSystem的同时
     * 向文件树中添加一些内容作为文件树中的已有内容
     * @see #prepare()
     * @param storage storage中的内容都将被添加到文件树中
     * @param filePermission 所添加的SharedLink所拥有的权限
     */
    public void prepare(SharedLinkStorage storage, int filePermission) {
    	prepare();
    	// TODO 文件权限不对
    	getSystem().load(storage, filePermission);
    }
    
    /**
     * 创建存储和文件树
     */
    public void prepare() {
    	if (mSharedLinkStorage == null) {
    		Log.d(TAG, "create SharedLinkStorage!");
    		mSharedLinkStorage = SharedLinkStorage.getStorage(mUserName);
    	}
    	
    	if (mSharedLinkSystem == null) {
    		Log.d(TAG, "create SharedLinkSystem!");
    		mSharedLinkSystem = SharedLinkSystem.getInstance(this);
    	}
    	if (!mSharedLinkSystem.isPrepared()) {
    		Log.d(TAG, "make SharedLinkSystem prepared!");
    		mSharedLinkSystem.prepare();
    	}
    }
    
    /**
     * 修正当前账户的上传路径
     * @param path
     * @return
     */
	public boolean setUpload(String path) {
		if (path == null || path.equals("")) {
			Log.e(TAG, "invalid upload path");
			return false;
		}

		return mSharedLinkStorage.set(KEY_UPLOAD, path);
	}
	
	/**
	 * 当没有指定的upload路径的时候，将返回账户对应的名字，即getUsername
	 * @return
	 */
	public String getUpload() {
		return getStorage().get(KEY_UPLOAD, ServerSettings.getUpload() + File.separator + getUsername());
	}
	
	/**
	 * Account对象并不是可以随意得到的，所以该方法是检测权限的核心方法
	 * 判断当前用户是否拥有读相关权限，用于执行读相关的FtpCmd
	 * @param accountPermission
	 * @param filePermission
	 * @return
	 */
	public static boolean canRead(int accountPermission, int filePermission) {
		return (accountPermission & filePermission & Permission.PERMISSION_READ_ALL) != Permission.PERMISSION_NONE;
	}
	
	/**
	 * Account对象并不是可以随意得到的，所以该方法是检测权限的核心方法
	 * 判断当前用户是否拥有任意的写权限，用于执行写相关的FtpCmd
	 * @param accountPermission
	 * @param filePermission
	 * @return
	 */
	public static boolean canWrite(int accountPermission, int filePermission) {
		return (accountPermission & filePermission & Permission.PERMISSION_WRITE_ALL) != Permission.PERMISSION_NONE; 
	}
	
	/**
	 * 不可以执行
	 * TODO 是否以后可以是在线浏览的权限
	 * @return
	 */
	public static boolean canExecute() {
		return false;
	}
	
	/**
	 * 判断当前的账户是否是匿名账户
	 * @return
	 */
	public abstract boolean isGuest();
	
	/**
	 * 判断当前账户是否为管理员账户
	 * @return
	 */
	public abstract boolean isAdministrator();
	
	/**
	 * 判断当前账户是否为普通用户账户
	 * @return
	 */
	public abstract boolean isUser();
	
	/**
	 * TODO 保证返回的内容不为null
	 * @return 用户名，不为null
	 */
    public String getUsername() {
        return mUserName;
    }

    /**
     * TODO 保证返回的内容不为null
	 * @return 密码，不为null
     */
    public String getPassword() {
    	return mPassword;
    }
    
    /**
     * 默认返回{@link Permission#PERMISSION_NONE}
     * @return
     */
    public int getPermission() {
    	return Permission.PERMISSION_NONE;
    }
    
    /**
     * Account拥有SharedLinkSystem
     * 使用public是因为很多的地方，例如FtpCmd中都需要对SharedLinkSystem进行操作
     * @return 当没有调用{@link #prepare()}时，文件树还没有生成，返回null
     */
    public SharedLinkSystem getSystem() {
    	return mSharedLinkSystem;
    }
    
    public SharedLinkStorage getStorage() {
    	return mSharedLinkStorage;
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
    
    /**
     * 添加Token的个数
     */
    public void registerToken() {
    	tokenCount++;
    }
    
    /**
     * 减少Token的个数
     */
    public void unregisterToken() {
    	tokenCount--;
    }
    
    public int getTokenCount() {
    	return tokenCount;
    }
}
