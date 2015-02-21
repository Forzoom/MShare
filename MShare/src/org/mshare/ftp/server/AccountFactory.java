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
 * TODO AccountFactory�Ƿ���Ҫ���Ǿ�̬����,��Ϊ��allAccounts����static��
 * TODO �ͻ����Ǳ߻ᷢ��QUIT��Ϣ��������͵Ļ����Ϳ����ж�Session��������������ͻ�����������أ�
 * TODO ������ж�һ��Account������Ҫ�����أ���SessionThread�У�û��һ��ʱ�䣬����ͻ��˷���һ����Ϣ������֤��ǰ�ͻ���������
 * @author HM
 *
 */
public class AccountFactory {
	private static final String TAG = AccountFactory.class.getSimpleName();
	
	// �����˻����û���������
	public static final String AnonymousUsername = "anonymous";
	public static final String AnonymousPassword = "guest";
	// ����Ա�˻����û���������
	public static final String AdminUsername = "admin";
	public static final String AdminPassword = "admin";
	
    public static final String SP_ACCOUNT_INFO = "accounts";
	
	// ���е��˻�����,�µ��˻��������ã�ÿ��sessionThread�����ǻ�ö�Ӧ���е�����
    private static HashMap<String, Account> allAccounts = new HashMap<String, Account>();
    
    // Ĭ�ϵĹ���Ա�˻�
    // TODO ����Ա�˻�ֻ��������
    public static Account adminAccount = new Account(AccountFactory.AdminUsername, AccountFactory.AdminPassword);
    // ����֪ͨ������Session
    // TODO ʹ��static�Ƿ�ã��ڶ���߳��У�����ʹ��ͬһ��Notifier,�����᲻����ʲô����,�����߳�ͬʱ����һ�������᲻��������
    private static SessionNotifier mNotifier;
    
	/**
     * ��ö�Ӧ��Accouont��ֻ�д��ڵ�username���ܻ��Account�Ķ���
     * ��õ�Account����������֤�����Ƿ���ȷ
     * ����ͬһ���˻�����õ�Account��ͬһ��Account���������
     * TODO ����ʹ��public�Ƿ����
     * @param username
     * @return
     */
	public static Account getAccount(String username) {
		Context context = MShareApp.getAppContext();
		SharedPreferences accountsSp = context.getSharedPreferences(SP_ACCOUNT_INFO, Context.MODE_PRIVATE);
		// ���account�е�����
		if (accountsSp.getBoolean(username, false) == false) {
			Log.e(TAG, "�ʺ� " + username + " ������");
			return null;
		}

		if (username.equals(AnonymousUsername)) {
			Log.d(TAG, "��ǰ�����˻����Ե�¼");
		} else if (username.equals(FsSettings.getUsername())) {
			Log.d(TAG, "��ǰĬ���˻����Ե�¼");
		}
		
		// TODO ��Ҫ���˻��������������
		if (username != null && username.matches("[0-9a-zA-Z]+")) {
			if (!allAccounts.containsKey(username)) {
				// allAccounts�в����ڣ��ʹ�SharedPreferences�л��
				SharedPreferences sp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
				String password = sp.getString(Account.KEY_PASSWORD, "");
				if (!password.equals("")) {
					Log.d(TAG, "�����ȷ���˻�");
					allAccounts.put(username, new Account(username, password));
				} else {
					Log.e(TAG, "��Ҫ���˻����벻���ڣ�password:" + password);
				}
			}
			
			return allAccounts.get(username);
		} else {
			// �û�������ȷ
			Log.e(TAG, "������û������Ϸ�");
			return null;
		}
	}
	

	/**
	 * ��Ҫ��check��ʹ��register�������ļ�
	 * ʹ��֮ǰ��Ӧ����⣬��ע���û����û������ܺ�Ĭ���û��������û��Լ�����Ա�˻���ͻ
	 * ����ͨ���ú���ע�������Ĭ���˻��������˻�
	 * 
	 * @param username
	 * @param password
	 * @param mPermission 
	 * @return
	 */
	protected static boolean register(String username, String password, int permission) {
		Log.d(TAG, "��ʼע���˻�,�û���:" + username + " ����:" + password);
		boolean createUserSuccess = false;
		Context context = MShareApp.getAppContext();
		// �����û��ļ�
		SharedPreferences userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		if (userSp.getString(Account.KEY_PASSWORD, "").equals("")) {
			Editor editor = userSp.edit();
			editor.putString(Account.KEY_PASSWORD, password);
			// ��û��ָ��Ȩ��ʱ����ʹ����ͨ�˻��Ķ�дȨ��
			editor.putInt(Account.KEY_PERMISSION, permission == Permission.PERMISSION_NONE ? Permission.PERMISSION_READ | Permission.PERMISSION_WRITE : permission);
			createUserSuccess = editor.commit();
		} else {
			Log.e(TAG, "Register Fail:username has already existed");
			return false;
		}
		// �������û��ļ�ʧ��
		if (!createUserSuccess) {
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
	public static void checkReservedAccount() {
		Context context = MShareApp.getAppContext();
		// ��������˻�
		if (!AccountFactory.isAccountExists(context, AccountFactory.AnonymousUsername)) {
			
			Log.d(TAG, "��ǰ�����˻���Ϣ������");
			Log.d(TAG, "��ʼע�������˻�");
			int permission = Permission.PERMISSION_READ_GUEST;
			boolean registerResult = AccountFactory.register(AccountFactory.AnonymousUsername, AccountFactory.AnonymousPassword, permission);
			Log.d(TAG, "����ע�������˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰ�����˻���Ϣ����");
		}
		
		// ���Ĭ���˻�
		if (!AccountFactory.isAccountExists(context, FsSettings.getUsername())) {
			
			Log.d(TAG, "��ǰĬ���˻���Ϣ������");
			Log.d(TAG, "��ʼע��Ĭ���˻�");
			int permission = Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
			boolean registerResult = AccountFactory.register(FsSettings.getUsername(), FsSettings.getPassword(), permission);
			Log.d(TAG, "����ע��Ĭ���˻�, ���:" + registerResult);
		} else {
			Log.d(TAG, "��ǰĬ���˻���Ϣ����");
		}
		
		// ������Ա�˻�
		if (!AccountFactory.isAccountExists(context, AccountFactory.AdminUsername)) {
			Log.d(TAG, "��ǰ����Ա�˻���Ϣ������");
			Log.d(TAG, "��ʼע�����Ա�˻�");
			int permission = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN;
			boolean registerResult = AccountFactory.register(AccountFactory.AdminUsername, AccountFactory.AdminPassword, permission);
			Log.d(TAG, "����ע�����Ա�˻�, ���:" + registerResult);
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
	public static boolean isAccountExists(Context context, String username) {
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
	public static boolean isAccountExists(String username) {
		Context context = MShareApp.getAppContext();
		return isAccountExists(context, username);
	}
	
	/**
	 * ����notifier
	 * TODO �����Ƿ�Ӧ��ʹ��static
	 * @param notifier
	 */
	public static void setSessionNotifier(SessionNotifier notifier) {
		mNotifier = notifier;
	}
}
