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
 * ά������Ա�˻��������˻�
 * �������е��˻�����������Ա�˻������Թ���Ա�˻��е����ݽ��в�����ʱ��ͬʱҲ��Ҫ�������Ĳ�ͬ�˻����в���
 * TODO AccountFactory�Ƿ���Ҫ���Ǿ�̬����,��Ϊ��allAccounts����static��
 * TODO �ͻ����Ǳ߻ᷢ��QUIT��Ϣ��������͵Ļ����Ϳ����ж�Session��������������ͻ�����������أ�
 * TODO ������ж�һ��Account������Ҫ�����أ���SessionThread�У�û��һ��ʱ�䣬����ͻ��˷���һ����Ϣ������֤��ǰ�ͻ���������
 * ���ڹ���Ա�˻������ļ�������򿪵�ʱ�򣬿��Բ鿴����Щ�ļ���������
 * 
 * SessionThread�ڴ�����ʱ��Ӧ��ͨ��FsService���AccountFactory�е�mVerifier��������֤��¼��Ϣ
 * 
 * Token����������������
 * SessionThread�в���ӵ��Account,����ӵ��token
 * ʹ��token����Account���в���������private Token();
 * ʹ��Token�������������϶�
 * ��Ҫ��ʹ��Token���Account��Ӧ���ļ�����Ȼ����в�������Account���������ڸ����¼�û��������Ϣ,�������õ����ݶ���ͨ��Token������
 * Token��Account֮�����϶Ƚ����
 * 
 * Ȩ�ޱ��棬���û�����ӵ�в�ͬ��Ȩ��
 * TODO Ȩ�ޱ�����sp���б�Ҫ��
 * 
 * ����������Command�ȵȣ�û�з������AccountFactory������ֻ��ʹ��Token���б�Ҫ�Ĳ�����FsServiceӵ��AccountFactory
 * ��ν�FsService�е�AccountFactory��adminToken�����ļ�������أ�
 * 
 * TODO ����Ա�˻������ֲ���ʹ��
 * ��AdminAccount��Token��AccountFactory������
 * 
 * AccountFactory��ʹ�õ���ģʽ������������������׻�ȡAccountFactory����
 * 
 * Account��������Ҫ��ʱ��Żᱻ���ص�allAccounts��
 *
 * ����ͳһ�������е�Account����ɿ��٣����Կ�������Ҫ��ʱ�����
 * 
 * TODO �û�����Ҫͳһ���й淶
 * @author HM
 *
 */
public class AccountFactory implements SharedLinkSystem.Callback {
	private static final String TAG = AccountFactory.class.getSimpleName();
	
	// �����˻��û���������
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";

	// ����Ա�˻����û���������
	public static final String AdminUsername = "admin";
	private static final String AdminPassword = "admin";
	
	// �����˻�������Ϣ��sp�����Է�װ��Щ������
    public static final String SP_ACCOUNT_INFO = "accounts";
	
	// ���е��˻�����,�µ��˻��������ã�ÿ��sessionThread�����ǻ�ö�Ӧ���е�����
    private static HashMap<String, Account> allAccounts = new HashMap<String, Account>();
    
    // �����˻�
    private GuestAccount guestAccount;
    // ����Ա�˻�
    private AdminAccount adminAccount;
    
    // adminAccount����Ӧ��ΨһToken
    private Token adminAccountToken;
    // ����֪ͨ������Session
    // TODO ʹ��static�Ƿ�ã��ڶ���߳��У�����ʹ��ͬһ��Notifier,�����᲻����ʲô����,�����߳�ͬʱ����һ�������᲻��������
    private SessionNotifier mNotifier;
    // ������֤
    private Verifier mVerifier;
    
    public static final int PERMISSION_ADMIN = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_USER = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_GUEST = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ_GUEST; 

    // ����ģʽ
    private static AccountFactory sAccountFactory;

    /**
     * ��������Ա�˻��������˻��Լ���֤��
     */
    private AccountFactory() {
    	
    	// ����adminAccount
    	adminAccount = new AdminAccount(AdminUsername, AdminPassword);
    	adminAccount.prepare();
		adminAccount.getSystem().setCallback(this);
		adminAccountToken = new Token(AdminUsername, AdminPassword, null);
		adminAccountToken.setAccount(adminAccount);
		
		// ���������˻���Ϣ
		guestAccount = new GuestAccount(AnonymousUsername, AnonymousPassword);
		guestAccount.prepare();
		
		// Verifier�Ĵ���
		mVerifier = new Verifier();
		Log.d(TAG, "create verifier");
	}

    /**
     * ʹ�õ���ģʽ
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
     * ��ö�Ӧ��Token��SessionΪ�˻��Token��Ӧ�õ���verifier�е�auth���������
     * @param username ��¼��ʹ�õ��û���
     * @param password
     * @return ����null��ʾ����ʧ�ܣ���������������˻������ڣ�����������ʱ���������
     */
	private Token getToken(String username, String password, SessionThread owner) {
		Context context = MShareApp.getAppContext();
		// ���allAccounts��accountSp�е����ݣ�ֻ�е����߶������ڵ�ʱ����ж�������
		if (allAccounts.get(username) == null && !isAccountExists(context, username)) {
			Log.e(TAG, "�ʺ� " + username + " ������");
			return null;
		}
		
		// ���ص�allAccounts��
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			if (!allAccounts.containsKey(username)) {
				if (!loadAccount(username)) {
					Log.e(TAG, "cannot get the token because the account is not exist");
					return null;
				}
			}
			
			// �ж��˻���Ϣ�Ƿ���ȷ
			Account account = allAccounts.get(username);
			Log.d(TAG, "AccountFactory getToken : " + username);
			if (account != null && authAttempt(account, username, password)) {
				Token token = new Token(username, password, owner);
				token.setAccount(account);
				// ��registerToken�������������?
				account.registerToken();
				return token;
			} else {
				Log.e(TAG, "loggin fail");
				return null;
			}
			
		} else {
			// �û�������ȷ
			Log.e(TAG, "illegal username : " + username);
			return null;
		}
	}
	
	/**
     * ����û����������Ƿ���ȷ
     * TODO �û�������ʱʹ���������˻���¼�����Բ�����Ҫ���û��������QUIT
     * @return
     */
    private boolean authAttempt(Account account, String username, String password) {
    	String correctUsername = account.getUsername(), correctPassword = account.getPassword();
    	Log.d(TAG, "AccountFactory authAttempt : " + username + " " + !account.isGuest() + " " + password + " cor " + correctUsername + " " + correctPassword);
		if (username != null && !account.isGuest() && password != null && password.equals(correctPassword)) {
			Log.d(TAG, "User logged in");
			return true;
		} else if (ServerSettings.allowAnoymous() && correctUsername.equals(AccountFactory.AnonymousUsername)) {
			// ����Ȩ��Ϊ�����˻�Ȩ��
			Log.i(TAG, "Guest logged in with password: " + password);
			return true;
		} else {
			// ��¼ʧ��
			Log.d(TAG, "Logged fail");
			return false;
		}
    }

	/**
	 * ʹ��֮ǰ��Ӧ����⣬��ע���û����û������ܺ�Ĭ���û��������û��Լ�����Ա�˻���ͻ
	 * ����ͨ���ú���ע���������ͨ�˻�
	 * TODO ��Ҫ����û���������İ�ȫ�ԺͺϷ���
	 * @param username
	 * @param password
	 * @param permission
	 * @return
	 */
	protected static boolean register(String username, String password, int permission) {
		Log.d(TAG, "+ע���˻�,�û���:" + username + " ����:" + password);
		boolean createAccountSuccess = false;
		Context context = MShareApp.getAppContext();
		// �����û��ļ�
		SharedPreferences userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		if (userSp.getString(Account.KEY_PASSWORD, "").equals("")) {
			Editor editor = userSp.edit();
			editor.putString(Account.KEY_PASSWORD, password);
			// ��û��ָ��Ȩ��ʱ����ʹ����ͨ�˻��Ķ�дȨ��
			editor.putInt(Account.KEY_PERMISSION, permission == Permission.PERMISSION_NONE ? PERMISSION_USER : permission);
			createAccountSuccess = editor.commit();
		} else {
			Log.e(TAG, "+���:username has already existed");
			return false;
		}
		// �������û��ļ�ʧ��
		if (!createAccountSuccess) {
			Log.e(TAG, "+���:create sharedPreferences fail");
			return false;
		}
		
		// ��accountInfo���������
		SharedPreferences accountsSp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		Editor accountEditor = accountsSp.edit();
		accountEditor.putBoolean(username, true);
		if (accountEditor.commit()) {
			Log.d(TAG, "+���:success");
			return true;
		} else {
			Log.e(TAG, "+���:fail");
			return false;
		}			
	}
	

	/**
	 * ��Ĭ���˻��������˻������ڵ�ʱ��ʹ��register����ע��,������Ա�˻�
	 * ��������ʵ���Ȩ��
	 */
	public void checkReservedAccount() {
		Context context = MShareApp.getAppContext();
		// ��������˻�
		if (!isAccountExists(context, AccountFactory.AnonymousUsername)) {
			
			Log.d(TAG, "��ǰ�����˻���Ϣ������");
			Log.d(TAG, "+�����˻�");
			int permission = Permission.PERMISSION_READ_GUEST;
			boolean registerResult = AccountFactory.register(AccountFactory.AnonymousUsername, AccountFactory.AnonymousPassword, permission);
			Log.d(TAG, "�����˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰ�����˻���Ϣ����");
		}
		
		// ���Ĭ����ͨ�˻�
		if (!isAccountExists(context, ServerSettings.getUsername())) {
			
			Log.d(TAG, "��ǰĬ���˻���Ϣ������");
			Log.d(TAG, "+Ĭ���˻�");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = AccountFactory.register(ServerSettings.getUsername(), ServerSettings.getPassword(), permission);
			Log.d(TAG, "Ĭ���˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰĬ���˻���Ϣ����");
		}
		
		// ������Ա�˻�
		if (!isAccountExists(context, AccountFactory.AdminUsername)) {
			Log.d(TAG, "��ǰ����Ա�˻���Ϣ������");
			Log.d(TAG, "+����Ա�˻�");
			int permission = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN;
			boolean registerResult = AccountFactory.register(AccountFactory.AdminUsername, AccountFactory.AdminPassword, permission);
			Log.d(TAG, "����Ա�˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰ����Ա�˻���Ϣ����");
		}
	}
	
	/**
	 * Account������allAccounts�У�Account�����ļ��������ܻ�ռ���ڴ棬���Ե�����ʹ�õ�ʱ�򣬽�Accountȥ����
	 * TODO �����Ҫrecycle�����ĺ�����
	 */
	public static boolean recycleAccount(Account account) {
		if (account.getTokenCount() != 0) {
			return false;
		}
		String username = account.getUsername();
		return allAccounts.remove(username) != null;
	}
	
	/**
	 * �����������Ƿ�����û���Ϊusername���˻�
	 * �������{@link #register(String, String, int)}������ע����˻������ø÷���Ӧ�÷���true
	 * @param context
	 * @param username
	 * @return �����򷵻�true�����򷵻�false
	 */
	public boolean isAccountExists(Context context, String username) {
		SharedPreferences sp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		boolean result = sp.getBoolean(username, false);
		Log.d(TAG, "account " + username + " exist result : " + result);
		return result;
	}
	
	/**
	 * ����˻���Ϣ�Ƿ����,ʹ��MShareApp.getAppContext()
	 * @see #isAccountExists(Context, String)
	 * @param username
	 * @return
	 */
	public boolean isAccountExists(String username) {
		Context context = MShareApp.getAppContext();
		return isAccountExists(context, username);
	}

	// �����˻���Ӧ������
	private String getAccountPassword(Context context, String username) {
		SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		return sp.getString(Account.KEY_PASSWORD, "");
	}

	/**
	 * ָ�������ļ��Ƿ񱻹���
	 * @param file ��Ҫ�жϵ��ļ�
	 * @return ���ļ������ұ�����ʱ������true�����򷵻�false
	 */
	public boolean isFileShared(File file) {
		// �ж��ļ���Ч
		if (file == null || !file.exists()) {
			Log.d(TAG, "invalid or unexisted file");
			return false;
		}
		
		return adminAccount.isFileShared(file);
	}
	
	/**
	 * ����notifier�����û�����ã���ô������Ա�˻��е����ݷ����仯ʱ������֪ͨ������Session
	 * @param notifier 
	 */
	public void bindSessionNotifier(SessionNotifier notifier) {
		mNotifier = notifier;
	}

	/**
	 * Ϊ�˾�����֤SessionNotifier�ܹ����������ͷ�
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
	 * ��Ҫ��֤����Ϊnull
	 * @return ����ֵ����Ϊnull
	 */
	public Token getAdminAccountToken() {
		return adminAccountToken;
	}
	
	/**
	 * ��SharedPreferences�м�����ͨ�û��˻�������
	 * TODO ��SharedPreferences��װ
	 * @param username ��Ҫ���ص��û���
	 * @return �ɹ�����true�����򷵻�false
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
		// ������Ա�е�������ӵ��µ��˻���
		newAccount.prepare(adminAccount.getStorage(), PERMISSION_ADMIN);
		allAccounts.put(username, newAccount);
		return true;
	}
	
	/**
	 * ���AccountFactory�е���֤������
	 * @return
	 */
	public Verifier getVerifier() {
		return mVerifier;
	}
	
	/**
	 * ������Ա�˻����������ݳ־û�
	 * TODO Ӧ�����֪ͨ������Session?
	 */
	@Override
	public void onPersist(String fakePath, String realPath) {
		// ѭ���������е��ļ���
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			SharedLink sharedLink = SharedLink.newSharedLink(fakePath, realPath);
			account.getSystem().addSharedLink(sharedLink, SharedLinkSystem.FILE_PERMISSION_ADMIN);
		}
		
		// TODO ֪ͨ���е�Session��ʹ��Notifier
		if (mNotifier != null) {
			mNotifier.notifyAddFile(adminAccountToken, null);
		}
	}

	@Override
	public void onUnpersist(String fakePath) {
		// TODO ��Ҫ��������Account�е��ļ���
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			account.getSystem().deleteSharedLink(fakePath);
		}
		
		// TODO ֪ͨ���е�Session��ʹ��Notifier
		if (mNotifier != null) {
			mNotifier.notifyDeleteFile(adminAccount, null);
		}
	}

	@Override
	public void onAdd() {
		// �����в��������еĲ�������onPersist��
	}

	@Override
	public void onDelete() {
		// �����в��������еĲ�������onUnpersist��
	}
	
	/**
	 * ���ڼ���û��ĵ�¼��Ϣ�Ƿ���ȷ��������Session������֤�ɹ����{@link Token}
	 * @author HM
	 *
	 */
	public class Verifier {
		/**
		 * Sessionͨ������auth����ö�Ӧ��Token����ʧ�ܵ�ʱ�򣬷���null
		 * ����װ��AccountFactory�е�getToken����
		 * @param owner ��������϶ȣ�
		 * @return ʧ��ʱ����null
		 */
		public Token auth(String username, String password, SessionThread owner) {
			return AccountFactory.this.getToken(username, password, owner);
		}
	}
	
	/**
	 * �����Account�е��ļ������в�����������ӣ�ɾ�����־û��ͷǳ־û�
	 * Token��AccountFactory���ܺͷ��ţ�����AccountFactory����Account���в���
	 * 
	 * TODO ��Ҫ�����е��û���������ʱ��token���п�������Ч�ģ��������û�����ʹ�õ�ʱ��token��Ϊ��Ч���ˣ�Ϊ�˷�ֹ���ⷢ����������Ҫ����isValid
	 * 
	 * ��Ҫ����Lock�Ļ��ƣ�Token����Lock��������Ҫrelease
	 * 
	 * ��Ϊprivate���캯�����ڲ��࣬��Ϊ��Token���ܱ������new����
	 * 
	 * @author HM
	 *
	 */
	public class Token {
		private String username;
		private String password;
		private SessionThread owner;
		private Account account;
		
		// ���ܱ����ⴴ��
		// ����username,password,sessionThread
		/**
		 * 
		 * @param username
		 * @param password
		 * @param owner ���ڱ�����ǰ���ĸ�Sessionӵ�е�Token�����ܱ�����Session��ӵ�к͵��ã���ʱ������������϶�
		 */
		private Token(String username, String password, SessionThread owner) {
			this.username = username;
			this.password = password;
			this.owner = owner;
		}
		
		/**
		 * �������ж��û��Ƿ�ӵ�������Ȩ��
		 * @return
		 */
		public boolean accessRead() {
			return Account.canWrite(account.getPermission(), Permission.PERMISSION_READ_ALL);
		}
		
		/**
		 * ��⵱ǰ�˻��Ƿ�ӵ�ж����Ȩ�ޣ�{@link Account#canRead(int, int)}
		 * @param filePermission �ļ�Ȩ��
		 * @return
		 */
		public boolean canRead(int filePermission) {
			return Account.canRead(account.getPermission(), filePermission);
		}

		/**
		 * ӵ��д���Ȩ�ޣ�{@link Account#canWrite(int, int)}
		 * @param filePermission �ļ���Ȩ��
		 * @return
		 */
		public boolean canWrite(int filePermission) {
			return Account.canWrite(account.getPermission(), filePermission);
		}
		/**
		 * �������ж��û��Ƿ�ӵ������дȨ��
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
		 * ����Account������ÿ�ζ�ȥallAccounts��Ѱ��
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
		 * �ڵ���token�ķ���֮ǰ����õ����Ա��жϵ�ǰ��token����
		 * @return
		 */
		public boolean isValid() {
			return (account != null && account.getUsername().equals(username) && account.getPassword().equals(password));
		}
		
		/**
		 * �����ͷ�Token���Ӷ�ʹAccount�ܹ����ͷ�
		 * TODO ����Account���ͷŻ��ƻ�������
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
