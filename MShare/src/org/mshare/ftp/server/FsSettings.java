/*
Copyright 2011-2013 Pieter Pareit
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mshare.ftp.server;

import java.io.Externalizable;
import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.mshare.main.MShareApp;
import org.mshare.main.MShareUtil;
/**
 * Ŀǰʹ��ԭ�������÷�������ʹ��SharedPreference
 * @author HM
 *
 */
public class FsSettings {

    private static final String TAG = FsSettings.class.getSimpleName();
    public static final String KEY_USERNAME = "username";
    public static final String VALUE_USERNAME_DEFAULT = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String VALUE_PASSWORD_DEFAULT = "password";
    
    // �˿ڣ�Ĭ��21
    public static final String KEY_PORT = "port";
    public static final String VALUE_PORT_DEFAULT = "2121";
    
    // ��������
    public static final String KEY_ALLOW_ANONYMOUS = "allow_anonymous";
    public static final boolean VALUE_ALLOW_ANONYMOUS_DEFAULT = false;
    
    // FTP�������������Ƶ�����ļ���
    public static final String KEY_ROOT_DIR = "root";
    public static final String VALUE_ROOT_DIR_DEFAULT = Environment.getExternalStorageDirectory().getAbsolutePath();
    
    /**
     * ����û�����
     * @return
     */
    public static String getUsername() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getString(KEY_USERNAME, VALUE_USERNAME_DEFAULT);
    }

    /**
     * �������
     * @return
     */
    public static String getPassword() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getString(KEY_PASSWORD, VALUE_PASSWORD_DEFAULT);
    }

    /**
     * Ĭ��ʹ��2121��Ϊ�˿ڣ�
     * @return
     */
    public static int getPort() {
        final SharedPreferences sp = getSharedPreferences();
        // TODO: port is always an number, so store this accordenly
        String portString = sp.getString(KEY_PORT, VALUE_PORT_DEFAULT);
        int port = Integer.valueOf(portString);
        Log.v(TAG, "Using port: " + port);
        return port;
    }
    
    /**
     * �Ƿ���������
     * @return
     */
    public static boolean allowAnoymous() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getBoolean(KEY_ALLOW_ANONYMOUS, VALUE_ALLOW_ANONYMOUS_DEFAULT);
    }

    /**
     * ������չ�洢����ʹ�ã��Ծ���Ҫ��chroot��Ϊһ���ļ����أ� ��ΪFile������һ���������ļ�
     * @return
     */
    public static File getRootDir() {
        final SharedPreferences sp = getSharedPreferences();
        String dirName = sp.getString(KEY_ROOT_DIR, "");
        File rootDir = new File(dirName);
        
        if (!dirName.equals("")) {
            rootDir = Environment.getExternalStorageDirectory();
        } else {
            rootDir = new File(VALUE_ROOT_DIR_DEFAULT); // ��û��ָ��root��ʱ��Ĭ�Ͻ���������Ϊ���Բ����Ķ���
        }
        if (!rootDir.isDirectory()) {
            Log.e(TAG, "getChrootDir: not a directory");
            return null;
        }
        return rootDir;
    }

    /**
     * �����û���
     * @param username
     */
    public static void setUsername(String username) {
    	final SharedPreferences sp = getSharedPreferences();
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString(KEY_USERNAME, username);
    	editor.commit();
    }
    
    /**
     * ��������
     * @param password
     */
    public static void setPassword(String password) {
    	final SharedPreferences sp = getSharedPreferences();
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString(KEY_PASSWORD, password);
    	editor.commit();
    }
    
    /**
     * ���ö˿�
     * @param port
     */
    public static void setPort(String port) {
    	final SharedPreferences sp = getSharedPreferences();
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString(KEY_PORT, port);
    	editor.commit();
    }
    /**
     * �޸ĸ�·������·����Ĭ��ֵ��{@link #VALUE_ROOT_DIR_DEFAULT}��Ҳ������չ�洢��·��������µ�·��������չ�洢·������·������ô�Ͳ����κζ���
     * ��������������£��ļ����ڲ�����һ���ļ��е�����²Ż�ִ���޸�
     * @param root
     */
    public static void setRootDir(String root) {
    	
    	if (root.length() >= VALUE_ROOT_DIR_DEFAULT.length() && root.startsWith(VALUE_ROOT_DIR_DEFAULT)) {
    		
    		File rootDir = new File(root);
    		if (rootDir.exists() && rootDir.isDirectory()) {
    			final SharedPreferences sp = getSharedPreferences();
            	SharedPreferences.Editor editor = sp.edit();
            	editor.putString(KEY_ROOT_DIR, root);
            	editor.commit();
    		} else {
    			Log.w(TAG, "��ָ����·������һ���ļ��У����޸�ԭ·��");
    		}
    	} else {
    		Log.w(TAG, "��Ч��·�������޸�ԭ·��");
    	}
    }
    
    /**
     * �Ƿ񱣳ֻ���״̬
     * @return
     */
    public static boolean shouldTakeFullWakeLock() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getBoolean("stayAwake", false);
    }

    /**
     * ���SharedPreference
     * @return the SharedPreferences for this application
     */
    private static SharedPreferences getSharedPreferences() {
        final Context context = MShareApp.getAppContext();
        return context.getSharedPreferences("server_setting", Context.MODE_PRIVATE);
    }

    // cleaning up after his
    protected static int inputBufferSize = 256;
    protected static boolean allowOverwrite = false;
    protected static int dataChunkSize = 8192; // do file I/O in 8k chunks
    protected static int sessionMonitorScrollBack = 10;
    protected static int serverLogScrollBack = 10;

    public static int getInputBufferSize() {
        return inputBufferSize;
    }

    public static void setInputBufferSize(int inputBufferSize) {
        FsSettings.inputBufferSize = inputBufferSize;
    }

    public static boolean isAllowOverwrite() {
        return allowOverwrite;
    }

    public static void setAllowOverwrite(boolean allowOverwrite) {
        FsSettings.allowOverwrite = allowOverwrite;
    }

    public static int getDataChunkSize() {
        return dataChunkSize;
    }

    public static void setDataChunkSize(int dataChunkSize) {
        FsSettings.dataChunkSize = dataChunkSize;
    }

    public static int getSessionMonitorScrollBack() {
        return sessionMonitorScrollBack;
    }

    public static void setSessionMonitorScrollBack(int sessionMonitorScrollBack) {
        FsSettings.sessionMonitorScrollBack = sessionMonitorScrollBack;
    }

    public static int getServerLogScrollBack() {
        return serverLogScrollBack;
    }

    public static void setLogScrollBack(int serverLogScrollBack) {
        FsSettings.serverLogScrollBack = serverLogScrollBack;
    }

}
