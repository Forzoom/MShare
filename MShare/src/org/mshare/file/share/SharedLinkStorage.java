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
 * ���ڴ���SharedPreferences�����û����������Ժ��ļ�������
 * 
 * �ļ���������Ӧ�õ�˽�пռ���
 *
 * �����־û����ݺͷǳ־û�����֮�������
 * 
 * SharedLinkSystem�е�SharedLink����Ӧ��������SharedLinkStorage
 * 
 * (Ϊload�����ص�׼��������)ͬʱ������Ҫ�µ�SharedLink�����ʱ��Ӧ���ܹ�����¶��������
 * 
 * TODO ����
 * 
 * TODO org.mshareҲ������Ҫ�Լ����������û�����Ҫ��hash�����Σ��˽�Java��hash����
 * 
 * TODO ��Ҫ���Ǳ�������,�����ļ�SD�����ܱ���
 * 
 * TODO ��Ҫ���������Ժ��ļ����������֣������������ƶ���������,�������ݱ����Ͳ�Ӧ����Account�д���
 * 
 * ���������е����ݶ������־û�
 * 
 * @author HM
 *
 */
public class SharedLinkStorage {
	private static final String TAG = SharedLinkStorage.class.getSimpleName();
	
	/**
	 * ���ڱ�������storage������
	 */
	private static HashMap<String, SharedLinkStorage> allStorages = new HashMap<String, SharedLinkStorage>();
	
	/**
	 * ���ڻ��sp��key
	 */
	private String spKey;
	/**
	 * ���־û������ݶ�ȡ����ӵ�����
	 */
	private ArrayList<SharedLink> list = new ArrayList<SharedLink>();
	
	/**
	 * ����fakePath��realPath����ʼ��SharedLink���󣬷�ΪSharedFile/SharedDirectory/SharedFakeDirectory��������뵽{@link #list}��
	 * TODO ��Ҫ��SharedPreferences��ز����޳�
	 */
	private SharedLinkStorage(String spKey) {
		this.spKey = spKey;
		
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);

		Set<String> keySet = sp.getAll().keySet();
		Iterator<String> iterator = keySet.iterator();
		// SharedLink�ĸ���
		int count = 0;
		
		while (iterator.hasNext()) {
			String fakePath = iterator.next();
			String realPath = sp.getString(fakePath, SharedLinkSystem.REAL_PATH_NONE);
			Log.d(TAG, "fakePath:" + fakePath);
			// ����SharedLink
			SharedLink sharedLink = SharedLink.newSharedLink(fakePath, realPath);
			
			// ������SharedLink��ӵ�ArrayList��
			if (sharedLink != null) {
				Log.d(TAG, "+content:fakePath:" + fakePath + " realPath:" + realPath);
				list.add(sharedLink);
				count++;
			}
		}
		Log.d(TAG, "load " + count + " SharedLink in storage");
	}
	
	/**
	 * ����û���Ӧ��Storage����
	 * @return
	 */
	public static SharedLinkStorage getStorage(String key) {
		Log.d(TAG, "get " + key + "'s storage");
		
		// ʹ��SharedPreferences������sp������Ϊ��������ݣ���������Ǳ���sp��key

		SharedLinkStorage storage = allStorages.get(key);
		if (storage == null) {
			allStorages.put(key, new SharedLinkStorage(key));
		}
		return allStorages.get(key);
	}
	
	/**
	 * �ӳ־û������л��һ������
	 * �����Ǵ�list�л��һ������
	 * @return �ⲻ����Ҫ��������жϾ�����SharedFile/SharedFakeDirectory/SharedDirectory��
	 */
	public SharedLink get(String key) {
		String realPath = get(key, SharedLinkSystem.REAL_PATH_NONE);
		if (!realPath.equals(SharedLinkSystem.REAL_PATH_NONE)) {
			// ��Ҫ����filePermission
			return SharedLink.newSharedLink(key, realPath);
		}
		Log.e(TAG, "key isn't valid");
		return null;
	}
	
	/**
	 * �ײ�ķ��������ڻ��String
	 * @param key
	 * @param defValue ��keyû�ж�Ӧ������ʱ�������ص�Ĭ������
	 * @return
	 */
	public String get(String key, String defValue) {
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}
	
	/**
	 * <p>�ײ㷽����������remove</p>
	 * <p>���ڴӳ־û������г�ȥ</p>
	 * @return �ɹ�ʱ����true
	 */
	public boolean remove(String key) {
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.remove(key);
		return editor.commit();
	}
	
	/**
	 * <p>���ڴӳ־û������г�ȥ����Ҫ��һ��SharedLink</p>
	 * @return �ɹ�ʱ����true
	 */
	public boolean remove(SharedLink sharedLink) {
		if (sharedLink == null) {
			Log.e(TAG, "sharedLink is null");
			return false;
		}
		
		return remove(sharedLink.getFakePath());
	}
	
	/**
	 * <p>�ײ㷽����key��value�����������־û�</p>
	 * @param key 
	 * @param value
	 * @return �־û��ɹ�ʱtrue, ���򷵻�false
	 */
	public boolean set(String key, String value) {
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		return editor.commit();
	}
	
	/**
	 * <p>����û��ļ����е����ݣ����������޸��ڴ�����</p>
	 * TODO ���ǳ־û������޸�
	 * TODO ����ʹ��Transmission����
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
	 * ���Ի�����е����ݣ���Щ���ݶ��ǿ�����{@link SharedLinkSystem}������ʹ�õ�SharedLink����
	 * TODO ��Ҫ�����ڴ�����
	 * @return
	 */
	public SharedLink[] getAll() {
		int count = list.size();
		
		SharedLink[] array = new SharedLink[count];
		// ���صľ���ArrayList�е�����
		return list.toArray(array);
	}
	
	/**
	 * ��õ���ȫ�µĶ�����δ����ӵ�{@link SharedLinkSystem}�У����ø÷��������ɴ������¶���
	 * 
	 * ��Ϊ���������ļ����У�û�а취ʹ�ù���Ա���ļ�����������Ҫclone���½��ļ���
	 * 
	 * @return ����ȫ�µ�SharedLink������δ����system�Լ�permission
	 */
	public static SharedLink[] cloneAndGetAll(SharedLinkStorage fromStorage) {
		// ʹ��getAll������һ���÷���
		SharedLink[] from = fromStorage.getAll();
		SharedLink[] to = new SharedLink[from.length];
		// ���ƴ�list�л�õ�����
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
	 * �޸��û��־û�����
	 * TODO ���ǲ���ҪEditor�������ݣ����⿼��ʹ��Handler��������Ϣ����
	 * @author HM
	 *
	 */
//	private class Editor {
//		
//	}
}
