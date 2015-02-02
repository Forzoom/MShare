package org.mshare.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mshare.main.FtpMainActivity.CmdDownLoad;
import org.mshare.main.FtpMainActivity.CmdUpload;
import org.mshare.main.UploadFileChooserAdapter.FileInfo;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class FtpFileManage extends Activity{
	
	private static String TAG = FtpMainActivity.class.getName();
	
	private CmdFactory mCmdFactory;
	private FTPClient mFTPClient;
	private ExecutorService mThreadPool;

	private static String mAtSDCardPath;

	private ProgressBar mPbLoad = null;

	private ListView mListView;
	private FtpFileAdapter mAdapter;
	private List<FTPFile> mFileList = new ArrayList<FTPFile>();
	private Object mLock = new Object();
	private int mSelectedPosistion = -1;

	private String mCurrentPWD; // ��ǰԶ��Ŀ¼
	private static final String OLIVE_DIR_NAME = "OliveDownload";

	// Upload
	private GridView mGridView;
	private View fileChooserView;
	private TextView mTvPath;
	private String mSdcardRootPath;
	private String mLastFilePath;
	private List<FileInfo> mUploadFileList;
	private UploadFileChooserAdapter mUploadAdapter;
	//

	private Dialog progressDialog;
	private Dialog uploadDialog;

	private Thread mDameonThread = null ;
	private boolean mDameonRunning = true;
	
	private String mFTPHost ;
	private int mFTPPort ;
	private String mFTPUser ;
	private String mFTPPassword ;
	
	private static final int MAX_THREAD_NUMBER = 5;
	private static final int MAX_DAMEON_TIME_WAIT = 2 * 1000; // millisecond

	private static final int MENU_OPTIONS_BASE = 0;
	private static final int MSG_CMD_CONNECT_OK = MENU_OPTIONS_BASE + 1;
	private static final int MSG_CMD_CONNECT_FAILED = MENU_OPTIONS_BASE + 2;
	private static final int MSG_CMD_LIST_OK = MENU_OPTIONS_BASE + 3;
	private static final int MSG_CMD_LIST_FAILED = MENU_OPTIONS_BASE + 4;
	private static final int MSG_CMD_CWD_OK = MENU_OPTIONS_BASE + 5;
	private static final int MSG_CMD_CWD_FAILED = MENU_OPTIONS_BASE + 6;
	private static final int MSG_CMD_DELE_OK = MENU_OPTIONS_BASE + 7;
	private static final int MSG_CMD_DELE_FAILED = MENU_OPTIONS_BASE + 8;
	private static final int MSG_CMD_RENAME_OK = MENU_OPTIONS_BASE + 9;
	private static final int MSG_CMD_RENAME_FAILED = MENU_OPTIONS_BASE + 10;

	private static final int MENU_OPTIONS_DOWNLOAD = MENU_OPTIONS_BASE + 20;
	private static final int MENU_OPTIONS_RENAME = MENU_OPTIONS_BASE + 21;
	private static final int MENU_OPTIONS_DELETE = MENU_OPTIONS_BASE + 22;
	private static final int MENU_DEFAULT_GROUP = 0;

	private static final int DIALOG_LOAD = MENU_OPTIONS_BASE + 40;
	private static final int DIALOG_RENAME = MENU_OPTIONS_BASE + 41;
	private static final int DIALOG_FTP_LOGIN = MENU_OPTIONS_BASE + 42;

	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		registerForContextMenu(mListView);
		
		mSdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mCmdFactory = new CmdFactory();
		mFTPClient = new FTPClient();
		mThreadPool = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
	}
	
	private void initView() {
		mListView = (ListView) findViewById(R.id.listviewApp);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int positioin, long id) {
				if (mFileList.get(positioin).getType() == FTPFile.TYPE_DIRECTORY) {
					executeCWDRequest(mFileList.get(positioin).getName());
				}
			}
		});

		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> adapterView,
							View view, int positioin, long id) {
						mSelectedPosistion = positioin;
						return false;
					}
				});

		mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						Log.v(TAG, "onCreateContextMenu ");
					}

				});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listviewApp) {
			menu.setHeaderTitle("�ļ�����");
			menu.add(MENU_DEFAULT_GROUP, MENU_OPTIONS_DOWNLOAD, Menu.NONE, "����");
			menu.add(MENU_DEFAULT_GROUP, MENU_OPTIONS_RENAME, Menu.NONE, "������");
			menu.add(MENU_DEFAULT_GROUP, MENU_OPTIONS_DELETE, Menu.NONE, "ɾ��");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (mSelectedPosistion < 0 || mFileList.size() < 0) {
			return false;
		}
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case MENU_OPTIONS_DOWNLOAD:
			if (mFileList.get(mSelectedPosistion).getType() == FTPFile.TYPE_FILE) {
				showDialog(DIALOG_LOAD);
				new CmdDownLoad().execute();
			} else {
				toast("ֻ���ϴ��ļ�");
			}
			break;
		case MENU_OPTIONS_RENAME:
			showDialog(DIALOG_RENAME);
			break;
		case MENU_OPTIONS_DELETE:
			executeDELERequest(
					mFileList.get(mSelectedPosistion).getName(),
					mFileList.get(mSelectedPosistion).getType() == FTPFile.TYPE_DIRECTORY);

			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_updownload:
			openFileDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			logv("mHandler --->" + msg.what);
			switch (msg.what) {
			case MSG_CMD_CONNECT_OK:
				toast("FTP���������ӳɹ�");
				if(mDameonThread == null){
					//�����ػ����̡�
					mDameonThread = new Thread(new DameonFtpConnector());
					mDameonThread.setDaemon(true);
					mDameonThread.start();
				}
				executeLISTRequest();
				break;
			case MSG_CMD_CONNECT_FAILED:
				toast("FTP����������ʧ�ܣ�������������");
				executeConnectRequest();
				break;
			case MSG_CMD_LIST_OK:
				toast("�������ݳɹ���");
				buildOrUpdateDataset();
				break;
			case MSG_CMD_LIST_FAILED:
				toast("��������ʧ�ܡ�");
				break;
			case MSG_CMD_CWD_OK:
				toast("�������ݳɹ���");
				executeLISTRequest();
				break;
			case MSG_CMD_CWD_FAILED:
				toast("��������ʧ�ܡ�");
				break;
			case MSG_CMD_DELE_OK:
				toast("�������ݳɹ���");
				executeLISTRequest();
				break;
			case MSG_CMD_DELE_FAILED:
				toast("��������ʧ�ܡ�");
				break;
			case MSG_CMD_RENAME_OK:
				toast("�������ݳɹ���");
				executeLISTRequest();
				break;
			case MSG_CMD_RENAME_FAILED:
				toast("��������ʧ�ܡ�");
				break;
			default:
				break;
			}
		}
	};
	
	private void buildOrUpdateDataset() {
		if (mAdapter == null) {
			mAdapter = new FtpFileAdapter(this, mFileList);
			mListView.setAdapter(mAdapter);
		}
		mAdapter.notifyDataSetChanged();
	}

	private void executeConnectRequest() {
		mThreadPool.execute(mCmdFactory.createCmdConnect());
	}

	private void executeDisConnectRequest() {
		mThreadPool.execute(mCmdFactory.createCmdDisConnect());
	}

	private void executePWDRequest() {
		mThreadPool.execute(mCmdFactory.createCmdPWD());
	}

	private void executeLISTRequest() {
		mThreadPool.execute(mCmdFactory.createCmdLIST());
	}

	private void executeCWDRequest(String path) {
		mThreadPool.execute(mCmdFactory.createCmdCWD(path));
	}

	private void executeDELERequest(String path, boolean isDirectory) {
		mThreadPool.execute(mCmdFactory.createCmdDEL(path, isDirectory));
	}

	private void executeREANMERequest(String newPath) {
		mThreadPool.execute(mCmdFactory.createCmdRENAME(newPath));
	}

	
	private void logv(String log) {
		Log.v(TAG, log);
	}

	private void toast(String hint) {
		Toast.makeText(this, hint, Toast.LENGTH_SHORT).show();
	}
	
	private static String getParentRootPath() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (mAtSDCardPath != null) {
				return mAtSDCardPath;
			} else {
				mAtSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + OLIVE_DIR_NAME;
				File rootFile = new File(mAtSDCardPath);
				if (!rootFile.exists()) {
					rootFile.mkdir();
				}
				return mAtSDCardPath;
			}
		}
		return null;
	}
	
	public class CmdFactory {

		public FtpCmd createCmdConnect() {
			return new CmdConnect();
		}

		public FtpCmd createCmdDisConnect() {
			return new CmdDisConnect();
		}

		public FtpCmd createCmdPWD() {
			return new CmdPWD();
		}

		public FtpCmd createCmdLIST() {
			return new CmdLIST();
		}

		public FtpCmd createCmdCWD(String path) {
			return new CmdCWD(path);
		}

		public FtpCmd createCmdDEL(String path, boolean isDirectory) {
			return new CmdDELE(path, isDirectory);
		}

		public FtpCmd createCmdRENAME(String newPath) {
			return new CmdRENAME(newPath);
		}
	}
	public class DameonFtpConnector implements Runnable {

		@Override
		public void run() {
			Log.v(TAG, "DameonFtpConnector ### run");
			while (mDameonRunning) {
				if (mFTPClient != null && !mFTPClient.isConnected()) {
					try {
						mFTPClient.connect(mFTPHost, mFTPPort);
						mFTPClient.login(mFTPUser, mFTPPassword);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				try {
					Thread.sleep(MAX_DAMEON_TIME_WAIT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public abstract class FtpCmd implements Runnable {

		public abstract void run();

	}

	public class CmdConnect extends FtpCmd {
		@Override
		public void run() {
			boolean errorAndRetry = false ;  //���ݲ�ͬ���쳣���ͣ��Ƿ����²���
			try {
				String[] welcome = mFTPClient.connect(mFTPHost, mFTPPort);
				if (welcome != null) {
					for (String value : welcome) {
						logv("connect " + value);
					}
				}
				mFTPClient.login(mFTPUser, mFTPPassword);
				mHandler.sendEmptyMessage(MSG_CMD_CONNECT_OK);
			}catch (IllegalStateException illegalEx) {
				illegalEx.printStackTrace();
				errorAndRetry = true ;
			}catch (IOException ex) {
				ex.printStackTrace();
				errorAndRetry = true ;
			}catch (FTPIllegalReplyException e) {
				e.printStackTrace();
			}catch (FTPException e) {
				e.printStackTrace();
				errorAndRetry = true ;
			}
			if(errorAndRetry && mDameonRunning){
				mHandler.sendEmptyMessageDelayed(MSG_CMD_CONNECT_FAILED, 2000);
			}
		}
	}

	public class CmdDisConnect extends FtpCmd {

		@Override
		public void run() {
			if (mFTPClient != null) {
				try {
					mFTPClient.disconnect(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public class CmdPWD extends FtpCmd {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String pwd = mFTPClient.currentDirectory();
				logv("pwd --- > " + pwd);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public class CmdLIST extends FtpCmd {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mCurrentPWD = mFTPClient.currentDirectory();
				FTPFile[] ftpFiles = mFTPClient.list();
				logv(" Request Size  : " + ftpFiles.length);
				synchronized (mLock) {
					mFileList.clear();
					mFileList.addAll(Arrays.asList(ftpFiles));
				}
				mHandler.sendEmptyMessage(MSG_CMD_LIST_OK);

			} catch (Exception ex) {
				mHandler.sendEmptyMessage(MSG_CMD_LIST_FAILED);
				ex.printStackTrace();
			}
		}
	}

	public class CmdCWD extends FtpCmd {

		String realivePath;

		public CmdCWD(String path) {
			realivePath = path;
		}

		@Override
		public void run() {
			try {
				mFTPClient.changeDirectory(realivePath);
				mHandler.sendEmptyMessage(MSG_CMD_CWD_OK);
			} catch (Exception ex) {
				mHandler.sendEmptyMessage(MSG_CMD_CWD_FAILED);
				ex.printStackTrace();
			}
		}
	}

	public class CmdDELE extends FtpCmd {

		String realivePath;
		boolean isDirectory;

		public CmdDELE(String path, boolean isDirectory) {
			realivePath = path;
			this.isDirectory = isDirectory;
		}

		@Override
		public void run() {
			try {
				if (isDirectory) {
					mFTPClient.deleteDirectory(realivePath);
				} else {
					mFTPClient.deleteFile(realivePath);
				}
				mHandler.sendEmptyMessage(MSG_CMD_DELE_OK);
			} catch (Exception ex) {
				mHandler.sendEmptyMessage(MSG_CMD_DELE_FAILED);
				ex.printStackTrace();
			}
		}
	}

	public class CmdRENAME extends FtpCmd {

		String newPath;

		public CmdRENAME(String newPath) {
			this.newPath = newPath;
		}

		@Override
		public void run() {
			try {
				mFTPClient.rename(mFileList.get(mSelectedPosistion).getName(),
						newPath);
				mHandler.sendEmptyMessage(MSG_CMD_RENAME_OK);
			} catch (Exception ex) {
				mHandler.sendEmptyMessage(MSG_CMD_RENAME_FAILED);
				ex.printStackTrace();
			}
		}
	}

	public class CmdDownLoad extends AsyncTask<Void, Integer, Boolean> {

		public CmdDownLoad() {

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String localPath = getParentRootPath() + File.separator
						+ mFileList.get(mSelectedPosistion).getName();
				mFTPClient.download(
						mFileList.get(mSelectedPosistion).getName(),
						new File(localPath),
						new DownloadFTPDataTransferListener(mFileList.get(
								mSelectedPosistion).getSize()));
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Boolean result) {
			toast(result ? "���سɹ�" : "����ʧ��");
			progressDialog.dismiss();
		}
	}

	public class CmdUpload extends AsyncTask<String, Integer, Boolean> {

		String path;

		public CmdUpload() {

		}

		@Override
		protected Boolean doInBackground(String... params) {
			path = params[0];
			try {
				File file = new File(path);
				mFTPClient.upload(file, new DownloadFTPDataTransferListener(
						file.length()));
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Boolean result) {
			toast(result ? path + "�ϴ��ɹ�" : "�ϴ�ʧ��");
			progressDialog.dismiss();
		}
	}

	private class DownloadFTPDataTransferListener implements
			FTPDataTransferListener {

		private int totolTransferred = 0;
		private long fileSize = -1;

		public DownloadFTPDataTransferListener(long fileSize) {
			if (fileSize <= 0) {
				throw new RuntimeException(
						"the size of file muset be larger than zero.");
			}
			this.fileSize = fileSize;
		}

		@Override
		public void aborted() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : aborted");
		}

		@Override
		public void completed() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : completed");
			setLoadProgress(mPbLoad.getMax());
		}

		@Override
		public void failed() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : failed");
		}

		@Override
		public void started() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : started");
		}

		@Override
		public void transferred(int length) {
			totolTransferred += length;
			float percent = (float) totolTransferred / this.fileSize;
			logv("FTPDataTransferListener : transferred # percent @@" + percent);
			setLoadProgress((int) (percent * mPbLoad.getMax()));
		}
	}
	
	//�ļ�ѡ������ع���ʵ��
		private void openFileDialog() {
			initDialog();
			uploadDialog = new AlertDialog.Builder(this).create();
			Window window = uploadDialog.getWindow();
			WindowManager.LayoutParams lp = window.getAttributes();
			window.setAttributes(lp);
			uploadDialog.show();
			uploadDialog.getWindow().setContentView(fileChooserView,
					new RelativeLayout.LayoutParams(400, 640));
		}

		private void initDialog() {
			fileChooserView = getLayoutInflater().inflate(
					R.layout.filechooser_show, null);
			fileChooserView.findViewById(R.id.imgBackFolder).setOnClickListener(
					mClickListener);
			mTvPath = (TextView) fileChooserView.findViewById(R.id.tvPath);
			mGridView = (GridView) fileChooserView.findViewById(R.id.gvFileChooser);
			mGridView.setEmptyView(fileChooserView.findViewById(R.id.tvEmptyHint));
			mGridView.setOnItemClickListener(mItemClickListener);
			setGridViewAdapter(mSdcardRootPath);
		}

		private void setGridViewAdapter(String filePath) {
			updateFileItems(filePath);
			mUploadAdapter = new UploadFileChooserAdapter(this, mUploadFileList);
			mGridView.setAdapter(mUploadAdapter);
		}

		private void updateFileItems(String filePath) {
			mLastFilePath = filePath;
			mTvPath.setText(mLastFilePath);

			if (mUploadFileList == null)
				mUploadFileList = new ArrayList<FileInfo>();
			if (!mUploadFileList.isEmpty())
				mUploadFileList.clear();

			File[] files = folderScan(filePath);

			for (int i = 0; i < files.length; i++) {
				if (files[i].isHidden()) // Ignore the hidden file
					continue;

				String fileAbsolutePath = files[i].getAbsolutePath();
				String fileName = files[i].getName();
				boolean isDirectory = false;
				if (files[i].isDirectory()) {
					isDirectory = true;
				}
				FileInfo fileInfo = new FileInfo(fileAbsolutePath, fileName,
						isDirectory);

				mUploadFileList.add(fileInfo);
			}
			// When first enter , the object of mAdatper don't initialized
			if (mUploadAdapter != null)
				mUploadAdapter.notifyDataSetChanged();
		}

		private File[] folderScan(String path) {
			File file = new File(path);
			File[] files = file.listFiles();
			return files;
		}

		private AdapterView.OnItemClickListener mItemClickListener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				FileInfo fileInfo = (FileInfo) (((UploadFileChooserAdapter) adapterView
						.getAdapter()).getItem(position));
				if (fileInfo.isDirectory()) {
					updateFileItems(fileInfo.getFilePath());
				} else {
					showDialog(DIALOG_LOAD);
					new CmdUpload().execute(fileInfo.getFilePath());
				}
			}
		};

		private View.OnClickListener mClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.imgBackFolder:
					backProcess();
					break;
				}
			}
		};

		public void backProcess() {
			if (!mLastFilePath.equals(mSdcardRootPath)) {
				File thisFile = new File(mLastFilePath);
				String parentFilePath = thisFile.getParent();
				updateFileItems(parentFilePath);
			} else {
				setResult(RESULT_CANCELED);
				uploadDialog.dismiss();
			}
		}

	
	public void setLoadProgress(int progress) {
		if (mPbLoad != null) {
			mPbLoad.setProgress(progress);
		}
	}
}


