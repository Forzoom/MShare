package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mshare.ftp.server.Account;
import org.mshare.ftp.server.AccountFactory;
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
 * BUG 当需要上传的文件名因为编码的不同而导致文件名乱码的时候，上传文件将失败
 * TODO 为管理员文件添加共享的文件夹，其中再分支出来，分为音乐，视频等等
 * TODO 需要考虑更好的方法来保存这些内容，考虑B+树，在树形结构上仍能递归处理
 * TODO 使用AccountFactory来管理Account账户的创建和回收,当用户QUIT的时候，或者是超时（是否需要设置超时？）的时候，会将Account中的sessionThread的register回收
 * TODO 管理员账户并不存在文件树，所有对于管理员账户中所有持久化内容，当一棵文件树被构建的时候，都将在新的文件树中创建
 * TODO 测试文件权限系统是否有效
 * TODO 当有文件被更新的时候，服务器必须能够通知客户端
 * TODO 在SharedLink中添加文件是否正在被使用isUsing?
 * TODO 客户端应当根据返回的文件权限进行相应的显示处理
 * 在第一次创建账户时可能会比较慢
 * 当前服务器不希望在传递的路径中有..或者.的内容
 * TODO 当扩展存储不存在的时候，不允许的许多操作，服务器端只能在cmd里面对失败做出响应吗
 * 客户端没有办法知道服务器是出现了什么样的问题导致了不可用
 * TODO 需要了解MediaScan相关的类
 * TODO 可能需要一个刷新按钮,就像一个文件浏览器一样？
 * 默认账户中的内容也能够被删除，因为现在所共享的文件都是默认账户中的内容
 * TODO 测试共享文件夹的功能
 * 如何在SharedLinkSystem启动的时候，创建文件树，应该只需要告诉SharedLinkSystem当前登录的对象是谁就可以创建
 * TODO 对于不合法的path，都将被SharedLinkSystem自动删除?
 * 保存文件系统并要求用户手动保存文件系统，或者提供按钮用来清除无效的Link对象
 * 关键是持久化内容保持太多容易造成效率问题，但随意删除持久化内容又会造成文件丢失，能否对当前正在使用的SD卡作为唯一性判断
 * 如果realPath中的内容消失了，那么也不应该立即就删除持久化path，而是需要记录说当前path无效，并且不显示，等待下次检测文件的时候，发现realPath真的不存在的情况下再去除持久化
 * 不允许在父文件夹没有创建的时候，创建其子文件夹,所以path的添加顺序很重要
 * 1.所有不正确的持久化path都会被删除，因此持久化内容的改变也需要注意
 * 2.本地文件显示也应当从一个本地的SharedLinkSystem中获得内容
 * SharedDirectory中所有的内容都是共享的
 * 可以将文件或者一个文件夹共享，将一个文件夹共享的时候，将不会将文件夹下的内容一一添加，而是在创建文件树的时候添加到其中
 * 只有真实的通过文件路径添加的分享文件，才能够被获得，其他的都应当是用于过渡的文件夹，不可能存在假的文件
 * TODO 文件夹可能并没有对应的真实文件夹,只有文件是真实的，所以不能将整个文件夹都共享，这样是不对的
 * TODO 共享文件的存在，如何才能在其他地方显示，现在要如何在文件浏览器中显示共享文件，再开启一个新的文件浏览器实在太麻烦
 * 如果以后要实现多个账户的情况，那么要如何浏览其他账户的权限内容？
 * TODO 需要了解根路径所对应的fakePath是什么，如果是""的话，应该修改为'/'
 * TODO 对于不存在的文件，每次启动的时候都需要清除
 * TODO 需要了解账户的权限设置:读权限，删除权限（写），重写/修改权限（写），执行权限全部为否,FTP在修改文件的时候，别的用户该怎么办？
 * TODO 只是共享文件被删除还是本地文件被删除？
 * 对于权限系统来说:对于管理员来说是权限全开，而对于账户来说拥有的权限需要限制
 * 
 * TODO 管理员该如何显示内容呢？在MShareFileBrowser中？需要在文件浏览器启动的时候在文件树中查看内容，但是文件树所对应的realPath并不知道在哪里，是不是需要构造一个真实的文件树
 * 
 * 文件共享层的存在是为了支持多用户拥有不同的共享权限和内容
 * 上传文件夹并不是共享文件夹
 * 
 * 当有持久化内容被更新的时候，将会通知所有的Session
 * 
 * <h3>关于文件权限</h3>
 * 需要区分普通用户和管理员所创建的文件
 * 对于普通用户的文件，对应的用户拥有其读写权限，管理员对文件有读写权限
 * 对于管理员文件，管理员有读写权限，普通用户仅拥有读权限
 * 所有的FTP用户都是普通用户，仅仅是服务器本身是管理员用户
 * @author HM
 *
 */
public class SharedLinkSystem {
	// SharedFileSystem中即是所有被共享的文件的集合
	// 其中构建了一棵树，用来表示所有被共享的文件
	private static final String TAG = SharedLinkSystem.class.getSimpleName();
	public static final String SEPARATOR = "/";
	public static final char SEPARATOR_CHAR = '/';
	// 根一旦被确定就无法修改，这样就没有办法使用unprepare方法
	private final SharedLink root;
	private SharedLink workingDir;
	// 上传文件所存放的位置
	// TODO 即设置在扩展存储org.mshare文件夹下,并在该文件夹下设置账户对应的文件夹
	private String uploadPath = null;
	// 只是用来获得Account和SharedPreferences
	/**
	 * 所有上传文件存放的位置，默认为sd卡下的org.mshare文件夹
	 * TODO uploadRoot需要上层的设置
	 */
	public static String uploadRoot;
	/**
	 * SharedLinkSystem所对应的Account
	 */
	private Account mAccount;
	/**
	 * 用以表明当前文件树是否已经准备完成，可以使用
	 */
	private boolean prepared = false;
	/**
	 * 因为对于SharedPreferences中返回的realPath可能会是正常的，也可能为""，即fakeDirectory的情况
	 * 为了表示在SharedPreferences中没有该realPath，所以使用|来表示，因为|不可能出现在文件名
	 * TODO 当文件上传时，需要判断文件名是否合法
	 */
	public static final String REAL_PATH_NONE = "|";
	/**
	 * 所有{@link SharedFakeDirectory}的realPath
	 */
	public static final String REAL_PATH_FAKE_DIRECTORY = "";
	
	/**
	 * 普通用户所创建的文件拥有写权限
	 */
	public static final int FILE_PERMISSION_USER = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN
			| Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
	/**
	 * 管理员所创建的文件
	 * 普通用户仅仅拥有读权限
	 */
	public static final int FILE_PERMISSION_ADMIN = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN
			| Permission.PERMISSION_READ | Permission.PERMISSION_READ_GUEST;
	
	/**
	 * 回调内容
	 */
	private Callback mCallback;
	
	// TODO 获得文件树对应的Account,如果Account是空的怎么办？要不要使用多例模式？当Account为null的时候，将无法获得一个SharedLinkSystem，或者是使用initial函数来让文件树prepare
	// 将构造函数中的一些操作移动到prepare函数中
	public SharedLinkSystem(Account account) {
		this.mAccount = account;
		// 根没有被持久化，所以需要每次自行创建,根文件只有读权限
		root = SharedLink.newFakeDirectory(this, SEPARATOR, Permission.PERMISSION_READ_ALL);
		
		// 设置当前的working directory为"/"
		setWorkingDir(SEPARATOR); // root作为working directory
	}
	
	/**
	 * 需要使用prepared来确保文件树被创建了
	 * prepared要怎么使用呢，用来判断
	 */
	public void prepare() {
		if (prepared) {
			Log.d(TAG, "already prepared");
			return;
		}
		
		// TODO 这样不好,需要将这些内容放在哪里加载呢？
		if (getAccount().isUser() || getAccount().isGuest()) {
			// 加载管理员文件
		}
		if (getAccount() != null) {
			SharedPreferences privateSp = getAccount().getSharedPreferences();
			load(privateSp, SharedLinkSystem.FILE_PERMISSION_USER);
		}
		
		prepareUpload();
		prepared = true;
	}
	
	/**
	 * 尝试多个持久化内容添加到文件树中
	 * 需要自己调用
	 * TODO 需要修正load，不应该使用sp，应该更能够够被改变
	 * @param sp 将尝试添加其中所有以"/"开头的内容
	 * @param filePermission 添加的文件的权限 {@link #FILE_PERMISSION_ADMIN}, {@link #FILE_PERMISSION_USER}
	 */
	public void load(SharedPreferences sp, int filePermission) {
		Log.d(TAG, "start load");
		Iterator<String> iterator = sp.getAll().keySet().iterator();
		int count = 0;
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			// 判断fakePath的第一位是否是'/'
			Log.d(TAG, "fakePath:" + key);
			if (key.charAt(0) == SEPARATOR_CHAR) {
				// 不可能在keySet中有，但是在sp中没有的情况
				String value = sp.getString(key, REAL_PATH_NONE);
				Log.d(TAG, "+content:fakePath:" + key + " realPath:" + value);
				if (addSharedLink(key, value, filePermission)) {
					count++;
				}
			}
		}
		Log.d(TAG, "end load, add " + count + " SharedLink object");
	}
	
	/**
	 * 准备存放上传文件的位置，创建真实的文件夹
	 */
	private void prepareUpload() {
		uploadPath = getAccount().getUpload();
		
		// 创建的上传文件夹
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			Log.e(TAG, "接收上传文件的文件夹不存在");
			// TODO 同样需要保证uploadDir是我们自己创建的"文件夹"
			if (uploadDir.mkdir()) {
				Log.d(TAG, "创建上传文件夹成功");
			} else {
				Log.e(TAG, "创建上传文件夹失败");
			}
		}
	}
	
	/**
	 * 根据当前Account的类型来添加文件
	 * @param fakePath
	 * @param realPath
	 * @return
	 */
	public boolean addSharedPath(String fakePath, String realPath) {
		int filePermission = mAccount.isAdministrator() ? FILE_PERMISSION_ADMIN : FILE_PERMISSION_USER;
		return addSharedLink(fakePath, realPath, filePermission);
	}
	
	/**
	 * 为SharedFileSystem添加新的内容，可以是文件或者文件夹
	 * 当realPath为{@link #REAL_PATH_FAKE_DIRECTORY}时，将其作为SharedFakeDirectory添加
	 * 如果添加的是一个共享文件夹，那么就要将共享文件夹下的所有内容递归加入文件树
	 * TODO ?当遇到无法添加到文件树中的Path的时候，也会将其持久化内容删除
	 * 
	 * 当所添加的真实文件并不存在的时候，共享层文件不会被显示出来
	 * 
	 * @param fakePath 对应的是SharedFileSystem中的文件路径，不能为""或者"/",并且必须是'/'开头
	 * @param realPath 只有在添加最终的内容的时候才会被使用
	 * @param filePermission 所添加的文件权限
	 * @return 成功返回true
	 */
	public boolean addSharedLink(String fakePath, String realPath, int filePermission) {
		if (!prepared) {
			Log.e(TAG, "invoke prepare first!");
			return false;
		}
		// split
		String[] crumbs = split(fakePath);
		// 检测fakePath是否有效
		if (!isFakePathValid(fakePath) || crumbs.length == 0) {
			Log.e(TAG, "invalid fakePath");
			return false;
		}
		
		Log.d(TAG, "+文件树: fakePath:" + fakePath + " realPath:" + realPath);
		String fileName = null;
		SharedLink file = root;
		
		// 是否要检查添加的路径是否是合法的内容
		for (int i = 0, len = crumbs.length - 1; i < len; i++) {
			fileName = crumbs[i];
			
			if (file.isDirectory()) {
				file = file.list().get(fileName);
				
				if (file == null) {
					// 路径不存在的情况下，将判定为路径不合法
					Log.e(TAG, "invalid path");
					return false;
				}
			} else {
				// 所有持久化后添加的path都应该是正确的
				Log.e(TAG, "invalid path");
				return false;
			}
		}
		
		fileName = crumbs[crumbs.length - 1];
		if (file.isDirectory() || file.isFakeDirectory()) { // 父文件是一个文件夹
			// 添加新的文件
			// TODO 被设置为共享的可能是一个Directoyr或者FakeDirectory
			SharedLink newSharedLink = null;
			
			if (realPath == REAL_PATH_FAKE_DIRECTORY) {
				// realPath为空，需要设置的是FakeDirectory
				newSharedLink = SharedLink.newFakeDirectory(this, fakePath, filePermission);
				Log.d(TAG, "+SharedFakeDirectory -> " + file.getFakePath());
			} else {
				File realFile = new File(realPath);
				if (realFile.exists()) {
					if (realFile.isFile()) {
						newSharedLink = SharedLink.newFile(this, fakePath, realPath, filePermission);
						Log.d(TAG, "+SharedFile -> " + file.getFakePath());
					} else if (realFile.isDirectory()) {
						newSharedLink = SharedLink.newDirectory(this, fakePath, realPath, filePermission);
						Log.d(TAG, "+SharedDirectory -> " + file.getFakePath());
						Log.d(TAG, "try add files in SharedDirectory");
						// 需要将共享文件夹下的所有内容都添加到文件树中
						// 对于文件，将会以SharedFile的形式加入文件树，对于文件夹，将已SharedDirectory的方式加入文件树
						File[] files = realFile.listFiles();
						for (int index = 0, len = files.length; index < len; index++) {
							File f = files[index];
							String _fakePath = getFakePath(newSharedLink.getFakePath(), f.getName()), _realPath = f.getAbsolutePath();
							// TODO 尝试添加,可能会出现错误
							addSharedLink(_fakePath, _realPath, filePermission);
						}
					}
				} else {
					Log.e(TAG, "真实文件并不存在");
					// TODO 是否需要删除该持久化内容
				}
			}
			
			if (newSharedLink != null) {
				file.list().put(fileName, newSharedLink);
				if (mCallback != null) {
					mCallback.onAdd();
				}
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
	 * @param fakePath 既支持相对路径，也支持文件名
	 */
	public boolean deleteSharedLink(String fakePath) {
		// getSharedLink并不是为了在这个时候使用的
		// 因为如果fakePath中是文件名的话，那么就会得到working directory文件夹下的内容
		// TODO 所以需要保证fakePath是相对路径
		
		SharedLink toDelete = getSharedLink(fakePath);
		if (toDelete != null) {
			SharedLink parent = getSharedLink(toDelete.getParent());
			// TODO 直接从文件树中删除，不知道是否会造成内存溢出，map中的内容不知是否会被回收
			if (parent.list().remove(toDelete.getName()) != null) {
				if (mCallback != null) {
					mCallback.onDelete();
				}
				Log.d(TAG, "delete success");
				return true;
			} else {
				Log.e(TAG, "delete fail");
				return false;
			}
		} else {
			Log.e(TAG, "file is not exist");
			return false;
		}
	}
	
	/**
	 * 持久化内容，调用commonPersist，sp对应Account的sp
	 * 将所有的内容添加到SharedPreferences中
	 */
	public boolean persist(String fakePath, String realPath) {
		SharedPreferences sp = getAccount().getSharedPreferences();
		boolean persistResult = false;
		// TODO 为了保存fakePath和realPath的联系，可能需要更好的持久化方式
		// 因为所需要的保存的内容可能会有很多，包括对于文件的其他信息的保存
		// 可能需要更新信息的操作按钮
		if (realPath == null) {
			Log.e(TAG, "realPath is null, should invoke unpersist()?");
			return false;
		}
		Editor editor = sp.edit();
		editor.putString(fakePath, realPath);
		persistResult = editor.commit();
		Log.d(TAG, "+persist: fakePath:" + fakePath + " realPath:" + realPath + " result:" + persistResult);
		if (persistResult) {
			if (mCallback != null) {
				mCallback.onPersist(fakePath, realPath);
			}
		}
		return persistResult;
	}
	
	/**
	 * 仅仅是为了删除被持久化的内容
	 * 在defaultSp中的内容不会被删除
	 * 在调用的时候，可能需要非持久话的文件是在默认账户中的
	 * @param fakePath
	 */
	public boolean unpersist(String fakePath) {
		SharedPreferences sp = getAccount().getSharedPreferences();
		boolean persistResult = false;
		String realPath = sp.getString(fakePath, REAL_PATH_NONE);
		// 文件并不存在
		if (realPath.equals(REAL_PATH_NONE)) { // 并不存在对应的持久化内容，为什么
			// do nothing
			return false;
		}
		
		Editor editor = sp.edit();
		editor.remove(fakePath);
		persistResult = editor.commit();
		Log.d(TAG, "-unpersist: fakePath:" + fakePath + " realPath:" + realPath + " result:" + persistResult);
		if (persistResult) {
			if (mCallback != null) {
				mCallback.onUnpersist(fakePath);
			}
		}
		return persistResult;
	}
	
	/**
	 * 修正持久化内容，一般用于修改文件名
	 * TODO 需要修正的内容部分在两个部分，不好解决
	 * 将尝试在private的部分修正持久化内容
	 */
	public boolean changePersist(String oldFakePath, String newFakePath, String newRealPath) {
		Log.d(TAG, "修正持久化内容");
		SharedPreferences sp = getAccount().getSharedPreferences();
		if (!sp.getString(oldFakePath, REAL_PATH_NONE).equals(REAL_PATH_NONE)) {
			Editor editor = sp.edit();
			// 删除原本内容
			editor.remove(oldFakePath);
			Log.d(TAG, "-oldFakePath :" + oldFakePath);
			editor.putString(newFakePath, newRealPath);
			Log.d(TAG, "+newFakePath :" + newFakePath + " newRealPath :" + newRealPath);
			boolean changeResult = editor.commit();
			Log.d(TAG, "=result: " + changeResult);
			return changeResult;
		} else {
			Log.e(TAG, "没有找到对应持久化内容");
			return false;
		}
	}
	
	// TODO 需要实现类似linux系统的内容,如果有更好的实现办法
	// 可能不需要这个函数，因为Android是在linux系统上，所以File.getCanonicalPath就可以了
	private String getCanonicalPath(String path) {
		return path;
	}
	
	/**
	 * 获得parent中的文件
	 * @param parent 
	 * @param param 仅仅支持文件名
	 * @return 可能返回null
	 */
	public SharedLink getSharedLink(SharedLink parent, String param) {
		return parent.list().get(param); 
	}
	
	/**
	 * 需要确保所有在文件树中的内容都在指定的文件范围内:例如扩展存储内
	 * @param param 可以是相对路径，或者是文件名
	 * @return 当获取失败的时候，可能是null
	 */
	public SharedLink getSharedLink(String param) {
		// 需要处理获得从root开始寻找的内容
        if (param.charAt(0) == SEPARATOR_CHAR) {
            // The STOR contained an absolute path
            SharedLink sl = this.root;
            Log.d(TAG, "root size:" + root.list().size());
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
	 * @param parent
	 * @param param
	 * @return
	 */
	public static String getFakePath(String parent, String param) {
		if (param.charAt(0) != SEPARATOR_CHAR) {
			if (parent.equals(SEPARATOR)) {
				return parent + param;
			} else {
				return parent + SEPARATOR + param;
			}
		} else {
			return param;
		}
	}
	
	/**
	 * 因为getSharedLink并不能获得不存在的SharedLinnk，所以需要使用该方法来获得一个path
	 * @return
	 */
	public String getFakePath(String param) {
		return getFakePath(getWorkingDirStr(), param);
	}
	
	/**
	 * 该函数的存在，是为了让所有的fakePath有统一的方式来获得父对象的内容
	 * 通过fakePath，获得该fakePath所对应的父文件内容
	 * @param fakePath
	 * @return
	 */
	public static String getParent(String fakePath) {
		if (!fakePath.equals(SEPARATOR)) {
			
			int lastIndex = fakePath.lastIndexOf(SharedLinkSystem.SEPARATOR);
			String parentFakePath = null;
			if (lastIndex == 0) {
				parentFakePath = SharedLinkSystem.SEPARATOR;
			} else {
				parentFakePath = fakePath.substring(0, lastIndex);
			}
			return parentFakePath;
		} else {
			Log.e(TAG, "root没有父文件");
			return null;
		}
	}
	
	/**
	 * 当前用于接收上传文件的路径
	 * @return
	 */
	public String getUploadDirPath() {
		return uploadPath;
	}
	
	/**
	 * 当前的工作路径
	 * @return
	 */
	public String getWorkingDirStr() {
		return workingDir.getFakePath();
	}
	
	/**
	 * 当前工作路径所对应的SharedLink对象
	 * @return
	 */
	public SharedLink getWorkingDir() {
		return workingDir;
	}
	
	/**
	 * 设置当前的工作路径,包括对于String和SharedFile对象的修改
	 * @param workingDir 支持文件名和相对路径
	 */
	public void setWorkingDir(String workingDir) {
		Log.d(TAG, "set working dir");
    	this.workingDir = getSharedLink(workingDir);
	}
	
	/**
	 * 用来获得对应的Account对象
	 * @return
	 */
	private Account getAccount() {
		return mAccount;
	}
	
	/**
	 * 只能有一个Callback,新的Callback将会顶替旧的Callback
	 * @see Callback
	 */
	public void setCallback(Callback callback) {
		mCallback = callback;
	}
	
	public static String join(String[] crumbs) {
		return join(crumbs, 0, crumbs.length);
	}
	
	public static String join(String[] crumbs, int start, int end) {
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
	
	public static String[] split(String path) {
		
		String[] ret = null;
		int pathLength = path.length();
		
		if (pathLength > 1) {
			// 操作path
			if (path.charAt(0) == SEPARATOR_CHAR) {
				path = path.substring(1);
			}
			if (path.charAt(path.length() - 1) == SEPARATOR_CHAR) {
				path = path.substring(0, path.length() - 1);
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
		
		Log.d(TAG, "split result size :" + ret.length);
		return ret;
	}
	
	public boolean isPrepared() {
		return prepared;
	}

	public static boolean isFakePathValid(String fakePath) {
		return fakePath.charAt(0) == SEPARATOR_CHAR;
	}
	
	public int getAccountPermission() {
		return mAccount.getPermission();
	}
	
	/**
	 * 
	 * @author HM
	 *
	 */
	public class Permission {
		// 仅仅是作为帐号的权限，映射在用户文件上
	    public static final int PERMISSION_READ_ADMIN = 0400;
	    public static final int PERMISSION_WRITE_ADMIN = 0200;
	    public static final int PERMISSION_EXECUTE_ADMIN = 0100;// execute永远不开放
	    
	    public static final int PERMISSION_READ = 040;
	    public static final int PERMISSION_WRITE = 020;
	    public static final int PERMISSION_EXECUTE = 010;// execute永远不开放
	    
	    public static final int PERMISSION_READ_GUEST = 04;
	    public static final int PERMISSION_WRITE_GUEST = 02;
	    public static final int PERMISSION_EXECUTE_GUEST = 01;// execute永远不开放
	    
	    public static final int PERMISSION_READ_ALL = 0444;
	    public static final int PERMISSION_WRITE_ALL = 0222;
	    public static final int PERMISSION_EXECUTE_ALL = 0111;// execute永远不开放

	    public static final int PERMISSION_NONE = 0;
	    
	}
	
	/**
	 * 当文件树发生变化的时候的回调函数
	 * @author HM
	 *
	 */
	public interface Callback {
		public void onPersist(String fakePath, String realPath);
		public void onUnpersist(String fakePath);
		/**
		 * 当调用了{@link SharedLinkSystem#addSharedLink(String, String, int)}时的回调函数
		 * 只有成功时才会调用
		 */
		public void onAdd();
		/**
		 * 当调用了{@link SharedLinkSystem#deleteSharedLink(String)}时的回调函数
		 * 只有成功时才会调用
		 */
		public void onDelete();
	}
}
