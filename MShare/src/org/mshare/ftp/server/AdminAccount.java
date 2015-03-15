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
	
	// 是否应该使用File
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
		
		// 将所有的内容筛选出来
		SharedLinkStorage storage = getStorage();
		SharedLink[] all = storage.getAll();
		int count = 0;
		
		// 迭代所有的内容
		for (int i = 0, len = all.length; i < len; i++) {
			SharedLink sharedLink = all[i];
			// file被多次引用，需要保证不会造成内存泄漏
			// AdminAccount所对应的realFile应该都是存在的
			File file = sharedLink.getRealFile();
			HashMap<String, File> dir = contentForShared.get(file.getParent());
			if (dir == null) { // 当对应的文件夹内容已经存在
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
	 * 判断文件是否是被共享的
	 * @return
	 */
	public boolean isFileShared(File file) {
		// TODO 需要判断吗？
		if (!getSystem().isPrepared()) {
			Log.e(TAG, "SharedLinkSystem is not prepared!");
			return false;
		}
		
		// TODO 可以有效率更高的方法
		HashMap<String, File> dir = contentForShared.get(file.getParent());
		if (dir != null) {
			if (dir.get(file.getName()) != null) {
				return true;
			}
		}
		
		return false;
	}
	
	
}
