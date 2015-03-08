package org.mshare.file.share;

import java.util.HashMap;

import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * ���ڴ���SharedPreferences�����û����������Ժ��ļ�������
 * 
 * �ļ���������Ӧ�õ�˽�пռ���
 * 
 * ��Ҫ��֤�ڴ��ܹ�����ʱ�Ļ���
 * 
 * TODO ��Ҫ��֤�ļ�����aes-256,java���ܺͽ��ܵ�����
 * 
 * TODO org.mshareҲ������Ҫ�Լ����������û�����Ҫ��hash�����Σ��˽�Java��hash����
 * 
 * TODO ��Ҫ���Ǳ�������,�����ļ�SD�����ܱ���
 * 
 * TODO ��Ҫ���������Ժ��ļ����������֣�
 * 
 * @author HM
 *
 */
public class SharedLinkStorage {

	private static final String TAG = SharedLinkStorage.class.getSimpleName();
	
	/**
	 * ���ڱ�������storage������
	 */
	private HashMap<String, SharedLinkStorage> allStorages = new HashMap<String, SharedLinkStorage>();
	
	// ��Ҫ����ɶ���ģʽ
	private SharedLinkStorage() {
		
	}
	
	/**
	 * ����û���Ӧ��Storage����
	 * @return
	 */
	public SharedLinkStorage getStorage(String key) {
		Log.d(TAG, "get " + key + "'s storage");
		
		// ʹ��SharedPreferences������sp������Ϊ���������
		Context context = MShareApp.getAppContext();
		SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
		
		SharedLinkStorage storage = allStorages.get(key);
		if (storage == null) {
			allStorages.put(key, new SharedLinkStorage());
		}
		return allStorages.get(key);
	}
	
	/**
	 * ��Ӧ����ļ���������
	 * @return �ⲻ����Ҫ��������жϾ�����SharedFile/SharedFakeDirectory/SharedDirectory��
	 */
	public SharedLink get(String key) {
		return null;
	}
	
	/**
	 * ����û��ļ����е����ݣ����������޸��ڴ�����
	 * TODO ���ǳ־û������޸�
	 * @param key
	 * @param file
	 * @return
	 */
	public boolean set(String key, SharedLink file) {
		return false;
	}
	
	/**
	 * �޸��û��־û�����
	 * @author HM
	 *
	 */
	private class Editor {
		
	}
}
