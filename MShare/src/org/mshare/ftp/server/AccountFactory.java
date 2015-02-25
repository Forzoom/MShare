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
 * TODO ���˹���Ա�˻�������Ҫ�����˻�һͬ���й���
 * �������е��˻�����������Ա�˻������Թ���Ա�˻��е����ݽ��в�����ʱ��ͬʱҲ��Ҫ�������Ĳ�ͬ�˻����в���
 * TODO AccountFactory�Ƿ���Ҫ���Ǿ�̬����,��Ϊ��allAccounts����static��
 * TODO �ͻ����Ǳ߻ᷢ��QUIT��Ϣ��������͵Ļ����Ϳ����ж�Session��������������ͻ�����������أ�
 * TODO ������ж�һ��Account������Ҫ�����أ���SessionThread�У�û��һ��ʱ�䣬����ͻ��˷���һ����Ϣ������֤��ǰ�ͻ���������
 * ���ڹ���Ա�˻������ļ�������򿪵�ʱ�򣬿��Բ鿴����Щ�ļ���������
 * 
 * ����Ա�˻�������getAccount���ɵ�
 * TODO ������USER��ʱ�򲻻���Account������ʹ��Token������Account
 * Token�ļ������������ģ����ǲ�����Account��¶
 * Token�н���ӵ��username,password,��sessionThread��sessionThread�����������
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
 * �����AdminAccount��Token��AccountFactory������
 * 
 * ��������Account��ֱ��ʹ�ã�����ʹ��Token
 * 
 * AccountFactory��ʹ�õ���ģʽ������������������׻�ȡAccountFactory����
 * 
 * TODO �����е�Accountͳһȫ������
 * 
 * TODO �û�����Ҫͳһ���й淶
 * @author HM
 *
 */
public class AccountFactory implements SharedLinkSystem.Callback {
	private static final String TAG = AccountFactory.class.getSimpleName();
	
	// �����˻����û���������
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";
	// ����Ա�˻����û���������
	// ����Ա�˻���Ӧ�ñ�֪��
	public static final String AdminUsername = "admin";
	private static final String AdminPassword = "admin";
	
	// �����˻�������Ϣ��sp
    public static final String SP_ACCOUNT_INFO = "accounts";
	
	// ���е��˻�����,�µ��˻��������ã�ÿ��sessionThread�����ǻ�ö�Ӧ���е�����
    private static HashMap<String, Account> allAccounts = new HashMap<String, Account>();
    // �����˻�
    private Account guestAccount;
    
    // Ĭ�ϵĹ���Ա�˻�
    private Account adminAccount;
    // adminAccount����Ӧ��ΨһToken
    private Token adminAccountToken;
    // ����֪ͨ������Session
    // TODO ʹ��static�Ƿ�ã��ڶ���߳��У�����ʹ��ͬһ��Notifier,�����᲻����ʲô����,�����߳�ͬʱ����һ�������᲻��������
    private static SessionNotifier mNotifier;
    
    public static final int PERMISSION_ADMIN = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_USER = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
    public static final int PERMISSION_GUEST = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_READ_GUEST; 
    
    /**
     * ����ͳһ�������е�Account����ɿ��٣����Կ�������Ҫ��ʱ�����
     */
    public AccountFactory() {
    	
    	// ����adminAccount
    	adminAccount = new AdminAccount(AdminUsername, AdminPassword);
		adminAccount.getSystem().setCallback(this);
		// TODO ���ǹ���Ա��Token����Ӧ����ô��ñȽϺã�ֱ��new?
		adminAccountToken = null;
//		adminAccount.se
		
		// ���������˻���Ϣ
		guestAccount = new GuestAccount(AnonymousUsername, AnonymousPassword);
	}
    
	/**
     * ��ö�Ӧ��Accouont��ֻ�д��ڵ�username���ܻ��Account�Ķ���
     * ��õ�Account����������֤�����Ƿ���ȷ
     * ����ͬһ���˻�����õ�Account��ͬһ��Account���������
     * @param username ��¼��ʹ�õ��û���
     * @param password
     * @return ����null�����˻������ڣ������������
     */
	public Token getToken(String username, String password, SessionThread owner) {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// ���account�е�����
		if (accountsSp.getBoolean(username, false)) {
			Log.e(TAG, "�ʺ� " + username + " ������");
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
			// �û�������ȷ
			Log.e(TAG, "illegal username : " + username);
			return null;
		}
	}
	
	/**
     * ����Ƿ��¼�ɹ�
     * TODO �û�������ʱʹ���������˻���¼�����Բ�����Ҫ���û��������QUIT
     * @return
     */
    private boolean authAttempt(Account account, String username, String password) {
    	String correctUsername = account.getUsername(), correctPassword = account.getPassword();
		if (username != null && !account.isAnonymous() && password != null && password.equals(correctPassword)) {
			Log.d(TAG, "User logged in");
			return true;
		} else if (FsSettings.allowAnoymous() && correctUsername.equals(AccountFactory.AnonymousUsername)) {
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
	 * 
	 * @param username
	 * @param password
	 * @param mPermission 
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
			Log.e(TAG, "Register Fail:username has already existed");
			return false;
		}
		// �������û��ļ�ʧ��
		if (!createAccountSuccess) {
			Log.e(TAG, "Register Fail:create sharedPreferences fail");
			return false;
		}
		
		// ��accountInfo���������
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
		if (!isAccountExists(context, FsSettings.getUsername())) {
			
			Log.d(TAG, "��ǰĬ���˻���Ϣ������");
			Log.d(TAG, "+Ĭ���˻�");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = AccountFactory.register(FsSettings.getUsername(), FsSettings.getPassword(), permission);
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
		if (account.getSessionCount() != 0) {
			return false;
		}
		String username = account.getUsername();
		return allAccounts.remove(username) != null;
	}
	
	/**
	 * �����������Ƿ�����û���Ϊusername���˻�
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
	 * ����˻���Ϣ�Ƿ����,ʹ��MShareApp.getAppContext()
	 * @param username
	 * @return
	 */
	public boolean isAccountExists(String username) {
		Context context = MShareApp.getAppContext();
		return isAccountExists(context, username);
	}
	
	/**
	 * ����notifier
	 * @param notifier
	 */
	public void setSessionNotifier(SessionNotifier notifier) {
		mNotifier = notifier;
	}

	// TODO ��Ҫ����
	public Token getAdminAccountToken() {
		
		return new Token(AdminUsername, AdminPassword, null);
	}
	
	/**
	 * ��SharedPreferences�м�����ͨ�û��˻�������
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
	 * ������Ա�˻����������ݳ־û�
	 * TODO Ӧ�����֪ͨ������Session?
	 */
	@Override
	public void onPersist(String fakePath, String realPath) {
		// TODO ��Ҫ��������Account�е��ļ���
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			account.getSystem().addSharedPath(fakePath, realPath, SharedLinkSystem.FILE_PERMISSION_ADMIN);
		}
		
		// TODO ֪ͨ���е�Session��ʹ��Notifier
		mNotifier.notifyAddFile(adminAccount, null);
	}

	@Override
	public void onUnpersist(String fakePath) {
		// TODO ��Ҫ��������Account�е��ļ���
		Set<String> keySet = allAccounts.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			Account account = allAccounts.get(key);
			account.getSystem().deleteSharedPath(fakePath);
		}
		
		// TODO ֪ͨ���е�Session��ʹ��Notifier
		mNotifier.notifyDeleteFile(adminAccount, null);
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
	 * �����Account�е��ļ������в�����������ӣ�ɾ�����־û��ͷǳ־û�
	 * Token��AccountFactory���ܺͷ��ţ�����AccountFactory����Account���в���
	 * @author HM
	 *
	 */
	public class Token {
		private String username;
		private String password;
		private SessionThread owner;
		
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

		public boolean addSharedPath(String fakePath, String realPath, int filePermission) {
			// TODO ʹ��allAccount����ò����Ǻܺ�
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
