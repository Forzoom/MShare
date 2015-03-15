package org.mshare.ftp.server;

import java.io.File;
import java.util.HashMap;

import org.mshare.file.share.SharedLink;
import org.mshare.file.share.SharedLinkStorage;
import org.mshare.file.share.SharedLinkSystem.Permission;

import android.util.Log;

/**
 * {@link Account}
 * {@link Account#prepare()}
 * @author HM
 *
 */
public class AdminAccount extends Account {
	private static final String TAG = AdminAccount.class.getSimpleName();
	
	private int mPermission = AccountFactory.PERMISSION_ADMIN;
	
	// �Ƿ�Ӧ��ʹ��File
	private HashMap<String, HashMap<String, File>> contentForShared = new HashMap<String, HashMap<String, File>>();
	
	/**
	 * {@link Account}
	 * {@link Account#prepare()}
	 * @param username
	 * @param password
	 */
	public AdminAccount(String username, String password) {
		super(username, password);
	}

	@Override
	public void prepare() {
		super.prepare();
		
		// �����е�����ɸѡ����
		SharedLinkStorage storage = getStorage();
		SharedLink[] all = storage.getAll();
		int count = 0;
		
		// �������е�����
		for (int i = 0, len = all.length; i < len; i++) {
			SharedLink sharedLink = all[i];
			// file��������ã���Ҫ��֤��������ڴ�й©
			// AdminAccount����Ӧ��realFileӦ�ö��Ǵ��ڵ�
			File file = sharedLink.getRealFile();
			HashMap<String, File> dir = contentForShared.get(file.getParent());
			if (dir == null) { // ����Ӧ���ļ��������Ѿ�����
				dir = new HashMap<String, File>();
				contentForShared.put(file.getParent(), dir);
			}
			dir.put(file.getName(), file);
			count++;
		}
		Log.d(TAG, count + " shared file is recored!");
	}
	
	@Override
	public int getPermission() {
		return mPermission;
	}

	@Override
	public boolean isGuest() {
		return false;
	}

	@Override
	public boolean isAdministrator() {
		return true;
	}

	@Override
	public boolean isUser() {
		return false;
	}
	
	/**
	 * �ж��ļ��Ƿ��Ǳ������
	 * @return
	 */
	public boolean isFileShared(File file) {
		// TODO ��Ҫ�ж���
		if (!getSystem().isPrepared()) {
			Log.e(TAG, "SharedLinkSystem is not prepared!");
			return false;
		}
		
		// TODO ������Ч�ʸ��ߵķ���
		HashMap<String, File> dir = contentForShared.get(file.getParent());
		if (dir != null) {
			if (dir.get(file.getName()) != null) {
				return true;
			}
		}
		
		return false;
	}
	
	
}
