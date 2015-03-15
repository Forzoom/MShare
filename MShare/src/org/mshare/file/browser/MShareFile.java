package org.mshare.file.browser;

import java.io.File;

/**
 * 
 * TODO 如何在刷新文件子内容和减少IO读取之间进行平衡
 * @author HM
 * 
 */
public class MShareFile extends File {

	private static final String TAG = MShareFile.class.getSimpleName();
	// 默认不分享
	private boolean shared = false;
	
	// TODO 如果设置了总共有多少个子文件被共享的话，就需要在文件浏览器中进行更多的操作
	// 但是这样是否是有必要的呢？
	
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
	 * @param path Pass AbsolutePath such as File.getAbsolutePath()
	 * @param hasParent
	 */
	public MShareFile(String path) {
		super(path);
	}
	
	/**
	 * 获得浏览器中所显示的名字
	 * @return 文件所显示的名字，如果设置了{@link #setDisplayName(String)}的话，将显示对应的内容
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
	 * 设置文件是否是共享的
	 * @param shared
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}
}
