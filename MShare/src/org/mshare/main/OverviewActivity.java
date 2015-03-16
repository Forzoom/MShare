package org.mshare.main;

import java.util.ArrayList;

import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.StatusController.StatusCallback;
import org.mshare.picture.BitmapButton;
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

/**
 * ViewSwitcher并不能将SurfaceView移动，所以还是有问题的
 * @author HM
 *
 */
public class OverviewActivity extends Activity implements SurfaceHolder.Callback, Handler.Callback, StatusController.StatusCallback {
	private static final String TAG = OverviewActivity.class.getSimpleName();

	// 所有将被绘制的内容
	private ArrayList<CanvasElement> canvasElements = new ArrayList<CanvasElement>();
	// SurfaceHolder
	private SurfaceHolder surfaceHolder;
	// 判断Fling的GestureDetector
	private GestureDetector gestureDetector;
	// 刷新SurfaceView所用的Handler
	private RefreshHandler refreshHandler;
	// 统一画笔
	private Paint canvasPaint = new Paint();
	// 当前是否在循环绘制
	private boolean isLooping = false;
	// 背景颜色
	private int stopColor;
	private int startColor;
	private int operatingColor;
	
	private PictureBackground pictureBackground;
	private RingButton serverButton;
	private BitmapButton settingsButton;
	// 所设置的缩小的内半径
	private int serverInnerRadius;
	// 所设置的放大的呼吸外半径
	private int serverOuterRadius;
	
	private ViewSwitcher viewSwitcher;
	
	private StatusController statusController;
	
	private boolean isSurfaceCreated = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overview_activity_container);
		
		// 设置ViewSwitcher
		viewSwitcher = (ViewSwitcher)findViewById(R.id.view_switcher);
		
		FrameLayout.LayoutParams serverParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// TODO 不知道为什么，在这里设置并没有用
		ViewGroup serverOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_flat, null);
		FrameLayout.LayoutParams clientParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewGroup clientOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_other, null);
		viewSwitcher.addView(serverOverview, serverParams);
		viewSwitcher.addView(clientOverview, clientParams);
		
		// 创建背景颜色
		stopColor = getResources().getColor(R.color.blue01);
		startColor = getResources().getColor(R.color.blue08);
		operatingColor = getResources().getColor(R.color.blue00);
		
		// 设置服务器相关
		ServerOverviewSurfaceView surfaceView = (ServerOverviewSurfaceView)findViewById(R.id.server_overview_surface_view);
		surfaceView.setGestureDetector(new GestureDetector(this, new GestureListener()));
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		
		refreshHandler = new RefreshHandler(Looper.myLooper(), this);
		gestureDetector = new GestureDetector(this, new SwitchListener());
		
		canvasPaint.setAntiAlias(true);
		canvasPaint.setColor(getResources().getColor(R.color.color_light_gray));
		canvasPaint.setAlpha(223);
		
		pictureBackground = new PictureBackground();
		serverButton = new RingButton();
		serverButton.setElementOnClickListener(new CanvasElement.ElementOnClickListener() {
			
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
				serverButton.startBounceAnimation(serverInnerRadius, startTime);
				
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
				pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), operatingColor, startTime);
			}
		});
		Bitmap settings = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
		settingsButton = new BitmapButton(settings); 
	}

	@Override
	protected void onStart() {
		// TODO 可能需要使用更加安全的BroadcastReceiver注册方式
		super.onStart();

		// 初始化StatusController，初始化所有状态
		statusController = new StatusController();
		statusController.setCallback(this);
		statusController.initial();
		
		// TODO 需要处理的内容太多了，考虑不加入开启AP的功能
		// 并没有AP cannot enable，所以对于isWifiApEnable函数，可以正确的执行,但是对于setWifiApEnabled就会报错
		// TODO 如果启动AP失败了之后，就将其写入配置文件，表明当前设备可能并不支持开启AP

		// 当前上传路径
//		uploadPathView.setText(FsSettings.getUpload());
		statusController.registerReceiver();
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		statusController.unregisterReceiver();
	}
	
	public boolean shouldLooping() {
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement element = canvasElements.get(i);
			if (element.hasAnimation()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		Log.d(TAG, "handleMessage");

		if (!isSurfaceCreated) {
			return false;
		}
		
		// 获得需要刷新的区域，仅仅能够在这里刷新
		Canvas canvas = surfaceHolder.lockCanvas();
		isLooping = false;
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement element = canvasElements.get(i);
			Log.d(TAG, "draw");
			element.draw(canvas, canvasPaint);
			if (element.hasAnimation()) {
				isLooping = true;
			}
		}

		if (isLooping) {
			Message message = refreshHandler.obtainMessage();
			refreshHandler.sendMessageDelayed(message, 20);
		}

		surfaceHolder.unlockCanvasAndPost(canvas);

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 用于检测Fling
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surface created");
		isSurfaceCreated = true;
		
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(getResources().getColor(R.color.blue08));
		int canvasWidth = canvas.getWidth(), canvasHeight = canvas.getHeight();

		// 在前的会先被绘制
		// 绘制背景色
		switch (statusController.getServerStatus()) {
		case StatusController.STATUS_SERVER_STARTED:
			pictureBackground.setCurrentColor(startColor);
			break;
		case StatusController.STATUS_SERVER_STOPPED:
			pictureBackground.setCurrentColor(stopColor);
			break;
		case StatusController.STATUS_SERVER_STARTING:
		case StatusController.STATUS_SERVER_STOPING:
			pictureBackground.setCurrentColor(operatingColor);
			break;
		}
		pictureBackground.setColorAnimation(pictureBackground.new ColorAnimation(pictureBackground, pictureBackground.getCurrentColor(), pictureBackground.getCurrentColor()));
		canvasElements.add(pictureBackground);
		
		// 设置按钮
		int x = canvasWidth - settingsButton.getBitmap().getWidth();
		settingsButton.setX(x);
		settingsButton.setY(0);
		
		// 圆环的参数设置不得不放在这里，因为要使用canvasWidth
		// 圆环
		serverInnerRadius = canvasWidth / 4 - 50;
		serverOuterRadius = canvasWidth / 4 + 30;
		serverButton.setCx(canvasWidth / 2);
		serverButton.setCy(canvasHeight / 2);
		serverButton.setInnerRadius(canvasWidth / 4 - 20); 
		serverButton.setOuterRadius(canvasWidth / 4);
		serverButton.setBounceAnimation(serverButton.new BounceAnimation(serverButton, serverInnerRadius));
		serverButton.setBreatheAnimation(serverButton.new OuterRadiusBreatheAnimation(serverButton, serverOuterRadius));
		canvasElements.add(serverButton);
		
		// 绘制基本内容
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement canvasElement = canvasElements.get(i);
			canvasElement.draw(canvas, canvasPaint);
		}
		
		holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surface changed");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surface destoryed");
		isSurfaceCreated = false;
	}
	

	@Override
	public void onServerStatusChange(int status) {
		// 可以将operating的颜色变化放在这里
		if (status == StatusController.STATUS_SERVER_STARTED) {
			long startTime = System.currentTimeMillis();
			
			pictureBackground.stopColorAnimation();
			CanvasAnimation colorAnimation = pictureBackground.getColorAnimation();
			if (colorAnimation != null) {
				colorAnimation.setDuration(500);
			}
			pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), startColor, startTime);
			
			serverButton.stopBreatheAnimation();
			CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
			if (breatheAnimation != null) {
				breatheAnimation.setDuration(3000);
				breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_INFINITE);
			}
			serverButton.startBreatheAnimation(serverOuterRadius, startTime);
			
		} else if (status == StatusController.STATUS_SERVER_STOPPED) {
			pictureBackground.stopColorAnimation();
			CanvasAnimation colorAnimation = pictureBackground.getColorAnimation();
			if (colorAnimation != null) {
				colorAnimation.setDuration(500);
			}
			pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), stopColor);
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
	
	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown");
			int x = (int)e.getX(), y = (int)e.getY();
			
			for (int index = 0, len = canvasElements.size(); index < len; index++) {
				CanvasElement element = canvasElements.get(index);
				element.click(x, y);
			}
			
			// TODO 修改成函数
			if (!isLooping) {
				Message message = refreshHandler.obtainMessage();				
				message.sendToTarget();
			}
			
			return super.onDown(e);
		}
		
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
			if (velocityX > 0.0f) {
				viewSwitcher.setInAnimation(OverviewActivity.this, R.anim.slide_in_left);
				viewSwitcher.setOutAnimation(OverviewActivity.this, R.anim.slide_out_right);
				viewSwitcher.showPrevious();
			} else {
				viewSwitcher.setInAnimation(OverviewActivity.this, R.anim.slide_in_right);
				viewSwitcher.setOutAnimation(OverviewActivity.this, R.anim.slide_out_left);
				viewSwitcher.showNext();
			}
			
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}	
}
