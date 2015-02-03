package org.mshare.main;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mshare.main.R;
import org.mshare.main.FtpMainActivity.CmdFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;


public class JoinConn extends Activity {
	
	private static String TAG = FtpMainActivity.class.getName();
	
	private GridView gridview;
	private Button btftp;
	private ArrayList<HashMap<String, Object>> listImageItem;  
    private SimpleAdapter simpleAdapter;
    
    private ListView mListView;
    private String mSdcardRootPath;
    private Object mLock = new Object();
	private int mSelectedPosistion = -1;

	private String mCurrentPWD; // 当前远程目录
    
    private CmdFactory mCmdFactory;
	private FTPClient mFTPClient;
	private ExecutorService mThreadPool;
	
	private String mFTPHost ;
	private int mFTPPort ;
	private String mFTPUser ;
	private String mFTPPassword ;
	
	private Thread mDameonThread = null ;
	private boolean mDameonRunning = true;
	
	private FtpFileAdapter mAdapter;
	private List<FTPFile> mFileList = new ArrayList<FTPFile>();
	
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
    
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slidelistview);
		gridview = (GridView) findViewById(R.id.gridview);
	    btftp = (Button) findViewById(R.id.btnewftp);
	    listImageItem = new ArrayList<HashMap<String, Object>>();  
	    
	    mSdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mCmdFactory = new CmdFactory();
		mFTPClient = new FTPClient();
		mThreadPool = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
      
	    simpleAdapter = new SimpleAdapter(  
	            this, listImageItem,  
	            R.layout.labelicon, new String[] {  
	                    "ItemImage", "ItemText" }, new int[] { R.id.imageview,  
	                    R.id.textview });  
	    //添加图片绑定  
//	    simpleAdapter.setViewBinder(new ViewBinder() {  
//	        public boolean setViewValue(View view, Object data,  
//	                String textRepresentation) {  
//	            if (view instanceof ImageView && data instanceof Drawable) {  
//	                ImageView iv = (ImageView) view;  
//	                iv.setImageDrawable((Drawable) data);  
//	                return true;  
//	            } else  
//	                return false;  
//	        }  
//	    });  
//	    gridview.setAdapter(simpleAdapter); 
	}
	
	public void customView(View source)
	{
		//装载/res/layout/login.xml界面布局
		TableLayout loginForm = (TableLayout)getLayoutInflater()
			.inflate( R.layout.login, null);	
		final EditText editHost = (EditText) loginForm.findViewById(R.id.editFTPHost);
		final EditText editPort= (EditText) loginForm.findViewById(R.id.editFTPPort);
		editPort.setText("3721");
		final EditText editUser = (EditText) loginForm.findViewById(R.id.editFTPUser);
		final EditText editPasword= (EditText) loginForm.findViewById(R.id.editPassword);
		new AlertDialog.Builder(this)
			// 设置对话框的图标
			.setIcon(R.drawable.app_default_icon)
			// 设置对话框的标题
			.setTitle("新建FTP服务器")
			// 设置对话框显示的View对象
			.setView(loginForm)
			// 为对话框设置一个“确定”按钮
			.setPositiveButton("确定" , new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog,
						int which)
				{
					if (TextUtils.isEmpty(editHost.getText()) || 
							TextUtils.isEmpty(editPort.getText()) || 
							TextUtils.isEmpty(editUser.getText()) ||
							TextUtils.isEmpty(editUser.getText())) {
						  toast("请将资料填写完整");
//						  JoinConn.this.finish();
						  return ;
					}
					try{
					    mFTPPort = Integer.parseInt(editPort.getText().toString().trim());
					}
					catch(NumberFormatException nfEx){
						nfEx.printStackTrace();
						toast("端口输入有误，请重试");
						return ;
					}
					mFTPHost = editHost.getText().toString().trim();
					mFTPUser = editUser.getText().toString().trim();
					mFTPPassword = editPasword.getText().toString().trim();
					Log.v(TAG, "mFTPHost #" + mFTPHost + " mFTPPort #" + mFTPPort 
							+ " mFTPUser #" + mFTPUser + " mFTPPassword #" + mFTPPassword);
					// 此处可执行登录处理
					executeConnectRequest();
				}
			})
			// 为对话框设置一个“取消”按钮
			.setNegativeButton("取消", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog,
						int which)
				{
					// 取消登录，不做任何事情。
				}
			})
			// 创建、并显示对话框
			.create()
			.show();
	}
	
	private void buildOrUpdateDataset() {
		HashMap<String, Object> map = new HashMap<String, Object>();  
		map.put("ItemImage", R.drawable.folder);// 添加图像资源的ID  
        map.put("ItemText", mFTPHost);// 按FTPHost做ItemText  
        listImageItem.add(map);
        
	    //添加图片绑定  
	    simpleAdapter.setViewBinder(new ViewBinder() {  
	        public boolean setViewValue(View view, Object data,  
	                String textRepresentation) {  
	            if (view instanceof ImageView && data instanceof Drawable) {  
	                ImageView iv = (ImageView) view;  
	                iv.setImageDrawable((Drawable) data);  
	                return true;  
	            } else  
	                return false;  
	        }  
	    });  
	    gridview.setAdapter(simpleAdapter);
	    gridview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Log.v(TAG,"test onItemSelected");
				Intent intent = new Intent(view.getContext(), FtpFileManage.class);
				Bundle data=new Bundle();
				data.putSerializable("mFileList", (Serializable) mFileList);
				data.putSerializable("mCurrentPWD", mCurrentPWD);
				data.putSerializable("mFTPHost",mFTPHost);
				data.putSerializable("mFTPPort",mFTPPort);
				data.putSerializable("mFTPUser",mFTPUser);
				data.putSerializable("mFTPPassword",mFTPPassword);
                intent.putExtras(data);
                view.getContext().startActivity(intent);
			}
	    	
	    });
//		if (mAdapter == null) {
//			mAdapter = new FtpFileAdapter(this, mFileList);
//			mListView.setAdapter(mAdapter);
//		}
//		mAdapter.notifyDataSetChanged();
	}
	
	private void executeConnectRequest() {
		if(mThreadPool==null)
			Log.v(TAG, "mThreadPool is null");
		if(mCmdFactory==null)
			Log.v(TAG, "mCmdFactory is null");
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
}



