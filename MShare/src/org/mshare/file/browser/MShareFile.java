package org.mshare.file.browser;

import java.io.File;

/**
 * 
 * TODO �����ˢ���ļ������ݺͼ���IO��ȡ֮�����ƽ��
 * @author HM
 * 
 */
public class MShareFile extends File {

	private static final String TAG = MShareFile.class.getSimpleName();
	// Ĭ�ϲ�����
	private boolean shared = false;
	
	// TODO ����������ܹ��ж��ٸ����ļ�������Ļ�������Ҫ���ļ�������н��и���Ĳ���
	// ���������Ƿ����б�Ҫ���أ�
	
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
	 * @param path Pass AbsolutePath such as File.getAbsolutePath()
	 * @param hasParent
	 */
	public MShareFile(String path) {
		super(path);
	}
	
	/**
	 * ��������������ʾ������
	 * @return �ļ�����ʾ�����֣����������{@link #setDisplayName(String)}�Ļ�������ʾ��Ӧ������
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
	
	/**
	 * �����ļ��Ƿ��ǹ����
	 * @param shared
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}
}
