package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mshare.ftp.server.FsSettings;
import org.mshare.ftp.server.SessionThread;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * TODO 目前对于不合法的path，都将被SharedLinkSystem自动删除
 * 保存文件系统并要求用户手动保存文件系统，或者提供按钮用来清除无效的Link对象
 * 关键是持久化内容保持太多容易造成效率问题，但随意删除持久化内容又会造成文件丢失，能否对当前正在使用的SD卡作为唯一性判断
 * 如果realPath中的内容消失了，那么也不应该立即就删除持久化path，而是需要记录说当前path无效，并且不显示，等待下次检测文件的时候，发现realPath真的不存在的情况下再去除持久化
 * 不允许在父文件夹没有创建的时候，创建其子文件夹,所以path的添加顺序很重要
 * 1.所有不正确的持久化path都会被删除，因此持久化内容的改变也需要注意
 * 2.本地文件显示也应当从一个本地的SharedLinkSystem中获得内容
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
	// 用来获得Account，但是其实只要设定了所需要存储的位置即可,主要是SharedPreferences需要通过Account对象获得
	private SessionThread sessionThread;
	
	public SharedLinkSystem(SessionThread sessionThread) {
		this.sessionThread = sessionThread;
		// TODO 暂时设定为扩展存储的路径
		String realPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		root = SharedLink.newFakeDirectory(this, "");
	}
	
	/**
	 * 添加新的内容
	 * 添加路径，为SharedFileSystem添加新的内容，可以是文件或者文件夹
	 * 当realPath为null的时候，会将其作为一个SharedFakeDirectory添加
	 * 如果添加的是一个共享文件夹，那么就要将共享文件夹下的所有内容递归加入文件树
	 * 但是共享文件夹下的内容不会被持久化
	 * 当遇到无法添加到文件树中的Path的时候，也会将其持久化内容删除
	 * TODO 如何判断是否添加成功了
	 * @param fakePath 对应的是SharedFileSystem中的文件路径，不能为""或者"/"
	 * @param realPath 只有在添加最终的内容的时候才会被使用
	 */
	public boolean addSharedPath(String fakePath, String realPath) {
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
				
				if (file == null) {
					// 路径不存在的情况下，将判定为路径不合法
					Log.e(TAG, "无法添加path，路径不合法");
					return false;
				}
			} else {
				// 所有持久化后添加的path都应该是正确的
				Log.e(TAG, "无法添加path,路径不合法");
				return false;
			}
		}
		
		fileName = crumbs[crumbs.length - 1];
		if (file.isDirectory() || file.isFakeDirectory()) { // 父文件是一个文件夹
			// 添加新的文件
			// TODO 被设置为共享的也有可能是一个文件夹
			// TODO 被设置的内容可能是一个FakeDirectory
			SharedLink newSharedLink = null;
			if (realPath == null) {
				// realPath为空，需要设置的是FakeDirectory
				newSharedLink = SharedLink.newFakeDirectory(this, fakePath);
				file.list().put(fileName, newSharedLink);
			} else {
				File realFile = new File(realPath);
				if (realFile.isFile()) {
					newSharedLink = SharedLink.newFile(this, file.getFakePath() + SEPARATOR + fileName, realPath);
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
		} else { 
			// TODO 删除持久化
			Log.e(TAG, "父文件不是一个文件夹");
			return false;
		}
	}
	
	/**
	 * 将文件树中的节点删除
	 * @param fakePath
	 */
	public void deleteSharedPath(String fakePath) {
		// getSharedLink并不是为了在这个时候使用的
		// 因为如果fakePath中是文件名的话，那么就会得到working directory文件夹下的内容
		// TODO 所以需要保证fakePath是相对路径
		SharedLink toDelete = getSharedLink(fakePath);
		if (toDelete != null) {
			SharedLink parent = toDelete.getParent();
			// TODO 直接从文件树中删除，不知道是否会造成内存溢出，map中的内容不知是否会被回收
			parent.list().remove(toDelete.getName());
		}
	}
	
	/**
	 * 持久化操作
	 * 将所有的内容添加到SharedPreferences中，关于FakeDirectory，该怎么办？
	 */
	public void persist(String fakePath, String realPath) {
		SharedPreferences sp = sessionThread.getAccount().getSharedPreferences();
		// TODO 为了保存fakePath和realPath的联系，可能需要更好的持久化方式
		// 因为所需要的保存的内容可能会有很多，包括对于文件的其他信息的保存
		// 可能需要更新信息的操作按钮
		Editor editor = sp.edit();
		if (realPath != null && !realPath.equals("")) {
			editor.putString(fakePath, realPath);
		} else {
			// TODO 将空改为
			editor.putString(fakePath, "");
		}
		editor.commit();
	}
	
	/**
	 * 仅仅是为了删除被持久化的内容
	 * @param fakePath
	 */
	public void unpersist(String fakePath) {
		SharedPreferences sp = sessionThread.getAccount().getSharedPreferences();
		// 没法找到更好的defaultValue,使用文件名中不许可的
		String realPath = sp.getString(fakePath, "|");
		// 文件并不存在
		if (realPath == "|") {
			// do nothing
		} else if (realPath.equals("")) { // 所对应的是fakeDirectory
			Editor editor = sp.edit();
			editor.putString(fakePath, null); // 设置null将删除对应内容
		} else {
			// 剩下的情况就是要将文件树中的内容删除
		}
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
