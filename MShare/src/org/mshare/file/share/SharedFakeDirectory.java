package org.mshare.file.share;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mshare.ftp.server.SessionThread;

import android.util.Log;
/**
 * 在SharedPreferences中所保存的realPath的值为""
 * @author HM
 *
 */
public class SharedFakeDirectory extends SharedLink {
	private static final String TAG = SharedFakeDirectory.class.getSimpleName();
	private int mType = TYPE_FAKE_DIRECTORY;
	private long mLastModified = 0l;
	
	public SharedFakeDirectory(SharedLinkSystem system) {
		super(system);
	}
	
	public boolean setLastModified(long lastModified) {
		mLastModified = lastModified;
		return true;
	}
	
	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isFakeDirectory() {
		return true;
	}
	
	@Override
	public SharedLink[] listFiles() {
		Map<String, SharedLink> map = this.map;
		int index = 0;
		int size = map.size();
		Log.d(TAG, "map size :" + size);
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
	public long lastModified() {
		return mLastModified;
	}

	@Override
	public boolean delete() {
		// 因为fakeDirectory也是需要持久化的
		getSystem().unpersist(getFakePath());
		getSystem().deleteSharedLink(getFakePath());
		// TODO 需要修改
		return true;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		if (newPath == null) {
			Log.e(TAG, "没有告知新的名字");
			return false;
		}
		
		String oldFakePath = getFakePath(), newFakePath = newPath.getFakePath();
		// 删除父文件中的旧内容
		SharedLink parent = getSystem().getSharedLink(getParent());
		parent.list().remove(getName());
		// 修正文件树中的内容
		setFakePath(newFakePath);
		// 向父文件中添加新内容
		parent.list().put(getName(), this);
		// 修正持久化内容
		// 将newRealPath设置null表示fakeDirectory
		getSystem().changePersist(oldFakePath, newFakePath, SharedLinkSystem.REAL_PATH_FAKE_DIRECTORY);
		
		return true;
	}

}
