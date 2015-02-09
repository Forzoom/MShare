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
	 * 检查扩展存储是否可用
	 * @return
	 */
	public static boolean isExternalStorageUsable() {
		String state = Environment.getExternalStorageState();
		// 仅仅当扩展存储锚点连接，可读写的时候才算有效
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
     * Checks to see if we are connected using wifi
     * 检测是否是WIFI环境
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
    	// 暂时使用file来操作，减少..和.出现的可能行
    	File file = new File(filePath);
    	return file.getName();
    }
    
    /**
     * 当前不允许出现"."，允许出现".."，"."表示当前路径，对于"."有关的内容有哪些
     * 当然最好还是不要出现.. 和.了
     * @param path
     * @return
     */
    public static String getCanonicalPath(String path) {
    	// 切分成crumbs
    	String[] crumbs = SharedLinkSystem.split(path);
    	String[] newCrumbs = new String[crumbs.length];
    	
    	for (int i = 0; i < crumbs.length; i++) {
//    		if ()
    	}
    	
    	return null;
    }
}
