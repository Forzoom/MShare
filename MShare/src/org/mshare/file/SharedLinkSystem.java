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
 * ������������:Ϊ�ļ������ȷȨ������
 * �ڵ�һ�δ����˻�ʱ�޷���¼
 * ��������Ĭ���˻����ļ��������жԸ��ļ������޸Ļ�ͬʱ��Ӧ�������˻��ϣ����������ļ��������ܺϲ������ǽ�����sessionThread�е��ļ�������һһ�޸ģ�
 * ��ǰ��ϣ���ж����ͬʱʹ��ͬһ���˻�����Ϊ�ᵼ�������˵��ļ����޷���֮�޸�
 * ��ǰ��������ϣ���ڴ��ݵ�·������..����.������
 * TODO ����չ�洢�����ڵ�ʱ�򣬲����������������������ֻ����cmd�����ʧ��������Ӧ�𣬿ͻ���û�а취֪���������ǳ�����ʲô�������⵼���˲�����
 * ��ǰ��չ�洢�������
 * TODO ��Ҫ�˽�MediaScan��ص���
 * TODO ������Ҫһ��ˢ�°�ť,����һ���ļ������һ��
 * Ĭ���˻��е�����Ҳ�ܹ���ɾ������Ϊ������������ļ�����Ĭ���˻��е�����
 * TODO ��ʱ��Ӧ��֧�ֹ����ļ���
 * �����SharedLinkSystem������ʱ�򣬴����ļ�����Ӧ��ֻ��Ҫ����SharedLinkSystem��ǰ��¼�Ķ�����˭�Ϳ��Դ���
 * TODO ���ڲ��Ϸ���path��������SharedLinkSystem�Զ�ɾ��
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
 * TODO ��Ҫ�˽��·������Ӧ��fakePath��ʲô�������""�Ļ���Ӧ���޸�Ϊ'/'
 * TODO ���ڲ����ڵ��ļ���ÿ��������ʱ����Ҫ���
 * TODO ��Ҫ�˽��˻���Ȩ������:��Ȩ�ޣ�ɾ��Ȩ�ޣ�д������д/�޸�Ȩ�ޣ�д����ִ��Ȩ��ȫ��Ϊ��,FTP���޸��ļ���ʱ�򣬱���û�����ô�죿
 * TODO ֻ�ǹ����ļ���ɾ�����Ǳ����ļ���ɾ����
 * ����Ȩ��ϵͳ��˵:���ڹ���Ա��˵��Ȩ��ȫ�����������˻���˵ӵ�е�Ȩ����Ҫ����
 * �ļ������Ĵ�����Ϊ��֧�ֶ��û�ӵ�в�ͬ�Ĺ���Ȩ�޺�����
 * 
 * TODO ��û�б�Ҫ���ϴ��ļ�������Ϊ�����ļ���
 * 
 * <h3>�����ļ�Ȩ��</h3>
 * ��Ҫ������ͨ�û��͹���Ա���������ļ�
 * ������ͨ�û����ļ�����Ӧ���û�ӵ�����дȨ�ޣ�����Ա���ļ��ж�дȨ��
 * ���ڹ���Ա�ļ�������Ա�ж�дȨ�ޣ���ͨ�û���ӵ�ж�Ȩ��
 * ���е�FTP�û�������ͨ�û��������Ƿ����������ǹ���Ա�û�
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
	/**
	 * �����ϴ��ļ���ŵ�λ�ã�Ĭ��Ϊsd���µ�org.mshare�ļ���
	 */
	public static String uploadRoot = null;
	
	/**
	 * ��Ϊ����SharedPreferences�з��ص�realPath���ܻ��������ģ�Ҳ����Ϊ""����fakeDirectory�����
	 * Ϊ�˱�ʾ��SharedPreferences��û�и�realPath������ʹ��|����ʾ����Ϊ|�����ܳ������ļ���
	 */
	public static final String REAL_PATH_NONE = "|";
	/**
	 * ����{@link SharedFakeDirectory}��realPath
	 */
	public static final String REAL_PATH_FAKE_DIRECTORY = "";
	
	/**
	 * ��ͨ�û�ӵ��дȨ��
	 */
	public static final int FILE_PERMISSION_USER = Account.PERMISSION_READ_ADMIN | Account.PERMISSION_WRITE_ADMIN
			| Account.PERMISSION_READ | Account.PERMISSION_WRITE | Account.PERMISSION_READ_GUEST;;
	/**
	 * ��ͨ�û�����ӵ�ж�Ȩ��
	 */
	public static final int FILE_PERMISSION_ADMIN = Account.PERMISSION_READ_ADMIN | Account.PERMISSION_WRITE_ADMIN
			| Account.PERMISSION_READ | Account.PERMISSION_READ_GUEST;
	
	public SharedLinkSystem(SessionThread sessionThread) {
		this.sessionThread = sessionThread;
		// ���־û�������
		Log.d(TAG, "root fakePath :" + root.getFakePath());
		root = SharedLink.newFakeDirectory(this, SEPARATOR, Account.PERMISSION_READ_ADMIN | Account.PERMISSION_READ | Account.PERMISSION_READ_GUEST);
		// ���õ�ǰ��working directoryΪ"/"
		setWorkingDir(SEPARATOR); // root��Ϊworking directory

		// ������Ĭ���˻�ʱ����Ĭ���˻��е�����һ������
		if (!getAccount().isAdministrator()) { // ������ͨ�˻�����Ҫ�������Ա�˻��������õĹ����ļ�
			SharedPreferences adminSp = Account.adminAccount.getSharedPreferences();
			Log.d(TAG, "sp size : " + adminSp.getAll().size());
			load(adminSp, "default", FILE_PERMISSION_ADMIN);
		}
		SharedPreferences privateSp = sessionThread.getAccount().getSharedPreferences();
		Log.d(TAG, "sp size : " + privateSp.getAll().size());
		load(privateSp, "private", FILE_PERMISSION_USER);
		
		prepareUpload();
	}
	
	/**
	 * ��ʱ����ΪSharedLink������Account��ʹ�õ�
	 * @return
	 */
	public Account getAccount() {
		return sessionThread.getAccount();
	}
	
	/**
	 * ������Ӵ��ڵĳ־û�����
	 */
	private void load(SharedPreferences sp, String tag, int filePermission) {
		Log.d(TAG, "start load");
		Iterator<String> iterator = sp.getAll().keySet().iterator();
		int count = 0;
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			// �ж�fakePath�ĵ�һλ�Ƿ���'/'
			Log.d(TAG, "fakePath:" + key);
			if (key.charAt(0) == SEPARATOR_CHAR) {
				// ��������keySet���У�������sp��û�е����
				String value = sp.getString(key, REAL_PATH_NONE);
				Log.d(TAG, "persist content:" + tag + " fakePath:" + key + " realPath:" + value);
				if (addSharedPath(key, value, filePermission)) {
					count++;
				}
			}
		}
		Log.d(TAG, "end load, add " + count + " SharedLink object");
	}
	
	/**
	 * ׼������ϴ��ļ���λ��
	 */
	private void prepareUpload() {
		uploadPath = getAccount().getUpload();
		
		// �������ϴ��ļ���
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			Log.e(TAG, "�����ϴ��ļ����ļ��в�����");
			// TODO ͬ����Ҫ��֤uploadDir�������Լ�������"�ļ���"
			if (uploadDir.mkdir()) {
				Log.d(TAG, "�����ϴ��ļ��гɹ�");
			} else {
				Log.e(TAG, "�����ϴ��ļ���ʧ��");
			}
		}
	}
	
	/**
	 * ���·����ΪSharedFileSystem����µ����ݣ��������ļ������ļ���
	 * ��realPathΪ{@link #REAL_PATH_FAKE_DIRECTORY}ʱ��������ΪSharedFakeDirectory���
	 * �����ӵ���һ�������ļ��У���ô��Ҫ�������ļ����µ��������ݵݹ�����ļ���
	 * ���ǹ����ļ����µ����ݲ��ᱻ�־û�
	 * �������޷���ӵ��ļ����е�Path��ʱ��Ҳ�Ὣ��־û�����ɾ��
	 * TODO ����ж��Ƿ���ӳɹ���
	 * @param fakePath ��Ӧ����SharedFileSystem�е��ļ�·��������Ϊ""����"/"
	 * @param realPath ֻ����������յ����ݵ�ʱ��Żᱻʹ��
	 * @return �ɹ��Ƿ���true
	 */
	public boolean addSharedPath(String fakePath, String realPath, int filePermission) {
		Log.d(TAG, "+�ļ���: fakePath:" + fakePath + " realPath:" + realPath);
		// �ָ����Ƭ
		String[] crumbs = split(fakePath);
		String fileName = null;
		SharedLink file = root;
		
		if (file == null) {
			Log.e(TAG, "root is null");
			return false;
		}
		if (crumbs.length == 0) {
			Log.e(TAG, "��·���޷��ı�");
			return false;
		}
		
		// TODO �Ƿ�Ҫ�����ӵ�·���Ƿ��ǺϷ�������
		for (int i = 0, len = crumbs.length - 1; i < len; i++) {
			fileName = crumbs[i];
			
			if (file.isDirectory()) {
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
			// TODO ������Ϊ����Ŀ�����һ��Directoyr����FakeDirectory
			SharedLink newSharedLink = null;
			
			if (realPath == REAL_PATH_FAKE_DIRECTORY) {
				// realPathΪ�գ���Ҫ���õ���FakeDirectory
				newSharedLink = SharedLink.newFakeDirectory(this, fakePath, filePermission);
				Log.d(TAG, "+SharedFakeDirectory -> " + file.getFakePath());
			} else {
				File realFile = new File(realPath);
				if (realFile.exists()) {
					if (realFile.isFile()) {
						newSharedLink = SharedLink.newFile(this, fakePath, realPath, filePermission);
						Log.d(TAG, "+SharedFile -> " + file.getFakePath());
					} else if (realFile.isDirectory()) {
						newSharedLink = SharedLink.newDirectory(this, fakePath, realPath, filePermission);
						Log.d(TAG, "+SharedDirectory -> " + file.getFakePath());
						Log.d(TAG, "try add files in sharedDirectory");
						// ��Ҫ�������ļ����µ��������ݶ���ӵ��ļ�����
						// �����ļ���������SharedFile����ʽ�����ļ����������ļ��У�����SharedDirectory�ķ�ʽ�����ļ���
						File[] files = realFile.listFiles();
						for (int index = 0, len = files.length; index < len; index++) {
							File f = files[index];
							String _fakePath = getFakePath(newSharedLink.getFakePath(), f.getName()), _realPath = f.getAbsolutePath();
							// TODO �������,���ܻ���ִ���
							addSharedPath(_fakePath, _realPath, filePermission);
						}
					}
				} else {
					Log.e(TAG, "��ʵ�ļ���������");
					// TODO �Ƿ���Ҫɾ���ó־û�����
				}
			}
			
			if (newSharedLink != null) {
				file.list().put(fileName, newSharedLink);
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
			SharedLink parent = getSharedLink(toDelete.getParent());
			// TODO ֱ�Ӵ��ļ�����ɾ������֪���Ƿ������ڴ������map�е����ݲ�֪�Ƿ�ᱻ����
			parent.list().remove(toDelete.getName());
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean commonPersist(SharedPreferences sp, String fakePath, String realPath) {
		// TODO Ϊ�˱���fakePath��realPath����ϵ��������Ҫ���õĳ־û���ʽ
		// ��Ϊ����Ҫ�ı�������ݿ��ܻ��кܶ࣬���������ļ���������Ϣ�ı���
		// ������Ҫ������Ϣ�Ĳ�����ť
		if (realPath == null) {
			Log.e(TAG, "realPath is null, should invoke unpersist?");
			return false;
		}
		Editor editor = sp.edit();
		editor.putString(fakePath, realPath);
		boolean persistResult = editor.commit();
		Log.d(TAG, "+�־û�: fakePath:" + fakePath + " realPath:" + realPath + " result:" + persistResult);
		return persistResult;
	}
	
	public static boolean commonUnpersist(SharedPreferences sp, String fakePath) {
		String realPath = sp.getString(fakePath, REAL_PATH_NONE);
		// �ļ���������
		if (realPath.equals(REAL_PATH_NONE)) { // �������ڶ�Ӧ�ĳ־û����ݣ�Ϊʲô
			// do nothing
			return false;
		} else {
			Editor editor = sp.edit();
			editor.remove(fakePath);
			return editor.commit();
		}
	}
	
	/**
	 * �־û�����
	 * �����е�������ӵ�SharedPreferences��
	 */
	public boolean persist(String fakePath, String realPath) {
		return commonPersist(sessionThread.getAccount().getSharedPreferences(), fakePath, realPath);
	}
	
	/**
	 * ������Ϊ��ɾ�����־û�������
	 * ��defaultSp�е����ݲ��ᱻɾ��
	 * �ڵ��õ�ʱ�򣬿�����Ҫ�ǳ־û����ļ�����Ĭ���˻��е�
	 * @param fakePath
	 */
	public boolean unpersist(String fakePath) {
		return commonUnpersist(sessionThread.getAccount().getSharedPreferences(), fakePath);
	}
	
	/**
	 * 
	 * TODO ��Ҫ���������ݲ������������֣����ý��
	 * ��������private�Ĳ��������־û�����
	 */
	public boolean changePersist(String oldFakePath, String newFakePath, String newRealPath) {
		Log.d(TAG, "�����־û�����");
		SharedPreferences sp = sessionThread.getAccount().getSharedPreferences();
		if (!sp.getString(oldFakePath, REAL_PATH_NONE).equals(REAL_PATH_NONE)) {
			Editor editor = sp.edit();
			// ɾ��ԭ������
			editor.remove(oldFakePath);
			Log.d(TAG, "ɾ��oldFakePath :" + oldFakePath);
			editor.putString(newFakePath, newRealPath);
			Log.d(TAG, "���newFakePath :" + newFakePath + " newRealPath :" + newRealPath);
			boolean changeResult = editor.commit();
			Log.d(TAG, "�����־û����� " + changeResult);
			return changeResult;
		} else {
			Log.e(TAG, "û���ҵ���Ӧ�־û�����");
			return false;
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
            Log.d(TAG, "root size:" + root.list().size());
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
	 * @param parent
	 * @param param
	 * @return
	 */
	public static String getFakePath(String parent, String param) {
		if (param.charAt(0) != SEPARATOR_CHAR) {
			if (parent.equals(SEPARATOR)) {
				return parent + param;
			} else {
				return parent + SEPARATOR + param;
			}
		} else {
			return param;
		}
	}
	
	/**
	 * ��ΪgetSharedLink�����ܻ�ò����ڵ�SharedLinnk��������Ҫʹ�ø÷��������һ��path
	 * @return
	 */
	public String getFakePath(String param) {
		return getFakePath(getWorkingDirStr(), param);
	}
	
	/**
	 * �ú����Ĵ��ڣ���Ϊ�������е�fakePath��ͳһ�ķ�ʽ����ø����������
	 * ͨ��fakePath����ø�fakePath����Ӧ�ĸ��ļ�����
	 * @param fakePath
	 * @return
	 */
	public static String getParent(String fakePath) {
		if (!fakePath.equals(SEPARATOR)) {
			
			int lastIndex = fakePath.lastIndexOf(SharedLinkSystem.SEPARATOR);
			String parentFakePath = null;
			if (lastIndex == 0) {
				parentFakePath = SharedLinkSystem.SEPARATOR;
			} else {
				parentFakePath = fakePath.substring(0, lastIndex);
			}
			return parentFakePath;
		} else {
			Log.e(TAG, "rootû�и��ļ�");
			return null;
		}
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
			if (path.charAt(path.length() - 1) == SEPARATOR_CHAR) {
				path = path.substring(0, path.length() - 1);
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
		
		Log.d(TAG, "split result size :" + ret.length);
		return ret;
	}
	
	/**
	 * ������ȴ�ӡ
	 */
	void print() {
		root.print();
	}
}
