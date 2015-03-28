package org.mshare.account;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mshare.file.share.SharedLink;
import org.mshare.file.share.SharedLinkSystem;
import org.mshare.file.share.SharedLinkSystem.Permission;
import org.mshare.server.ServerSettings;
import org.mshare.server.ftp.SessionNotifier;
import org.mshare.server.ftp.SessionThread;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * 维护管理员账户、匿名账户
 * 管理所有的账户，包括管理员账户，当对管理员账户中的内容进行操作的时候，同时也需要对其他的不同账户进行操作
 * TODO AccountFactory是否需要都是静态方法,因为连allAccounts都是static的
 * TODO 客户端那边会发送QUIT消息吗，如果发送的话，就可以判断Session数量，可是如果客户意外断线了呢？
 * TODO 该如何判断一个Account对象需要回收呢？在SessionThread中，没过一段时间，就向客户端发送一个消息，以验证当前客户端仍在线
 * 存在管理员账户，当文件浏览器打开的时候，可以查看有哪些文件被共享了
 * 
 * SessionThread在创建的时候应该通过FsService获得AccountFactory中的mVerifier，用来验证登录信息
 * 
 * Token的引入增加了消耗
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
 * 有AdminAccount的Token由AccountFactory来管理
 * 
 * AccountFactory不使用单例模式，否则其他类可以轻易获取AccountFactory对象
 * 
 * Account仅仅在需要的时候才会被加载到allAccounts中
 *
 * 担心统一加载所有的Account会造成卡顿，所以考虑在需要的时候加载
 * 
 * TODO 用户名需要统一进行规范
 * @author HM
 *
 */
public class AccountFactory implements SharedLinkSystem.Callback {
	private static final String TAG = AccountFactory.class.getSimpleName();
	
	// 匿名账户用户名和密码
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";

	// 管理员账户的用户名和密码
	public static final String AdminUsername = "admin";
	private static final String AdminPassword = "admin";
	
	// 保存账户存在信息的sp，可以封装这些内容吗
    public static final String SP_ACCOUNT_INFO = "accounts";
	
	// 所有的账户内容,新的账户从这里获得，每个sessionThread仅仅是获得对应其中的引用
    private static HashMap<String, Account> allAccounts = new HashMap<String, Account>();
    
    // 匿名账户
    private GuestAccount guestAccount;
    // 管理员账户
    private AdminAccount adminAccount;
    
    // adminAccount所对应的唯一Token
    private Token adminAccountToken;
    // 用于通知其他的Session
    // TODO 使用static是否好，在多个线程中，将会使用同一个Notifier,这样会不会有什么错误,两个线程同时调用一个方法会不会有问题
    private SessionNotifier mNotifier;
    // 用于验证
    private Verifier mVerifier;
    
    public static final int PERMISSION_ADMIN = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_USER = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_GUEST = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ_GUEST; 

    // 单例模式
    private static AccountFactory sAccountFactory;

    /**
     * 创建管理员账户、匿名账户以及验证器
     */
    private AccountFactory() {
    	
    	// 加载adminAccount
    	adminAccount = new AdminAccount(AdminUsername, AdminPassword);
    	adminAccount.prepare();
		adminAccount.getSystem().setCallback(this);
		adminAccountToken = new Token(AdminUsername, AdminPassword, null);
		adminAccountToken.setAccount(adminAccount);
		
		// 创建匿名账户信息
		guestAccount = new GuestAccount(AnonymousUsername, AnonymousPassword);
		guestAccount.prepare();
		
		// Verifier的创建
		mVerifier = new Verifier();
		Log.d(TAG, "create verifier");
	}

    /**
     * 使用单例模式
     * @return
     */
    public static AccountFactory getInstance() {
        if (sAccountFactory == null) {
            sAccountFactory = new AccountFactory();
            sAccountFactory.checkReservedAccount();
            Log.d(TAG, "AccountFactory is created");
        }
        return sAccountFactory;
    }

	/**
     * 获得对应的Token，Session为了获得Token，应该调用verifier中的auth方法来获得
     * @param username 登录所使用的用户名
     * @param password
     * @return 返回null表示请求失败，可能是所请求的账户不存在，或者是请求时的密码错误
     */
	private Token getToken(String username, String password, SessionThread owner) {
		Context context = MShareApp.getAppContext();
		// 检测allAccounts和accountSp中的内容，只有当两者都不存在的时候才判定不存在
		if (allAccounts.get(username) == null && !isAccountExists(context, username)) {
			Log.e(TAG, "帐号 " + username + " 不存在");
			return null;
		}
		
		// 加载到allAccounts中
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			if (!allAccounts.containsKey(username)) {
				if (!loadAccount(username)) {
					Log.e(TAG, "cannot get the token because the account is not exist");
					return null;
				}
			}
			
			// 判断账户信息是否正确
			Account account = allAccounts.get(username);
			Log.d(TAG, "AccountFactory getToken : " + username);
			if (account != null && authAttempt(account, username, password)) {
				Token token = new Token(username, password, owner);
				token.setAccount(account);
				// 将registerToken放在这里合适吗?
				account.registerToken();
				return token;
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
     * 检测用户名和密码是否正确
     * TODO 用户可以随时使用其他的账户登录，所以并不能要求用户必须调用QUIT
     * @return
     */
    private boolean authAttempt(Account account, String username, String password) {
    	String correctUsername = account.getUsername(), correctPassword = account.getPassword();
    	Log.d(TAG, "AccountFactory authAttempt : " + username + " " + !account.isGuest() + " " + password + " cor " + correctUsername + " " + correctPassword);
		if (username != null && !account.isGuest() && password != null && password.equals(correctPassword)) {
			Log.d(TAG, "User logged in");
			return true;
		} else if (ServerSettings.allowAnoymous() && correctUsername.equals(AccountFactory.AnonymousUsername)) {
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
	 * TODO 需要检测用户名和密码的安全性和合法性
	 * @param username
	 * @param password
	 * @param permission
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
			Log.e(TAG, "+结果:username has already existed");
			return false;
		}
		// 当创建用户文件失败
		if (!createAccountSuccess) {
			Log.e(TAG, "+结果:create sharedPreferences fail");
			return false;
		}
		
		// 向accountInfo中添加内容
		SharedPreferences accountsSp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		Editor accountEditor = accountsSp.edit();
		accountEditor.putBoolean(username, true);
		if (accountEditor.commit()) {
			Log.d(TAG, "+结果:success");
			return true;
		} else {
			Log.e(TAG, "+结果:fail");
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
		if (!isAccountExists(context, ServerSettings.getUsername())) {
			
			Log.d(TAG, "当前默认账户信息不存在");
			Log.d(TAG, "+默认账户");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = AccountFactory.register(ServerSettings.getUsername(), ServerSettings.getPassword(), permission);
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
		if (account.getTokenCount() != 0) {
			return false;
		}
		String username = account.getUsername();
		return allAccounts.remove(username) != null;
	}
	
	/**
	 * 检测服务器中是否存在用户名为username的账户
	 * 如果调用{@link #register(String, String, int)}函数所注册的账户，调用该方法应该返回true
	 * @param context
	 * @param username
	 * @return 存在则返回true，否则返回false
	 */
	public boolean isAccountExists(Context context, String username) {
		SharedPreferences sp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		boolean result = sp.getBoolean(username, false);
		Log.d(TAG, "account " + username + " exist result : " + result);
		return result;
	}
	
	/**
	 * 检测账户信息是否存在,使用MShareApp.getAppContext()
	 * @see #isAccountExists(Context, String)
	 * @param username
	 * @return
	 */
	public boolean isAccountExists(String username) {
		Context context = MShareApp.getAppContext();
		return isAccountExists(context, username);
	}

	// 返回账户对应的密码
	private String getAccountPassword(Context context, String username) {
		SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		return sp.getString(Account.KEY_PASSWORD, "");
	}

	/**
	 * 指明本地文件是否被共享
	 * @param file 所要判断的文件
	 * @return 当文件存在且被共享时，返回true，否则返回false
	 */
	public boolean isFileShared(File file) {
		// 判断文件有效
		if (file == null || !file.exists()) {
			Log.d(TAG, "invalid or unexisted file");
			return false;
		}
		
		return adminAccount.isFileShared(file);
	}
	
	/**
	 * 设置notifier，如果没有设置，那么当管理员账户中的内容发生变化时，不会通知其他的Session
	 * @param notifier 
	 */
	public void bindSessionNotifier(SessionNotifier notifier) {
		mNotifier = notifier;
	}

	/**
	 * 为了尽量保证SessionNotifier能够被正常的释放
	 */
	public void releaseSessionNotifier() {
		mNotifier = null;
	}
	
	/**
	 * 
	 * @return may be null
	 */
	public SessionNotifier getSessionNotifier() {
		return mNotifier;
	}
	
	/**
	 * 需要保证不会为null
	 * @return 返回值不会为null
	 */
	public Token getAdminAccountToken() {
		return adminAccountToken;
	}
	
	/**
	 * 从SharedPreferences中加载普通用户账户的内容
	 * TODO 将SharedPreferences封装
	 * @param username 需要加载的用户名
	 * @return 成功返回true，否则返回false
	 */
	private boolean loadAccount(String username) {
		if (allAccounts.containsKey(username)) {
			Log.e(TAG, "already container account : " + username);
			return false;
		}

		Context context = MShareApp.getAppContext();
		if (!isAccountExists(context, username)) {
			return false;
		}

		String password = getAccountPassword(context, username);
		if (password == null || password.equals("")) {
			return false;
		}
		
		Log.d(TAG, "add new account : " + username + " password : " + password);
		Account newAccount = new UserAccount(username, password);
		// 将管理员中的内容添加到新的账户中
		newAccount.prepare(adminAccount.getStorage(), PERMISSION_ADMIN);
		allAccounts.put(username, newAccount);
		return true;
	}
	
	/**
	 * 获得AccountFactory中的验证器对象
	 * @return
	 */
	public Verifier getVerifier() {
		return mVerifier;
	}
	
	/**
	 * 当管理员账户中有新内容持久化
	 * TODO 应该如何通知其他的Session?
	 */
	@Override
	public void onPersist(String fakePath, String realPath) {
		// 循环处理所有的文件树
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			SharedLink sharedLink = SharedLink.newSharedLink(fakePath, realPath);
			account.getSystem().addSharedLink(sharedLink, SharedLinkSystem.FILE_PERMISSION_ADMIN);
		}
		
		// TODO 通知所有的Session，使用Notifier
		if (mNotifier != null) {
			mNotifier.notifyAddFile(adminAccountToken, null);
		}
	}

	@Override
	public void onUnpersist(String fakePath) {
		// TODO 需要调整所有Account中的文件树
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			account.getSystem().deleteSharedLink(fakePath);
		}
		
		// TODO 通知所有的Session，使用Notifier
		if (mNotifier != null) {
			mNotifier.notifyDeleteFile(adminAccount, null);
		}
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
	 * 用于检测用户的登录信息是否正确，并且向Session返回验证成功后的{@link Token}
	 * @author HM
	 *
	 */
	public class Verifier {
		/**
		 * Session通过调用auth来获得对应的Token，当失败的时候，返回null
		 * 即包装了AccountFactory中的getToken方法
		 * @param owner 增加了耦合度？
		 * @return 失败时返回null
		 */
		public Token auth(String username, String password, SessionThread owner) {
			return AccountFactory.this.getToken(username, password, owner);
		}
	}
	
	/**
	 * 允许对Account中的文件树进行操作，包括添加，删除，持久化和非持久化
	 * Token由AccountFactory保管和发放，交由AccountFactory来对Account进行操作
	 * 
	 * TODO 需要当所有的用户都结束的时候，token才有可能是无效的，不能在用户仍在使用的时候，token变为无效的了，为了防止意外发生，还是需要调用isValid
	 * 
	 * 需要类似Lock的机制，Token就是Lock，所以需要release
	 * 
	 * 作为private构造函数的内部类，是为了Token不能被随意地new出来
	 * 
	 * @author HM
	 *
	 */
	public class Token {
		private String username;
		private String password;
		private SessionThread owner;
		private Account account;
		
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
		
		/**
		 * 仅用于判断用户是否拥有任意读权限
		 * @return
		 */
		public boolean accessRead() {
			return Account.canWrite(account.getPermission(), Permission.PERMISSION_READ_ALL);
		}
		
		/**
		 * 检测当前账户是否拥有读相关权限，{@link Account#canRead(int, int)}
		 * @param filePermission 文件权限
		 * @return
		 */
		public boolean canRead(int filePermission) {
			return Account.canRead(account.getPermission(), filePermission);
		}

		/**
		 * 拥有写相关权限，{@link Account#canWrite(int, int)}
		 * @param filePermission 文件的权限
		 * @return
		 */
		public boolean canWrite(int filePermission) {
			return Account.canWrite(account.getPermission(), filePermission);
		}
		/**
		 * 仅用于判断用户是否拥有任意写权限
		 * @return
		 */
		public boolean accessWrite() {
			return Account.canWrite(account.getPermission(), Permission.PERMISSION_WRITE_ALL);
		}
		
		public boolean isAdministrator() {
			return account.isAdministrator();
		}
		
		public boolean isUser() {
			return account.isUser();
		}
		
		public boolean isGuest() {
			return account.isGuest();
		}
		
		/**
		 * 设置Account，不用每次都去allAccounts中寻找
		 * @param account
		 */
		private void setAccount(Account account) {
			this.account = account;
		}
		
		public SharedLinkSystem getSystem() {
			if (account != null) {
				return account.getSystem();
			} else {
				return null;
			}
		}
		
		/**
		 * 在调用token的方法之前，最好调用以便判断当前的token正常
		 * @return
		 */
		public boolean isValid() {
			return (account != null && account.getUsername().equals(username) && account.getPassword().equals(password));
		}
		
		/**
		 * 用于释放Token，从而使Account能够被释放
		 * TODO 但是Account的释放机制还不完善
		 */
		public void release() {
			this.username = null;
			this.password = null;
			this.owner = null;
			
			account.unregisterToken();
			this.account = null;
		}
	}
}
