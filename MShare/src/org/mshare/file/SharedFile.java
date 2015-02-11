package org.mshare.file;

import java.io.File;

import android.util.Log;

// ����Ҫ������ realPath��fakePath
public class SharedFile extends SharedLink {
	private static final String TAG = SharedFile.class.getSimpleName();
	
	private int type = TYPE_FILE;
	public SharedFile(SharedLinkSystem system) {
		super(system);
	}
	
	@Override
	public boolean isFile() {
		return true;
	}
	@Override
	public boolean isDirectory() {
		return false;
	}
	@Override
	public boolean isFakeDirectory() {
		return false;
	}

	@Override
	public SharedLink[] listFiles() {
		return null;
	}

	@Override
	public long length() {
		return getRealFile().length();
	}

	@Override
	public boolean canRead() {
		return getRealFile().canRead();
	}

	@Override
	public long lastModified() {
		return getRealFile().lastModified();
	}

	/**
	 * ɾ��һ���ļ�����Ҫ�Դ洢�ļ������ݽ��д���
	 * ������������ɾ��һ���ļ�
	 * �Ƿ��б�Ҫ:ʵ���첽��ɾ��
	 * ɾ���ļ� ɾ���־û� ɾ���ļ���  
	 */
	@Override
	public boolean delete() {
		
		// ������Ҫ�ж��û���Ȩ����
		if (!getSystem().getAccount().canWrite()) {
			return false;
		}
		
		File file = getRealFile();
		if (!file.exists()) {
			Log.e(TAG, "��Ҫɾ����ԭ�ļ�������");
			return false;
		}
		if (file.isDirectory()) {
			Log.e(TAG, "����ɾ��һ���ļ���");
			// TODO ��Ҫ�������ڵ���ʾ���ݣ����ǽ��ó־û�·��ɾ����
			return false;
		}
		// Ŀǰ�����û�����Ȩ����ɾ��Ĭ���˻��������������
		if (file.delete()) {
			// ���Խ��־û�����ɾ������ɾ���ļ����е�����
			if (getSystem().unpersist(fakePath)) {
				// ȥ�־û����ܻ�ʧ�ܣ��������´μ���ϵͳ��ʱ��Ӧ���ܹ���ɾ��
				SharedLinkSystem.unpersistAll(fakePath, realPath);
			}
			getSystem().deleteSharedPath(fakePath);
			return true;
		} else {
			return false;
		}
		// ����û��дȨ�޵����ݣ����߶���defaultSp�����ݸ���ô�����أ����û��Ȩ�޵�����£���Ҫʹ���ĸ��������ظ��أ�
		// ʹ�õȴ��ķ�ʽ������,��persist������ɾ����ͺ�
	}

	@Override
	public boolean exists() {
		return getRealFile().exists();
	}

	/**
	 * do nothing
	 */
	@Override
	public boolean mkdir() {
		return false;
	}

	@Override
	public boolean setLastModified(long time) {
		return getRealFile().setLastModified(time);
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		// �����޸���ʵ�ļ����ļ���
		// ���дȨ��
		if (!getSystem().getAccount().canWrite()) {
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
		if (!realFile.isFile()) {
			Log.e(TAG, "is not file");
			return false;
		}
		File toFile = newPath.getRealFile();
		// ����������
		if (realFile.renameTo(toFile)) {
			return true;
		}
		
		return false;
	}
	
}
