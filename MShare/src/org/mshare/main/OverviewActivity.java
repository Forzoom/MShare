package org.mshare.main;

import java.util.ArrayList;

import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.StatusController.StatusCallback;
import org.mshare.picture.SettingsButton;
import org.mshare.picture.CanvasAnimation;
import org.mshare.picture.CanvasElement;
import org.mshare.picture.PictureBackground;
import org.mshare.picture.RingButton;
import org.mshare.picture.RefreshHandler;
import org.mshare.picture.ServerOverviewSurfaceView;

import android.animation.ArgbEvaluator;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
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
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
	private RingButton serverButton;
	private SettingsButton settingsButton;
	
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
		
		// 设置按钮
		Bitmap settingsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
		settingsButton = new SettingsButton(settingsBitmap); 
		settingsButton.setElementOnClickListener(new SettingsButtonListener());
		surfaceView.setSettingsButton(settingsButton);
		
		// 服务器按钮
		serverButton = new RingButton();
		serverButton.setElementOnClickListener(new ServerButtonListener());
		surfaceView.setServerButton(serverButton);
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
}
