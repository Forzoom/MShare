package org.mshare.scan;

import java.io.IOException;

import org.mshare.main.R;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class ScanActivity extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = ScanActivity.class.getSimpleName();
	private SurfaceView surfaceView;
	private Camera c;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.capture);
		// ����Ƿ�ӵ��camera
		boolean hasCamara = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		Toast.makeText(this, "��� " + hasCamara, Toast.LENGTH_SHORT).show();
		
		// ����surfaceView
		surfaceView = (SurfaceView)findViewById(R.id.preview_view);
		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(this);
		
		c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		
		// ��3.0�İ汾֮ǰ��Ҫ����Ĵ���
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (c != null) {
			try {
				c.setPreviewDisplay(holder);
				c.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            c.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            c.setPreviewDisplay(holder);
            c.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }	
	}
	
}
