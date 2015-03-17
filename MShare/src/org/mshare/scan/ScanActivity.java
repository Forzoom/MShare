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
 * ����ʵ���ǲ��ɵ���
 * Preview������Ҳ������ȷ,����Ҫ֪������preview������������
 * TODO ����viewfinder�ķ�Χ���ǲ����
 * TODO �Ƿ���Ҫʹ��startActivityForResult?
 * TODO �����ķ�Χ�Կ���������
 * ��manifest�ļ��б�֤������
 * ��Ҫ����setResult
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
		
		viewfinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		
		decodeThread = new DecodeThread(this, null, null, new ViewfinderResultPointCallback(viewfinderView));
		decodeThread.start();

		// ��3.0�İ汾֮ǰ��Ҫ����Ĵ���
		// ����ʽ������ʱ��Ӧ��ɾ��
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
		// TODO �����������˽���
		Log.d(TAG, "start request preview frame in Activity");
		// ��һ��request������½������ȫ�ڵ�ͼƬ,��������Ϊ��������
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
