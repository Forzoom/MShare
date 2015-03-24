package org.mshare.file.share;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

/**
 * 对于文件树中的内容，对于SharedDirectory中的内容，文件树中都有，但是并没有被持久化
 * @author HM
 *
 */
public class SharedDirectory extends SharedLink {
	private static final String TAG = SharedDirectory.class.getSimpleName();
	
	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public boolean isFakeDirectory() {
		return false;
	}

	/**
	 * 获得并返回所有子文件
	 * 所有的文件内容都将通过system来获得
	 * @return 如果没有对应的system，那么就会返回null
	 */
	@Override
	public SharedLink[] listFiles() {
		Map<String, SharedLink> map = this.map;
		int index = 0;
		int size = map.size();
		Log.d(TAG, "map size :" + size + ", will list out");
		SharedLink[] files = new SharedLink[size];
		
    	Set<String> keySet = map.keySet();
    	Iterator<String> iterator = keySet.iterator();
    	while (iterator.hasNext()) {
    		String key = iterator.next();
    		files[index++] = map.get(key);
    	}
    	
		return files;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public boolean canRead() {
		return super.canRead() && getRealFile().canRead();
	}

	@Override
	public boolean canWrite() {
		return super.canWrite() && getRealFile().canWrite();
	}

	@Override
	public long lastModified() {
		return getRealFile().lastModified();
	}

	@Override
	public boolean exists() {
		return getRealFile().exists();
	}

	@Override
	public boolean delete() {

		if (!canWrite()) {
			Log.e(TAG, "permission denied");
			return false;
		}
		
		File file = getRealFile();
		if (!file.exists()) {
			Log.e(TAG, "需要删除的原文件不存在");
			return false;
		}
		// 目前所有用户都有权限在删除默认账户中所共享的内容
		if (file.delete()) {
			// 尝试将持久化内容删除，并删除文件树中的内容
			String fakePath = getFakePath(), realPath = getRealPath();
			getSystem().unpersist(fakePath);
			// 去持久化可能会失败，但是在下次加载系统的时候，应该能够被删除
			getSystem().deleteSharedLink(fakePath);
			return true;
		} else {
			return false;
		}
		// 对于没有写权限的内容，或者对于defaultSp的内容该怎么处理呢？如果没有权限的情况下，需要使用哪个命令来回复呢？
		// 使用等待的方式来触发,将persist的内容删除后就好
	}

	@Override
	public boolean setLastModified(long time) {
		return getRealFile().setLastModified(time);
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		// 尝试修改真实文件的文件名
		// 检测写权限
		if (!canWrite()) {
			Log.e(TAG, "write permission denied");
			return false;
		}
		// 尝试修改
		File realFile = getRealFile();
		if (realFile == null) {
			// TODO 处理文件不存在的情况
			Log.e(TAG, "file not exist");
			return false;
		}
		// TODO 减少检查
		if (!realFile.isDirectory()) {
			Log.e(TAG, "is not directory");
			return false;
		}
		File toFile = newPath.getRealFile();
		// 尝试重命名
		if (!realFile.renameTo(toFile)) {
			Log.e(TAG, "尝试重命名文件失败");
			return false;
		}
		Log.d(TAG, "realFile重命名成功:" + realFile.getAbsolutePath());
		
		// 准备内容
		String oldFakePath = getFakePath(), newFakePath = newPath.getFakePath();
		String newRealPath = realFile.getParent() + File.separator + toFile.getName();
		// 需要调整父文件中的内容
		SharedLink parent = getSystem().getSharedLink(getParent());
		parent.list().remove(getName());
		// 更新当前文件对象
		setFakePath(newFakePath);
		setRealPath(newRealPath);
		// 向父文件添加新的内容
		parent.list().put(getName(), this);
		// 尝试修正持久化内容
		getSystem().changePersist(oldFakePath, newFakePath, newRealPath);
		return true;
	}
}
