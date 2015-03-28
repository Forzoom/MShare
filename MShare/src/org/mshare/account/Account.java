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
 * TODO ʹ��HASHֵ��ΪSP�ļ�
 * ����Ա�˻��е��ļ������б�Ҫ���ڵģ������жϳ־û������Ƿ���ȷ
 * ������Ա�˻������ݳ־û���ʱ����Ҫ�����е��ļ�����������ݣ���Ҫ������
 * ԭ���������һ���������Account�Ƿ���ڵ���Ϣ���Ƿ��б�Ҫ����������Ϣ�أ�
 * �����б�Ҫ�����е��˻������б���
 * ����˻����ܶ�����ʱ�ģ����п��ܣ��������Ĵ����ǳ־õģ������ļ�������Ϊ���ֻ��ˣ����ܸ��ಢ������һ����ʱ�����ݽ���ʹ��
 * ��authת��Ϊʹ��token
 * 
 * ��ΪSessionû�а취���Account��������canRead��canWrite����filePermission���ж���Token��ִ��
 * 
 * TODO ��Ҫ�����޸��û���������
 * TODO �����Ƿ�Account��ΪAccountFactory���ڲ��࣬��ΪAccount��Ӧ���ܹ������new���������ڲ����в�֪���ܲ��ܱ�֤�䲻��new����
 * TODO ���ǽ��ļ������޸Ĳ�������Account�и���һ�ݣ����Ҽ���notify,ֻ��Ҫ��add��delete�ļ�������notify,����persist������Ҫnotify
 * TODO ���ǹ���Ա�˻��еĲ�һ��
 * TODO ���ǽ�notifyֱ�ӷֳ������ã�����notifier��Ӧ����SharedLinkSystem�б������ˣ����߽�Notifier��Ϊ��̬����
 * ��Account������addSharedPath��persist��������
 * ����Ҫ��system�е������ƶ���Account��,����һ�£����ȫ��������Account�У��ᵼ����϶ȸ�
 * 
 * ��Ҫ����{@link #prepare()}�������ļ�������
 * 
 * @author HM
 *
 */
public abstract class Account {
	private static final String TAG = Account.class.getSimpleName();
	
	// ��ǰ�˻����û���������
    private String mUserName = null;
    private String mPassword = null;
    
    // ������ļ���
    private SharedLinkSystem mSharedLinkSystem;
    // �����洢
    private SharedLinkStorage mSharedLinkStorage;
    
	// �����SharedPreferences�еļ�ֵ
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PERMISSION = "permission";
    public static final String KEY_UPLOAD = "upload";
    
    /**
     * ʹ�õ�ǰAccount��Session����
     */
    public int tokenCount;
    
    // TODO ����unprepared���������ڽ��ļ����ͷ�
    public Account(String username, String password) {
    	this.mUserName = username != null ? username : "";
    	this.mPassword = password;
    	// TODO ����USER��ʱ���ļ����������ˣ���Ϊ����
    }
    
    /**
     * ��׼���ļ���SharedLinkSystem��ͬʱ
     * ���ļ��������һЩ������Ϊ�ļ����е���������
     * @see #prepare()
     * @param storage storage�е����ݶ�������ӵ��ļ�����
     * @param filePermission ����ӵ�SharedLink��ӵ�е�Ȩ��
     */
    public void prepare(SharedLinkStorage storage, int filePermission) {
    	prepare();
    	// TODO �ļ�Ȩ�޲���
    	getSystem().load(storage, filePermission);
    }
    
    /**
     * �����洢���ļ���
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
     * ������ǰ�˻����ϴ�·��
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
	 * ��û��ָ����upload·����ʱ�򣬽������˻���Ӧ�����֣���getUsername
	 * @return
	 */
	public String getUpload() {
		return getStorage().get(KEY_UPLOAD, ServerSettings.getUpload() + File.separator + getUsername());
	}
	
	/**
	 * Account���󲢲��ǿ�������õ��ģ����Ը÷����Ǽ��Ȩ�޵ĺ��ķ���
	 * �жϵ�ǰ�û��Ƿ�ӵ�ж����Ȩ�ޣ�����ִ�ж���ص�FtpCmd
	 * @param accountPermission
	 * @param filePermission
	 * @return
	 */
	public static boolean canRead(int accountPermission, int filePermission) {
		return (accountPermission & filePermission & Permission.PERMISSION_READ_ALL) != Permission.PERMISSION_NONE;
	}
	
	/**
	 * Account���󲢲��ǿ�������õ��ģ����Ը÷����Ǽ��Ȩ�޵ĺ��ķ���
	 * �жϵ�ǰ�û��Ƿ�ӵ�������дȨ�ޣ�����ִ��д��ص�FtpCmd
	 * @param accountPermission
	 * @param filePermission
	 * @return
	 */
	public static boolean canWrite(int accountPermission, int filePermission) {
		return (accountPermission & filePermission & Permission.PERMISSION_WRITE_ALL) != Permission.PERMISSION_NONE; 
	}
	
	/**
	 * ������ִ��
	 * TODO �Ƿ��Ժ���������������Ȩ��
	 * @return
	 */
	public static boolean canExecute() {
		return false;
	}
	
	/**
	 * �жϵ�ǰ���˻��Ƿ��������˻�
	 * @return
	 */
	public abstract boolean isGuest();
	
	/**
	 * �жϵ�ǰ�˻��Ƿ�Ϊ����Ա�˻�
	 * @return
	 */
	public abstract boolean isAdministrator();
	
	/**
	 * �жϵ�ǰ�˻��Ƿ�Ϊ��ͨ�û��˻�
	 * @return
	 */
	public abstract boolean isUser();
	
	/**
	 * TODO ��֤���ص����ݲ�Ϊnull
	 * @return �û�������Ϊnull
	 */
    public String getUsername() {
        return mUserName;
    }

    /**
     * TODO ��֤���ص����ݲ�Ϊnull
	 * @return ���룬��Ϊnull
     */
    public String getPassword() {
    	return mPassword;
    }
    
    /**
     * Ĭ�Ϸ���{@link Permission#PERMISSION_NONE}
     * @return
     */
    public int getPermission() {
    	return Permission.PERMISSION_NONE;
    }
    
    /**
     * Accountӵ��SharedLinkSystem
     * ʹ��public����Ϊ�ܶ�ĵط�������FtpCmd�ж���Ҫ��SharedLinkSystem���в���
     * @return ��û�е���{@link #prepare()}ʱ���ļ�����û�����ɣ�����null
     */
    public SharedLinkSystem getSystem() {
    	return mSharedLinkSystem;
    }
    
    public SharedLinkStorage getStorage() {
    	return mSharedLinkStorage;
    }
    
    /**
     * ������������Account���бȽ�
     */
    @Override
    public boolean equals(Object o) {
    	if (!(o instanceof Account)) {
    		return false;
    	}
    	Account account = (Account)o;
    	String username = account.getUsername();
    	String password = account.getPassword();
    	// TODO username��password����null��
    	if (username.equals(mUserName) && password.equals(mPassword)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * ���Token�ĸ���
     */
    public void registerToken() {
    	tokenCount++;
    }
    
    /**
     * ����Token�ĸ���
     */
    public void unregisterToken() {
    	tokenCount--;
    }
    
    public int getTokenCount() {
    	return tokenCount;
    }
}
