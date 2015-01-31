package org.mshare.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mshare.main.UploadFileChooserAdapter.FileInfo;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

	private String mCurrentPWD; // 当前远程目录
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
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			logv("mHandler --->" + msg.what);
			switch (msg.what) {
			case MSG_CMD_CONNECT_OK:
				toast("FTP服务器连接成功");
				if(mDameonThread == null){
					//启动守护进程。
					mDameonThread = new Thread(new DameonFtpConnector());
					mDameonThread.setDaemon(true);
					mDameonThread.start();
				}
				executeLISTRequest();
				break;
			case MSG_CMD_CONNECT_FAILED:
				toast("FTP服务器连接失败，正在重新连接");
				executeConnectRequest();
				break;
			case MSG_CMD_LIST_OK:
				toast("请求数据成功。");
				buildOrUpdateDataset();
				break;
			case MSG_CMD_LIST_FAILED:
				toast("请求数据失败。");
				break;
			case MSG_CMD_CWD_OK:
				toast("请求数据成功。");
				executeLISTRequest();
				break;
			case MSG_CMD_CWD_FAILED:
				toast("请求数据失败。");
				break;
			case MSG_CMD_DELE_OK:
				toast("请求数据成功。");
				executeLISTRequest();
				break;
			case MSG_CMD_DELE_FAILED:
				toast("请求数据失败。");
				break;
			case MSG_CMD_RENAME_OK:
				toast("请求数据成功。");
				executeLISTRequest();
				break;
			case MSG_CMD_RENAME_FAILED:
				toast("请求数据失败。");
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
			boolean errorAndRetry = false ;  //根据不同的异常类型，是否重新捕获
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
			toast(result ? "下载成功" : "下载失败");
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
			toast(result ? path + "上传成功" : "上传失败");
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
	
	public void setLoadProgress(int progress) {
		if (mPbLoad != null) {
			mPbLoad.setProgress(progress);
		}
	}
}


