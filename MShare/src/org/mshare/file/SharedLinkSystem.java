package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mshare.ftp.server.FsSettings;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * 1.伪造的文件系统，所有的内容都应当从这里获得。
 * 2.本地文件系统中的内容也应当从一个本地的SharedSystem中获得额外的内容
 * 3.LinkSystem包含所有被分享的内容
 * 4.FTP客户端所获得的内容应当是SharedFileSystem中的
 * 5.所有对于分享权限的设置应当在SharedFileSystem中
 * 7.共享文件夹中所有的内容都是共享的
 * 可以将文件或者一个文件夹共享，将一个文件夹共享的时候，将不会将文件夹下的内容一一添加，而是在创建文件树的时候添加到其中
 * 只有真实的通过文件路径添加的分享文件，才能够被获得，其他的都应当是用于过渡的文件夹，不可能存在假的文件
 * TODO 文件夹可能并没有对应的真实文件夹,只有文件是真实的，所以不能将整个文件夹都共享，这样是不对的
 * TODO 共享文件的存在，如何才能在其他地方显示，现在要如何在文件浏览器中显示共享文件，再开启一个新的文件浏览器实在太麻烦
 * TODO 如何设置账户的权限呢?
 * 如果以后要实现多个账户的情况，那么要如何浏览其他账户的权限内容？
 * TODO 新创建的文件应该放在哪里？扩展存储?即拥有写权限的情况下，需要在扩展存储中设置位置保存内容
 * TODO 需要了解根路径所对应的fakePath是什么，如果是""的话，应该修改为'/'
 * TODO 对于不存在的文件，每次启动的时候都需要清除
 * TODO 需要了解账户的权限设置:读权限，删除权限（写），重写/修改权限（写），执行权限全部为否,FTP在修改文件的时候，别的用户该怎么办？
 * TODO 只是共享文件被删除还是本地文件被删除？
 * 对于权限系统来说:对于管理员来说是权限全开，而对于账户来说拥有的权限需要限制
 * @author HM
 *
 */
public class SharedLinkSystem {
	// SharedFileSystem中即是所有被共享的文件的集合
	// 其中构建了一棵树，用来表示所有被共享的文件
	private static final String TAG = SharedLinkSystem.class.getSimpleName();
	public static final String SEPARATOR = "/";
	public static final char SEPARATOR_CHAR = '/';
	private SharedLink root = null;
	private String workingDirStr = "";
	private SharedLink workingDir = root;
	// 所有需要持久存储的文件
	private ArrayList<String> arr = new ArrayList<String>();
	// 写权限存在情况下所防止的位置
	// TODO 即设置在扩展存储org.mshare文件夹下,并在该文件夹下设置账户对应的文件夹
	private String writePath = null;
	
	public SharedLinkSystem() {
		// TODO 暂时设定为扩展存储的路径
		String realPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		root = SharedLink.newFakeDirectory(this, "");
	}
	
	public void persistSharedPath(String fakePath) {
		
	}
	
	/**
	 * 添加新的内容
	 * 添加路径，为SharedFileSystem添加新的内容，可以是文件或者文件夹
	 * 当realPath为null的时候，会将其作为一个SharedFakeDirectory添加
	 * TODO 如何判断是否添加成功了
	 * @param realPath 只有在添加最终的内容的时候才会被使用
	 * @param fakePath 对应的是SharedFileSystem中的文件路径
	 */
	public boolean addSharedPath(String fakePath, String realPath) {
		// 先添加到持久化中
		// 创建一个路径
//		SharedPath sp = SharedPath.new;
		// 存储fakePath
		
		
		// 分割成碎片
		String[] crumbs = split(fakePath);
		String fileName = null;
		SharedLink file = root, parentFile = null;
		
		if (file == null) {
			Log.e(TAG, "root is null");
			return false;
		}
		if (crumbs.length == 0) {
			Log.e(TAG, "根路径不能改变");
			return false;
		}
		
		// TODO 是否要检查添加的路径是否是合法的内容
		for (int i = 0, len = crumbs.length - 1; i < len; i++) {
			fileName = crumbs[i];
			
			if (file.isDirectory()) {
				parentFile = file; // 记录父文件
				file = file.list().get(fileName);
			} else {
				// TODO 决定文件是否应该被覆盖,临时return false
				Log.e(TAG, "可能有文件要被覆盖");
				return false;
			}
			
			if (file == null) { // 路径是否存在
				// TODO 需要设置fakePath
				SharedLink newFakeDirectory = SharedLink.newFakeDirectory(this, join(new String[] {parentFile.getFakePath(), fileName}));
				parentFile.list().put(fileName, newFakeDirectory);
			}
		}
		
		fileName = crumbs[crumbs.length - 1];
		if (file.isDirectory()) { // 父文件是一个文件夹
			// 添加新的文件
			// TODO 被设置为共享的也有可能是一个文件夹
			// TODO 被设置的内容可能是一个FakeDirectory
			SharedLink newSharedLink = null;
			if (realPath == null) { // 需要设置的是FakeDirectory
				newSharedLink = SharedLink.newFakeDirectory(this, fakePath);
				file.list().put(fileName, newSharedLink);
			} else {
				File realFile = new File(realPath);
				if (realFile.isFile()) {
					newSharedLink = SharedLink.newFile(file.getFakePath() + SEPARATOR + fileName, realPath);
					file.list().put(fileName, newSharedLink);
				} else if (realFile.isDirectory()) {
					newSharedLink = SharedLink.newDirectory(this, file.getFakePath() + SEPARATOR + fileName, realPath);
					file.list().put(fileName, newSharedLink);
				}
			}
			
			if (newSharedLink != null) {
				return true;
			} else {
				return false;
			}
		} else if (file.isFile()) { 
			// TODO 父文件是一个文件，需要覆盖？
			return false;
		} else {
			// TODO 暂时return false
			return false;
		}
	}
	
	/**
	 * 持久化操作
	 */
	public void persist() {
		
	}
	
	public void deleteSharedPath(String fakePath) {
		
	}
	
	// TODO 需要实现类似linux系统的内容,如果有更好的实现办法
	// 可能不需要这个函数，因为Android是在linux系统上，所以File.getCanonicalPath就可以了
	private String getCanonicalPath(String path) {
		return path;
	}
	
	public SharedLink getSharedLink(SharedLink parent, String param) {
		return parent.list().get(param); 
	}
	
	/**
	 * 需要确保所有在文件树中的内容都在指定的文件范围内:例如扩展存储内
	 * @param param
	 * @return 当获取失败的时候，可能是null
	 */
	public SharedLink getSharedLink(String param) {
		// 需要处理获得从root开始寻找的内容
        if (param.charAt(0) == SEPARATOR_CHAR) {
            // The STOR contained an absolute path
            SharedLink sl = this.root;
            
            String[] crumbs = split(param);
            for (int i = 0; i < crumbs.length; i++) {
            	sl = sl.list().get(crumbs[i]);
            	if (sl == null) {
        			Log.w(TAG, "internal file is not exist");
            		return null;
            	}
            }
            
            return sl;
        }

        // 是一个文件，所以就使用当前的内容
        // The STOR contained a relative path
        return workingDir.list().get(param);
	}
	
	/**
	 * TODO 需要修改名字
	 * 获得当前workingDirStr所对应的SharedFile
	 * @return
	 */
	public SharedLink getFile() {
		
		String[] crumbs = split(workingDirStr);
    	SharedLink sf = root;
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
	
	public String getWorkingDirStr() {
		return workingDirStr;
	}
	
	public SharedLink getWorkingDir() {
		return workingDir;
	}
	
	/**
	 * 设置当前的工作路径,包括对于String和SharedFile对象的修改
	 * @param workingDir
	 */
	public void setWorkingDir(String workingDir) {
		try {
			// TODO 真实路径可能需要自己来实现
        	this.workingDirStr = new File(workingDir).getCanonicalPath();
        	
        	this.workingDir = getFile();
        } catch (IOException e) {
            Log.i(TAG, "SessionThread canonical error");
        }
	}

	public String join(String[] crumbs) {
		return join(crumbs, 0, crumbs.length);
	}
	
	public String join(String[] crumbs, int start, int end) {
		// 修正start和end
		start = (start <= 0) ? 0 : start;
		end = (end >= crumbs.length) ? crumbs.length - 1 : end;
		
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (crumbs[i] != null && !crumbs[i].equals("")) {
				builder.append(SEPARATOR + crumbs[i]);
			}
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
