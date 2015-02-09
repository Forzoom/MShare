package org.mshare.file;

import java.io.File;

public class SharedDirectory extends SharedLink {
	private int type = TYPE_DIRECTORY;

	public SharedDirectory(SharedLinkSystem system) {
		super(system);
	}
	
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
		File realFile = getRealFile();
		File[] files = realFile.listFiles();
		
		// TODO �ڴ���Directory��ʱ�򣬾���Ҫ�����ݼ��뵽�ļ�����
		for (int i = 0, len = files.length; i < len; i++) {
			File file = files[i];
			// �����е��ļ������ڸ��ļ�����,����ϣ���ݹ���ʹ�ã����ܻ����Ч�����⣬���Թ����ļ�������ǲ���
			// 
			getSystem().addSharedPath(getFakePath() + SharedLinkSystem.SEPARATOR + file.getName(), file.getAbsolutePath());
		}
		
		return null;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public boolean canRead() {
		return getRealFile().canRead();
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
		// TODO 
		return false;
	}

	/**
	 * ��֪���ǲ��������Ȩ���������������飬���е�Ȩ��Ӧ��ֻ�ж���
	 */
	@Override
	public boolean mkdir() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setLastModified(long time) {
		return getRealFile().setLastModified(time);
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		// TODO Auto-generated method stub
		return false;
	}
}
