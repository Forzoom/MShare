package org.mshare.file;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;
/**
 * ��SharedPreferences���������realPath��ֵΪ""
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

	/**
	 * Fake_Directory�����ǿɶ���
	 */
	@Override
	public boolean canRead() {
		return true;
	}



	@Override
	public long lastModified() {
		return mLastModified;
	}

	@Override
	public boolean delete() {
		// ��ΪfakeDirectoryҲ����Ҫ�־û���
		getSystem().unpersist(fakePath);
		getSystem().deleteSharedPath(fakePath);
		// TODO ��Ҫ�޸�
		return true;
	}

	@Override
	public boolean mkdir() {
		// ����һ���µ�
		return false;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		
		return false;
	}
}
