package org.mshare.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class MShareUtil {

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
    public static boolean isConnectedUsingWifi() {
        Context context = MShareApp.getAppContext();
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() == true
                && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
