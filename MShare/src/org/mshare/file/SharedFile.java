package org.mshare.file;
// ����Ҫ������ realPath��fakePath
public class SharedFile extends SharedLink {
	private int type = TYPE_FILE;
	public SharedFile() {}
	
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
	 */
	@Override
	public void delete() {
//		system.de
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
	
}
