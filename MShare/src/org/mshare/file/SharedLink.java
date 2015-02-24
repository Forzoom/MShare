package org.mshare.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mshare.file.SharedLinkSystem.Permission;
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
	// �����������ܻ�ʹ�����߸о�������
	
	private static final String TAG = SharedLink.class.getSimpleName();
	
	static final int TYPE_UNKNOWN = 0x0;
	static final int TYPE_FILE = 0x1;
	static final int TYPE_DIRECTORY = 0x2;
	static final int TYPE_FAKE_DIRECTORY = 0x3;
	
	// ���е�������HashMap������
	protected HashMap<String, SharedLink> map = new HashMap<String, SharedLink>();
	// ������·��
	private SharedLinkSystem mSystem = null;
	private SharedLink parent = null;
	private String fakePath = null;
	private String realPath = null;
	private File realFile = null;
	
	// ��Ҫ���õ�Ȩ������
	// mPermission = 0777
	private int mPermission = 0; // Ĭ��û���κ�Ȩ��
	int type = TYPE_UNKNOWN;
	
	public SharedLink(SharedLinkSystem system) {
		this.mSystem = system;
	}
	
	/**
	 * �������ڴ���һ��SharedLink���󣬶���һ���������ļ�����
	 * �������ʵ�ļ��Ƿ���ڲ�����һ���ļ�
	 * TODO �Ժ������Ҫʹ��flag���ж��Ƿ�����������
	 * @param system
	 * @param fakePath
	 * @param realPath
	 * @return
	 */
	public static final SharedLink newFile(SharedLinkSystem system, String fakePath, String realPath, int filePermission) {
	
		SharedLink file = new SharedFile(system);
		file.setFakePath(fakePath); 
		file.setRealPath(realPath);
		file.setPermission(filePermission);
		return file;
	}
	
	public static final SharedLink newDirectory(SharedLinkSystem system, String fakePath, String realPath, int filePermission) {
		SharedLink file = new SharedDirectory(system);
		file.setFakePath(fakePath);
		file.setRealPath(realPath);
		file.setPermission(filePermission);
		return null;
	}
	
	// ��Ҫ����lastModified��ģ���ļ��д���
	public static final SharedLink newFakeDirectory(SharedLinkSystem system, String fakePath, int filePermission) {
		SharedLink file = new SharedFakeDirectory(system);
		
		file.setFakePath(fakePath);
		file.setLastModified(System.currentTimeMillis());
		file.setPermission(filePermission);
		return file;
	}
	
	public SharedLink newInstance(int type) {
		return null;
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
		return Account.canRead(getSystem().getAccount(), getPermission());
	}
	
	/**
	 * override�ú���Ӧ�õ���super.canRead
	 * @return
	 */
	public boolean canWrite() {
		return Account.canWrite(getSystem().getAccount(), getPermission());
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
	 * @return ���᷵��File������֤Fileһ���ܹ�����ʹ��
	 */
	public File getRealFile() {
		if (realFile == null || !realFile.getAbsoluteFile().equals(realPath)) {
			realFile = new File(realPath);
		}
		return realFile;
	}
	
	/**
	 * �ļ��Ĵ�С
	 * @return
	 */
	public abstract long length();
	
	// TODO �������Ĵ������ڴ���Դ,�������ڵ���
	void print() {
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
	private void setPermission(int permission) {
		mPermission = permission;
	}
	
	public int getPermission() {
		return mPermission;
	}
	
	/**
	 * ���ڻ��ls������Ӧ���ļ�Ȩ�ޱ�ʾ
	 * TODO ��Ҫ�������Ÿ�Ч�ķ���
	 * @return 
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
		lsPermission.append((filePermission & Permission.PERMISSION_EXECUTE_GUEST) != 0 ? "r" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_EXECUTE_GUEST) != 0 ? "w" : "-");
		lsPermission.append((filePermission & Permission.PERMISSION_EXECUTE_GUEST) != 0 ? "x" : "-");
		
		Log.d(TAG, "Ls permission string : " + lsPermission.toString());
		return lsPermission.toString();
	}
}