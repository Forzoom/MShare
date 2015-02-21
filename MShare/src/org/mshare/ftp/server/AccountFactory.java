package org.mshare.ftp.server;

import java.util.HashMap;

import org.mshare.file.SharedLinkSystem.Permission;
import org.mshare.ftp.server.FsService.SessionNotifier;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * TODO AccountFactory是否需要都是静态方法,因为连allAccounts都是static的
 * TODO 客户端那边会发送QUIT消息吗，如果发送的话，就可以判断Session数量，可是如果客户意外断线了呢？
 * TODO 该如何判断一个Account对象需要回收呢？在SessionThread中，没过一段时间，就向客户端发送一个消息，以验证当前客户端仍在线
 * @author HM
 *
 */
public class AccountFactory {
	private static final String TAG = AccountFactory.class.getSimpleName();
	
	// 匿名账户的用户名和密码
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";
	// 管理员账户的用户名和密码
	public static final String AdminUsername = "admin";
	public static final String AdminPassword = "admin";
	
    public static final String SP_ACCOUNT_INFO = "accounts";
	
	// 所有的账户内容,新的账户从这里获得，每个sessionThread仅仅是获得对应其中的引用
    private static HashMap<String, Account> allAccounts = new HashMap<String, Account>();
    
    // 默认的管理员账户
    // TODO 管理员账户只能在这里
    public static Account adminAccount = new Account(AccountFactory.AdminUsername, AccountFactory.AdminPassword);
    // 用于通知其他的Session
    // TODO 使用static是否好，在多个线程中，将会使用同一个Notifier,这样会不会有什么错误,两个线程同时调用一个方法会不会有问题
    private static SessionNotifier mNotifier;
    
	/**
     * 获得对应的Accouont，只有存在的username才能获得Account的对象
     * 获得的Account对象用于验证密码是否正确
     * 对于同一个账户，获得的Account是同一个Account对象的引用
     * TODO 考虑使用public是否合适
     * @param username
     * @return
     */
	public static Account getAccount(String username) {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// 检测account中的内容
		if (accountsSp.getBoolean(username, false) == false) {
			Log.e(TAG, "帐号 " + username + " 不存在");
			return null;
		}

		if (username.equals(AnonymousUsername)) {
			Log.d(TAG, "当前匿名账户尝试登录");
		} else if (username.equals(FsSettings.getUsername())) {
			Log.d(TAG, "当前默认账户尝试登录");
		}
		
		// TODO 需要对账户名做更多的限制
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			if (!allAccounts.containsKey(username)) {
				// allAccounts中不存在，就从SharedPreferences中获得
				SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
				String password = sp.getString(Account.KEY_PASSWORD, "");
				if (!password.equals("")) {
					Log.d(TAG, "获得正确的账户");
					allAccounts.put(username, new Account(username, password));
				} else {
					Log.e(TAG, "需要的账户密码不存在，password:" + password);
				}
			}
			
			return allAccounts.get(username);
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
	protected static boolean register(String username, String password, int permission) {
		Log.d(TAG, "开始注册账户,用户名:" + username + " 密码:" + password);
		boolean createUserSuccess = false;
		Context context = MShareApp.getAppContext();
		// 创建用户文件
		SharedPreferences userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		if (userSp.getString(Account.KEY_PASSWORD, "").equals("")) {
			Editor editor = userSp.edit();
			editor.putString(Account.KEY_PASSWORD, password);
			// 当没有指定权限时，将使用普通账户的读写权限
			editor.putInt(Account.KEY_PERMISSION, permission == Permission.PERMISSION_NONE ? Permission.PERMISSION_READ | Permission.PERMISSION_WRITE : permission);
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
		SharedPreferences accountsSp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
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
		// 检测匿名账户
		if (!AccountFactory.isAccountExists(context, AccountFactory.AnonymousUsername)) {
			
			Log.d(TAG, "当前匿名账户信息不存在");
			Log.d(TAG, "开始注册匿名账户");
			int permission = Permission.PERMISSION_READ_GUEST;
			boolean registerResult = AccountFactory.register(AccountFactory.AnonymousUsername, AccountFactory.AnonymousPassword, permission);
			Log.d(TAG, "结束注册匿名账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前匿名账户信息存在");
		}
		
		// 检测默认账户
		if (!AccountFactory.isAccountExists(context, FsSettings.getUsername())) {
			
			Log.d(TAG, "当前默认账户信息不存在");
			Log.d(TAG, "开始注册默认账户");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = AccountFactory.register(FsSettings.getUsername(), FsSettings.getPassword(), permission);
			Log.d(TAG, "结束注册默认账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前默认账户信息存在");
		}
		
		// 检测管理员账户
		if (!AccountFactory.isAccountExists(context, AccountFactory.AdminUsername)) {
			Log.d(TAG, "当前管理员账户信息不存在");
			Log.d(TAG, "开始注册管理员账户");
			int permission = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN;
			boolean registerResult = AccountFactory.register(AccountFactory.AdminUsername, AccountFactory.AdminPassword, permission);
			Log.d(TAG, "结束注册管理员账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前管理员账户信息存在");
		}
	}
	
	/**
	 * Account存在于allAccounts中，Account中有文件树，可能会占用内存，所以当不再使用的时候，将Account去除？
	 * TODO 真的需要recycle这样的函数吗？
	 */
	public static boolean recycleAccount(Account account) {
		if (account.getSessionCount() != 0) {
			return false;
		}
		String username = account.getUsername();
		return allAccounts.remove(username) != null;
	}
	
	/**
	 * 检测服务器中是否存在用户名为username的账户
	 * @param context
	 * @param username
	 * @return
	 */
	public static boolean isAccountExists(Context context, String username) {
		SharedPreferences sp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		boolean result = sp.getBoolean(username, false);
		Log.d(TAG, "account " + username + " exist result : " + result);
		return result;
	}
	
	/**
	 * 检测账户信息是否存在,使用MShareApp.getAppContext()
	 * @param username
	 * @return
	 */
	public static boolean isAccountExists(String username) {
		Context context = MShareApp.getAppContext();
		return isAccountExists(context, username);
	}
	
	/**
	 * 设置notifier
	 * TODO 考虑是否应该使用static
	 * @param notifier
	 */
	public static void setSessionNotifier(SessionNotifier notifier) {
		mNotifier = notifier;
	}
}
