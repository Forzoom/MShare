package org.mshare.file;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SharedFakeDirectory extends SharedLink {
	private int mType = TYPE_FAKE_DIRECTORY;
	private long mLastModified = 0l;
	
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
		SharedLink[] files = new SharedLink[size];
		
    	Set<String> keySet = map.keySet();
    	Iterator<String> iterator = keySet.iterator();
    	while (iterator.hasNext()) {
    		String key = iterator.next();
    		files[index] = map.get(key);
    	}
    	
		return files;
	}

	@Override
	public long length() {
		return 0;
	}

	/**
	 * Fake_Directory必须是可读的
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
	public void delete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean mkdir() {
		// 创建一个新的
		return false;
	}

	@Override
	public boolean exists() {
		return true;
	}
}
