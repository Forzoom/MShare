package org.mshare.file.share;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

/**
 * �����ļ����е����ݣ�����SharedDirectory�е����ݣ��ļ����ж��У����ǲ�û�б��־û�
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
	 * ��ò������������ļ�
	 * ���е��ļ����ݶ���ͨ��system�����
	 * @return ���û�ж�Ӧ��system����ô�ͻ᷵��null
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
			Log.e(TAG, "��Ҫɾ����ԭ�ļ�������");
			return false;
		}
		// Ŀǰ�����û�����Ȩ����ɾ��Ĭ���˻��������������
		if (file.delete()) {
			// ���Խ��־û�����ɾ������ɾ���ļ����е�����
			String fakePath = getFakePath(), realPath = getRealPath();
			getSystem().unpersist(fakePath);
			// ȥ�־û����ܻ�ʧ�ܣ��������´μ���ϵͳ��ʱ��Ӧ���ܹ���ɾ��
			getSystem().deleteSharedLink(fakePath);
			return true;
		} else {
			return false;
		}
		// ����û��дȨ�޵����ݣ����߶���defaultSp�����ݸ���ô�����أ����û��Ȩ�޵�����£���Ҫʹ���ĸ��������ظ��أ�
		// ʹ�õȴ��ķ�ʽ������,��persist������ɾ����ͺ�
	}

	@Override
	public boolean setLastModified(long time) {
		return getRealFile().setLastModified(time);
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		// �����޸���ʵ�ļ����ļ���
		// ���дȨ��
		if (!canWrite()) {
			Log.e(TAG, "write permission denied");
			return false;
		}
		// �����޸�
		File realFile = getRealFile();
		if (realFile == null) {
			// TODO �����ļ������ڵ����
			Log.e(TAG, "file not exist");
			return false;
		}
		// TODO ���ټ��
		if (!realFile.isDirectory()) {
			Log.e(TAG, "is not directory");
			return false;
		}
		File toFile = newPath.getRealFile();
		// ����������
		if (!realFile.renameTo(toFile)) {
			Log.e(TAG, "�����������ļ�ʧ��");
			return false;
		}
		Log.d(TAG, "realFile�������ɹ�:" + realFile.getAbsolutePath());
		
		// ׼������
		String oldFakePath = getFakePath(), newFakePath = newPath.getFakePath();
		String newRealPath = realFile.getParent() + File.separator + toFile.getName();
		// ��Ҫ�������ļ��е�����
		SharedLink parent = getSystem().getSharedLink(getParent());
		parent.list().remove(getName());
		// ���µ�ǰ�ļ�����
		setFakePath(newFakePath);
		setRealPath(newRealPath);
		// ���ļ�����µ�����
		parent.list().put(getName(), this);
		// ���������־û�����
		getSystem().changePersist(oldFakePath, newFakePath, newRealPath);
		return true;
	}
}
