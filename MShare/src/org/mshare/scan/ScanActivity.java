package org.mshare.scan;

import java.io.IOException;

import org.mshare.main.R;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
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
		// 手动设置Framing的大小
		cameraManager.setManualFramingRect(300, 300);
		
		viewfinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		
		Button captureButton = (Button)findViewById(R.id.capture_button);
		captureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "start request preview frame in Activity");
				cameraManager.requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.WHAT_DECODE);
			}
		});
		
		// TODO 需要创建viewfinder
		decodeThread = new DecodeThread(this, null, null, new ViewfinderResultPointCallback(viewfinderView));
		decodeThread.start();
		
		// TODO 需要代码检测当前的版本
		// 在3.0的版本之前需要下面的代码
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
	 * 使用unlock都无法使相机资源得到释放
	 */
	@Override
	protected void onDestroy() {
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
		// 开始处理
//		Log.d(TAG, "start request preview frame in Activity");
//		cameraManager.requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.WHAT_DECODE);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
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
