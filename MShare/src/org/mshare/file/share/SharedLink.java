package org.mshare.file.share;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mshare.file.share.SharedLinkSystem.Permission;
import org.mshare.ftp.server.Account;

import android.util.Log;

/**
 * �����system��ͬʹ�ò��ܷ�����ȷ�ļ�ֵ������������
 * TODO ��β���ȷ�������param�в�����..��.������
 * TODO ���е�SharedLink��û��..��.������
 * �������̳�File��
 * ����TreeNode�Ĺ����У���ҪΪ��ָ��type
 * @author HM
 *
 */
public abstract class SharedLink {
	// ��Ҫ�ļ���ϵ��ӳ�䣬���������Ҫ���ļ�·�������ص�����ʵ���ļ�·��
	
	private static final String TAG = SharedLink.class.getSimpleName();
	
	// ���е�������HashMap������
	protected HashMap<String, SharedLink> map = new HashMap<String, SharedLink>();
	// ������·��
	private SharedLinkSystem mSystem = null;
	private SharedLink parent = null;
	private String fakePath = null;
	private String realPath = null;
	private File realFile = null;

	/**
	 * �ļ�Ȩ�ޣ�Ĭ��û���κ�Ȩ��
	 * TODO ��ҪPERMISSION_UNSET ��ʾ��ǰȨ�޻�û����
	 */
	private int mPermission = SharedLinkSystem.Permission.PERMISSION_NONE; // Ĭ��û���κ�Ȩ��
	
	public SharedLink() {
		// û�в���
	}
	
	/**
	 * �������ڴ���һ��SharedLink���󣬶���һ���������ļ�����
	 * �������ʵ�ļ��Ƿ���ڲ�����һ���ļ�
	 * ���ж�fakePath��realPath�Ƿ�����
	 * TODO �Ժ������Ҫʹ��flag���ж��Ƿ�����������
	 * @param fakePath
	 * @param realPath
	 * @return
	 */
	public static final SharedLink newFile(String fakePath, String realPath, int filePermission) {
		SharedLink file = newFile(fakePath, realPath);
		file.setPermission(filePermission);
		return file;
	}
	
	public static final SharedLink newFile(SharedLinkSystem system, String fakePath, String realPath, int filePermission) {
		SharedLink file = newFile(fakePath, realPath);
		file.bindSystem(system);
		return file;
	}
	
	/**
	 * ���ж�fakePath��realPath�Ƿ�����
	 * @param fakePath
	 * @param realPath
	 * @return
	 */
	public static final SharedLink newFile(String fakePath, String realPath) {
		SharedLink file = new SharedFile();
		file.setFakePath(fakePath); 
		file.setRealPath(realPath);
		return file;
	}
	
	/**
	 * ���ж�fakePath��realPath�Ƿ�����
	 * @param fakePath
	 * @param realPath
	 * @param filePermission
	 * @return
	 */
	public static final SharedLink newDirectory(String fakePath, String realPath, int filePermission) {
		SharedLink file = newDirectory(fakePath, realPath);
		file.setPermission(filePermission);
		return file;
	}
	
	/**
	 * ���ж�fakePath��realPath�Ƿ�����
	 * @param fakePath
	 * @param realPath
	 * @return
	 */
	public static final SharedLink newDirectory(String fakePath, String realPath) {
		SharedLink file = new SharedDirectory();
		file.setFakePath(fakePath);
		file.setRealPath(realPath);
		return file;
	}
	
	/**
	 * �������ڴ���һ��FakeDirectory���󣬲��ж�fakePath��realPath�Ƿ�����
	 * @param fakePath
	 * @param filePermission
	 * @return
	 */
	public static final SharedLink newFakeDirectory(String fakePath, int filePermission) {
		SharedLink file = newFakeDirectory(fakePath);
		file.setPermission(filePermission);
		return file;
	}
	
	public static final SharedLink newFakeDirectory(SharedLinkSystem system, String fakePath, int filePermission) {
		SharedLink file = newFakeDirectory(fakePath, filePermission);
		file.bindSystem(system);
		return file;
	}
	
	/**
	 * TODO ��ҪȨ��?fakeDirectoryȨ�޺���ͬ
	 * @param fakePath
	 * @return
	 */
	public static final SharedLink newFakeDirectory(String fakePath) {
		SharedLink file = new SharedFakeDirectory();
		file.setFakePath(fakePath);
		file.setLastModified(System.currentTimeMillis());
		return file;
	}
	
	/**
	 * <p>��fakePath/realPath������Ӧ��SharedFile/SharedDirectory/SharedFakeDirectory</p>
	 * <p>�����ж��ļ��Ƿ�Ӧ�ô��ڣ�����exists(),REAL_PATH_NONE�ȵȣ��������ж��Ƿ�Ӧ����ӵ��ļ�����</p>
	 * @param fakePath
	 * @param realPath
	 * @return ��ʧ��ʱ����null
	 */
	public static final SharedLink newSharedLink(String fakePath, String realPath) {
		// fakePathҪ��
		if (!SharedLinkSystem.isFakePathLegal(fakePath)) {
			Log.e(TAG, "fakePath is illegal");
			return null;
		}
		// TODO ��Ҫ���õ��ж�realPath���Ƿ��в��Ϸ����ַ�
		if (realPath.equals(SharedLinkSystem.REAL_PATH_NONE)) {
			Log.e(TAG, "realPath is illegal");
			return null;
		}
		Log.d(TAG, "newSharedLink:fakePath:" + fakePath + " realPath:" + realPath);
		SharedLink sharedLink = null;
		File realFile = new File(realPath);

		// ����fakePath��realPath��ö�Ӧ����
		if (!realPath.equals(SharedLinkSystem.REAL_PATH_FAKE_DIRECTORY) && realFile.exists()) {
			if (realFile.isFile()) {
				// ��FilePermission�������������鷳
				// Storage����ʹ�����Ӧ��ʹ�õ�Ȩ��
				// ��ʱʹ�ù���ԱȨ��
				sharedLink = SharedLink.newFile(fakePath, realPath);
			} else if (realFile.isDirectory()) {
				sharedLink = SharedLink.newDirectory(fakePath, realPath);
			}
		} else if (realPath.equals(SharedLinkSystem.REAL_PATH_FAKE_DIRECTORY)) { // ��ӦFakeDirectory
			// ��ӦSharedFakeDirectory
			sharedLink = SharedLink.newFakeDirectory(fakePath);
		}
		
		return sharedLink;
	}
	
	/**
	 * exist�ڲ�ͬ��ʵ�����в�ͬ��Ч��
	 * @return
	 */
	public abstract boolean exists();
	public abstract boolean isFile();
	public abstract boolean isDirectory();
	public abstract boolean isFakeDirectory();
	
	/**
	 * override�ú���Ӧ�õ���super.canRead
	 * �����SharedLinkSystem�У���ǰ�˻��Ƿ���Ȩ�޶�ȡ��SharedLink������
	 * TODO ���е��ļ����е��ļ���Ӧ���ܹ�����ȡ�����ܹ�����ȡ�����ݣ���Ӧ�ó�����Ӧ����
	 * @return
	 */
	public boolean canRead() {
		return Account.canRead(getSystem().getAccountPermission(), getPermission());
	}
	
	/**
	 * override�ú���Ӧ�õ���super.canRead
	 * @return
	 */
	public boolean canWrite() {
		return Account.canWrite(getSystem().getAccountPermission(), getPermission());
	}
	
	public abstract long lastModified();
	public abstract boolean setLastModified(long time);
	public abstract boolean delete();
	public abstract boolean renameTo(SharedLink newPath);
	
	public SharedLinkSystem getSystem() {
		return mSystem;
	}
	
	public HashMap<String, SharedLink> list() {
		if (isDirectory() || isFakeDirectory()) {
			return map;
		} else {
			return null;
		}
	}
	
	// TODO ���ڿ��ܵ�Ч������
	public abstract SharedLink[] listFiles();
	
	public String getRealPath() {
		return realPath;
	}
	
	public String getFakePath() {
		return fakePath;
	}
	
	public String getName() {
		int separatorIndex = fakePath.lastIndexOf(SharedLinkSystem.SEPARATOR);
        return (separatorIndex < 0) ? fakePath : fakePath.substring(separatorIndex + 1, fakePath.length());
	}
	
	/**
	 * ��FakeDirectory����getRealFile��ʱ�򷵻�null
	 * @return ���᷵��File������֤Fileһ���ܹ�����ʹ�ã�������null
	 */
	public File getRealFile() {
		if (isFile() || isFakeDirectory()) {
			if (realFile == null || !realFile.getAbsoluteFile().equals(realPath)) {
				realFile = new File(realPath);
			}
			return realFile;
		} else {
			return null;
		}
	}
	
	/**
	 * �ļ��Ĵ�С
	 * @return �����ļ�����0
	 */
	public abstract long length();
	
	// TODO �������Ĵ������ڴ���Դ,�������ڵ���
	// ������ʽ�����Ǻܺÿ�
	public void print() {
		String fakeName = getName();
		if (isDirectory()) {
			System.out.println("(" + fakeName);
			Set<String> set = map.keySet();
			Iterator<String> iterator = set.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				map.get(key).print();
			}
			System.out.println(")");
		} else if (isFile()) {
			System.out.println(fakeName);
		}
		
	}
	
	/**
	 * ���õ���SharedLinkSystem.getParent
	 * @return
	 */
	public String getParent() {
		return SharedLinkSystem.getParent(getFakePath());
	}
	
	public void setFakePath(String fakePath) {
		this.fakePath = fakePath;
	}
	
	public void setRealPath(String realPath) {
		realFile = new File(realPath);
		this.realPath = realPath;
	}
	
	/**
	 * ����Ȩ�޵�����ֻ��������new������ִ��?
	 * @param permission
	 */
	public void setPermission(int permission) {
		mPermission = permission;
	}
	
	/**
	 * ��õ�ǰ�ļ����������
	 * @return
	 */
	public int getPermission() {
		return mPermission;
	}
	
	/**
	 * ���ڻ��ls������Ӧ���ļ�Ȩ�ޱ�ʾ
	 * TODO ��Ҫ�������Ÿ�Ч�ķ���
	 * @return ���Ls�ַ����е�Ȩ�޲���
	 */
	public String getLsPermission() {
		StringBuilder lsPermission = new StringBuilder();
		if (isDirectory() || isFakeDirectory()) {
			lsPermission.append("d");
		} else {
			lsPermission.append("-");
		}

		int filePermission = getPermission();
		lsPermission.append((filePermission & Permission.PERMISSION_READ_ADMIN) != 0 ? "r" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_WRITE_ADMIN) != 0 ? "w" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_EXECUTE_ADMIN) != 0 ? "x" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_READ) != 0 ? "r" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_WRITE) != 0 ? "w" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_EXECUTE) != 0 ? "x" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_READ_GUEST) != 0 ? "r" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_WRITE_GUEST) != 0 ? "w" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_EXECUTE_GUEST) != 0 ? "x" : "-");
		
		Log.d(TAG, "Ls permission string : " + lsPermission.toString());
		return lsPermission.toString();
	}
	
	/**
	 * �����㷵��SharedLink,��Ϊ������SharedFile�������������,����SharedLink���岻��
	 */
	public void bindSystem(SharedLinkSystem system) {
		this.mSystem = system;
	}
}