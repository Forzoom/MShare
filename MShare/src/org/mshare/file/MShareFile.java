package org.mshare.file;

import java.io.File;

import android.util.Log;
// TODO �����ˢ���ļ������ݺͼ���IO��ȡ֮�����ƽ��
/**
 * 
 * @author HM
 * 
 */
public class MShareFile extends File {

	private static final String TAG = "MShareFile";
	
	/**
	 * ��һ��ͨ������`getSubFiles`�õ����ļ��������ļ�
	 */
	private MShareFile[] subFiles = null;
	
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
		return getName();
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
	 * ����������ļ�
	 * @return һ��`MShareFile`������, or null if the `list()` == null
	 */
	public MShareFile[] getSubFiles() {
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
}
