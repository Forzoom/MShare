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
 * ����ʵ���ǲ��ɵ���
 * Preview������Ҳ������ȷ,����Ҫ֪������preview������������
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
		// ������Ļ����
		Window window = getWindow();
	    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);
		
		// ����surfaceView
		surfaceView = (SurfaceView)findViewById(R.id.preview_view);
		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(this);
		
		// ����cameraManager
		cameraManager = new CameraManager(this);
		// �ֶ�����Framing�Ĵ�С
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
		
		// TODO ��Ҫ����viewfinder
		decodeThread = new DecodeThread(this, null, null, new ViewfinderResultPointCallback(viewfinderView));
		decodeThread.start();
		
		// TODO ��Ҫ�����⵱ǰ�İ汾
		// ��3.0�İ汾֮ǰ��Ҫ����Ĵ���
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
	 * ʹ��unlock���޷�ʹ�����Դ�õ��ͷ�
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// ʹDecodeThreadֹͣ
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
		// ��Ҫ���õ���DecodeThread��DecodeHandler
		// ��ʼ����
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
	 * �������
	 * @param surfaceHolder Ӧ�ñ�֤surfaceHolder����Ϊnull
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
