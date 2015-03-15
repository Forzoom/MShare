package org.mshare.main;

import java.util.ArrayList;

import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.StatusController.StatusCallback;
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
 * ViewSwitcher�����ܽ�SurfaceView�ƶ������Ի����������
 * @author HM
 *
 */
public class OverviewActivity extends Activity implements SurfaceHolder.Callback, Handler.Callback, StatusController.StatusCallback {
	private static final String TAG = OverviewActivity.class.getSimpleName();
	
	private ArrayList<CanvasElement> canvasElements = new ArrayList<CanvasElement>();
	
	SurfaceHolder surfaceHolder; 
	private GestureDetector gestureDetector;
	RefreshHandler refreshHandler;
	
	// ��ʹ�õ�ͳһ����
	private Paint canvasPaint = new Paint();
	
	boolean isLooping = false;
	
	int colorBlueDarken;
	int colorBlueLighten;
	
	private PictureBackground pictureBackground = new PictureBackground();
	private RingButton serverButton = new RingButton();
	// �����õ���С���ڰ뾶
	int serverInnerRadius;
	
	private ViewSwitcher viewSwitcher;
	
	private StatusController statusController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overview_activity_container);
		
		// ����ViewSwitcher
		viewSwitcher = (ViewSwitcher)findViewById(R.id.view_switcher);
		
		FrameLayout.LayoutParams serverParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// ��֪��Ϊʲô�����������ò�û����
		ViewGroup serverOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_flat, null);
		FrameLayout.LayoutParams clientParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewGroup clientOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_other, null);
		
		viewSwitcher.addView(serverOverview, serverParams);
		viewSwitcher.addView(clientOverview, clientParams);
		
		// ���÷��������
		ServerOverviewSurfaceView surfaceView = (ServerOverviewSurfaceView)findViewById(R.id.server_overview_surface_view);
		surfaceView.setGestureDetector(new GestureDetector(this, new GestureListener()));
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		
		refreshHandler = new RefreshHandler(Looper.myLooper(), this);
		gestureDetector = new GestureDetector(this, new SwitchListener());
		
		// ��ʼ��StatusController����ʼ������״̬
		statusController = new StatusController();
		statusController.setCallback(this);
		statusController.initial();
		
		canvasPaint.setAntiAlias(true);
		canvasPaint.setColor(getResources().getColor(R.color.color_light_gray));
		canvasPaint.setAlpha(223);
		
	}

	@Override
	protected void onStart() {
		// TODO ������Ҫʹ�ø��Ӱ�ȫ��BroadcastReceiverע�᷽ʽ
		super.onStart();
		
		// TODO ��ʱ���õļ�⵱ǰ�Ƿ���MOBILE�źţ�
		// TODO ��Ҫ����������̫���ˣ����ǲ����뿪��AP�Ĺ���
		// ��û��AP cannot enable�����Զ���isWifiApEnable������������ȷ��ִ��,���Ƕ���setWifiApEnabled�ͻᱨ��
		// TODO �������APʧ����֮�󣬾ͽ���д�������ļ���������ǰ�豸���ܲ���֧�ֿ���AP

		// ��ǰ�ϴ�·��
//		uploadPathView.setText(FsSettings.getUpload());
		statusController.registerReceiver();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		statusController.unregisterReceiver();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		Log.d(TAG, "handleMessage");

		// �����Ҫˢ�µ����򣬽����ܹ�������ˢ��
		Canvas canvas = surfaceHolder.lockCanvas();
		canvas.drawColor(getResources().getColor(R.color.blue08));
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
			refreshHandler.sendMessageDelayed(message, 10);
		}

		surfaceHolder.unlockCanvasAndPost(canvas);

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// ���ڼ��Fling
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surface created");
		
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(getResources().getColor(R.color.blue08));
		int canvasWidth = canvas.getWidth(), canvasHeight = canvas.getHeight();

		// �ᱻ���Ȼ���
		colorBlueDarken = getResources().getColor(R.color.blue01);
		colorBlueLighten = getResources().getColor(R.color.blue08);
		pictureBackground.setCurrentColor(statusController.getServerStatus() == StatusController.STATUS_SERVER_STOPPED ? colorBlueDarken : colorBlueLighten);
		canvasElements.add(pictureBackground);
		
		// Բ���Ĳ������ò��ò����������ΪҪʹ��canvasWidth
		
		// Բ��
		serverInnerRadius = canvasWidth / 4 - 50;
		serverButton.setInnerRadius(canvasWidth / 4 - 20);
		serverButton.setOuterRadius(canvasWidth / 4);
		serverButton.setCx(canvasWidth / 2);
		serverButton.setCy(canvasHeight / 2);
		canvasElements.add(serverButton);
		
		// ���ƻ�������
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
	}
	

	@Override
	public void onServerStatusChange(int status) {
		// ��������Ⱥ״̬�仯��ʱ����Ҫ��������������ɫ
		
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
	
	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown");
			int x = (int)e.getX(), y = (int)e.getY();
			
			// ʹ��bounceЧ��
			// ���������κε�animation
			if (serverButton.isClicked(x, y) && !serverButton.hasAnimation()) {
				long startTime = System.currentTimeMillis();
				CanvasAnimation bounceAnimation = serverButton.addBounceAnimation(serverInnerRadius);
				bounceAnimation.setStartTime(startTime);
				bounceAnimation.setDuration(500);
				CanvasAnimation colorAnimation = null;
				
				if (statusController.getServerStatus() == StatusController.STATUS_SERVER_STOPPED) {
					colorAnimation = pictureBackground.addColorAnimation(colorBlueDarken, colorBlueLighten);
					statusController.setServerStatus(StatusController.STATUS_SERVER_STARTED);
				} else {
					colorAnimation = pictureBackground.addColorAnimation(colorBlueLighten, colorBlueDarken);
					statusController.setServerStatus(StatusController.STATUS_SERVER_STOPPED);
				}
				
				if (colorAnimation != null) {
					colorAnimation.setStartTime(startTime);
					colorAnimation.setDuration(500);
				}
				
				// TODO �޸ĳɺ���
				if (!isLooping) {
					// ����PictureButton
					Message message = refreshHandler.obtainMessage();				
					message.sendToTarget();
				}
			}
			
			return super.onDown(e);
		}
		
	}
	
	class SwitchListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			Log.d(TAG, "onFling");
			// ��
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