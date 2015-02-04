package org.mshare.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

/**
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
	protected String fakePath = null;
	protected String realPath = null;
	private File realFile = null;
	
	int type = TYPE_UNKNOWN;
	
	public SharedLink(SharedLinkSystem system) {
		this.mSystem = system;
	}
	
	public static final SharedLink newFile(SharedLinkSystem system, String fakePath, String realPath) {
		
		File realFile = new File(realPath);
		if (!realFile.exists()) {
			Log.w(TAG, "�ļ������ڣ��޷�����SharedFile");
			return null;
		} else if (!realFile.isFile()) {
			Log.w(TAG, "the required file is not a 'File'���޷�����SharedFile");
			return null;
		}
	
		SharedLink sf = new SharedFile(system);
		sf.fakePath = fakePath; 
		sf.realPath = realPath;
		sf.realFile = realFile;
		return sf;
	}
	
	public static final SharedLink newDirectory(SharedLinkSystem system, String fakePath, String realPath) {
		SharedDirectory sd = new SharedDirectory(system);
		sd.fakePath = fakePath;
		sd.realPath = realPath;
		return null;
	}
	
	// ��Ҫ����lastModified��ģ���ļ��д���
	public static final SharedLink newFakeDirectory(SharedLinkSystem system, String fakePath) {
		SharedFakeDirectory sfd = new SharedFakeDirectory(system);
		
		sfd.fakePath = fakePath;
		sfd.setLastModified(System.currentTimeMillis());
		
		return sfd;
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
	 * TODO ���е��ļ����е��ļ���Ӧ���ܹ�����ȡ�����ܹ�����ȡ�����ݣ���Ӧ�ó�����Ӧ����
	 * @return
	 */
	public abstract boolean canRead();
	public abstract long lastModified();
	public abstract boolean setLastModified(long time);
	public abstract boolean delete();
	public abstract boolean mkdir();
	
	public SharedLinkSystem getSystem() {
		return mSystem;
	}
	
	public HashMap<String, SharedLink> list() {
		if (isDirectory()) {
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
	
	public File getRealFile() {
		return realFile;
	}
	
	public abstract long length();
	
	// TODO �������Ĵ������ڴ���Դ
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
	
	
	
	public SharedLink getParent() {
		
		int lastIndex = fakePath.lastIndexOf(SharedLinkSystem.SEPARATOR);
		String parentFakePath = fakePath.substring(0, lastIndex);
		
		return mSystem.getSharedLink(parentFakePath);
	}
}