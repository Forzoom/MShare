package org.mshare.file.browser;

import java.io.File;

import android.util.Log;

/**
 * 
 * TODO �����ˢ���ļ������ݺͼ���IO��ȡ֮�����ƽ��
 * @author HM
 * 
 */
public class MShareFile implements FileBrowserFile {
	private static final String TAG = MShareFile.class.getSimpleName();
	
	// Ĭ�ϲ�����
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
	 * �жϵ�ǰ�ļ��Ƿ񱻹���Ա����Ϊ�����ļ�
	 * @return
	 */
	public boolean isShared() {
		return this.shared;
	}
	
	/**
	 * ����������ļ�
	 * @return һ��MShareFile������, or null if the `list()` == null
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
		
		// �����
		for (int i = 0, len = fileList.length; i < len; i++) {
			ret[i] = new MShareFile(dir + "/" + fileList[i]);
		}
		
		return ret;
	}
	
	/**
	 * �����ļ��Ƿ���
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
