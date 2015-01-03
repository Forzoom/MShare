package org.mshare.file;

import java.io.File;
import java.util.ArrayList;

import android.text.TextUtils;
import android.util.Log;

/**
 * implemention of basic stack operation
 * @author HM
 *
 */
public class MShareCrumbs {

	private static final String TAG = "MShareCrumbs";
	
	private static final int DEPTH_DEFAULT = -1;
	
	private static final int DEPTH_MAX_DEFAULT = 10;
	/**
	 * the stack for saving files(to be exact, directory)
	 */
	private MShareFile[] stack = null;
	/**
	 * stack top pointer
	 */
	private int top = DEPTH_DEFAULT;
	/**
	 * the capacity of stack
	 */
	private int maxDepth = DEPTH_MAX_DEFAULT;
	/**
	 * the current maxDepth, often equal to the `top`
	 */
	private int curDepth = DEPTH_DEFAULT;
	/**
	 * path the crumbs represent
	 */
	private String path = null;
	
	public MShareCrumbs(File file) {
		stack = new MShareFile[maxDepth];
		
		push(new MShareFile(file.getAbsolutePath()));
	}
	
	/**
	 * get the top file
	 * @return
	 */
	public MShareFile get() {
		return this.stack[this.top];
	}
	
	/**
	 * get crumbs current maxDepth
	 * @return
	 */
	public int getDepth() {
		return curDepth;
	}
	
	/**
	 * push new `MShareFile` into crumbs and increase `curDepth`
	 * if curDepth less than `top`, `top` will be changed to `curDepth`
	 * @param file
	 */
	public void push(MShareFile file) {
		this.top = ++this.curDepth;
		this.stack[this.curDepth] = file;
		refreshPath();
	}
	
	public boolean canPop() {
		if (this.curDepth > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * pop a `MShareFile` 
	 * @return
	 */
	public MShareFile pop() {
		if (canPop()) {
			MShareFile ret = stack[curDepth];
			stack[curDepth--] = null;
			top = curDepth;
			refreshPath();
			return ret;
		} else {
			return null;
		}
	}
	
	/**
	 * get files in current maxDepth level 
	 * @return curDepth files or null
	 */
	public MShareFile[] getFiles() {
		if (curDepth < maxDepth && stack[curDepth] != null) {
			return stack[curDepth].getSubFiles();
		}
		return null;
	}
	
	/**
	 * can save the result with a property
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * refresh the `path`
	 */
	private void refreshPath() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0, len = this.curDepth; i <= len; i++) {
			list.add(this.stack[i].getName());
		}
		this.path = TextUtils.join(File.separator, list);
	}
	
	/**
	 * set current crumb depth
	 * @param depth
	 */
	public void setDepth(int depth) {
		this.curDepth = depth;
	}
	
}
