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
 * 目前使用原本的设置方法，即使用SharedPreference
 * @author HM
 *
 */
public class FsSettings {

    private static final String TAG = FsSettings.class.getSimpleName();
    public static final String KEY_USERNAME = "username";
    public static final String VALUE_USERNAME_DEFAULT = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String VALUE_PASSWORD_DEFAULT = "password";
    
    // 端口：默认21
    public static final String KEY_PORT = "port";
    public static final String VALUE_PORT_DEFAULT = "2121";
    
    // 允许匿名
    public static final String KEY_ALLOW_ANONYMOUS = "allow_anonymous";
    public static final boolean VALUE_ALLOW_ANONYMOUS_DEFAULT = false;
    
    // FTP服务器所被限制的最大文件夹
    public static final String KEY_ROOT_DIR = "root";
    public static final String VALUE_ROOT_DIR_DEFAULT = Environment.getExternalStorageDirectory().getAbsolutePath();
    
    // 上传文件存放路径
    public static final String KEY_UPLOAD = "upload";
    // TODO 这个路径可能会出错
    public static final String VALUE_UPLOAD_DEFAULT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "org.mshare";
    
    /**
     * 获得用户名称
     * @return
     */
    public static String getUsername() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getString(KEY_USERNAME, VALUE_USERNAME_DEFAULT);
    }

    /**
     * 获得密码
     * @return
     */
    public static String getPassword() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getString(KEY_PASSWORD, VALUE_PASSWORD_DEFAULT);
    }

    /**
     * 默认使用2121作为端口？
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
     * 是否允许匿名
     * @return
     */
    public static boolean allowAnoymous() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getBoolean(KEY_ALLOW_ANONYMOUS, VALUE_ALLOW_ANONYMOUS_DEFAULT);
    }

    /**
     * TODO 目前SharedLinkSystem中的上传路径和该上传路径独立
     * 如果路径不存在，将使用mkdirs来创建
     * @return
     */
    public static String getUpload() {
    	final SharedPreferences sp = getSharedPreferences();
        String uploadPath = sp.getString(KEY_UPLOAD, VALUE_UPLOAD_DEFAULT);
        
        File uploadRoot = new File(uploadPath);
        // TODO 如何保证文件夹存在呢，如果不存在，就不能再使用共享文件层吧
        if (!uploadRoot.exists()) {
        	uploadRoot.mkdirs();
        }
        
        Log.v(TAG, "upload path : " + uploadPath);
        return uploadPath; 
    }
    
    /**
     * 设置用户名
     * @param username
     */
    public static void setUsername(String username) {
    	final SharedPreferences sp = getSharedPreferences();
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString(KEY_USERNAME, username);
    	editor.commit();
    }
    
    /**
     * 设置密码
     * @param password
     */
    public static void setPassword(String password) {
    	final SharedPreferences sp = getSharedPreferences();
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString(KEY_PASSWORD, password);
    	editor.commit();
    }
    
    /**
     * 设置端口
     * @param port
     */
    public static void setPort(String port) {
    	final SharedPreferences sp = getSharedPreferences();
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString(KEY_PORT, port);
    	editor.commit();
    }

    public static void setUpload(String uploadPath) {
    	final SharedPreferences sp = getSharedPreferences();
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString(KEY_UPLOAD, uploadPath);
    	editor.commit();
    }
    
    /**
     * 是否保持唤醒状态
     * @return
     */
    public static boolean shouldTakeFullWakeLock() {
        final SharedPreferences sp = getSharedPreferences();
        return sp.getBoolean("stayAwake", false);
    }

    /**
     * 获得SharedPreference
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
