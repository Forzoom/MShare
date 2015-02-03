package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mshare.ftp.server.FsSettings;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * 1.α����ļ�ϵͳ�����е����ݶ�Ӧ���������á�
 * 2.�����ļ�ϵͳ�е�����ҲӦ����һ�����ص�SharedSystem�л�ö��������
 * 3.LinkSystem�������б����������
 * 4.FTP�ͻ�������õ�����Ӧ����SharedFileSystem�е�
 * 5.���ж��ڷ���Ȩ�޵�����Ӧ����SharedFileSystem��
 * 7.�����ļ��������е����ݶ��ǹ����
 * ���Խ��ļ�����һ���ļ��й�����һ���ļ��й����ʱ�򣬽����Ὣ�ļ����µ�����һһ��ӣ������ڴ����ļ�����ʱ����ӵ�����
 * ֻ����ʵ��ͨ���ļ�·����ӵķ����ļ������ܹ�����ã������Ķ�Ӧ�������ڹ��ɵ��ļ��У������ܴ��ڼٵ��ļ�
 * TODO �ļ��п��ܲ�û�ж�Ӧ����ʵ�ļ���,ֻ���ļ�����ʵ�ģ����Բ��ܽ������ļ��ж����������ǲ��Ե�
 * TODO �����ļ��Ĵ��ڣ���β����������ط���ʾ������Ҫ������ļ����������ʾ�����ļ����ٿ���һ���µ��ļ������ʵ��̫�鷳
 * TODO ��������˻���Ȩ����?
 * ����Ժ�Ҫʵ�ֶ���˻����������ôҪ�����������˻���Ȩ�����ݣ�
 * TODO �´������ļ�Ӧ�÷��������չ�洢?��ӵ��дȨ�޵�����£���Ҫ����չ�洢������λ�ñ�������
 * TODO ��Ҫ�˽��·������Ӧ��fakePath��ʲô�������""�Ļ���Ӧ���޸�Ϊ'/'
 * TODO ���ڲ����ڵ��ļ���ÿ��������ʱ����Ҫ���
 * TODO ��Ҫ�˽��˻���Ȩ������:��Ȩ�ޣ�ɾ��Ȩ�ޣ�д������д/�޸�Ȩ�ޣ�д����ִ��Ȩ��ȫ��Ϊ��,FTP���޸��ļ���ʱ�򣬱���û�����ô�죿
 * TODO ֻ�ǹ����ļ���ɾ�����Ǳ����ļ���ɾ����
 * ����Ȩ��ϵͳ��˵:���ڹ���Ա��˵��Ȩ��ȫ�����������˻���˵ӵ�е�Ȩ����Ҫ����
 * @author HM
 *
 */
public class SharedLinkSystem {
	// SharedFileSystem�м������б�������ļ��ļ���
	// ���й�����һ������������ʾ���б�������ļ�
	private static final String TAG = SharedLinkSystem.class.getSimpleName();
	public static final String SEPARATOR = "/";
	public static final char SEPARATOR_CHAR = '/';
	private SharedLink root = null;
	private String workingDirStr = "";
	private SharedLink workingDir = root;
	// ������Ҫ�־ô洢���ļ�
	private ArrayList<String> arr = new ArrayList<String>();
	// дȨ�޴������������ֹ��λ��
	// TODO ����������չ�洢org.mshare�ļ�����,���ڸ��ļ����������˻���Ӧ���ļ���
	private String writePath = null;
	
	public SharedLinkSystem() {
		// TODO ��ʱ�趨Ϊ��չ�洢��·��
		String realPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		root = SharedLink.newFakeDirectory(this, "");
	}
	
	public void persistSharedPath(String fakePath) {
		
	}
	
	/**
	 * ����µ�����
	 * ���·����ΪSharedFileSystem����µ����ݣ��������ļ������ļ���
	 * ��realPathΪnull��ʱ�򣬻Ὣ����Ϊһ��SharedFakeDirectory���
	 * TODO ����ж��Ƿ���ӳɹ���
	 * @param realPath ֻ����������յ����ݵ�ʱ��Żᱻʹ��
	 * @param fakePath ��Ӧ����SharedFileSystem�е��ļ�·��
	 */
	public boolean addSharedPath(String fakePath, String realPath) {
		// ����ӵ��־û���
		// ����һ��·��
//		SharedPath sp = SharedPath.new;
		// �洢fakePath
		
		
		// �ָ����Ƭ
		String[] crumbs = split(fakePath);
		String fileName = null;
		SharedLink file = root, parentFile = null;
		
		if (file == null) {
			Log.e(TAG, "root is null");
			return false;
		}
		if (crumbs.length == 0) {
			Log.e(TAG, "��·�����ܸı�");
			return false;
		}
		
		// TODO �Ƿ�Ҫ�����ӵ�·���Ƿ��ǺϷ�������
		for (int i = 0, len = crumbs.length - 1; i < len; i++) {
			fileName = crumbs[i];
			
			if (file.isDirectory()) {
				parentFile = file; // ��¼���ļ�
				file = file.list().get(fileName);
			} else {
				// TODO �����ļ��Ƿ�Ӧ�ñ�����,��ʱreturn false
				Log.e(TAG, "�������ļ�Ҫ������");
				return false;
			}
			
			if (file == null) { // ·���Ƿ����
				// TODO ��Ҫ����fakePath
				SharedLink newFakeDirectory = SharedLink.newFakeDirectory(this, join(new String[] {parentFile.getFakePath(), fileName}));
				parentFile.list().put(fileName, newFakeDirectory);
			}
		}
		
		fileName = crumbs[crumbs.length - 1];
		if (file.isDirectory()) { // ���ļ���һ���ļ���
			// ����µ��ļ�
			// TODO ������Ϊ�����Ҳ�п�����һ���ļ���
			// TODO �����õ����ݿ�����һ��FakeDirectory
			SharedLink newSharedLink = null;
			if (realPath == null) { // ��Ҫ���õ���FakeDirectory
				newSharedLink = SharedLink.newFakeDirectory(this, fakePath);
				file.list().put(fileName, newSharedLink);
			} else {
				File realFile = new File(realPath);
				if (realFile.isFile()) {
					newSharedLink = SharedLink.newFile(file.getFakePath() + SEPARATOR + fileName, realPath);
					file.list().put(fileName, newSharedLink);
				} else if (realFile.isDirectory()) {
					newSharedLink = SharedLink.newDirectory(this, file.getFakePath() + SEPARATOR + fileName, realPath);
					file.list().put(fileName, newSharedLink);
				}
			}
			
			if (newSharedLink != null) {
				return true;
			} else {
				return false;
			}
		} else if (file.isFile()) { 
			// TODO ���ļ���һ���ļ�����Ҫ���ǣ�
			return false;
		} else {
			// TODO ��ʱreturn false
			return false;
		}
	}
	
	/**
	 * �־û�����
	 */
	public void persist() {
		
	}
	
	public void deleteSharedPath(String fakePath) {
		
	}
	
	// TODO ��Ҫʵ������linuxϵͳ������,����и��õ�ʵ�ְ취
	// ���ܲ���Ҫ�����������ΪAndroid����linuxϵͳ�ϣ�����File.getCanonicalPath�Ϳ�����
	private String getCanonicalPath(String path) {
		return path;
	}
	
	public SharedLink getSharedLink(SharedLink parent, String param) {
		return parent.list().get(param); 
	}
	
	/**
	 * ��Ҫȷ���������ļ����е����ݶ���ָ�����ļ���Χ��:������չ�洢��
	 * @param param
	 * @return ����ȡʧ�ܵ�ʱ�򣬿�����null
	 */
	public SharedLink getSharedLink(String param) {
		// ��Ҫ�����ô�root��ʼѰ�ҵ�����
        if (param.charAt(0) == SEPARATOR_CHAR) {
            // The STOR contained an absolute path
            SharedLink sl = this.root;
            
            String[] crumbs = split(param);
            for (int i = 0; i < crumbs.length; i++) {
            	sl = sl.list().get(crumbs[i]);
            	if (sl == null) {
        			Log.w(TAG, "internal file is not exist");
            		return null;
            	}
            }
            
            return sl;
        }

        // ��һ���ļ������Ծ�ʹ�õ�ǰ������
        // The STOR contained a relative path
        return workingDir.list().get(param);
	}
	
	/**
	 * TODO ��Ҫ�޸�����
	 * ��õ�ǰworkingDirStr����Ӧ��SharedFile
	 * @return
	 */
	public SharedLink getFile() {
		
		String[] crumbs = split(workingDirStr);
    	SharedLink sf = root;
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
	
	public String getWorkingDirStr() {
		return workingDirStr;
	}
	
	public SharedLink getWorkingDir() {
		return workingDir;
	}
	
	/**
	 * ���õ�ǰ�Ĺ���·��,��������String��SharedFile������޸�
	 * @param workingDir
	 */
	public void setWorkingDir(String workingDir) {
		try {
			// TODO ��ʵ·��������Ҫ�Լ���ʵ��
        	this.workingDirStr = new File(workingDir).getCanonicalPath();
        	
        	this.workingDir = getFile();
        } catch (IOException e) {
            Log.i(TAG, "SessionThread canonical error");
        }
	}

	public String join(String[] crumbs) {
		return join(crumbs, 0, crumbs.length);
	}
	
	public String join(String[] crumbs, int start, int end) {
		// ����start��end
		start = (start <= 0) ? 0 : start;
		end = (end >= crumbs.length) ? crumbs.length - 1 : end;
		
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (crumbs[i] != null && !crumbs[i].equals("")) {
				builder.append(SEPARATOR + crumbs[i]);
			}
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
