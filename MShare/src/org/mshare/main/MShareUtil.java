package org.mshare.main;

import java.io.File;
import java.text.BreakIterator;

import org.mshare.file.SharedLinkSystem;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class MShareUtil {

	public static final int WIFI = ConnectivityManager.TYPE_WIFI;
	public static final int ETHERNET = ConnectivityManager.TYPE_ETHERNET;
	
	/**
	 * �����չ�洢�Ƿ����
	 * @return
	 */
	public static boolean isExternalStorageUsable() {
		String state = Environment.getExternalStorageState();
		// ��������չ�洢ê�����ӣ��ɶ�д��ʱ�������Ч
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
     * Checks to see if we are connected using wifi
     * ����Ƿ���WIFI����
     * @return true if connected using wifi
     */
    public static boolean isConnectedUsing(int type) {
        Context context = MShareApp.getAppContext();
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() == true
                && (ni.getType() & type) != 0;
    }
    
    public static String guessName(String filePath) {
    	// ��ʱʹ��file������������..��.���ֵĿ�����
    	File file = new File(filePath);
    	return file.getName();
    }
    
    /**
     * ��ǰ���������"."���������".."��"."��ʾ��ǰ·��������"."�йص���������Щ
     * ��Ȼ��û��ǲ�Ҫ����.. ��.��
     * @param path
     * @return
     */
    public static String getCanonicalPath(String path) {
    	// �зֳ�crumbs
    	String[] crumbs = SharedLinkSystem.split(path);
    	String[] newCrumbs = new String[crumbs.length];
    	
    	for (int i = 0; i < crumbs.length; i++) {
//    		if ()
    	}
    	
    	return null;
    }
}
