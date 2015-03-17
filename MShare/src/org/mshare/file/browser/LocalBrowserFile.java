package org.mshare.file.browser;

import java.io.File;

import android.util.Log;

/**
 * 
 * TODO �����ˢ���ļ������ݺͼ���IO��ȡ֮�����ƽ��
 * @author HM
 * 
 */
public class LocalBrowserFile implements FileBrowserFile {
	private static final String TAG = LocalBrowserFile.class.getSimpleName();
	
	// Ĭ�ϲ�����
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
	 * �жϵ�ǰ�ļ��Ƿ񱻹���Ա����Ϊ�����ļ�
	 * @return
	 */
	public boolean isShared() {
		return this.shared;
	}
	
	/**
	 * �����ļ��Ƿ���
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
