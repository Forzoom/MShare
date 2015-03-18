package org.mshare.scan;

import java.io.IOException;

import org.mshare.main.ConnectInfo;
import org.mshare.main.R;

import com.google.zxing.Result;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

/**
 * 方向实在是不可调整
 * Preview的区域也许并不正确,所需要知道的是preview的区域是哪里
 * TODO 现在viewfinder的范围仍是不大对
 * TODO 是否需要使用startActivityForResult?
 * TODO 解析的范围仍可能有问题
 * 在manifest文件中保证是竖屏
 * 需要设置setResult
 * @author HM
 *
 */
public class ScanActivity extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = ScanActivity.class.getSimpleName();
	
	private SurfaceView surfaceView;
	private CameraManager cameraManager;
	private boolean hasSurface;
	private DecodeThread decodeThread;
	private ViewfinderView viewfinderView;
	
	public static final String EXTRA_CONNECT_INFO = "connect_info";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置屏幕常亮
		Window window = getWindow();
	    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);
		
		// 创建surfaceView
		surfaceView = (SurfaceView)findViewById(R.id.preview_view);
		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(this);
		
		// 创建cameraManager
		cameraManager = new CameraManager(this);
		
		viewfinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		
		decodeThread = new DecodeThread(this, null, null, new ViewfinderResultPointCallback(viewfinderView));
		decodeThread.start();

		// 在3.0的版本之前需要下面的代码
		// 在正式发布的时候应当删除
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		surfaceView = (SurfaceView)findViewById(R.id.preview_view);
		SurfaceHolder holder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(holder);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		cameraManager.closeDriver();
	}
	
	/**
	 * 
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		// 使DecodeThread停止
		Message quit = Message.obtain(decodeThread.getHandler(), DecodeHandler.WHAT_QUIT);
	    quit.sendToTarget();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
			cameraManager.startPreview();
		}
		// 需要设置的是DecodeThread和DecodeHandler
		// TODO 在这里启动了解析
		Log.d(TAG, "start request preview frame in Activity");
		// 第一次request的情况下将会出现全黑的图片,可能是因为放在桌上
		cameraManager.requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.WHAT_DECODE);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}
	
	/**
	 * 启动相机
	 * @param surfaceHolder 应该保证surfaceHolder不会为null
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
		} catch (RuntimeException e) {
		// Barcode Scanner has seen crashes in the wild of this variety:
		// java.?lang.?RuntimeException: Fail to connect to camera service
		Log.w(TAG, "Unexpected error initializing camera", e);
		}
	}
	
	public CameraManager getCameraManager() {
		return cameraManager;
	}
}
