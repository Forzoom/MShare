package org.mshare.main;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mshare.file.MshareFileMenu;
import org.mshare.server.ServerSettings;
import org.mshare.server.ftp.ServerService;
import org.mshare.picture.CanvasElement;
import org.mshare.picture.PictureBackground;
import org.mshare.picture.RingButton;
import org.mshare.picture.ServerOverviewSurfaceView;
import org.mshare.scan.ScanActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * ViewSwitcher并不能将SurfaceView移动，所以还是有问题的
 * @author HM
 *
 */
public class OverviewActivity extends Activity implements StatusController.StatusCallback {
	private static final String TAG = OverviewActivity.class.getSimpleName();

	// 判断Fling的GestureDetector
	private GestureDetector gestureDetector;
	// 用于切换的ViewSwitcher
	private ViewSwitcher viewSwitcher;
	
	// 状态控制器
	private StatusController statusController;

	/* FTP服务器 */
	// 服务器UI SurfaceView
	private ServerOverviewSurfaceView surfaceView;
	// 服务器菜单
	private LinearLayout serverMenu;
	private MshareFileMenu menuInStop;
	private MshareFileMenu menuInStart;

	//连接参数
	private ServerListGridView gridview;
	private LinearLayout btftp, btscan;
	private ArrayList<HashMap<String, Object>> listImageItem;  
    private SimpleAdapter simpleAdapter;
    
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overview_activity_container);
		
		// 在第一次启动的时候使用默认配置
		PreferenceManager.setDefaultValues(this, R.xml.server_settings, false);
		
		// 获得基本配置内容
//		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//		String nickName = sp.getString("nickname", "");
		
		// 设置ViewSwitcher
		viewSwitcher = (ViewSwitcher)findViewById(R.id.view_switcher);
		FrameLayout.LayoutParams serverParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewGroup serverOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_server, null);
		FrameLayout.LayoutParams clientParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewGroup clientOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_client, null);
		viewSwitcher.addView(serverOverview, serverParams);
		viewSwitcher.addView(clientOverview, clientParams);
		
		// 设置状态控制器
		statusController = new StatusController();
		statusController.setCallback(this);

		// 服务器菜单
		serverMenu = (LinearLayout)serverOverview.findViewById(R.id.server_overview_menu);
		// 服务器停止状态下的Menu
		menuInStop = new MshareFileMenu(this, serverMenu);
		// 本地文件浏览器按钮
		menuInStop.addButton(R.drawable.icon_folder, new LocalFileBrowserListener());
		// 服务器启动状态下的Menu
		menuInStart = new MshareFileMenu(this, serverMenu);
		
		// 二维码按钮
		menuInStart.addButton(R.drawable.icon_folder, new LocalFileBrowserListener());
		menuInStart.addButton(R.drawable.qrcode, new QrcodeViewListener());
		menuInStart.addButton(R.drawable.qrcode, new BasicViewListener());
		
		// 设置surfaceView
		surfaceView = (ServerOverviewSurfaceView)findViewById(R.id.server_overview_surface_view);
		surfaceView.setStatusController(statusController);

        // 设置onFling的GestureDetector
		gestureDetector = new GestureDetector(this, new SwitchListener());

        // 为surfaceView中的内容添加监听器
        surfaceView.setServerButtonListener(new ServerButtonListener());
        surfaceView.setSettingsButtonListener(new SettingsButtonListener());

		// 客户端界面中加入连接的GridView
		gridview = (ServerListGridView) findViewById(R.id.gridview);
		gridview.setGestureDetector(gestureDetector);
		
	    btftp = (LinearLayout) findViewById(R.id.btftp);
	    btscan = (LinearLayout) findViewById(R.id.btscan);
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
	    
	    btftp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				customView();
				
			}
		});
	    
	    btscan.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(OverviewActivity.this, ScanActivity.class);
				startActivityForResult(intent, 0);
			}
		});
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		//当requestCode、resultCode同时为0是，也就是处理特定结果的结果
		
		if(requestCode == 0 && resultCode == Activity.RESULT_OK){

//			mFTPPort = Integer.parseInt(editPort.getText().toString().trim());
//			mFTPHost = editHost.getText().toString().trim();
//			mFTPUser = editUser.getText().toString().trim();
//			mFTPPassword = editPasword.getText().toString().trim();
			
			if (intent == null) {
				Log.e(TAG, "the intent is null, stop!");
				return;
			}
			
			ConnectInfo connectInfo = intent.getParcelableExtra(ScanActivity.EXTRA_CONNECT_INFO);
			
			String port = connectInfo.getPort();
			String host = connectInfo.getHost();
			String username = connectInfo.getUsername();
			String password = connectInfo.getPassword();
			
			mFTPPort = Integer.parseInt(port.trim());
			mFTPHost = host.trim();
			mFTPUser = username.trim();
			mFTPPassword = password.trim();
			
			Log.v(TAG, "mFTPHost #" + mFTPHost + " mFTPPort #" + mFTPPort 
					+ " mFTPUser #" + mFTPUser + " mFTPPassword #" + mFTPPassword);
			// 此处可执行登录处理
			executeConnectRequest();
		}
	}
	
	public void customView()
	{
		//装载/res/layout/login.xml界面布局
		TableLayout loginForm = (TableLayout)getLayoutInflater()
			.inflate( R.layout.login, null);	
		final EditText editHost = (EditText) loginForm.findViewById(R.id.editFTPHost);
		editHost.setText("192.168.0.101");
		final EditText editPort= (EditText) loginForm.findViewById(R.id.editFTPPort);
		editPort.setText("3721");
		final EditText editUser = (EditText) loginForm.findViewById(R.id.editFTPUser);
		editUser.setText("123");
		final EditText editPasword= (EditText) loginForm.findViewById(R.id.editPassword);
		editPasword.setText("abc");
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

        if (mFTPClient.isMShareServerSupport()) {

            String uuid = mFTPClient.getUUID();
            SharedPreferences sp = getSharedPreferences("server_list", Context.MODE_PRIVATE);
            String nickname = sp.getString(uuid, "");
            if (!nickname.equals("")) {
                map.put("ItemText", nickname);
            } else {
                map.put("ItemText", mFTPHost);
            }
        } else {
            map.put("ItemText", mFTPHost);
        }

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
	}
	
	private void executeConnectRequest() {
		if(mThreadPool==null)
			Log.v(TAG, "mThreadPool is null");
		if(mCmdFactory==null)
			Log.v(TAG, "mCmdFactory is null");
		mThreadPool.execute(mCmdFactory.createCmdConnect());
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
				buildOrUpdateDataset();
				break;
			case MSG_CMD_CONNECT_FAILED:
				toast("FTP服务器连接失败，正在重新连接");
				executeConnectRequest();
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
                // 连接结束
				String[] welcome = mFTPClient.connect(mFTPHost, mFTPPort);
				if (welcome != null) {
					for (String value : welcome) {
						logv("connect " + value);
					}
				}
                // 尝试登陆
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
	
	public class CmdFactory {

		public FtpCmd createCmdConnect() {
			return new CmdConnect();
		}

		public FtpCmd createCmdDisConnect() {
			return new CmdDisConnect();
		}

	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		// TODO 可能需要使用更加安全的BroadcastReceiver注册方式
		super.onStart();

		// 初始化StatusController，初始化所有状态
		
		// TODO 需要处理的内容太多了，考虑不加入开启AP的功能
		// 并没有AP cannot enable，所以对于isWifiApEnable函数，可以正确的执行,但是对于setWifiApEnabled就会报错
		// TODO 如果启动AP失败了之后，就将其写入配置文件，表明当前设备可能并不支持开启AP

		// 当前上传路径
//		uploadPathView.setText(ServerSettings.getUpload());
		statusController.registerReceiver();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		statusController.initial();

		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		statusController.unregisterReceiver();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 用于检测Fling
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public void onServerStatusChange(int status) {
		Log.d(TAG, "onServerStatus");
		// 可以将operating的颜色变化放在这里
		if (status == StatusController.STATUS_SERVER_STARTED) {
            // 使用启动动画
            surfaceView.startServerAnimation();

            // 菜单动画
            menuInStop.hideAnimation();
            menuInStart.showAnimation();
		} else if (status == StatusController.STATUS_SERVER_STOPPED) {
            // 使用停止动画
            surfaceView.stopServerAniamtion();

            // 处理菜单动画
            menuInStart.hideAnimation();
            menuInStop.showAnimation();
		}
		
	}
	
	@Override
	public void onWifiStatusChange(int state) {
		Log.d(TAG, "on wifi state change");
		switch (state) {
		// 表示的是手机不支持WIFI
		case StatusController.STATE_WIFI_DISABLE:
		case StatusController.STATE_WIFI_ENABLE:
			if (ServerService.isRunning()) {
				// 尝试关闭服务器
//				stopServer();
			}
//			ftpAddrView.setText("未知");
			break;
		case StatusController.STATE_WIFI_USING:
			// 设置显示的IP地址
//			ftpAddrView.setText(ServerService.getLocalInetAddress().getHostAddress());
			break;
		}
	}
	
	/**
	 * 这里可能会有代码重复,需要将上面的内容除去
	 */
	@Override
	public void onWifiApStatusChange(int status) {
		Log.d(TAG, "on wifi ap state change");
		// TODO 地址可能并不是这样设置的，所以暂时将这些注释
		// 设置地址
//		byte[] address = ServerService.getLocalInetAddress().getAddress();
//		String addressStr = "";
//		for (int i = 0, len = address.length; i < len; i++) {
//			byte b = address[i];
//			addressStr += String.valueOf(((int)b + 256)) + " ";
//		}
//		ftpApIp.setText(addressStr);
//		ftpApIp.setVisibility(View.VISIBLE);
	}

	@Override
	public void onWifiP2pStatusChange(int status) {
		// TODO Auto-generated method stub
		Log.d(TAG, "on wifi p2p state change");
	}

	@Override
	public void onExternalStorageChange(int status) {
		// TODO 对于扩展存储的变化能够作为响应
		Log.d(TAG, "on external storage state change");
	}

	@Override
	public void onNfcStatusChange(int status) {
		Log.d(TAG, "on nfc state change");
	}
	
	/**
	 * 尝试启动服务器
	 */
	private void startServer() {
		// 设置新的配置内容
		sendBroadcast(new Intent(ServerService.ACTION_START_FTPSERVER));
	}
	
	/**
	 * 尝试停止服务器
	 */
	private void stopServer() {
		sendBroadcast(new Intent(ServerService.ACTION_STOP_FTPSERVER));
		
	}
	
	class SwitchListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			Log.d(TAG, "onFling");
			// 右
			if (velocityX > 0.0f && Math.abs(velocityX) > 500.0f) {
				viewSwitcher.setInAnimation(OverviewActivity.this, R.anim.slide_in_left);
				viewSwitcher.setOutAnimation(OverviewActivity.this, R.anim.slide_out_right);
				viewSwitcher.showPrevious();
			} else if (velocityX < 0.0f && Math.abs(velocityX) > 500.0f) {
				viewSwitcher.setInAnimation(OverviewActivity.this, R.anim.slide_in_right);
				viewSwitcher.setOutAnimation(OverviewActivity.this, R.anim.slide_out_left);
				viewSwitcher.showNext();
			}
			
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}
	
	private class ServerButtonListener implements CanvasElement.ElementOnClickListener {

		@Override
		public void onClick() {
			int serverStatus = statusController.getServerStatus();
			if (serverStatus != StatusController.STATUS_SERVER_STARTED && serverStatus != StatusController.STATUS_SERVER_STOPPED) {
				Log.d(TAG, "is operating server now");
				return;
			}
			
			// 执行bounceAnimaion和启动和关闭服务器
			long startTime = System.currentTimeMillis();

            // 启动了bounceAnimation
            RingButton serverButton = surfaceView.getServerButton();
			serverButton.stopBounceAnimation();
			serverButton.startBounceAnimation(surfaceView.getServerInnerRadius(), startTime, 500);
			
			// 修改服务器状态、启动或关闭服务器
			if (serverStatus == StatusController.STATUS_SERVER_STARTED) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STOPING);
				stopServer();
			} else if (serverStatus == StatusController.STATUS_SERVER_STOPPED) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STARTING);
				startServer();
			}
			
			// 修改背景色到执行状态
            PictureBackground pictureBackground = surfaceView.getPictureBackground();
		    pictureBackground.stopColorAnimation();
			pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), surfaceView.getOperatingColor(), startTime, 500);
		}
	}
	
	private class SettingsButtonListener implements CanvasElement.ElementOnClickListener {
		@Override
		public void onClick() {
			Log.d(TAG, "settings button clicked!");
			surfaceView.getSettingsButton().startAlphaAnimation(223);
			
			// 尝试启动serverSettings
			Intent startServerSettingsIntent = new Intent(OverviewActivity.this, ServerSettingActivity.class);
			startActivity(startServerSettingsIntent);
		}
	}
	
	private class LocalFileBrowserListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent startFileBrowserIntent = new Intent(OverviewActivity.this, FileBrowserActivity.class);
			startActivity(startFileBrowserIntent);
		}
	}
	
	private class QrcodeViewListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent startQrcodeIntent = new Intent(OverviewActivity.this, QRCodeConnectActivity.class);
			
			// 暂时在这里判断服务器是否正在运行，只有当服务器争取额运行的时候才能够打开qrcode
			if (ServerService.isRunning()) {
				String host = ServerService.getLocalInetAddress().toString().substring(1);
				Log.d(TAG, "special log: " + host);
				String port = String.valueOf(ServerSettings.getPort());
				String username = ServerSettings.getUsername();
				String password = ServerSettings.getPassword();
				
				ConnectInfo connectInfo = new ConnectInfo(host, port, username, password);
				
				startQrcodeIntent.putExtra(QRCodeConnectActivity.EXTRA_CONTENT, connectInfo);
				startActivity(startQrcodeIntent);
			} else {
				// do nothing..
				Log.e(TAG, "try to view qrcode, but the ftp service is not running!");
			}
			
		}
	}

	private class BasicViewListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// 启动
			Intent startBasicIntent = new Intent(OverviewActivity.this, BasicConnectActivity.class);
			startActivity(startBasicIntent);
		}
	}

	/* 通过反射机制调用AP开启功能 */
//	WifiManager wm = (WifiManager)getSystemService(Service.WIFI_SERVICE);
//	
//	try {
//		// 用于获得WifiConfiguration
//		Method getWifiApConfigurationMethod = wm.getClass().getDeclaredMethod("getWifiApConfiguration");
//		WifiConfiguration config = (WifiConfiguration)getWifiApConfigurationMethod.invoke(wm);
//		
//		Method setWifiApEnabledMethod = wm.getClass().getDeclaredMethod("setWifiApEnabled");
//		setWifiApEnabledMethod.invoke(wm, config, enable);
//		
//	} catch (Exception e) {
//		Toast.makeText(this, "AP无法启动", Toast.LENGTH_SHORT).show();
//		e.printStackTrace();
//	}
}
