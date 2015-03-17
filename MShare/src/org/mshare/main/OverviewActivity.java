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
 * ViewSwitcher�����ܽ�SurfaceView�ƶ������Ի����������
 * @author HM
 *
 */
public class OverviewActivity extends Activity implements StatusController.StatusCallback {
	private static final String TAG = OverviewActivity.class.getSimpleName();

	// �ж�Fling��GestureDetector
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
		
		// �ڵ�һ��������ʱ��ʹ��Ĭ������
		PreferenceManager.setDefaultValues(this, R.xml.server_settings, false);
		
		// ��û�����������
//		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//		String nickName = sp.getString("nickname", "");
		
		// ����ViewSwitcher
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
		
		// ���ļ�������Ļص�����
		RelativeLayout fileBrowserButton = (RelativeLayout)serverOverview.findViewById(R.id.overview_activity_file_browser_button);
		fileBrowserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startFileBrowserIntent = new Intent(OverviewActivity.this, FileBrowserActivity.class);
				startActivity(startFileBrowserIntent);
			}
		});
		
		// ���ļ�������Ļص�����
		RelativeLayout qrcodeButton = (RelativeLayout)serverOverview.findViewById(R.id.overview_activity_qrcode_button);
		qrcodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startQrcodeIntent = new Intent(OverviewActivity.this, QRCodeConnectActivity.class);
				startQrcodeIntent.putExtra(QRCodeConnectActivity.EXTRA_CONTENT, new ConnectInfo("192.168.1.1", "2121", "username", "password"));
				startActivity(startQrcodeIntent);
			}
		});
		
		// ����surfaceView
		surfaceView = (ServerOverviewSurfaceView)findViewById(R.id.server_overview_surface_view);
		surfaceView.setStatusController(statusController);
		// ����onFling��GestureDetector
		gestureDetector = new GestureDetector(this, new SwitchListener());
		
		/* ��������Ҫ���Ƶ�Ԫ�� */
		// ����
		pictureBackground = new PictureBackground();
		surfaceView.setPictureBackground(pictureBackground);
		
		// ���ð�ť
		Bitmap settingsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
		settingsButton = new SettingsButton(settingsBitmap); 
		settingsButton.setElementOnClickListener(new SettingsButtonListener());
		surfaceView.setSettingsButton(settingsButton);
		
		// ��������ť
		serverButton = new RingButton();
		serverButton.setElementOnClickListener(new ServerButtonListener());
		surfaceView.setServerButton(serverButton);
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		// TODO ������Ҫʹ�ø��Ӱ�ȫ��BroadcastReceiverע�᷽ʽ
		super.onStart();

		// ��ʼ��StatusController����ʼ������״̬
		
		// TODO ��Ҫ���������̫���ˣ����ǲ����뿪��AP�Ĺ���
		// ��û��AP cannot enable�����Զ���isWifiApEnable������������ȷ��ִ��,���Ƕ���setWifiApEnabled�ͻᱨ��
		// TODO �������APʧ����֮�󣬾ͽ���д�������ļ���������ǰ�豸���ܲ���֧�ֿ���AP

		// ��ǰ�ϴ�·��
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
		// ���ڼ��Fling
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public void onServerStatusChange(int status) {
		Log.d(TAG, "onServerStatus");
		// ���Խ�operating����ɫ�仯��������
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
			// ����������ɫ
			pictureBackground.stopColorAnimation();
			CanvasAnimation colorAnimation = pictureBackground.getColorAnimation();
			if (colorAnimation != null) {
				colorAnimation.setDuration(500);
			}
			pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), surfaceView.getStopColor());
			
			// ������������
			CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
			// ͨ������repeatMode��������ѭ��������ʱ��ͻ��Զ�stop
			if (breatheAnimation != null) {
				breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_ONCE);
			}
		}
		
	}
	
	@Override
	public void onWifiStatusChange(int state) {
		Log.d(TAG, "on wifi state change");
		switch (state) {
		// ��ʾ�����ֻ���֧��WIFI
		case StatusController.STATE_WIFI_DISABLE:
		case StatusController.STATE_WIFI_ENABLE:
			if (FsService.isRunning()) {
				// ���Թرշ�����
//				stopServer();
			}
//			ftpAddrView.setText("δ֪");
			break;
		case StatusController.STATE_WIFI_USING:
			// ������ʾ��IP��ַ
//			ftpAddrView.setText(FsService.getLocalInetAddress().getHostAddress());
			break;
		}
	}
	
	/**
	 * ������ܻ��д����ظ�,��Ҫ����������ݳ�ȥ
	 */
	@Override
	public void onWifiApStatusChange(int status) {
		Log.d(TAG, "on wifi ap state change");
		// TODO ��ַ���ܲ������������õģ�������ʱ����Щע��
		// ���õ�ַ
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
		// TODO ������չ�洢�ı仯�ܹ���Ϊ��Ӧ
		Log.d(TAG, "on external storage state change");
	}

	@Override
	public void onNfcStatusChange(int status) {
		Log.d(TAG, "on nfc state change");
	}
	
	/**
	 * ��������������
	 */
	private void startServer() {
		// �����µ���������
		sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
	}
	
	/**
	 * ����ֹͣ������
	 */
	private void stopServer() {
		sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
		
	}
	
	class SwitchListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			Log.d(TAG, "onFling");
			// ��
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
			
			// ִ��bounceAnimaion�������͹رշ�����
			long startTime = System.currentTimeMillis();
			
			serverButton.stopBounceAnimation();
			CanvasAnimation bounceAnimation = serverButton.getBounceAnimation();
			bounceAnimation.setDuration(500);
			serverButton.startBounceAnimation(surfaceView.getServerInnerRadius(), startTime);
			
			// �޸ķ�����״̬��������رշ�����
			if (serverStatus == StatusController.STATUS_SERVER_STARTED) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STOPING);
				stopServer();
			} else if (serverStatus == StatusController.STATUS_SERVER_STOPPED) {
				statusController.setServerStatus(StatusController.STATUS_SERVER_STARTING);
				startServer();
			}
			
			// �޸ı���ɫ
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
			
			// ��������serverSettings
			Intent startServerSettingsIntent = new Intent(OverviewActivity.this, ServerSettingActivity.class);
			startActivity(startServerSettingsIntent);
		}
	}
}
