package org.mshare.file.share;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * 用于代替SharedPreferences保存用户的配置属性和文件树内容
 * 
 * 文件被保存在应用的私有空间内
 *
 * 构建持久化内容和非持久化内容之间的桥梁
 * 
 * SharedLinkSystem中的SharedLink对象应该来自于SharedLinkStorage
 * 
 * (为load函数特地准备的内容)同时，当需要新的SharedLink对象的时候，应该能够获得新对象的数组
 * 
 * TODO 加密
 * 
 * TODO org.mshare也并不需要自己来创建？用户名需要用hash来掩饰，了解Java的hash内容
 * 
 * TODO 需要考虑备份内容,考虑文件SD卡加密备份
 * 
 * TODO 需要将配置属性和文件树内容区分？将配置内容移动到这里面,配置内容本来就不应该在Account中处理
 * 
 * 所有在其中的内容都将被持久化
 * 
 * @author HM
 *
 */
public class SharedLinkStorage {
	private static final String TAG = SharedLinkStorage.class.getSimpleName();
	
	/**
	 * 用于保存所有storage的内容
	 */
	private static HashMap<String, SharedLinkStorage> allStorages = new HashMap<String, SharedLinkStorage>();
	
	/**
	 * 用于获得sp的key
	 */
	private String spKey;
	/**
	 * 将持久化的内容读取后添加到其中
	 */
	private ArrayList<SharedLink> list = new ArrayList<SharedLink>();
	
	/**
	 * 根据fakePath和realPath，初始化SharedLink对象，分为SharedFile/SharedDirectory/SharedFakeDirectory，将其加入到{@link #list}中
	 * TODO 需要将SharedPreferences相关部分剔除
	 */
	private SharedLinkStorage(String spKey) {
		this.spKey = spKey;
		
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);

		Set<String> keySet = sp.getAll().keySet();
		Iterator<String> iterator = keySet.iterator();
		// SharedLink的个数
		int count = 0;
		
		while (iterator.hasNext()) {
			String fakePath = iterator.next();
			String realPath = sp.getString(fakePath, SharedLinkSystem.REAL_PATH_NONE);
			Log.d(TAG, "fakePath:" + fakePath);
			// 创建SharedLink
			SharedLink sharedLink = SharedLink.newSharedLink(fakePath, realPath);
			
			// 将所有SharedLink添加到ArrayList中
			if (sharedLink != null) {
				Log.d(TAG, "+content:fakePath:" + fakePath + " realPath:" + realPath);
				list.add(sharedLink);
				count++;
			}
		}
		Log.d(TAG, "load " + count + " SharedLink in storage");
	}
	
	/**
	 * 获得用户对应的Storage内容
	 * @return
	 */
	public static SharedLinkStorage getStorage(String key) {
		Log.d(TAG, "get " + key + "'s storage");
		
		// 使用SharedPreferences，但是sp不能作为保存的内容，这里仅仅是保存sp的key

		SharedLinkStorage storage = allStorages.get(key);
		if (storage == null) {
			allStorages.put(key, new SharedLinkStorage(key));
		}
		return allStorages.get(key);
	}
	
	/**
	 * 从持久化内容中获得一个对象
	 * 而不是从list中获得一个对象？
	 * @return 这不是需要在这里就判断究竟是SharedFile/SharedFakeDirectory/SharedDirectory吗？
	 */
	public SharedLink get(String key) {
		String realPath = get(key, SharedLinkSystem.REAL_PATH_NONE);
		if (!realPath.equals(SharedLinkSystem.REAL_PATH_NONE)) {
			// 需要修正filePermission
			return SharedLink.newSharedLink(key, realPath);
		}
		Log.e(TAG, "key isn't valid");
		return null;
	}
	
	/**
	 * 底层的方法，用于获得String
	 * @param key
	 * @param defValue 当key没有对应的内容时候所返回的默认内容
	 * @return
	 */
	public String get(String key, String defValue) {
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}
	
	/**
	 * <p>底层方法，无条件remove</p>
	 * <p>用于从持久化内容中除去</p>
	 * @return 成功时返回true
	 */
	public boolean remove(String key) {
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.remove(key);
		return editor.commit();
	}
	
	/**
	 * <p>用于从持久化内容中除去，需要是一个SharedLink</p>
	 * @return 成功时返回true
	 */
	public boolean remove(SharedLink sharedLink) {
		if (sharedLink == null) {
			Log.e(TAG, "sharedLink is null");
			return false;
		}
		
		return remove(sharedLink.getFakePath());
	}
	
	/**
	 * <p>底层方法，key和value将被无条件持久化</p>
	 * @param key 
	 * @param value
	 * @return 持久化成功时true, 否则返回false
	 */
	public boolean set(String key, String value) {
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		return editor.commit();
	}
	
	/**
	 * <p>添加用户文件树中的内容，仅仅用于修改内存内容</p>
	 * TODO 考虑持久化内容修改
	 * TODO 考虑使用Transmission事务
	 * @param key
	 * @param file
	 * @return
	 */
	public boolean set(SharedLink file) {
		if (file == null) {
			Log.e(TAG, "SharedLink is null");
			return false;
		}
		return set(file.getFakePath(), file.getRealPath());
	}
	
	/**
	 * 尝试获得所有的内容，这些内容都是可能在{@link SharedLinkSystem}中正在使用的SharedLink对象
	 * TODO 需要降低内存消耗
	 * @return
	 */
	public SharedLink[] getAll() {
		int count = list.size();
		
		SharedLink[] array = new SharedLink[count];
		// 返回的就是ArrayList中的内容
		return list.toArray(array);
	}
	
	/**
	 * 获得的是全新的对象，尚未被添加到{@link SharedLinkSystem}中，调用该方法将生成大量的新对象
	 * 
	 * 因为在其他的文件树中，没有办法使用管理员的文件树，所以需要clone并新建文件树
	 * 
	 * @return 所有全新的SharedLink对象，尚未分配system以及permission
	 */
	public static SharedLink[] cloneAndGetAll(SharedLinkStorage fromStorage) {
		// 使用getAll并不是一个好方法
		SharedLink[] from = fromStorage.getAll();
		SharedLink[] to = new SharedLink[from.length];
		// 复制从list中获得的内容
		for (int i = 0, len = from.length; i < len; i++) {
			SharedLink fromLink = from[i];
			String fakePath = fromLink.getFakePath(), realPath = fromLink.getRealPath();
			if (fromLink.isFile()) {
				to[i] = SharedLink.newFile(fakePath, realPath);
			} else if (fromLink.isDirectory()) {
				to[i] = SharedLink.newDirectory(fakePath, realPath);
			} else if (fromLink.isFakeDirectory()) {
				to[i] = SharedLink.newFakeDirectory(fakePath);
			}
		}
		return to;
	}
	
	/**
	 * 修改用户持久化内容
	 * TODO 考虑不需要Editor，简化内容，另外考虑使用Handler来进行消息处理
	 * @author HM
	 *
	 */
//	private class Editor {
//		
//	}
}
