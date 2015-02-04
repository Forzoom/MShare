package org.mshare.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

/**
 * TODO 如何才能确保传入的param中不存在..和.的内容
 * TODO 所有的SharedLink都没有..和.的内容
 * 决定不继承File类
 * 创建TreeNode的过程中，需要为其指定type
 * @author HM
 *
 */
public abstract class SharedLink {
	// 需要文件关系的映射，输入的是需要的文件路径，返回的是真实的文件路径
	// 但是这样可能会使调用者感觉到困难
	
	private static final String TAG = SharedLink.class.getSimpleName();
	
	static final int TYPE_UNKNOWN = 0x0;
	static final int TYPE_FILE = 0x1;
	static final int TYPE_DIRECTORY = 0x2;
	static final int TYPE_FAKE_DIRECTORY = 0x3;
	
	// 所有的子树用HashMap来包含
	protected HashMap<String, SharedLink> map = new HashMap<String, SharedLink>();
	// 完整的路径
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
			Log.w(TAG, "文件不存在，无法创建SharedFile");
			return null;
		} else if (!realFile.isFile()) {
			Log.w(TAG, "the required file is not a 'File'，无法创建SharedFile");
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
	
	// 需要设置lastModified来模拟文件夹创建
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
	 * exist在不同的实现中有不同的效果
	 * @return
	 */
	public abstract boolean exists();
	public abstract boolean isFile();
	public abstract boolean isDirectory();
	public abstract boolean isFakeDirectory();
	
	/**
	 * TODO 所有的文件树中的文件都应该能够被读取，不能够被读取的内容，不应该出现在应用中
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
	
	// TODO 存在可能的效率问题
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
	
	// TODO 可能消耗大量的内存资源
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