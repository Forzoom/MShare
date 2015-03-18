package org.mshare.file.browser;

import java.io.File;

import android.util.Log;

/**
 * 
 * TODO 如何在刷新文件子内容和减少IO读取之间进行平衡
 * @author HM
 * 
 */
public class LocalBrowserFile implements FileBrowserFile {
	private static final String TAG = LocalBrowserFile.class.getSimpleName();
	
	// 默认不分享
	private boolean shared = false;
	// 
	private File file;
	
	public LocalBrowserFile(File file) {
		this.file = file;
	}

	public LocalBrowserFile(String absolutePath) {
		this.file = new File(absolutePath);
	}
	
	/**
	 * 判断当前文件是否被管理员设置为共享文件
	 * @return
	 */
	public boolean isShared() {
		return this.shared;
	}
	
	/**
	 * 设置文件是否共享
	 * @param shared
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public File getFile() {
		return this.file;
	}
	
	@Override
	public boolean isFile() {
		return file.isFile();
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public boolean canRead() {
		return file.canRead();
	}

	@Override
	public boolean canWrite() {
		return file.canWrite();
	}
}
