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

	@Override
	public SharedLink[] listFiles() {
		File realFile = getRealFile();
		
		File[] files = realFile.listFiles();
		
		for (int i = 0, len = files.length; i < len; i++) {
			File file = files[i];
			if (file.isFile()) {
				SharedLink.newFile(getFakePath() + SharedLinkSystem.SEPARATOR + file.getName(), file.getAbsolutePath());
			} else if (file.isDirectory()) {
//				SharedLink.newDirectory()
			}
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 不知道是不是有这个权限来做这样的事情，所有的权限应该只有读把
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
}
