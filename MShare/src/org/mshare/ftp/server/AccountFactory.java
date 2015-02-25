package org.mshare.ftp.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mshare.file.SharedLinkSystem;
import org.mshare.file.SharedLinkSystem.Permission;
import org.mshare.ftp.server.FsService.SessionNotifier;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * TODO 除了管理员账户，还需要匿名账户一同进行管理
 * 管理所有的账户，包括管理员账户，当对管理员账户中的内容进行操作的时候，同时也需要对其他的不同账户进行操作
 * TODO AccountFactory是否需要都是静态方法,因为连allAccounts都是static的
 * TODO 客户端那边会发送QUIT消息吗，如果发送的话，就可以判断Session数量，可是如果客户意外断线了呢？
 * TODO 该如何判断一个Account对象需要回收呢？在SessionThread中，没过一段时间，就向客户端发送一个消息，以验证当前客户端仍在线
 * 存在管理员账户，当文件浏览器打开的时候，可以查看有哪些文件被共享了
 * 
 * 管理员账户不是用getAccount生成的
 * TODO 当调用USER的时候不会获得Account，或者使用Token来代替Account
 * Token的加入增加了消耗，但是不会让Account暴露
 * Token中仅仅拥有username,password,和sessionThread，sessionThread用来反向调用
 * SessionThread中不再拥有Account,而是拥有token
 * 使用token来对Account进行操作，可以private Token();
 * 使用Token操作将会提高耦合度
 * 主要是使用Token获得Account对应的文件树，然后进行操作，而Account仅仅是用于负责记录用户的相关信息,其中有用的内容都将通过Token来传递
 * Token和Account之间的耦合度将提高
 * 
 * 权限保存，让用户可以拥有不同的权限
 * TODO 权限保存在sp中有必要吗？
 * 
 * 对于其他的Command等等，没有方法获得AccountFactory，所以只能使用Token进行必要的操作，FsService拥有AccountFactory
 * 如何将FsService中的AccountFactory的adminToken交给文件浏览器呢？
 * 
 * TODO 管理员账户的名字不可使用
 * 如果有AdminAccount的Token由AccountFactory来管理
 * 
 * 将不再让Account被直接使用，而是使用Token
 * 
 * AccountFactory不使用单例模式，否则其他类可以轻易获取AccountFactory对象
 * 
 * TODO 将所有的Account统一全部加载
 * 
 * TODO 用户名需要统一进行规范
 * @author HM
 *
 */
public class AccountFactory implements SharedLinkSystem.Callback {
	private static final String TAG = AccountFactory.class.getSimpleName();
	
	// 匿名账户的用户名和密码
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";
	// 管理员账户的用户名和密码
	// 管理员账户不应该被知道
	public static final String AdminUsername = "admin";
	private static final String AdminPassword = "admin";
	
	// 保存账户存在信息的sp
    public static final String SP_ACCOUNT_INFO = "accounts";
	
	// 所有的账户内容,新的账户从这里获得，每个sessionThread仅仅是获得对应其中的引用
    private static HashMap<String, Account> allAccounts = new HashMap<String, Account>();
    // 匿名账户
    private Account guestAccount;
    
    // 默认的管理员账户
    private Account adminAccount;
    // adminAccount所对应的唯一Token
    private Token adminAccountToken;
    // 用于通知其他的Session
    // TODO 使用static是否好，在多个线程中，将会使用同一个Notifier,这样会不会有什么错误,两个线程同时调用一个方法会不会有问题
    private static SessionNotifier mNotifier;
    
    public static final int PERMISSION_ADMIN = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_USER = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_GUEST = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ_GUEST; 
    
    /**
     * 担心统一加载所有的Account会造成卡顿，所以考虑在需要的时候加载
     */
    public AccountFactory() {
    	
    	// 加载adminAccount
    	adminAccount = new AdminAccount(AdminUsername, AdminPassword);
		adminAccount.getSystem().setCallback(this);
		// TODO 考虑管理员的Token对象应该怎么获得比较好，直接new?
		adminAccountToken = null;
//		adminAccount.se
		
		// 创建匿名账户信息
		guestAccount = new GuestAccount(AnonymousUsername, AnonymousPassword);
	}
    
	/**
     * 获得对应的Accouont，只有存在的username才能获得Account的对象
     * 获得的Account对象用于验证密码是否正确
     * 对于同一个账户，获得的Account是同一个Account对象的引用
     * @param username 登录所使用的用户名
     * @param password
     * @return 返回null代表账户不存在，或者密码错误
     */
	public Token getToken(String username, String password, SessionThread owner) {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// 检测account中的内容
		if (accountsSp.getBoolean(username, false)) {
			Log.e(TAG, "帐号 " + username + " 不存在");
			return null;
		}
		
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			if (!allAccounts.containsKey(username)) {
				if (!loadAccount(username)) {
					Log.e(TAG, "cannot get the token because the account is not exist");
					return null;
				}
			}
			
			Account account = allAccounts.get(username);
			
			if (account != null && authAttempt(account, username, password)) {
				return new Token(username, password, owner);
			} else {
				Log.e(TAG, "loggin fail");
				return null;
			}
			
		} else {
			// 用户名不正确
			Log.e(TAG, "illegal username : " + username);
			return null;
		}
	}
	
	/**
     * 检测是否登录成功
     * TODO 用户可以随时使用其他的账户登录，所以并不能要求用户必须调用QUIT
     * @return
     */
    private boolean authAttempt(Account account, String username, String password) {
    	String correctUsername = account.getUsername(), correctPassword = account.getPassword();
		if (username != null && !account.isAnonymous() && password != null && password.equals(correctPassword)) {
			Log.d(TAG, "User logged in");
			return true;
		} else if (FsSettings.allowAnoymous() && correctUsername.equals(AccountFactory.AnonymousUsername)) {
			// 设置权限为匿名账户权限
			Log.i(TAG, "Guest logged in with password: " + password);
			return true;
		} else {
			// 登录失败
			Log.d(TAG, "Logged fail");
			return false;
		}
    }

	/**
	 * 使用之前，应当检测，所注册用户的用户名不能和默认用户、匿名用户以及管理员账户冲突
	 * 允许通过该函数注册和生成普通账户
	 * 
	 * @param username
	 * @param password
	 * @param mPermission 
	 * @return
	 */
	protected static boolean register(String username, String password, int permission) {
		Log.d(TAG, "+注册账户,用户名:" + username + " 密码:" + password);
		boolean createAccountSuccess = false;
		Context context = MShareApp.getAppContext();
		// 创建用户文件
		SharedPreferences userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		if (userSp.getString(Account.KEY_PASSWORD, "").equals("")) {
			Editor editor = userSp.edit();
			editor.putString(Account.KEY_PASSWORD, password);
			// 当没有指定权限时，将使用普通账户的读写权限
			editor.putInt(Account.KEY_PERMISSION, permission == Permission.PERMISSION_NONE ? PERMISSION_USER : permission);
			createAccountSuccess = editor.commit();
		} else {
			Log.e(TAG, "Register Fail:username has already existed");
			return false;
		}
		// 当创建用户文件失败
		if (!createAccountSuccess) {
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
	public void checkReservedAccount() {
		Context context = MShareApp.getAppContext();
		// 检测匿名账户
		if (!isAccountExists(context, AccountFactory.AnonymousUsername)) {
			
			Log.d(TAG, "当前匿名账户信息不存在");
			Log.d(TAG, "+匿名账户");
			int permission = Permission.PERMISSION_READ_GUEST;
			boolean registerResult = AccountFactory.register(AccountFactory.AnonymousUsername, AccountFactory.AnonymousPassword, permission);
			Log.d(TAG, "匿名账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前匿名账户信息存在");
		}
		
		// 检测默认普通账户
		if (!isAccountExists(context, FsSettings.getUsername())) {
			
			Log.d(TAG, "当前默认账户信息不存在");
			Log.d(TAG, "+默认账户");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = AccountFactory.register(FsSettings.getUsername(), FsSettings.getPassword(), permission);
			Log.d(TAG, "默认账户, 结果:" + registerResult);
		} else {
			Log.d(TAG, "当前默认账户信息存在");
		}
		
		// 检测管理员账户
		if (!isAccountExists(context, AccountFactory.AdminUsername)) {
			Log.d(TAG, "当前管理员账户信息不存在");
			Log.d(TAG, "+管理员账户");
			int permission = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN;
			boolean registerResult = AccountFactory.register(AccountFactory.AdminUsername, AccountFactory.AdminPassword, permission);
			Log.d(TAG, "管理员账户, 结果:" + registerResult);
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
	public boolean isAccountExists(Context context, String username) {
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
	public boolean isAccountExists(String username) {
		Context context = MShareApp.getAppContext();
		return isAccountExists(context, username);
	}
	
	/**
	 * 设置notifier
	 * @param notifier
	 */
	public void setSessionNotifier(SessionNotifier notifier) {
		mNotifier = notifier;
	}

	// TODO 需要修正
	public Token getAdminAccountToken() {
		
		return new Token(AdminUsername, AdminPassword, null);
	}
	
	/**
	 * 从SharedPreferences中加载普通用户账户的内容
	 * @param username
	 * @return
	 */
	private boolean loadAccount(String username) {
		if (allAccounts.containsKey(username)) {
			Log.e(TAG, "already container account : " + username);
			return false;
		}
		
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		String password = sp.getString(Account.KEY_PASSWORD, "");
		
		if (password.equals("")) {
			Log.e(TAG, "account is not exist");
			return false;
		}
		
		Log.d(TAG, "add new account : " + username);
		allAccounts.put(username, new UserAccount(username, password));
		return true;
	}
	
	/**
	 * 当管理员账户中有新内容持久化
	 * TODO 应该如何通知其他的Session?
	 */
	@Override
	public void onPersist(String fakePath, String realPath) {
		// TODO 需要调整所有Account中的文件树
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			account.getSystem().addSharedPath(fakePath, realPath, SharedLinkSystem.FILE_PERMISSION_ADMIN);
		}
		
		// TODO 通知所有的Session，使用Notifier
		mNotifier.notifyAddFile(adminAccount, null);
	}

	@Override
	public void onUnpersist(String fakePath) {
		// TODO 需要调整所有Account中的文件树
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			account.getSystem().deleteSharedPath(fakePath);
		}
		
		// TODO 通知所有的Session，使用Notifier
		mNotifier.notifyDeleteFile(adminAccount, null);
	}

	@Override
	public void onAdd() {
		// 不进行操作，所有的操作都在onPersist中
	}

	@Override
	public void onDelete() {
		// 不进行操作，所有的操作都在onUnpersist中
	}
	
	/**
	 * 允许对Account中的文件树进行操作，包括添加，删除，持久化和非持久化
	 * Token由AccountFactory保管和发放，交由AccountFactory来对Account进行操作
	 * @author HM
	 *
	 */
	public class Token {
		private String username;
		private String password;
		private SessionThread owner;
		
		// 不能被随意创建
		// 包含username,password,sessionThread
		/**
		 * 
		 * @param username
		 * @param password
		 * @param owner 用于表明当前是哪个Session拥有的Token，不能被其他Session所拥有和调用，暂时是这样，但耦合度
		 */
		private Token(String username, String password, SessionThread owner) {
			this.username = username;
			this.password = password;
			this.owner = owner;
		}

		public boolean addSharedPath(String fakePath, String realPath, int filePermission) {
			// TODO 使用allAccount来获得并不是很好
			return allAccounts.get(username).getSystem().addSharedPath(fakePath, realPath, filePermission);
		}
		
		public boolean deleteSharedPath(String fakePath) {
			return allAccounts.get(username).getSystem().deleteSharedPath(fakePath);
		}
		
		public boolean persist(String fakePath, String realPath) {
			return allAccounts.get(username).getSystem().persist(fakePath, realPath);
		}
		
		public boolean unpersist(String fakePath) {
			return allAccounts.get(username).getSystem().unpersist(fakePath);
		}
	}
}
