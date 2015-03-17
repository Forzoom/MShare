package org.mshare.file.browser;

import java.io.File;

import android.util.Log;

/**
 * 
 * TODO 如何在刷新文件子内容和减少IO读取之间进行平衡
 * @author HM
 * 
 */
public class MShareFile implements FileBrowserFile {
	private static final String TAG = MShareFile.class.getSimpleName();
	
	// 默认不分享
	private boolean shared = false;
	// 
	private File file;
	
	public MShareFile(File file) {
		this.file = file;
	}

	public MShareFile(String absolutePath) {
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
	 * 获得所有子文件
	 * @return 一个MShareFile的数组, or null if the `list()` == null
	 */
	public FileBrowserFile[] listFiles() {
		Log.d(TAG, "list files");
		
		if (!isDirectory()) { // is directory
			Log.e(TAG, "is not a directory");
			return null;
		}

		String[] fileList = file.list();
		if (fileList == null) {
			 return null;
		}
		
		String dir = getAbsolutePath();
		MShareFile[] ret = new MShareFile[fileList.length];
		
		// 填充结果
		for (int i = 0, len = fileList.length; i < len; i++) {
			ret[i] = new MShareFile(dir + "/" + fileList[i]);
		}
		
		return ret;
	}
	
	/**
	 * 设置文件是否共享
	 * @param shared
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
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
