package org.mshare.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mshare.ftp.server.Account;

import android.util.Log;

/**
 * 必须和system共同使用才能发挥正确的价值，但提高了耦合
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
	private String fakePath = null;
	private String realPath = null;
	private File realFile = null;
	
	// 需要设置的权限内容
	// mPermission = 0777
	private int mPermission = 0; // 默认没有任何权限
	int type = TYPE_UNKNOWN;
	
	public SharedLink(SharedLinkSystem system) {
		this.mSystem = system;
	}
	
	/**
	 * 仅仅用于创建一个SharedLink对象，对象不一定存在与文件树中
	 * 不检测真实文件是否存在并且是一个文件
	 * TODO 以后可能需要使用flag来判断是否检测上面内容
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
	
	// 需要设置lastModified来模拟文件夹创建
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
	 * exist在不同的实现中有不同的效果
	 * @return
	 */
	public abstract boolean exists();
	public abstract boolean isFile();
	public abstract boolean isDirectory();
	public abstract boolean isFakeDirectory();
	
	/**
	 * 所以覆写该函数的，都应该调用super该函数
	 * TODO 所有的文件树中的文件都应该能够被读取，不能够被读取的内容，不应该出现在应用中
	 * @return
	 */
	public boolean canRead() {
		return Account.canRead(getSystem().getAccount(), getPermission());
	}
	
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
	
	/**
	 * realFile可能会是null
	 * @return
	 */
	public File getRealFile() {
		if (realFile == null || !realFile.getAbsoluteFile().equals(realPath)) {
			realFile = new File(realPath);
		}
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
	 * 对于权限的设置只能在三个new方法中执行?
	 * @param permission
	 */
	private void setPermission(int permission) {
		mPermission = permission;
	}
	
	public int getPermission() {
		return mPermission;
	}
}