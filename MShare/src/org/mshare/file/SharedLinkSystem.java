package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mshare.ftp.server.Account;
import org.mshare.ftp.server.FsSettings;
import org.mshare.ftp.server.SessionThread;
import org.mshare.main.MShareApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * ��ǰ��ϣ���ж����ͬʱʹ��ͬһ���˻�����Ϊ�ᵼ�������˵��ļ����޷���֮�޸�
 * ��ǰ��������ϣ���ڴ��ݵ�·������..����.������
 * TODO ����չ�洢�����ڵ�ʱ�򣬲����������������������ֻ����cmd�����ʧ��������Ӧ�𣬿ͻ���û�а취֪���������ǳ�����ʲô�������⵼���˲�����
 * ��ǰ��չ�洢�������
 * TODO ��Ҫ�˽�MediaScan��ص���
 * TODO ������Ҫһ��ˢ�°�ť,����һ���ļ������һ��
 * Ĭ���˻��е�����Ҳ�ܹ���ɾ������Ϊ������������ļ�����Ĭ���˻��е�����
 * TODO ��ʱ��Ӧ��֧�ֹ����ļ���
 * �����SharedLinkSystem������ʱ�򣬴����ļ�����Ӧ��ֻ��Ҫ����SharedLinkSystem��ǰ��¼�Ķ�����˭�Ϳ��Դ���
 * TODO Ŀǰ���ڲ��Ϸ���path��������SharedLinkSystem�Զ�ɾ��
 * �����ļ�ϵͳ��Ҫ���û��ֶ������ļ�ϵͳ�������ṩ��ť���������Ч��Link����
 * �ؼ��ǳ־û����ݱ���̫���������Ч�����⣬������ɾ���־û������ֻ�����ļ���ʧ���ܷ�Ե�ǰ����ʹ�õ�SD����ΪΨһ���ж�
 * ���realPath�е�������ʧ�ˣ���ôҲ��Ӧ��������ɾ���־û�path��������Ҫ��¼˵��ǰpath��Ч�����Ҳ���ʾ���ȴ��´μ���ļ���ʱ�򣬷���realPath��Ĳ����ڵ��������ȥ���־û�
 * �������ڸ��ļ���û�д�����ʱ�򣬴��������ļ���,����path�����˳�����Ҫ
 * 1.���в���ȷ�ĳ־û�path���ᱻɾ������˳־û����ݵĸı�Ҳ��Ҫע��
 * 2.�����ļ���ʾҲӦ����һ�����ص�SharedLinkSystem�л������
 * SharedDirectory�����е����ݶ��ǹ����
 * ���Խ��ļ�����һ���ļ��й�����һ���ļ��й����ʱ�򣬽����Ὣ�ļ����µ�����һһ��ӣ������ڴ����ļ�����ʱ����ӵ�����
 * ֻ����ʵ��ͨ���ļ�·����ӵķ����ļ������ܹ�����ã������Ķ�Ӧ�������ڹ��ɵ��ļ��У������ܴ��ڼٵ��ļ�
 * TODO �ļ��п��ܲ�û�ж�Ӧ����ʵ�ļ���,ֻ���ļ�����ʵ�ģ����Բ��ܽ������ļ��ж����������ǲ��Ե�
 * TODO �����ļ��Ĵ��ڣ���β����������ط���ʾ������Ҫ������ļ����������ʾ�����ļ����ٿ���һ���µ��ļ������ʵ��̫�鷳
 * ����Ժ�Ҫʵ�ֶ���˻����������ôҪ�����������˻���Ȩ�����ݣ�
 * TODO �´������ļ�Ӧ�÷��������չ�洢?��ӵ��дȨ�޵�����£���Ҫ����չ�洢������λ�ñ������ݣ�������Ҫ�ܹ�����
 * TODO ��Ҫ�˽��·������Ӧ��fakePath��ʲô�������""�Ļ���Ӧ���޸�Ϊ'/'
 * TODO ���ڲ����ڵ��ļ���ÿ��������ʱ����Ҫ���
 * TODO ��Ҫ�˽��˻���Ȩ������:��Ȩ�ޣ�ɾ��Ȩ�ޣ�д������д/�޸�Ȩ�ޣ�д����ִ��Ȩ��ȫ��Ϊ��,FTP���޸��ļ���ʱ�򣬱���û�����ô�죿
 * TODO ֻ�ǹ����ļ���ɾ�����Ǳ����ļ���ɾ����
 * ����Ȩ��ϵͳ��˵:���ڹ���Ա��˵��Ȩ��ȫ�����������˻���˵ӵ�е�Ȩ����Ҫ����
 * �ļ������Ĵ�����Ϊ��֧�ֶ��û�ӵ�в�ͬ�Ĺ���Ȩ�޺�����
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
	// �ϴ��ļ�����ŵ�λ��
	// TODO ����������չ�洢org.mshare�ļ�����,���ڸ��ļ����������˻���Ӧ���ļ���
	private String uploadPath = null;
	// ֻ���������Account��SharedPreferences
	private SessionThread sessionThread;
	
	public static final String REAL_PATH_NONE = "|";
	
	public SharedLinkSystem(SessionThread sessionThread) {
		this.sessionThread = sessionThread;
		// TODO ��ʱ�趨Ϊ��չ�洢��·��
		String realPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		root = SharedLink.newFakeDirectory(this, "");
		// TODO ���������ļ�������Ҫ����account��defaultAccount�е�����
		
		SharedPreferences defaultSp = Account.getDefaultSharedPreferences();
		Iterator<String> defaultI = defaultSp.getAll().keySet().iterator();
		
		// �ڹ��캯����ʹ��ѭ�����ð�
		
		while (defaultI.hasNext()) {
			String key = defaultI.next();
			String value = defaultSp.getString(key, "");
			// �����ڵ����ݣ��ǲ���Ӧ��ɾ��
			if (value.equals("")) {
				Log.w(TAG, "�������õĳ־û����ݣ��Ƿ�Ӧ��ɾ��");
			}
			
			addSharedPath(key, value);
		}
		
		SharedPreferences privateSp = sessionThread.getAccount().getSharedPreferences();
		Iterator<String> privateI = privateSp.getAll().keySet().iterator();
		// ���˽�е��ļ�����
		while (privateI.hasNext()) {
			String key = privateI.next();
			String value = privateSp.getString(key, "");
			// �����ڵ����ݣ��ǲ���Ӧ��ɾ��
			if (value.equals("")) {
				Log.w(TAG, "�������õĳ־û����ݣ��Ƿ�Ӧ��ɾ��");
			}
			
			addSharedPath(key, value);
		}
		// TODO ��fakeDirecotry�־û��������Ѿ��־û������ݣ���ʱ�������޸�
		// ���õ�ǰ��working directoryΪ"/"
		persist(SEPARATOR, null); // ����һ��fakeDirectory��Ϊroot
		addSharedPath(SEPARATOR, null);
		setWorkingDir(SEPARATOR); // root��Ϊworking directory
		
		prepareUpload();
	}
	
	/**
	 * TODO ��ʱ����ΪSharedLink������Account��ʹ�õ�
	 * @return
	 */
	public Account getAccount() {
		return sessionThread.getAccount();
	}
	
	/**
	 * ׼������ϴ��ļ���λ��
	 * TODO �����ڸ�λ�õ����ݶ�����Ϊ�ϴ��ļ�?
	 */
	private void prepareUpload() {
		// Ĭ�ϴ������չ�洢��
		File externalStorageDir = Environment.getExternalStorageDirectory();
		String externalStoragePath = externalStorageDir.getAbsolutePath();
		
		// org.mshare�ļ���
		File orgMshare = new File(externalStoragePath + File.separator + "org.mshare");
		if (!orgMshare.exists()) {
			Log.w(TAG, "org.mshare�ļ��в�����");
			// TODO ������һ���ļ���,��������ļ��е�ʱ����ô��
			// �����ļ���
			if (orgMshare.mkdir()) {
				Log.d(TAG, "����org.mshare�ļ��гɹ�");
			} else {
				Log.d(TAG, "����org.mshare�ļ���ʧ��");
			}
		}
		
		// uploadPath�϶�Ϊnull�����Աؽ�ָ���ϴ�·��Ϊorg.mshare/username
		if (uploadPath == null) {
			uploadPath = orgMshare + File.separator + getAccount().getUsername();
			Log.d(TAG, "��ǰû��ָ���ϴ�·�����µ��ϴ�·��Ϊ :" + uploadPath);
		}
		
		// �������ϴ��ļ���
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			Log.e(TAG, "�����ϴ��ļ����ļ��в�����");
			// TODO ͬ����Ҫ��֤uploadDir�������Լ�������"�ļ���"
			if (uploadDir.mkdir()) {
				Log.d(TAG, "�����ϴ��ļ��гɹ�");
			}
		}
	}
	
	/**
	 * ����µ�����
	 * ���·����ΪSharedFileSystem����µ����ݣ��������ļ������ļ���
	 * ��realPathΪnull��ʱ�򣬻Ὣ����Ϊһ��SharedFakeDirectory���
	 * �����ӵ���һ�������ļ��У���ô��Ҫ�������ļ����µ��������ݵݹ�����ļ���
	 * ���ǹ����ļ����µ����ݲ��ᱻ�־û�
	 * �������޷���ӵ��ļ����е�Path��ʱ��Ҳ�Ὣ��־û�����ɾ��
	 * TODO ����ж��Ƿ���ӳɹ���
	 * @param fakePath ��Ӧ����SharedFileSystem�е��ļ�·��������Ϊ""����"/"
	 * @param realPath ֻ����������յ����ݵ�ʱ��Żᱻʹ��
	 */
	public boolean addSharedPath(String fakePath, String realPath) {
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
				
				if (file == null) {
					// ·�������ڵ�����£����ж�Ϊ·�����Ϸ�
					Log.e(TAG, "�޷����path��·�����Ϸ�");
					return false;
				}
			} else {
				// ���г־û�����ӵ�path��Ӧ������ȷ��
				Log.e(TAG, "�޷����path,·�����Ϸ�");
				return false;
			}
		}
		
		fileName = crumbs[crumbs.length - 1];
		if (file.isDirectory() || file.isFakeDirectory()) { // ���ļ���һ���ļ���
			// ����µ��ļ�
			// TODO ������Ϊ�����Ҳ�п�����һ���ļ���
			// TODO �����õ����ݿ�����һ��FakeDirectory
			SharedLink newSharedLink = null;
			if (realPath == null) {
				// realPathΪ�գ���Ҫ���õ���FakeDirectory
				newSharedLink = SharedLink.newFakeDirectory(this, fakePath);
				file.list().put(fileName, newSharedLink);
			} else {
				File realFile = new File(realPath);
				if (realFile.isFile()) {
					newSharedLink = SharedLink.newFile(this, file.getFakePath() + SEPARATOR + fileName, realPath);
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
		} else { 
			// TODO ɾ���־û�
			Log.e(TAG, "���ļ�����һ���ļ���");
			return false;
		}
	}
	
	/**
	 * ���ļ����еĽڵ�ɾ��
	 * @param fakePath
	 */
	public void deleteSharedPath(String fakePath) {
		// getSharedLink������Ϊ�������ʱ��ʹ�õ�
		// ��Ϊ���fakePath�����ļ����Ļ�����ô�ͻ�õ�working directory�ļ����µ�����
		// TODO ������Ҫ��֤fakePath�����·��
		SharedLink toDelete = getSharedLink(fakePath);
		if (toDelete != null) {
			SharedLink parent = toDelete.getParent();
			// TODO ֱ�Ӵ��ļ�����ɾ������֪���Ƿ������ڴ������map�е����ݲ�֪�Ƿ�ᱻ����
			parent.list().remove(toDelete.getName());
		}
	}
	
	/**
	 * ���־û������ݽ��������û������Կ����Ĺ����ļ�����
	 * realPath��ָ���Ĳ����ڵ��ļ��������ᱻ�־û�
	 * TODO ��ʱ�޷�����fakePath
	 */
	public static void persistAll(String realPath) {
		// TODO ������Ҫ��getSharedPreferences�����޸ģ������Ӻ��ѿ�
		SharedPreferences sp = Account.getDefaultSharedPreferences();
		// TODO ��Ҫ�ж��ļ��Ƿ�Ϸ�����
		File toShare = new File(realPath);
		if (!toShare.exists()) {
			Log.e(TAG, "Ҫ�־û����ļ�������");
			return;
		}
		Editor editor = sp.edit();
		String fakePath = SharedLinkSystem.SEPARATOR + toShare.getName();
		editor.putString(fakePath, realPath);
		editor.commit();
	}
	
	/**
	 * ��ǰû�а취��sp�н��и���Ĳ���
	 * ����Ҳû�и��õİ취������ʹ�����л������ݿ⻹�ǲ���
	 * @param realPath
	 */
	public static void unpersistAll(String realPath) {
		SharedPreferences sp = Account.getDefaultSharedPreferences();
		// TODO ��ǰʹ�õ����ķ�ʽ��ɾ���־û�
		Set<String> keySet = sp.getAll().keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = sp.getString(key, "");
			
			// ͨ�������ķ�ʽ���ҵ�realPath������������Ϊ��
			if (value.equals(realPath)) {
				Editor editor = sp.edit();
				editor.putString(key, null);
				editor.commit();
				return;
			}
		}
	}
	
	/**
	 * �־û�����
	 * �����е�������ӵ�SharedPreferences�У�����FakeDirectory������ô�죿
	 */
	public void persist(String fakePath, String realPath) {
		SharedPreferences sp = sessionThread.getAccount().getSharedPreferences();
		// TODO Ϊ�˱���fakePath��realPath����ϵ��������Ҫ���õĳ־û���ʽ
		// ��Ϊ����Ҫ�ı�������ݿ��ܻ��кܶ࣬���������ļ���������Ϣ�ı���
		// ������Ҫ������Ϣ�Ĳ�����ť
		Editor editor = sp.edit();
		if (realPath != null) {
			editor.putString(fakePath, realPath);
		}
		
		editor.commit();
	}
	
	/**
	 * ������Ϊ��ɾ�����־û�������
	 * ��defaultSp�е����ݲ��ᱻɾ��
	 * �ڵ��õ�ʱ�򣬿�����Ҫ�ǳ־û����ļ�����Ĭ���˻��е�
	 * @param fakePath
	 */
	public boolean unpersist(String fakePath) {
		SharedPreferences sp = sessionThread.getAccount().getSharedPreferences();
		String realPath = sp.getString(fakePath, REAL_PATH_NONE);
		// �ļ���������
		if (realPath.equals(REAL_PATH_NONE)) { // �������ڶ�Ӧ�ĳ־û����ݣ�Ϊʲô
			// do nothing
			return false;
		} else { 
			// ����Ӧ����fakeDirectory
			// ����File��DirectoryҲ��Ҫ��Ϊnull
			Editor editor = sp.edit();
			editor.putString(fakePath, null); // ����null��ɾ����Ӧ����
			// TODO ���ص������Կ�����false
			return editor.commit();
		}
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
	 * ���ļ�����Ѱ�ҵ�ǰworkingDir��Ӧ��SharedLink
	 * @return
	 */
	public SharedLink searchWorkingDir() {
		
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
	
	/**
	 * ��ǰ���ڽ����ϴ��ļ���·��
	 * @return
	 */
	public String getUploadDirPath() {
		return uploadPath;
	}
	
	/**
	 * ��ǰ�Ĺ���·��
	 * @return
	 */
	public String getWorkingDirStr() {
		return workingDirStr;
	}
	
	/**
	 * ��ǰ����·������Ӧ��SharedLink����
	 * @return
	 */
	public SharedLink getWorkingDir() {
		return workingDir;
	}
	
	/**
	 * ���õ�ǰ�Ĺ���·��,��������String��SharedFile������޸�
	 * @param workingDir
	 */
	public void setWorkingDir(String workingDir) {
		try {
			// TODO Canonical·��������Ҫ�Լ���ʵ��
        	this.workingDirStr = new File(workingDir).getCanonicalPath();
        	this.workingDir = searchWorkingDir();
        } catch (IOException e) {
            Log.i(TAG, "SessionThread canonical error");
        }
	}

	public static String join(String[] crumbs) {
		return join(crumbs, 0, crumbs.length);
	}
	
	public static String join(String[] crumbs, int start, int end) {
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
	
	public static String[] split(String path) {
		
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
