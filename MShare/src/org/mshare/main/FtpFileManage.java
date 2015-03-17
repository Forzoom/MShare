package org.mshare.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
	private String cachePath;

	private ProgressBar mPbLoad = null;

	private GridView mGridFile;//原文件列表
	private FtpFileAdapter mAdapter;
	private List<FTPFile> mFileList = new ArrayList<FTPFile>();
	private Object mLock = new Object();
	private int mSelectedPosistion = -1;

	private String mCurrentPWD; // 当前远程目录
	private static final String OLIVE_DIR_NAME = "MShareDownload";
	private static final String CACHE_DIR_NAME = "MShareCache";

	
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
	/** 
     * 定义了初始缓存区的大小，当视频加载到初始缓存区满的时候，播放器开始播放， 
     */  
    private static final int READY_BUFF = 1316 * 1024*10;

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
	private static final int MSG_CMD_CDU_OK = MENU_OPTIONS_BASE + 11;
	private static final int MSG_CMD_CDU_FAILED = MENU_OPTIONS_BASE + 12;
	private static final int MSG_CMD_OPEN_OK = MENU_OPTIONS_BASE + 13;
	private static final int MSG_CMD_OPEN_FAILED = MENU_OPTIONS_BASE + 14;
	private static final int CACHE_VIDEO_READY = MENU_OPTIONS_BASE + 15;
	
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
		Intent intent = getIntent(); 
//		mFileList = (List<FTPFile>) intent.getSerializableExtra("mFileList");//?
//		mCurrentPWD = (String) intent.getSerializableExtra("mCurrentPWD");
//		Log.v(TAG, "测试"+mFileList.toString());
		
		registerForContextMenu(mGridFile);
		
//		if (mAdapter == null) {
//			mAdapter = new FtpFileAdapter(this, mFileList);
//			mListView.setAdapter(mAdapter);
//		}
//		mAdapter.notifyDataSetChanged();
		
		mSdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mCmdFactory = new CmdFactory();
		mFTPClient = new FTPClient();
		mThreadPool = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
		
		mFTPPort =  (Integer) intent.getSerializableExtra("mFTPPort");
		Log.v(TAG, "测试mFTPPort "+mFTPPort);
		mFTPHost = (String) intent.getSerializableExtra("mFTPHost");
		mFTPUser = (String) intent.getSerializableExtra("mFTPUser");
		mFTPPassword = (String) intent.getSerializableExtra("mFTPPassword");
		Log.v(TAG, "mFTPHost #" + mFTPHost + " mFTPPort #" + mFTPPort 
				+ " mFTPUser #" + mFTPUser + " mFTPPassword #" + mFTPPassword);
		executeConnectRequest();
	}
	
	private void initView() {
		
		Button mButton = (Button) findViewById(R.id.preFolder);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				executeCDURequest();
			}
		});
		
		Button mUpload = (Button) findViewById(R.id.upload);
		mUpload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openFileDialog();
			}
		});
		
		mGridFile = (GridView) findViewById(R.id.gvFileBrowser);

		mGridFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int positioin, long id) {
				if (mFileList.get(positioin).getType() == FTPFile.TYPE_DIRECTORY) {
					executeCWDRequest(mFileList.get(positioin).getName());
				}else if(mFileList.get(positioin).getType() == FTPFile.TYPE_FILE){
					mSelectedPosistion = positioin;
					String fileName = mFileList.get(mSelectedPosistion).getName();
					if(getMIMEType(fileName).startsWith("video")){
						showDialog(DIALOG_LOAD);
						executeOpenRequest();
					}else{
						showDialog(DIALOG_LOAD);
						new CmdOPEN().execute();
					}
				}
			}
		});

		mGridFile.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> adapterView,
							View view, int positioin, long id) {
						mSelectedPosistion = positioin;
						return false;
					}
				});

		mGridFile.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						Log.v(TAG, "onCreateContextMenu ");
					}

				});
		
//		mListView.setOnKeyListener(new OnKeyListener() {
//			
//			@Override
//			public boolean onKey(View source, int keycode, KeyEvent event) {
//				Log.v(null, "setOnKeyListener");
//				// TODO Auto-generated method stub
//				if(event.getAction() == KeyEvent.ACTION_DOWN){
//					switch(event.getKeyCode())
//					{
//						case KeyEvent.KEYCODE_BACK:
//							executeCDURequest();
//					}
//				}
//				return false;
//			}
//		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.gvFileBrowser) {
			menu.setHeaderTitle("文件操作");
			menu.add(MENU_DEFAULT_GROUP, MENU_OPTIONS_DOWNLOAD, Menu.NONE, "下载");
			menu.add(MENU_DEFAULT_GROUP, MENU_OPTIONS_RENAME, Menu.NONE, "重命名");
			menu.add(MENU_DEFAULT_GROUP, MENU_OPTIONS_DELETE, Menu.NONE, "删除");
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
				toast("只能下载文件");
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
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOAD:
			return createLoadDialog();
		case DIALOG_RENAME:
			return createRenameDialog();
		default:
			return null;
		}
	}
	
	private Dialog createLoadDialog() {

		View rootLoadView = getLayoutInflater().inflate(
				R.layout.dialog_load_file, null);
		mPbLoad = (ProgressBar) rootLoadView.findViewById(R.id.pbLoadFile);

		progressDialog = new AlertDialog.Builder(this).setTitle("请稍等片刻...")
				.setView(rootLoadView).setCancelable(false).create();

		progressDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						setLoadProgress(0);
					}
				});

		return progressDialog;
	}

	private Dialog createRenameDialog() {

		View rootLoadView = getLayoutInflater().inflate(R.layout.dialog_rename,
				null);
		final EditText edit = (EditText) rootLoadView
				.findViewById(R.id.editNewPath);

		return new AlertDialog.Builder(this)
				.setTitle("重命名...")
				.setView(rootLoadView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface uploadDialog, int which) {
						// TODO Auto-generated method stub
						if (!TextUtils.isEmpty(edit.getText())) {
							executeREANMERequest(edit.getText().toString());
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface uploadDialog, int which) {
						// TODO Auto-generated method stub

					}
				}).create();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(cachePath != null){
			File cacheFile = new File(cachePath);
			if(cacheFile.exists()){
				cacheFile.delete();
				if(cachePath.endsWith("1.mp4")){  
					cachePath = cachePath.replace("1.mp4", "2.mp4");    
                }else if(cachePath.endsWith("2.mp4")){  
                	cachePath = cachePath.replace("2.mp4", "3.mp4");  
                }else{  
                	cachePath = cachePath.replace("3.mp4", "1.mp4");  
                }
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		mDameonRunning = false ;
		Thread thread = new Thread(mCmdFactory.createCmdDisConnect()) ;
		thread.start();
		//等待连接中断
		try {
			thread.join(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mThreadPool.shutdownNow();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			logv("mHandler --->" + msg.what);
			switch (msg.what) {
			case MSG_CMD_CONNECT_OK:
//				toast("FTP服务器连接成功");
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
			case MSG_CMD_CDU_OK:
				toast("请求数据成功。");
				executeLISTRequest();
				break;
			case MSG_CMD_CDU_FAILED:
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
			case MSG_CMD_OPEN_OK:
				toast("请求数据成功。");
				break;
			case MSG_CMD_OPEN_FAILED:
				toast("请求数据失败。");
				break;
			case CACHE_VIDEO_READY:
				openFile(new File(cachePath));  
				break;
			default:
				break;
			}
		}
	};
	
	private void buildOrUpdateDataset() {
		if (mAdapter == null) {
			mAdapter = new FtpFileAdapter(this, mFileList);
			mGridFile.setAdapter(mAdapter);
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
	
	private void executeCDURequest() {
		mThreadPool.execute(mCmdFactory.createCmdCDU());
	}

	private void executeDELERequest(String path, boolean isDirectory) {
		mThreadPool.execute(mCmdFactory.createCmdDEL(path, isDirectory));
	}

	private void executeREANMERequest(String newPath) {
		mThreadPool.execute(mCmdFactory.createCmdRENAME(newPath));
	}

	private void executeOpenRequest() {
		mThreadPool.execute(mCmdFactory.createCmdOpenVideo());
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
	
	private static String getParentCachePath() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (mAtSDCardPath != null) {
				return mAtSDCardPath;
			} else {
				mAtSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CACHE_DIR_NAME;
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
		
		public FtpCmd createCmdCDU() {
			return new CmdCDU();
		}
		
		public FtpCmd createCmdDEL(String path, boolean isDirectory) {
			return new CmdDELE(path, isDirectory);
		}

		public FtpCmd createCmdRENAME(String newPath) {
			return new CmdRENAME(newPath);
		}
		
		public FtpCmd createCmdOpenVideo() {
			return new CmdOpenVideo();
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
	
	public class CmdCDU extends FtpCmd {

		@Override
		public void run() {
			try {
				String dir = mFTPClient.currentDirectory();
				Log.v(TAG, "1 "+dir);
				mFTPClient.changeDirectoryUp();
				dir = mFTPClient.currentDirectory();
				Log.v(TAG, dir);
				mHandler.sendEmptyMessage(MSG_CMD_CDU_OK);
			} catch (Exception ex) {
				mHandler.sendEmptyMessage(MSG_CMD_CDU_FAILED);
				ex.printStackTrace();
			}
		}
	}

	public class CmdOPEN extends AsyncTask<Void, Integer, Boolean> {
		
		String localPath = getParentCachePath() + File.separator
				+ mFileList.get(mSelectedPosistion).getName();
		
		File cacheFile = new File(localPath);
		
		public CmdOPEN() {
			cachePath = localPath;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				mFTPClient.download(
						mFileList.get(mSelectedPosistion).getName(),cacheFile,
						new DownloadFTPDataTransferListener(mFileList.get(
								mSelectedPosistion).getSize()));
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
			openFile(cacheFile);
			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Boolean result) {
			cacheFile.deleteOnExit();
			toast(result ? "打开成功" : "打开失败");
			progressDialog.dismiss();
		}
	}
	
	public class CmdOpenVideo extends FtpCmd {

		@Override
		public void run() {
			String localPath = getParentCachePath() + File.separator
					+ "VideoCache" + File.separator;
			if (cachePath == null) {  
				cachePath = localPath + "1.mp4";  
            }
			FileOutputStream out = null;
			try {
				InputStream is =  mFTPClient.openStream(
						mFileList.get(mSelectedPosistion).getName());
			    byte buf[] = new byte[4 * 1024];
	            
	            File cacheFile = new File(cachePath);  
	            
                if (!cacheFile.exists()) {  
                    cacheFile.getParentFile().mkdirs();  
                    cacheFile.createNewFile();  
                } 
                
                out = new FileOutputStream(cacheFile, true);
				
                int size = 0, readSize = 0, fileNum=0;
                
				while ((size = is.read(buf)) != -1) {
					if (size > 0) {  
                        try {  
                            if(readSize>=READY_BUFF){  
                                fileNum++;  
                                  
                                switch(fileNum%3){  
                                    case 0:  
                                        out=new FileOutputStream(localPath+"1.mp4");  
                                        break;  
                                    case 1:  
                                        out=new FileOutputStream(localPath+"2.mp4");  
                                        break;  
                                    case 2:  
                                        out=new FileOutputStream(localPath+"3.mp4");  
                                        break;  
                                }  
                                  
                                readSize=0;   
                                mHandler.sendEmptyMessage(CACHE_VIDEO_READY);  
                            }  
                            out.write(buf, 0, size);  
                            out.flush();  
                            readSize += size;  
                            size = 0;// 循环接收  
                              
                              
                        } catch (Exception e) {  
                            Log.e(TAG, "出现异常0",e);  
                        }  
                          
                  }else{  
                      Log.i(TAG, "TS流停止发送数据");  
                  }
                }} catch (Exception e) {  
                    Log.e(TAG, "出现异常",e);  
                } finally {  
                    if (out != null) {  
                        try {  
                            out.close();  
                            progressDialog.dismiss();
                        } catch (IOException e) {  
                            //  
                            Log.e(TAG, "出现异常1",e);  
                        }  
                    }  
  
//				mHandler.sendEmptyMessage(MSG_CMD_CDU_OK);
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
			toast(result ? "下载成功，文件保存至/MShareDownload" : "下载失败");
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
	
	//文件选择器相关功能实现
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
	
	private String detectType(String fileName) throws IOException,
	FTPIllegalReplyException, FTPException {
		int start = fileName.lastIndexOf('.') + 1;
		int stop = fileName.length();
		if (start > 0 && start < stop - 1) {
			String ext = fileName.substring(start, stop);
			ext = ext.toLowerCase();
			return ext;
		}
		return null;
	}
	
	private void openFile(File file){
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//设置intent的Action属性 
		intent.setAction(Intent.ACTION_VIEW);
		//获取文件file的MIME类型 
		String type = getMIMEType(file);
		//设置intent的data和Type属性。 
		intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
		//跳转 
		startActivity(intent);
	}
	
	private String getMIMEType(File file) {  
		final String[][] MIME_MapTable={
				//{后缀名，	MIME类型}
				{".3gp",	"video/3gpp"},
				{".apk",	"application/vnd.android.package-archive"},
				{".asf",	"video/x-ms-asf"},
				{".avi",	"video/x-msvideo"},
				{".bin",	"application/octet-stream"},
				{".bmp",  	"image/bmp"},
				{".c",	"text/plain"},
				{".class",	"application/octet-stream"},
				{".conf",	"text/plain"},
				{".cpp",	"text/plain"},
				{".doc",	"application/msword"},
				{".docx",	"application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
				{".xls",	"application/vnd.ms-excel"}, 
				{".xlsx",	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
				{".exe",	"application/octet-stream"},
				{".gif",	"image/gif"},
				{".gtar",	"application/x-gtar"},
				{".gz",	"application/x-gzip"},
				{".h",	"text/plain"},
				{".htm",	"text/html"},
				{".html",	"text/html"},
				{".jar",	"application/java-archive"},
				{".java",	"text/plain"},
				{".jpeg",	"image/jpeg"},
				{".jpg",	"image/jpeg"},
				{".js",	"application/x-javascript"},
				{".log",	"text/plain"},
				{".m3u",	"audio/x-mpegurl"},
				{".m4a",	"audio/mp4a-latm"},
				{".m4b",	"audio/mp4a-latm"},
				{".m4p",	"audio/mp4a-latm"},
				{".m4u",	"video/vnd.mpegurl"},
				{".m4v",	"video/x-m4v"},	
				{".m4v",	"video/x-matroska"},
				{".mov",	"video/quicktime"},
				{".mp2",	"audio/x-mpeg"},
				{".mp3",	"audio/x-mpeg"},
				{".mp4",	"video/mp4"},
				{".mpc",	"application/vnd.mpohun.certificate"},		
				{".mpe",	"video/mpeg"},	
				{".mpeg",	"video/mpeg"},	
				{".mpg",	"video/mpeg"},	
				{".mpg4",	"video/mp4"},	
				{".mpga",	"audio/mpeg"},
				{".msg",	"application/vnd.ms-outlook"},
				{".ogg",	"audio/ogg"},
				{".pdf",	"application/pdf"},
				{".png",	"image/png"},
				{".pps",	"application/vnd.ms-powerpoint"},
				{".ppt",	"application/vnd.ms-powerpoint"},
				{".pptx",	"application/vnd.openxmlformats-officedocument.presentationml.presentation"},
				{".prop",	"text/plain"},
				{".rc",	"text/plain"},
				{".rmvb",	"audio/x-pn-realaudio"},
				{".rtf",	"application/rtf"},
				{".sh",	"text/plain"},
				{".tar",	"application/x-tar"},	
				{".tgz",	"application/x-compressed"}, 
				{".txt",	"text/plain"},
				{".wav",	"audio/x-wav"},
				{".wma",	"audio/x-ms-wma"},
				{".wmv",	"audio/x-ms-wmv"},
				{".wps",	"application/vnd.ms-works"},
				{".xml",	"text/plain"},
				{".z",	"application/x-compress"},
				{".zip",	"application/x-zip-compressed"},
				{"",		"*/*"}	
			};
	      
	    String type="*/*";  
	    String fName = file.getName();  
	    //获取后缀名前的分隔符"."在fName中的位置。  
	    int dotIndex = fName.lastIndexOf(".");  
	    if(dotIndex < 0){  
	        return type;  
	    }  
	    /* 获取文件的后缀名 */  
	    String end=fName.substring(dotIndex,fName.length()).toLowerCase();  
	    if(end=="")return type;  
	    //在MIME和文件类型的匹配表中找到对应的MIME类型。  
	    for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？  
	        if(end.equals(MIME_MapTable[i][0]))  
	            type = MIME_MapTable[i][1];  
	    }         
	    return type;  
	}
	
	private String getMIMEType(String fName) {  
		final String[][] MIME_MapTable={
				//{后缀名，	MIME类型}
				{".3gp",	"video/3gpp"},
				{".apk",	"application/vnd.android.package-archive"},
				{".asf",	"video/x-ms-asf"},
				{".avi",	"video/x-msvideo"},
				{".bin",	"application/octet-stream"},
				{".bmp",  	"image/bmp"},
				{".c",	"text/plain"},
				{".class",	"application/octet-stream"},
				{".conf",	"text/plain"},
				{".cpp",	"text/plain"},
				{".doc",	"application/msword"},
				{".docx",	"application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
				{".xls",	"application/vnd.ms-excel"}, 
				{".xlsx",	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
				{".exe",	"application/octet-stream"},
				{".gif",	"image/gif"},
				{".gtar",	"application/x-gtar"},
				{".gz",	"application/x-gzip"},
				{".h",	"text/plain"},
				{".htm",	"text/html"},
				{".html",	"text/html"},
				{".jar",	"application/java-archive"},
				{".java",	"text/plain"},
				{".jpeg",	"image/jpeg"},
				{".jpg",	"image/jpeg"},
				{".js",	"application/x-javascript"},
				{".log",	"text/plain"},
				{".m3u",	"audio/x-mpegurl"},
				{".m4a",	"audio/mp4a-latm"},
				{".m4b",	"audio/mp4a-latm"},
				{".m4p",	"audio/mp4a-latm"},
				{".m4u",	"video/vnd.mpegurl"},
				{".m4v",	"video/x-m4v"},	
				{".m4v",	"video/x-matroska"},
				{".mov",	"video/quicktime"},
				{".mp2",	"audio/x-mpeg"},
				{".mp3",	"audio/x-mpeg"},
				{".mp4",	"video/mp4"},
				{".mpc",	"application/vnd.mpohun.certificate"},		
				{".mpe",	"video/mpeg"},	
				{".mpeg",	"video/mpeg"},	
				{".mpg",	"video/mpeg"},	
				{".mpg4",	"video/mp4"},	
				{".mpga",	"audio/mpeg"},
				{".msg",	"application/vnd.ms-outlook"},
				{".ogg",	"audio/ogg"},
				{".pdf",	"application/pdf"},
				{".png",	"image/png"},
				{".pps",	"application/vnd.ms-powerpoint"},
				{".ppt",	"application/vnd.ms-powerpoint"},
				{".pptx",	"application/vnd.openxmlformats-officedocument.presentationml.presentation"},
				{".prop",	"text/plain"},
				{".rc",	"text/plain"},
				{".rmvb",	"audio/x-pn-realaudio"},
				{".rtf",	"application/rtf"},
				{".sh",	"text/plain"},
				{".tar",	"application/x-tar"},	
				{".tgz",	"application/x-compressed"}, 
				{".txt",	"text/plain"},
				{".wav",	"audio/x-wav"},
				{".wma",	"audio/x-ms-wma"},
				{".wmv",	"audio/x-ms-wmv"},
				{".wps",	"application/vnd.ms-works"},
				{".xml",	"text/plain"},
				{".z",	"application/x-compress"},
				{".zip",	"application/x-zip-compressed"},
				{"",		"*/*"}	
			};
	      
	    String type="*/*";   
	    //获取后缀名前的分隔符"."在fName中的位置。  
	    int dotIndex = fName.lastIndexOf(".");  
	    if(dotIndex < 0){  
	        return type;  
	    }  
	    /* 获取文件的后缀名 */  
	    String end=fName.substring(dotIndex,fName.length()).toLowerCase();  
	    if(end=="")return type;  
	    //在MIME和文件类型的匹配表中找到对应的MIME类型。  
	    for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？  
	        if(end.equals(MIME_MapTable[i][0]))  
	            type = MIME_MapTable[i][1];  
	    }         
	    return type;  
	}
}




