package org.mshare.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mshare.ftp.server.Account;
import org.mshare.ftp.server.AccountFactory;
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
 * BUG ����Ҫ�ϴ����ļ�����Ϊ����Ĳ�ͬ�������ļ��������ʱ���ϴ��ļ���ʧ��
 * TODO Ϊ����Ա�ļ���ӹ�����ļ��У������ٷ�֧��������Ϊ���֣���Ƶ�ȵ�
 * TODO ��Ҫ���Ǹ��õķ�����������Щ���ݣ�����B+���������νṹ�����ܵݹ鴦��
 * TODO ʹ��AccountFactory������Account�˻��Ĵ����ͻ���,���û�QUIT��ʱ�򣬻����ǳ�ʱ���Ƿ���Ҫ���ó�ʱ������ʱ�򣬻ὫAccount�е�sessionThread��register����
 * TODO ����Ա�˻����������ļ��������ж��ڹ���Ա�˻������г־û����ݣ���һ���ļ�����������ʱ�򣬶������µ��ļ����д���
 * TODO �����ļ�Ȩ��ϵͳ�Ƿ���Ч
 * TODO �����ļ������µ�ʱ�򣬷����������ܹ�֪ͨ�ͻ���
 * TODO ��SharedLink������ļ��Ƿ����ڱ�ʹ��isUsing?
 * TODO �ͻ���Ӧ�����ݷ��ص��ļ�Ȩ�޽�����Ӧ����ʾ����
 * �ڵ�һ�δ����˻�ʱ���ܻ�Ƚ���
 * ��ǰ��������ϣ���ڴ��ݵ�·������..����.������
 * TODO ����չ�洢�����ڵ�ʱ�򣬲����������������������ֻ����cmd�����ʧ��������Ӧ��
 * �ͻ���û�а취֪���������ǳ�����ʲô�������⵼���˲�����
 * TODO ��Ҫ�˽�MediaScan��ص���
 * TODO ������Ҫһ��ˢ�°�ť,����һ���ļ������һ����
 * Ĭ���˻��е�����Ҳ�ܹ���ɾ������Ϊ������������ļ�����Ĭ���˻��е�����
 * TODO ���Թ����ļ��еĹ���
 * �����SharedLinkSystem������ʱ�򣬴����ļ�����Ӧ��ֻ��Ҫ����SharedLinkSystem��ǰ��¼�Ķ�����˭�Ϳ��Դ���
 * TODO ���ڲ��Ϸ���path��������SharedLinkSystem�Զ�ɾ��?
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
 * 
 * TODO ����Ա�������ʾ�����أ���MShareFileBrowser�У���Ҫ���ļ������������ʱ�����ļ����в鿴���ݣ������ļ�������Ӧ��realPath����֪��������ǲ�����Ҫ����һ����ʵ���ļ���
 * 
 * �ļ������Ĵ�����Ϊ��֧�ֶ��û�ӵ�в�ͬ�Ĺ���Ȩ�޺�����
 * �ϴ��ļ��в����ǹ����ļ���
 * 
 * ���г־û����ݱ����µ�ʱ�򣬽���֪ͨ���е�Session
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
	// ��һ����ȷ�����޷��޸ģ�������û�а취ʹ��unprepare����
	private final SharedLink root;
	private SharedLink workingDir;
	// �ϴ��ļ�����ŵ�λ��
	// TODO ����������չ�洢org.mshare�ļ�����,���ڸ��ļ����������˻���Ӧ���ļ���
	private String uploadPath = null;
	// ֻ���������Account��SharedPreferences
	/**
	 * �����ϴ��ļ���ŵ�λ�ã�Ĭ��Ϊsd���µ�org.mshare�ļ���
	 * TODO uploadRoot��Ҫ�ϲ������
	 */
	public static String uploadRoot;
	/**
	 * SharedLinkSystem����Ӧ��Account
	 */
	private Account mAccount;
	/**
	 * ���Ա�����ǰ�ļ����Ƿ��Ѿ�׼����ɣ�����ʹ��
	 */
	private boolean prepared = false;
	/**
	 * ��Ϊ����SharedPreferences�з��ص�realPath���ܻ��������ģ�Ҳ����Ϊ""����fakeDirectory�����
	 * Ϊ�˱�ʾ��SharedPreferences��û�и�realPath������ʹ��|����ʾ����Ϊ|�����ܳ������ļ���
	 * TODO ���ļ��ϴ�ʱ����Ҫ�ж��ļ����Ƿ�Ϸ�
	 */
	public static final String REAL_PATH_NONE = "|";
	/**
	 * ����{@link SharedFakeDirectory}��realPath
	 */
	public static final String REAL_PATH_FAKE_DIRECTORY = "";
	
	/**
	 * ��ͨ�û����������ļ�ӵ��дȨ��
	 */
	public static final int FILE_PERMISSION_USER = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN
			| Permission.PERMISSION_READ | Permission.PERMISSION_WRITE;
	/**
	 * ����Ա���������ļ�
	 * ��ͨ�û�����ӵ�ж�Ȩ��
	 */
	public static final int FILE_PERMISSION_ADMIN = Permission.PERMISSION_READ_ADMIN | Permission.PERMISSION_WRITE_ADMIN
			| Permission.PERMISSION_READ | Permission.PERMISSION_READ_GUEST;
	
	/**
	 * �ص�����
	 */
	private Callback mCallback;
	
	// TODO ����ļ�����Ӧ��Account,���Account�ǿյ���ô�죿Ҫ��Ҫʹ�ö���ģʽ����AccountΪnull��ʱ�򣬽��޷����һ��SharedLinkSystem��������ʹ��initial���������ļ���prepare
	// �����캯���е�һЩ�����ƶ���prepare������
	public SharedLinkSystem(Account account) {
		this.mAccount = account;
		// ��û�б��־û���������Ҫÿ�����д���,���ļ�ֻ�ж�Ȩ��
		root = SharedLink.newFakeDirectory(this, SEPARATOR, Permission.PERMISSION_READ_ALL);
		
		// ���õ�ǰ��working directoryΪ"/"
		setWorkingDir(SEPARATOR); // root��Ϊworking directory
	}
	
	/**
	 * ��Ҫʹ��prepared��ȷ���ļ�����������
	 * preparedҪ��ôʹ���أ������ж�
	 */
	public void prepare() {
		if (prepared) {
			Log.d(TAG, "already prepared");
			return;
		}
		
		// TODO ��������,��Ҫ����Щ���ݷ�����������أ�
		if (getAccount().isUser() || getAccount().isGuest()) {
			// ���ع���Ա�ļ�
		}
		if (getAccount() != null) {
			SharedPreferences privateSp = getAccount().getSharedPreferences();
			load(privateSp, SharedLinkSystem.FILE_PERMISSION_USER);
		}
		
		prepareUpload();
		prepared = true;
	}
	
	/**
	 * ���Զ���־û�������ӵ��ļ�����
	 * ��Ҫ�Լ�����
	 * TODO ��Ҫ����load����Ӧ��ʹ��sp��Ӧ�ø��ܹ������ı�
	 * @param sp �������������������"/"��ͷ������
	 * @param filePermission ��ӵ��ļ���Ȩ�� {@link #FILE_PERMISSION_ADMIN}, {@link #FILE_PERMISSION_USER}
	 */
	public void load(SharedPreferences sp, int filePermission) {
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
				Log.d(TAG, "+content:fakePath:" + key + " realPath:" + value);
				if (addSharedLink(key, value, filePermission)) {
					count++;
				}
			}
		}
		Log.d(TAG, "end load, add " + count + " SharedLink object");
	}
	
	/**
	 * ׼������ϴ��ļ���λ�ã�������ʵ���ļ���
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
	 * ���ݵ�ǰAccount������������ļ�
	 * @param fakePath
	 * @param realPath
	 * @return
	 */
	public boolean addSharedPath(String fakePath, String realPath) {
		int filePermission = mAccount.isAdministrator() ? FILE_PERMISSION_ADMIN : FILE_PERMISSION_USER;
		return addSharedLink(fakePath, realPath, filePermission);
	}
	
	/**
	 * ΪSharedFileSystem����µ����ݣ��������ļ������ļ���
	 * ��realPathΪ{@link #REAL_PATH_FAKE_DIRECTORY}ʱ��������ΪSharedFakeDirectory���
	 * �����ӵ���һ�������ļ��У���ô��Ҫ�������ļ����µ��������ݵݹ�����ļ���
	 * TODO ?�������޷���ӵ��ļ����е�Path��ʱ��Ҳ�Ὣ��־û�����ɾ��
	 * 
	 * ������ӵ���ʵ�ļ��������ڵ�ʱ�򣬹�����ļ����ᱻ��ʾ����
	 * 
	 * @param fakePath ��Ӧ����SharedFileSystem�е��ļ�·��������Ϊ""����"/",���ұ�����'/'��ͷ
	 * @param realPath ֻ����������յ����ݵ�ʱ��Żᱻʹ��
	 * @param filePermission ����ӵ��ļ�Ȩ��
	 * @return �ɹ�����true
	 */
	public boolean addSharedLink(String fakePath, String realPath, int filePermission) {
		if (!prepared) {
			Log.e(TAG, "invoke prepare first!");
			return false;
		}
		// split
		String[] crumbs = split(fakePath);
		// ���fakePath�Ƿ���Ч
		if (!isFakePathValid(fakePath) || crumbs.length == 0) {
			Log.e(TAG, "invalid fakePath");
			return false;
		}
		
		Log.d(TAG, "+�ļ���: fakePath:" + fakePath + " realPath:" + realPath);
		String fileName = null;
		SharedLink file = root;
		
		// �Ƿ�Ҫ�����ӵ�·���Ƿ��ǺϷ�������
		for (int i = 0, len = crumbs.length - 1; i < len; i++) {
			fileName = crumbs[i];
			
			if (file.isDirectory()) {
				file = file.list().get(fileName);
				
				if (file == null) {
					// ·�������ڵ�����£����ж�Ϊ·�����Ϸ�
					Log.e(TAG, "invalid path");
					return false;
				}
			} else {
				// ���г־û�����ӵ�path��Ӧ������ȷ��
				Log.e(TAG, "invalid path");
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
						Log.d(TAG, "try add files in SharedDirectory");
						// ��Ҫ�������ļ����µ��������ݶ���ӵ��ļ�����
						// �����ļ���������SharedFile����ʽ�����ļ����������ļ��У�����SharedDirectory�ķ�ʽ�����ļ���
						File[] files = realFile.listFiles();
						for (int index = 0, len = files.length; index < len; index++) {
							File f = files[index];
							String _fakePath = getFakePath(newSharedLink.getFakePath(), f.getName()), _realPath = f.getAbsolutePath();
							// TODO �������,���ܻ���ִ���
							addSharedLink(_fakePath, _realPath, filePermission);
						}
					}
				} else {
					Log.e(TAG, "��ʵ�ļ���������");
					// TODO �Ƿ���Ҫɾ���ó־û�����
				}
			}
			
			if (newSharedLink != null) {
				file.list().put(fileName, newSharedLink);
				if (mCallback != null) {
					mCallback.onAdd();
				}
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
	 * @param fakePath ��֧�����·����Ҳ֧���ļ���
	 */
	public boolean deleteSharedLink(String fakePath) {
		// getSharedLink������Ϊ�������ʱ��ʹ�õ�
		// ��Ϊ���fakePath�����ļ����Ļ�����ô�ͻ�õ�working directory�ļ����µ�����
		// TODO ������Ҫ��֤fakePath�����·��
		
		SharedLink toDelete = getSharedLink(fakePath);
		if (toDelete != null) {
			SharedLink parent = getSharedLink(toDelete.getParent());
			// TODO ֱ�Ӵ��ļ�����ɾ������֪���Ƿ������ڴ������map�е����ݲ�֪�Ƿ�ᱻ����
			if (parent.list().remove(toDelete.getName()) != null) {
				if (mCallback != null) {
					mCallback.onDelete();
				}
				Log.d(TAG, "delete success");
				return true;
			} else {
				Log.e(TAG, "delete fail");
				return false;
			}
		} else {
			Log.e(TAG, "file is not exist");
			return false;
		}
	}
	
	/**
	 * �־û����ݣ�����commonPersist��sp��ӦAccount��sp
	 * �����е�������ӵ�SharedPreferences��
	 */
	public boolean persist(String fakePath, String realPath) {
		SharedPreferences sp = getAccount().getSharedPreferences();
		boolean persistResult = false;
		// TODO Ϊ�˱���fakePath��realPath����ϵ��������Ҫ���õĳ־û���ʽ
		// ��Ϊ����Ҫ�ı�������ݿ��ܻ��кܶ࣬���������ļ���������Ϣ�ı���
		// ������Ҫ������Ϣ�Ĳ�����ť
		if (realPath == null) {
			Log.e(TAG, "realPath is null, should invoke unpersist()?");
			return false;
		}
		Editor editor = sp.edit();
		editor.putString(fakePath, realPath);
		persistResult = editor.commit();
		Log.d(TAG, "+persist: fakePath:" + fakePath + " realPath:" + realPath + " result:" + persistResult);
		if (persistResult) {
			if (mCallback != null) {
				mCallback.onPersist(fakePath, realPath);
			}
		}
		return persistResult;
	}
	
	/**
	 * ������Ϊ��ɾ�����־û�������
	 * ��defaultSp�е����ݲ��ᱻɾ��
	 * �ڵ��õ�ʱ�򣬿�����Ҫ�ǳ־û����ļ�����Ĭ���˻��е�
	 * @param fakePath
	 */
	public boolean unpersist(String fakePath) {
		SharedPreferences sp = getAccount().getSharedPreferences();
		boolean persistResult = false;
		String realPath = sp.getString(fakePath, REAL_PATH_NONE);
		// �ļ���������
		if (realPath.equals(REAL_PATH_NONE)) { // �������ڶ�Ӧ�ĳ־û����ݣ�Ϊʲô
			// do nothing
			return false;
		}
		
		Editor editor = sp.edit();
		editor.remove(fakePath);
		persistResult = editor.commit();
		Log.d(TAG, "-unpersist: fakePath:" + fakePath + " realPath:" + realPath + " result:" + persistResult);
		if (persistResult) {
			if (mCallback != null) {
				mCallback.onUnpersist(fakePath);
			}
		}
		return persistResult;
	}
	
	/**
	 * �����־û����ݣ�һ�������޸��ļ���
	 * TODO ��Ҫ���������ݲ������������֣����ý��
	 * ��������private�Ĳ��������־û�����
	 */
	public boolean changePersist(String oldFakePath, String newFakePath, String newRealPath) {
		Log.d(TAG, "�����־û�����");
		SharedPreferences sp = getAccount().getSharedPreferences();
		if (!sp.getString(oldFakePath, REAL_PATH_NONE).equals(REAL_PATH_NONE)) {
			Editor editor = sp.edit();
			// ɾ��ԭ������
			editor.remove(oldFakePath);
			Log.d(TAG, "-oldFakePath :" + oldFakePath);
			editor.putString(newFakePath, newRealPath);
			Log.d(TAG, "+newFakePath :" + newFakePath + " newRealPath :" + newRealPath);
			boolean changeResult = editor.commit();
			Log.d(TAG, "=result: " + changeResult);
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
	
	/**
	 * ���parent�е��ļ�
	 * @param parent 
	 * @param param ����֧���ļ���
	 * @return ���ܷ���null
	 */
	public SharedLink getSharedLink(SharedLink parent, String param) {
		return parent.list().get(param); 
	}
	
	/**
	 * ��Ҫȷ���������ļ����е����ݶ���ָ�����ļ���Χ��:������չ�洢��
	 * @param param ���������·�����������ļ���
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
		return workingDir.getFakePath();
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
	 * @param workingDir ֧���ļ��������·��
	 */
	public void setWorkingDir(String workingDir) {
		Log.d(TAG, "set working dir");
    	this.workingDir = getSharedLink(workingDir);
	}
	
	/**
	 * ������ö�Ӧ��Account����
	 * @return
	 */
	private Account getAccount() {
		return mAccount;
	}
	
	/**
	 * ֻ����һ��Callback,�µ�Callback���ᶥ��ɵ�Callback
	 * @see Callback
	 */
	public void setCallback(Callback callback) {
		mCallback = callback;
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
	
	public boolean isPrepared() {
		return prepared;
	}

	public static boolean isFakePathValid(String fakePath) {
		return fakePath.charAt(0) == SEPARATOR_CHAR;
	}
	
	public int getAccountPermission() {
		return mAccount.getPermission();
	}
	
	/**
	 * 
	 * @author HM
	 *
	 */
	public class Permission {
		// ��������Ϊ�ʺŵ�Ȩ�ޣ�ӳ�����û��ļ���
	    public static final int PERMISSION_READ_ADMIN = 0400;
	    public static final int PERMISSION_WRITE_ADMIN = 0200;
	    public static final int PERMISSION_EXECUTE_ADMIN = 0100;// execute��Զ������
	    
	    public static final int PERMISSION_READ = 040;
	    public static final int PERMISSION_WRITE = 020;
	    public static final int PERMISSION_EXECUTE = 010;// execute��Զ������
	    
	    public static final int PERMISSION_READ_GUEST = 04;
	    public static final int PERMISSION_WRITE_GUEST = 02;
	    public static final int PERMISSION_EXECUTE_GUEST = 01;// execute��Զ������
	    
	    public static final int PERMISSION_READ_ALL = 0444;
	    public static final int PERMISSION_WRITE_ALL = 0222;
	    public static final int PERMISSION_EXECUTE_ALL = 0111;// execute��Զ������

	    public static final int PERMISSION_NONE = 0;
	    
	}
	
	/**
	 * ���ļ��������仯��ʱ��Ļص�����
	 * @author HM
	 *
	 */
	public interface Callback {
		public void onPersist(String fakePath, String realPath);
		public void onUnpersist(String fakePath);
		/**
		 * ��������{@link SharedLinkSystem#addSharedLink(String, String, int)}ʱ�Ļص�����
		 * ֻ�гɹ�ʱ�Ż����
		 */
		public void onAdd();
		/**
		 * ��������{@link SharedLinkSystem#deleteSharedLink(String)}ʱ�Ļص�����
		 * ֻ�гɹ�ʱ�Ż����
		 */
		public void onDelete();
	}
}
