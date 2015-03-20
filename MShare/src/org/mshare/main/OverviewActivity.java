package org.mshare.main;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.JoinConn.CmdCWD;
import org.mshare.main.JoinConn.CmdConnect;
import org.mshare.main.JoinConn.CmdDELE;
import org.mshare.main.JoinConn.CmdDisConnect;
import org.mshare.main.JoinConn.CmdFactory;
import org.mshare.main.JoinConn.CmdLIST;
import org.mshare.main.JoinConn.CmdPWD;
import org.mshare.main.JoinConn.CmdRENAME;
import org.mshare.main.JoinConn.DameonFtpConnector;
import org.mshare.main.JoinConn.FtpCmd;
import org.mshare.main.StatusController.StatusCallback;
import org.mshare.picture.CircleAvater;
import org.mshare.picture.SettingsButton;
import org.mshare.picture.CanvasAnimation;
import org.mshare.picture.CanvasElement;
import org.mshare.picture.PictureBackground;
import org.mshare.picture.RingButton;
import org.mshare.picture.RefreshHandler;
import org.mshare.picture.ServerOverviewSurfaceView;
import org.mshare.scan.ScanActivity;

import android.animation.ArgbEvaluator;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;
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
	
	private ViewSwitcher viewSwitcher;
	
	private StatusController statusController;
	
	private ServerOverviewSurfaceView surfaceView;
	
	private PictureBackground pictureBackground;
	private CircleAvater circleAvater;
	private RingButton serverButton;
	private SettingsButton settingsButton;
	
	//连接参数
	private ServerListGridView gridview;
	private LinearLayout btftp, btscan;
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
		ViewGroup serverOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_flat, null);
		FrameLayout.LayoutParams clientParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewGroup clientOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_other, null);
		viewSwitcher.addView(serverOverview, serverParams);
		viewSwitcher.addView(clientOverview, clientParams);
		
		// 
		statusController = new StatusController();
		statusController.setCallback(this);
		
		// 打开文件浏览器的回调函数
		RelativeLayout fileBrowserButton = (RelativeLayout)serverOverview.findViewById(R.id.overview_activity_file_browser_button);
		fileBrowserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startFileBrowserIntent = new Intent(OverviewActivity.this, FileBrowserActivity.class);
				startActivity(startFileBrowserIntent);
			}
		});
		
		// 打开文件浏览器的回调函数
		RelativeLayout qrcodeButton = (RelativeLayout)serverOverview.findViewById(R.id.overview_activity_qrcode_button);
		qrcodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startQrcodeIntent = new Intent(OverviewActivity.this, QRCodeConnectActivity.class);
				startQrcodeIntent.putExtra(QRCodeConnectActivity.EXTRA_CONTENT, new ConnectInfo("192.168.1.1", "2121", "username", "password"));
				startActivity(startQrcodeIntent);
			}
		});
		
		// 设置surfaceView
		surfaceView = (ServerOverviewSurfaceView)findViewById(R.id.server_overview_surface_view);
		surfaceView.setStatusController(statusController);
		// 设置onFling的GestureDetector
		gestureDetector = new GestureDetector(this, new SwitchListener());
		
		/* 创建所需要绘制的元素 */
		// 背景
		pictureBackground = new PictureBackground();
		surfaceView.setPictureBackground(pictureBackground);
		
		// 头像
		circleAvater = new CircleAvater();
		surfaceView.setCircleAvater(circleAvater);
		
		// 设置按钮
		Bitmap settingsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
		settingsButton = new SettingsButton(settingsBitmap); 
		settingsButton.setElementOnClickListener(new SettingsButtonListener());
		surfaceView.setSettingsButton(settingsButton);
		
		// 服务器按钮
		serverButton = new RingButton();
		serverButton.setElementOnClickListener(new ServerButtonListener());
		surfaceView.setServerButton(serverButton);
		
		// 添加加入链接的组件
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
			
			ConnectInfo connectInfo = ConnectInfo.parse(intent.getStringExtra("result"));
			
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
//		uploadPathView.setText(FsSettings.getUpload());
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
			long startTime = System.currentTimeMillis();
			
			pictureBackground.stopColorAnimation();
			CanvasAnimation colorAnimation = pictureBackground.getColorAnimation();
			if (colorAnimation != null) {
				colorAnimation.setDuration(500);
			}
			pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), surfaceView.getStartColor(), startTime);
			
			serverButton.stopBreatheAnimation();
			CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
			if (breatheAnimation != null) {
				breatheAnimation.setDuration(3000);
				breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_INFINITE);
			}
			serverButton.startBreatheAnimation(surfaceView.getServerOuterRadius(), startTime);
			
		} else if (status == StatusController.STATUS_SERVER_STOPPED) {
			// 调整背景颜色
			pictureBackground.stopColorAnimation();
			CanvasAnimation colorAnimation = pictureBackground.getColorAnimation();
			if (colorAnimation != null) {
				colorAnimation.setDuration(500);
			}
			pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), surfaceView.getStopColor());
			
			// 调整呼吸动画
			CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
			// 通过设置repeatMode，当动画循环结束的时候就会自动stop
			if (breatheAnimation != null) {
				breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_ONCE);
			}
		}
		
	}
	
	@Override
	public void onWifiStatusChange(int state) {
		Log.d(TAG, "on wifi state change");
		switch (state) {
		// 表示的是手机不支持WIFI
		case StatusController.STATE_WIFI_DISABLE:
		case StatusController.STATE_WIFI_ENABLE:
			if (FsService.isRunning()) {
				// 尝试关闭服务器
//				stopServer();
			}
//			ftpAddrView.setText("未知");
			break;
		case StatusController.STATE_WIFI_USING:
			// 设置显示的IP地址
//			ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
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
//		byte[] address = FsService.getLocalInetAddress().getAddress();
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
		sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
	}
	
	/**
	 * 尝试停止服务器
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		
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
			
			serverButton.stopBounceAnimation();
			CanvasAnimation bounceAnimation = serverButton.getBounceAnimation();
			bounceAnimation.setDuration(500);
			serverButton.startBounceAnimation(surfaceView.getServerInnerRadius(), startTime);
			
			// 修改服务器状态、启动或关闭服务器
			if (serverStatus == StatusController.STATUS_SERVER_STARTED) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STOPING);
				stopServer();
			} else if (serverStatus == StatusController.STATUS_SERVER_STOPPED) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STARTING);
				startServer();
			}
			
			// 修改背景色
			pictureBackground.stopColorAnimation();
			CanvasAnimation colorAnimation = pictureBackground.getColorAnimation();
			colorAnimation.setDuration(500);
			pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), surfaceView.getOperatingColor(), startTime);
		}
	}
	
	private class SettingsButtonListener implements CanvasElement.ElementOnClickListener {
		@Override
		public void onClick() {
			Log.d(TAG, "settings button clicked!");
			settingsButton.startAlphaAnimation(223);
			
			// 尝试启动serverSettings
			Intent startServerSettingsIntent = new Intent(OverviewActivity.this, ServerSettingActivity.class);
			startActivity(startServerSettingsIntent);
		}
	}
	
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
