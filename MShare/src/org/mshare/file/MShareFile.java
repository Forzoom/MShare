package org.mshare.file;

import java.io.File;

import android.util.Log;
// TODO �����ˢ���ļ������ݺͼ���IO��ȡ֮�����ƽ��
/**
 * TODO ��Ӷ��ڷ�����ж�
 * @author HM
 * 
 */
public class MShareFile extends File {

	private static final String TAG = MShareFile.class.getSimpleName();
	// Ĭ�ϲ�����
	private boolean shared = false;
	
	/**
	 * ������ʾ���ļ�������е�����
	 * TODO ����Ҫ����ΪiconName
	 */
	private String mDisplayName;
	
	public MShareFile(File file) {
		super(file.getAbsolutePath());
	}
	
	/**
	 * 
	 * @param path Pass AbsolutePath such as `File.getAbsolutePath()`
	 * @param hasParent
	 */
	public MShareFile(String path) {
		super(path);
	}
	
	/**
	 * ��������������ʾ������
	 * @return �ļ�����ʾ������
	 */
	public String getDisplayName() {
		return mDisplayName == null ? getName() : mDisplayName; 
	}
	/**
	 * ����������ʾ���ļ�������е�����
	 * @param displayName
	 */
	public void setDisplayName(String displayName) {
		this.mDisplayName = displayName;
	}
	
	/**
	 * ����ļ�����չ��
	 * @return
	 */
	public String getExtname() {
		String name = getName();
		int subStart = name.lastIndexOf(".");
		
		if (subStart != -1) {
			return name.substring(subStart);
		} else {
			return "";
		}
	}
	
	public boolean isShared() {
		return this.shared;
	}
	
	/**
	 * ����������ļ�
	 * @return һ��`MShareFile`������, or null if the `list()` == null
	 */
	public MShareFile[] getFiles() {
		// default ret
		MShareFile[] ret = null;
		
		if (this.isDirectory()) { // is directory
			int retIndex = 0;
			String[] fileList = this.list();
			
			if (fileList == null) {
				 return null;
			}
			
			String dir = this.getAbsolutePath();
			ret = new MShareFile[fileList.length];
			
			// fill the ret
			for (int i = 0, len = fileList.length; i < len; i++) {
				ret[retIndex] = new MShareFile(dir + "/" + fileList[i]);
				retIndex++;
			}
		}
		
		return ret;
	}
	
	public void setShared(boolean shared) {
		this.shared = shared;
	}
}
