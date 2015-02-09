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
	 * 获得并返回所有子文件
	 * 所有的文件内容都将通过system来获得
	 * @return 如果没有对应的system，那么就会返回null
	 */
	@Override
	public SharedLink[] listFiles() {
		File realFile = getRealFile();
		File[] files = realFile.listFiles();
		
		// TODO 在创建Directory的时候，就需要将内容加入到文件树中
		for (int i = 0, len = files.length; i < len; i++) {
			File file = files[i];
			// 将所有的文件都放在该文件夹下,并不希望递归来使用，可能会造成效率问题，所以共享文件夹真的是不好
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

	@Override
	public boolean renameTo(SharedLink newPath) {
		// TODO Auto-generated method stub
		return false;
	}
}
