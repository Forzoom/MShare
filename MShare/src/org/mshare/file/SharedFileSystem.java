package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.text.TextUtils;
import android.util.Log;

/**
 * 1.α����ļ�ϵͳ�����е����ݶ�Ӧ���������á�
 * 2.�����ļ�ϵͳ�е�����ҲӦ����һ�����ص�SharedSystem�л�ö��������
 * 3.LinkSystem�������б����������
 * 4.FTP�ͻ�������õ�����Ӧ����SharedFileSystem�е�
 * 5.���ж��ڷ���Ȩ�޵�����Ӧ����SharedFileSystem��
 * 6.SharedFileSystem�е��ļ����Ĵ�������Ҫ�����ļ�·��
 * ���Խ��ļ�����һ���ļ��й�����һ���ļ��й����ʱ�򣬽����Ὣ�ļ����µ�����һһ��ӣ������ڴ����ļ�����ʱ����ӵ�����
 * ֻ����ʵ��ͨ���ļ�·����ӵķ����ļ������ܹ�����ã������Ķ�Ӧ�������ڹ��ɵ��ļ��У������ܴ��ڼٵ��ļ�
 * TODO �ļ��п��ܲ�û�ж�Ӧ����ʵ�ļ���,ֻ���ļ�����ʵ�ģ����Բ��ܽ������ļ��ж����������ǲ��Ե�
 * @author HM
 *
 */
public class SharedFileSystem {
	// SharedFileSystem�м������б�������ļ��ļ���
	// ���й�����һ������������ʾ���б�������ļ�
	private static final String TAG = SharedFileSystem.class.getSimpleName();
	public static final String SEPARATOR = "/";
	public static final char SEPARATOR_CHAR = '/';
	private SharedFile root = new SharedFile("/mnt/...", "", "");
	private String workingDir = "";
	private SharedFile workingDirFile = root;
	
	public SharedFileSystem() {
		initial();
		root.setType(SharedFile.TYPE_DIRECTORY);
	}
	
	/**
	 * ��ʼ�����ݣ��������ļ�
	 */
	public void initial() {
//		root = 
	}
	
	/**
	 * ���·����ΪSharedFileSystem����µ����ݣ��������ļ������ļ���
	 * @param realPath ֻ����������յ����ݵ�ʱ��Żᱻʹ��
	 * @param path ��Ӧ����SharedFileSystem�е��ļ�·��
	 */
	public void addSharedPath(String realPath, String path) {
		// �ָ����Ƭ
		String[] crumbs = split(path);
		String fileName = null;
		SharedFile file = root, parentFile = null;
		
		if (file == null) {
			Log.e(TAG, "root is null");
			return;
		}
		if (crumbs.length == 0) {
			Log.e(TAG, "��·�����ܸı�");
			return;
		}
		
		// TODO �Ƿ�Ҫ�����ӵ�·���Ƿ��ǺϷ�������
		for (int i = 0, len = crumbs.length - 1; i < len; i++) {
			fileName = crumbs[i];
			
			if (file.isDirectory()) {
				parentFile = file; // ��¼���ļ�
				file = file.list().get(fileName);
			} else {
				// TODO �����ļ��Ƿ�Ӧ�ñ�����
				Log.e(TAG, "�������ļ�Ҫ������");
				return;
			}
			
			if (file == null) { // ·���Ƿ����
				SharedFile newFile = new SharedFile(null, parentFile + SEPARATOR + fileName, fileName);
				newFile.setType(SharedFile.TYPE_FAKE_DIRECTORY);
				parentFile.list().put(crumbs[i], newFile);
			}
		}
		
		fileName = crumbs[crumbs.length - 1];
		if (file.isDirectory()) {
			SharedFile newFile = new SharedFile(realPath, file.getAbsolutePath() + SEPARATOR + fileName, fileName);
			newFile.setType(SharedFile.TYPE_FILE);
			file.list().put(fileName, newFile);
		} else if (file.isFile()) { 
			// TODO ��Ҫ���ǣ�
		}
	}
	
	// TODO ��Ҫʵ������linuxϵͳ������,����и��õ�ʵ�ְ취
	// ���ܲ���Ҫ�����������ΪAndroid����linuxϵͳ�ϣ�����File.getCanonicalPath�Ϳ�����
	private String getCanonicalPath(String path) {
		return path;
	}
	
	/**
	 * ��õ�ǰ����·���£�����Ӧ������
	 * @param pathname
	 * @return
	 */
	public SharedFile getFile(String pathname) {
		
		if (workingDirFile.getAbsolutePath() != workingDir) {
			Log.e(TAG, "workingDirFile is not match to workingDir");
			return null;
		}
		
		String[] crumbs = split(workingDir);
    	SharedFile sf = root;
    	for (int i = 0; i < crumbs.length; i++) {
    		sf = sf.list().get(crumbs[i]);
    		if (sf == null) {
    			Log.w(TAG, "internal file is not exist");
        		return null;
        	}
    	}
    	
		return sf;
	}
	
	public File getRealFile(String pathname) {
//		getFile(pathname).get
		return null;
	}
	
	public String getWorkingDir() {
		return workingDir;
	}
	
	public void setWorkingDir(String workingDir) {
		try {
			// TODO ��ʵ·��������Ҫ�Լ���ʵ��
        	this.workingDir = new File(workingDir).getCanonicalPath();
        	
//        	this.workingDirNode = ;
        } catch (IOException e) {
            Log.i(TAG, "SessionThread canonical error");
        }
	}

	public String join(String[] crumbs, int start, int end) {
		// ����start��end
		start = (start <= 0) ? 0 : start;
		end = (end >= crumbs.length) ? crumbs.length - 1 : end;
		
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			builder.append(crumbs[i]);
		}
		return builder.toString();
	}
	
	public String[] split(String path) {
		
		String[] ret = null;
		int pathLength = path.length();
		
		if (pathLength > 1) {
			// ����path
			if (path.charAt(0) == SEPARATOR_CHAR) {
				path = path.substring(1);
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
		
		return ret;
	}
	
	/**
	 * ������ȴ�ӡ
	 */
	void print() {
		root.print();
	}
}
