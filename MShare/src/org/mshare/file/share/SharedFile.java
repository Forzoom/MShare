package org.mshare.file.share;

import java.io.File;

import android.util.Log;

// ����Ҫ������ realPath��fakePath
public class SharedFile extends SharedLink {
	private static final String TAG = SharedFile.class.getSimpleName();
	
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

	/**
	 * ɾ��һ���ļ�����Ҫ�Դ洢�ļ������ݽ��д���
	 * ������������ɾ��һ���ļ�
	 * �Ƿ��б�Ҫ:ʵ���첽��ɾ��
	 * ɾ���ļ� ɾ���־û� ɾ���ļ���  
	 */
	@Override
	public boolean delete() {
		
		// ������Ҫ�ж��û���Ȩ����
		if (!canWrite()) {
			Log.e(TAG, "�û�û��Ȩ��ִ��ɾ������");
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
	public boolean exists() {
		return getRealFile().exists();
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
		if (!realFile.isFile()) {
			Log.e(TAG, "is not file");
			return false;
		}
		File toFile = newPath.getRealFile();
		// ����������
		if (!realFile.renameTo(toFile)) {
			Log.e(TAG, "�����������ļ�ʧ��");
			return false;
		}
		Log.d(TAG, "��ʵ�ļ��������ɹ�:" + realFile.getAbsolutePath());
		
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
