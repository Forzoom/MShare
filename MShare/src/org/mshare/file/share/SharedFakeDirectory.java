package org.mshare.file.share;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mshare.ftp.server.SessionThread;

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

	@Override
	public long lastModified() {
		return mLastModified;
	}

	@Override
	public boolean delete() {
		// ��ΪfakeDirectoryҲ����Ҫ�־û���
		getSystem().unpersist(getFakePath());
		getSystem().deleteSharedLink(getFakePath());
		// TODO ��Ҫ�޸�
		return true;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		if (newPath == null) {
			Log.e(TAG, "û�и�֪�µ�����");
			return false;
		}
		
		String oldFakePath = getFakePath(), newFakePath = newPath.getFakePath();
		// ɾ�����ļ��еľ�����
		SharedLink parent = getSystem().getSharedLink(getParent());
		parent.list().remove(getName());
		// �����ļ����е�����
		setFakePath(newFakePath);
		// ���ļ������������
		parent.list().put(getName(), this);
		// �����־û�����
		// ��newRealPath����null��ʾfakeDirectory
		getSystem().changePersist(oldFakePath, newFakePath, SharedLinkSystem.REAL_PATH_FAKE_DIRECTORY);
		
		return true;
	}

}
