package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.text.TextUtils;
import android.util.Log;

/**
 * 1.伪造的文件系统，所有的内容都应当从这里获得。
 * 2.本地文件系统中的内容也应当从一个本地的SharedSystem中获得额外的内容
 * 3.LinkSystem包含所有被分享的内容
 * 4.FTP客户端所获得的内容应当是SharedFileSystem中的
 * 5.所有对于分享权限的设置应当在SharedFileSystem中
 * 6.SharedFileSystem中的文件树的创建，需要许多的文件路径
 * 可以将文件或者一个文件夹共享，将一个文件夹共享的时候，将不会将文件夹下的内容一一添加，而是在创建文件树的时候添加到其中
 * 只有真实的通过文件路径添加的分享文件，才能够被获得，其他的都应当是用于过渡的文件夹，不可能存在假的文件
 * TODO 文件夹可能并没有对应的真实文件夹,只有文件是真实的，所以不能将整个文件夹都共享，这样是不对的
 * @author HM
 *
 */
public class SharedFileSystem {
	// SharedFileSystem中即是所有被共享的文件的集合
	// 其中构建了一棵树，用来表示所有被共享的文件
	private static final String TAG = SharedFileSystem.class.getSimpleName();
	public static final String SEPARATOR = "/";
	public static final char SEPARATOR_CHAR = '/';
	private SharedFile root = new SharedFile("/mnt/...", "", "");
	private String workingDir = "";
	private SharedFile workingDirFile = root;
	
	public SharedFileSystem() {
		initial();
		root.setType(SharedFile.TYPE_DIRECTORY);
	}
	
	/**
	 * 初始化内容，包括根文件
	 */
	public void initial() {
//		root = 
	}
	
	/**
	 * 添加路径，为SharedFileSystem添加新的内容，可以是文件或者文件夹
	 * @param realPath 只有在添加最终的内容的时候才会被使用
	 * @param path 对应的是SharedFileSystem中的文件路径
	 */
	public void addSharedPath(String realPath, String path) {
		// 分割成碎片
		String[] crumbs = split(path);
		String fileName = null;
		SharedFile file = root, parentFile = null;
		
		if (file == null) {
			Log.e(TAG, "root is null");
			return;
		}
		if (crumbs.length == 0) {
			Log.e(TAG, "根路径不能改变");
			return;
		}
		
		// TODO 是否要检查添加的路径是否是合法的内容
		for (int i = 0, len = crumbs.length - 1; i < len; i++) {
			fileName = crumbs[i];
			
			if (file.isDirectory()) {
				parentFile = file; // 记录父文件
				file = file.list().get(fileName);
			} else {
				// TODO 决定文件是否应该被覆盖
				Log.e(TAG, "可能有文件要被覆盖");
				return;
			}
			
			if (file == null) { // 路径是否存在
				SharedFile newFile = new SharedFile(null, parentFile + SEPARATOR + fileName, fileName);
				newFile.setType(SharedFile.TYPE_FAKE_DIRECTORY);
				parentFile.list().put(crumbs[i], newFile);
			}
		}
		
		fileName = crumbs[crumbs.length - 1];
		if (file.isDirectory()) {
			SharedFile newFile = new SharedFile(realPath, file.getAbsolutePath() + SEPARATOR + fileName, fileName);
			newFile.setType(SharedFile.TYPE_FILE);
			file.list().put(fileName, newFile);
		} else if (file.isFile()) { 
			// TODO 需要覆盖？
		}
	}
	
	// TODO 需要实现类似linux系统的内容,如果有更好的实现办法
	// 可能不需要这个函数，因为Android是在linux系统上，所以File.getCanonicalPath就可以了
	private String getCanonicalPath(String path) {
		return path;
	}
	
	/**
	 * 获得当前工作路径下，所对应的内容
	 * @param pathname
	 * @return
	 */
	public SharedFile getFile(String pathname) {
		
		if (workingDirFile.getAbsolutePath() != workingDir) {
			Log.e(TAG, "workingDirFile is not match to workingDir");
			return null;
		}
		
		String[] crumbs = split(workingDir);
    	SharedFile sf = root;
    	for (int i = 0; i < crumbs.length; i++) {
    		sf = sf.list().get(crumbs[i]);
    		if (sf == null) {
    			Log.w(TAG, "internal file is not exist");
        		return null;
        	}
    	}
    	
		return sf;
	}
	
	public File getRealFile(String pathname) {
//		getFile(pathname).get
		return null;
	}
	
	public String getWorkingDir() {
		return workingDir;
	}
	
	public void setWorkingDir(String workingDir) {
		try {
			// TODO 真实路径可能需要自己来实现
        	this.workingDir = new File(workingDir).getCanonicalPath();
        	
//        	this.workingDirNode = ;
        } catch (IOException e) {
            Log.i(TAG, "SessionThread canonical error");
        }
	}

	public String join(String[] crumbs, int start, int end) {
		// 修正start和end
		start = (start <= 0) ? 0 : start;
		end = (end >= crumbs.length) ? crumbs.length - 1 : end;
		
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			builder.append(crumbs[i]);
		}
		return builder.toString();
	}
	
	public String[] split(String path) {
		
		String[] ret = null;
		int pathLength = path.length();
		
		if (pathLength > 1) {
			// 操作path
			if (path.charAt(0) == SEPARATOR_CHAR) {
				path = path.substring(1);
			}
			ret = path.split(SEPARATOR);
		} else if (pathLength == 1) {
			if (path.charAt(0) == SEPARATOR_CHAR) {
				ret = new String[0];
			} else {
				Log.e(TAG, "the path must be " + SEPARATOR_CHAR);
			}
		} else if (pathLength == 0) {
			ret = new String[0];
		} else {
			Log.e(TAG, "length of String cannot be lesser than 0");
		}
		
		return ret;
	}
	
	/**
	 * 深度优先打印
	 */
	void print() {
		root.print();
	}
}
