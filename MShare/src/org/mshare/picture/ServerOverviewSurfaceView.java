package org.mshare.picture;

import java.util.ArrayList;

import org.mshare.ftp.server.FsService;
import org.mshare.main.MShareApp;
import org.mshare.main.OverviewActivity;
import org.mshare.main.R;
import org.mshare.main.ServerSettingActivity;
import org.mshare.main.StatusController;
import org.mshare.picture.PictureBackground.ColorAnimation;
import org.mshare.picture.RingButton.BounceAnimation;
import org.mshare.picture.RingButton.OuterRadiusBreatheAnimation;
import org.mshare.picture.SettingsButton.AlphaAnimation;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;;

public class ServerOverviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Handler.Callback {
	private static final String TAG = ServerOverviewSurfaceView.class.getSimpleName();
	// ����surfaceView�ϵ��¼����
	private GestureDetector gestureDetector;
	// SurfaceHolder
	private SurfaceHolder surfaceHolder;
	
	// ����
	private boolean isSurfaceCreated = false;

	// ���н������Ƶ�����
	private ArrayList<CanvasElement> canvasElements = new ArrayList<CanvasElement>();
	
	// ͳһ����
	private Paint canvasPaint = new Paint();
	// ��ǰ�Ƿ���ѭ������
	private boolean isLooping = false;
	
	// ˢ��SurfaceView���õ�Handler
	private RefreshHandler refreshHandler;
	
	// ������ɫ
	private int stopColor;
	private int startColor;
	private int operatingColor;
	private int transparentColor;
	
	// �����õ���С���ڰ뾶
	private int serverInnerRadius;
	
	// �����õķŴ�ĺ�����뾶
	private int serverOuterRadius;
		
	private StatusController statusController;

	private PictureBackground pictureBackground;
	private CircleAvater circleAvater;

	private RingButton serverButton;
	private SettingsButton settingsButton;
	
	public ServerOverviewSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ServerOverviewSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ServerOverviewSurfaceView(Context context) {
		super(context);
		init();
	}

	// ��ʼ��
	private void init() {
		// ����holder
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		
		// ����GestureDetector��refreshHandler
		gestureDetector = new GestureDetector(getContext(), new GestureListener());
		refreshHandler = new RefreshHandler(Looper.myLooper(), this);
		
		// ���û���
		canvasPaint.setAntiAlias(true);
		canvasPaint.setColor(getResources().getColor(R.color.color_light_gray));
		canvasPaint.setAlpha(223);
		
		// ����������ɫ
		stopColor = getResources().getColor(R.color.blue01);
		startColor = getResources().getColor(R.color.blue08);
		operatingColor = getResources().getColor(R.color.blue00);
		transparentColor = getResources().getColor(R.color.color_transparent);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
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

		// ��ǰ�Ļ��ȱ�����
		// ���Ʊ���ɫ
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
//		canvasElements.add(pictureBackground);
		
		// ���ð�ť
		int x = canvasWidth - settingsButton.getBitmap().getWidth() - 12;
		settingsButton.setX(x);
		settingsButton.setY(12);
		settingsButton.setPadding(12, 12, 12, 12);
		AlphaAnimation alphaAnimation = settingsButton.new AlphaAnimation(settingsButton, 223);
		alphaAnimation.setDuration(300);
		settingsButton.setAlphaAnimation(alphaAnimation);
//		canvasElements.add(settingsButton);
		
		int avaterRadius = canvasWidth / 4;
		Bitmap source = BitmapFactory.decodeResource(MShareApp.getAppContext().getResources(), R.drawable.avater_1);
		Bitmap avaterBitmap = CircleAvaterCreator.createAvater(source, avaterRadius);
		
		circleAvater.setCx(canvas.getWidth() / 2);
		circleAvater.setCy(canvas.getHeight() / 2);
		circleAvater.setRadius(avaterRadius);
		circleAvater.setAvater(avaterBitmap);
		
		// Բ���Ĳ������ò��ò����������ΪҪʹ��canvasWidth
		// Բ��
		serverInnerRadius = canvasWidth / 4 - 50;
		serverOuterRadius = canvasWidth / 4 + 30;
		serverButton.setCx(canvasWidth / 2);
		serverButton.setCy(canvasHeight / 2);
		serverButton.setInnerRadius(canvasWidth / 4 - 20); 
		serverButton.setOuterRadius(canvasWidth / 4);
		serverButton.setBounceAnimation(serverButton.new BounceAnimation(serverButton, serverInnerRadius));
		serverButton.setBreatheAnimation(serverButton.new OuterRadiusBreatheAnimation(serverButton, serverOuterRadius));
//		canvasElements.add(serverButton);
		
		// ���ƻ�������
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement canvasElement = canvasElements.get(i);
			canvasElement.draw(canvas, canvasPaint);
		}
		
		holder.unlockCanvasAndPost(canvas);
		
		// ��ʱ������������Ч��
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
		isLooping = false;
		// ������е�elements����ʱ������
//		canvasElements.clear();
	}

	@Override
	public boolean handleMessage(Message msg) {
		Log.d(TAG, "handleMessage");

		if (!isSurfaceCreated) {
			return false;
		}
		
		// �����Ҫˢ�µ����򣬽����ܹ�������ˢ��
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
	
	//�ж�surface�Ƿ񴴽�
	public boolean isSurfaceCreated() {
		return isSurfaceCreated;
	}
	
	public void addElement(CanvasElement canvasElement) {
		canvasElements.add(canvasElement);
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
	
	// ����GestureDetector
	public void setGestureDetector(GestureDetector gestureDetector) {
		this.gestureDetector = gestureDetector;
	}

	public int getStopColor() {
		return stopColor;
	}

	public void setStopColor(int stopColor) {
		this.stopColor = stopColor;
	}

	public int getStartColor() {
		return startColor;
	}

	public void setStartColor(int startColor) {
		this.startColor = startColor;
	}

	public int getOperatingColor() {
		return operatingColor;
	}

	public void setOperatingColor(int operatingColor) {
		this.operatingColor = operatingColor;
	}

	public PictureBackground getPictureBackground() {
		return pictureBackground;
	}

	public void setPictureBackground(PictureBackground pictureBackground) {
		this.pictureBackground = pictureBackground;
		addElement(pictureBackground);
	}

	public RingButton getServerButton() {
		return serverButton;
	}

	public void setServerButton(RingButton serverButton) {
		this.serverButton = serverButton;
		addElement(serverButton);
	}

	public SettingsButton getSettingsButton() {
		return settingsButton;
	}

	public void setSettingsButton(SettingsButton settingsButton) {
		this.settingsButton = settingsButton;
		addElement(settingsButton);
	}

	public StatusController getStatusController() {
		return statusController;
	}

	public void setStatusController(StatusController statusController) {
		this.statusController = statusController;
	}

	public int getServerInnerRadius() {
		return serverInnerRadius;
	}

	public void setServerInnerRadius(int serverInnerRadius) {
		this.serverInnerRadius = serverInnerRadius;
	}

	public int getServerOuterRadius() {
		return serverOuterRadius;
	}

	public void setServerOuterRadius(int serverOuterRadius) {
		this.serverOuterRadius = serverOuterRadius;
	}
	
	public CircleAvater getCircleAvater() {
		return circleAvater;
	}
	public void setCircleAvater(CircleAvater circleAvater) {
		this.circleAvater = circleAvater;
		addElement(circleAvater);
	}
	
	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown x : " + e.getX() + " y : " + e.getY());
			int x = (int)e.getX(), y = (int)e.getY();
			Log.d(TAG, "has " + canvasElements.size() + " elements");
			for (int index = 0, len = canvasElements.size(); index < len; index++) {
				CanvasElement element = canvasElements.get(index);
				element.click(x, y);
			}
			
			// TODO �޸ĳɺ���
			if (!isLooping) {
				Message message = refreshHandler.obtainMessage();				
				message.sendToTarget();
			}
			
			return super.onDown(e);
		}
	}

}
