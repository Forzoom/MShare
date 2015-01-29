package org.mshare.file;

import java.io.File;

import android.util.Log;
// TODO 如何在刷新文件子内容和减少IO读取之间进行平衡
/**
 * TODO 添加对于分享的判定
 * @author HM
 * 
 */
public class MShareFile extends File {

	private static final String TAG = MShareFile.class.getSimpleName();
	// 默认不分享
	private boolean shared = false;
	
	/**
	 * 用于显示在文件浏览器中的名字
	 * TODO 可能要改名为iconName
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
	 * 获得浏览器中所显示的名字
	 * @return 文件所显示的名字
	 */
	public String getDisplayName() {
		return mDisplayName == null ? getName() : mDisplayName; 
	}
	/**
	 * 设置用于显示在文件浏览器中的名字
	 * @param displayName
	 */
	public void setDisplayName(String displayName) {
		this.mDisplayName = displayName;
	}
	
	/**
	 * 获得文件的扩展名
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
	 * 获得所有子文件
	 * @return 一个`MShareFile`的数组, or null if the `list()` == null
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
