package org.mshare.file.share;

import java.util.HashMap;

import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 用于代替SharedPreferences保存用户的配置属性和文件树内容
 * 
 * 文件被保存在应用的私有空间内
 * 
 * 需要保证内存能够被即时的回收
 * 
 * TODO 需要保证文件加密aes-256,java加密和解密的艺术
 * 
 * TODO org.mshare也并不需要自己来创建？用户名需要用hash来掩饰，了解Java的hash内容
 * 
 * TODO 需要考虑备份内容,考虑文件SD卡加密备份
 * 
 * TODO 需要将配置属性和文件树内容区分？
 * 
 * @author HM
 *
 */
public class SharedLinkStorage {

	private static final String TAG = SharedLinkStorage.class.getSimpleName();
	
	/**
	 * 用于保存所有storage的内容
	 */
	private HashMap<String, SharedLinkStorage> allStorages = new HashMap<String, SharedLinkStorage>();
	
	// 需要保存成多例模式
	private SharedLinkStorage() {
		
	}
	
	/**
	 * 获得用户对应的Storage内容
	 * @return
	 */
	public SharedLinkStorage getStorage(String key) {
		Log.d(TAG, "get " + key + "'s storage");
		
		// 使用SharedPreferences，但是sp不能作为保存的内容
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
		
		SharedLinkStorage storage = allStorages.get(key);
		if (storage == null) {
			allStorages.put(key, new SharedLinkStorage());
		}
		return allStorages.get(key);
	}
	
	/**
	 * 对应获得文件树的内容
	 * @return 这不是需要在这里就判断究竟是SharedFile/SharedFakeDirectory/SharedDirectory吗？
	 */
	public SharedLink get(String key) {
		return null;
	}
	
	/**
	 * 添加用户文件树中的内容，仅仅用于修改内存内容
	 * TODO 考虑持久化内容修改
	 * @param key
	 * @param file
	 * @return
	 */
	public boolean set(String key, SharedLink file) {
		return false;
	}
	
	/**
	 * 修改用户持久化内容
	 * @author HM
	 *
	 */
	private class Editor {
		
	}
}
