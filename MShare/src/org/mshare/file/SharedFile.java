package org.mshare.file;
// 所需要的内容 realPath和fakePath
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
	 * 删除一个文件，需要对存储文件的内容进行处理
	 * 并不是真正地删除一个文件
	 */
	@Override
	public boolean delete() {
		// SharedFile可能是持久化的，也可能不是持久化的
		// 无论是否是持久化的，尝试将持久化内容删除，并删除文件树中的内容
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
	
}
