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
	
	private static final String TAG = SharedLink.class.getSimpleName();
	
	// 所有的子树用HashMap来包含
	protected HashMap<String, SharedLink> map = new HashMap<String, SharedLink>();
	// 完整的路径
	private SharedLinkSystem mSystem = null;
	private SharedLink parent = null;
	private String fakePath = null;
	private String realPath = null;
	private File realFile = null;

	/**
	 * 文件权限，默认没有任何权限
	 * TODO 需要PERMISSION_UNSET 表示当前权限还没设置
	 */
	private int mPermission = SharedLinkSystem.Permission.PERMISSION_NONE; // 默认没有任何权限
	
	public SharedLink() {
		// 没有参数
	}
	
	/**
	 * 仅仅用于创建一个SharedLink对象，对象不一定存在与文件树中
	 * 不检测真实文件是否存在并且是一个文件
	 * 不判断fakePath和realPath是否有用
	 * TODO 以后可能需要使用flag来判断是否检测上面内容
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
	 * 不判断fakePath和realPath是否有用
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
	 * 不判断fakePath和realPath是否有用
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
	 * 不判断fakePath和realPath是否有用
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
	 * 仅仅用于创建一个FakeDirectory对象，不判断fakePath和realPath是否有用
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
	 * TODO 需要权限?fakeDirectory权限好像不同
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
	 * <p>由fakePath/realPath获得相对应的SharedFile/SharedDirectory/SharedFakeDirectory</p>
	 * <p>仅仅判断文件是否应该存在，包括exists(),REAL_PATH_NONE等等，并不能判断是否应该添加到文件树中</p>
	 * @param fakePath
	 * @param realPath
	 * @return 当失败时返回null
	 */
	public static final SharedLink newSharedLink(String fakePath, String realPath) {
		// fakePath要求
		if (!SharedLinkSystem.isFakePathLegal(fakePath)) {
			Log.e(TAG, "fakePath is illegal");
			return null;
		}
		// TODO 需要更好的判断realPath中是否有不合法的字符
		if (realPath.equals(SharedLinkSystem.REAL_PATH_NONE)) {
			Log.e(TAG, "realPath is illegal");
			return null;
		}
		Log.d(TAG, "newSharedLink:fakePath:" + fakePath + " realPath:" + realPath);
		SharedLink sharedLink = null;
		File realFile = new File(realPath);

		// 根据fakePath和realPath获得对应子类
		if (!realPath.equals(SharedLinkSystem.REAL_PATH_FAKE_DIRECTORY) && realFile.exists()) {
			if (realFile.isFile()) {
				// 将FilePermission放在这里真是麻烦
				// Storage本身就代表是应该使用的权限
				// 暂时使用管理员权限
				sharedLink = SharedLink.newFile(fakePath, realPath);
			} else if (realFile.isDirectory()) {
				sharedLink = SharedLink.newDirectory(fakePath, realPath);
			}
		} else if (realPath.equals(SharedLinkSystem.REAL_PATH_FAKE_DIRECTORY)) { // 对应FakeDirectory
			// 对应SharedFakeDirectory
			sharedLink = SharedLink.newFakeDirectory(fakePath);
		}
		
		return sharedLink;
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
	 * override该函数应该调用super.canRead
	 * 检测在SharedLinkSystem中，当前账户是否有权限读取该SharedLink的内容
	 * TODO 所有的文件树中的文件都应该能够被读取，不能够被读取的内容，不应该出现在应用中
	 * @return
	 */
	public boolean canRead() {
		return Account.canRead(getSystem().getAccountPermission(), getPermission());
	}
	
	/**
	 * override该函数应该调用super.canRead
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
	 * 当FakeDirectory调用getRealFile的时候返回null
	 * @return 将会返回File，不保证File一定能够正常使用，可能是null
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
	 * 文件的大小
	 * @return 对于文件返回0
	 */
	public abstract long length();
	
	// TODO 可能消耗大量的内存资源,仅仅用于调试
	// 而且样式并不是很好看
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
	 * 调用的是SharedLinkSystem.getParent
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
	 * 对于权限的设置只能在三个new方法中执行?
	 * @param permission
	 */
	public void setPermission(int permission) {
		mPermission = permission;
	}
	
	/**
	 * 获得当前文件的相关内容
	 * @return
	 */
	public int getPermission() {
		return mPermission;
	}
	
	/**
	 * 用于获得ls中所对应的文件权限表示
	 * TODO 需要更加优雅高效的方法
	 * @return 获得Ls字符串中的权限部分
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
	 * 不打算返回SharedLink,因为可能是SharedFile来调用这个方法,返回SharedLink意义不大
	 */
	public void bindSystem(SharedLinkSystem system) {
		this.mSystem = system;
	}
}