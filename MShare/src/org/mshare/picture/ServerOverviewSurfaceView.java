package org.mshare.picture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.mshare.main.R;
import org.mshare.main.StatusController;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
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
    private int ringColor;
	private int stopColor;
	private int startColor;
	private int operatingColor;
	private int transparentColor;
	
	// �����õ���С���ڰ뾶
	private int bounceInnerRadius;
	
	// �����õķŴ�ĺ�����뾶
	private int breatheOuterRadius;
		
	private StatusController statusController;

    // ����Ҫ��Canvas�ϻ��Ƶ�����
	private PictureBackground pictureBackground;
	private CircleAvater circleAvater;
	private RingButton serverButton;
	private SettingsButton settingsButton;

	public static final int DURATION_BOUNCE_ANIMATION = 500;
	public static final int DURATION_BREATHE_ANIMATION = 3000;

	public static final int DURATION_COLOR_ANIMATION = 500;


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
        Resources res = getResources();
        ringColor = res.getColor(R.color.Color_White);
		stopColor = res.getColor(R.color.blue01);
		startColor = res.getColor(R.color.blue08);
		operatingColor = res.getColor(R.color.blue00);
		transparentColor = res.getColor(R.color.color_transparent);

        // ϣ�������ﴴ��������Ҫ��Canvas�Ͻ��л��Ƶ�����
        // ����
        if (pictureBackground == null) {
            pictureBackground = new PictureBackground();
            addElement(pictureBackground);
        }

        // ͷ��
        if (circleAvater == null) {
            circleAvater = new CircleAvater();
            addElement(circleAvater);
        }

        // ���ð�ť
        if (settingsButton == null) {
            Bitmap settingsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
            settingsButton = new SettingsButton(settingsBitmap);
            addElement(settingsButton);
        }

        // ��������ť
        if (serverButton == null) {// null����£���������Ӷ���
            serverButton = new RingButton();
            addElement(serverButton);
        }
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

        /* ��������Ҫ���Ƶ�Ԫ�أ���ǰ����addElement�����Ľ����Ȼ��ƣ��Ȼ��Ƶ����ݱ�����Ƶ����ݸ��� */

        // ���Ʊ���ɫ
        switch (statusController.getServerStatus()) {
            case StatusController.STATUS_SERVER_STARTED:
            	Log.d(TAG, "draw start color");
                pictureBackground.setCurrentColor(startColor);
                break;
            case StatusController.STATUS_SERVER_STOPPED:
            	Log.d(TAG, "draw stop color");
                pictureBackground.setCurrentColor(stopColor);
                break;
            case StatusController.STATUS_SERVER_STARTING:
            case StatusController.STATUS_SERVER_STOPING:
            	Log.d(TAG, "draw operate color");
                pictureBackground.setCurrentColor(operatingColor);
                break;
        }

        Log.d(TAG, "picture background " + Integer.toHexString(pictureBackground.getCurrentColor()));
        
        int x = canvasWidth - settingsButton.getBitmap().getWidth() - 12;
        int paddingTop = 12;
        settingsButton.setX(x);
        settingsButton.setY(paddingTop);
        settingsButton.setPadding(12, paddingTop, 12, 12);

        // Բ���Ĳ������ò��ò����������ΪҪʹ��canvasWidth
        bounceInnerRadius = canvasWidth / 4 - 50;
        breatheOuterRadius = canvasWidth / 4 + 30;
        Log.d(TAG, "server inner radius " + bounceInnerRadius);
        Log.d(TAG, "server outer radius " + breatheOuterRadius);
        Point center = new Point(canvasWidth / 2, canvasHeight / 2);
        serverButton.setRingColor(ringColor);
        serverButton.setCenter(center);
        serverButton.setRadius(canvasWidth / 4 - 20, canvasWidth / 4);

		int avaterRadius = canvasWidth / 4;
		Bitmap avaterBitmap = CircleAvaterCreator.createAvater(R.drawable.avater_1, avaterRadius);

		// �����������ʱ�򣬸���ô�죿ʹ��Ĭ�ϵ�ͷ��
		try {
			avaterBitmap = BitmapFactory.decodeStream(getContext().openFileInput("avater"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		circleAvater.setCenter(new Point(canvasWidth / 2, canvasHeight / 2));
		circleAvater.setRadius(avaterRadius);
		circleAvater.setAvater(avaterBitmap);

		// ���ƻ�������
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement canvasElement = canvasElements.get(i);
			canvasElement.draw(canvas, canvasPaint);
		}
		
		holder.unlockCanvasAndPost(canvas);
		
		// ��ʱ������������Ч��
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surface changed");

		Canvas canvas = holder.lockCanvas();
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();

		// �������е�ͼƬ����
		int x = canvasWidth - settingsButton.getBitmap().getWidth() - 12;
		int paddingTop = 12;
		settingsButton.setX(x);
		settingsButton.setY(paddingTop);
		settingsButton.setPadding(12, paddingTop, 12, 12);

		// Բ���Ĳ������ò��ò����������ΪҪʹ��canvasWidth
		bounceInnerRadius = canvasWidth / 4 - 50;
		breatheOuterRadius = canvasWidth / 4 + 30;
		Point center = new Point(canvasWidth / 2, canvasHeight / 2);
		serverButton.setRingColor(ringColor);
		serverButton.setCenter(center);
		serverButton.setRadius(canvasWidth / 4 - 20, canvasWidth / 4);

		int avaterRadius = canvasWidth / 4;
		Bitmap avaterBitmap = CircleAvaterCreator.createAvater(R.drawable.avater_1, avaterRadius);

		circleAvater.setCenter(new Point(canvasWidth / 2, canvasHeight / 2));
		circleAvater.setRadius(avaterRadius);
		circleAvater.setAvater(avaterBitmap);

		// ���ƻ�������
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement canvasElement = canvasElements.get(i);
			canvasElement.draw(canvas, canvasPaint);
		}

		// �жϵ�ǰ�ķ�����״̬
		if (statusController.getServerStatus() == StatusController.STATUS_SERVER_STARTED) {
			// ��Ҫ������������
			serverButton.stopBreatheAnimation();
			serverButton.startBreatheAnimation(breatheOuterRadius, System.currentTimeMillis(), DURATION_BREATHE_ANIMATION);
		}
		
		holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surface destoryed");
		isSurfaceCreated = false;
		isLooping = false;
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
		Log.d(TAG, "has " + canvasElements.size() + " element");
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement element = canvasElements.get(i);
			element.draw(canvas, canvasPaint);
			if (element.hasAnimation()) {
				isLooping = true;
			}
		}

		if (isLooping) {
			startLooping();
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

    // Ԥ�õķ�������������
    public void startServerAnimation() {
        // ����SurfaceView�еĶ���Ч��
        long startTime = System.currentTimeMillis();

        pictureBackground.stopColorAnimation();
        pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), getStartColor(), startTime, DURATION_COLOR_ANIMATION);

        // �����������Ч��
        serverButton.stopBreatheAnimation();
        // �ȴ���һ�εĶ�������
        CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
        if (breatheAnimation != null) {
            breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_INFINITE);
        }
        Log.d(TAG, "start animation : " + getBreatheOuterRadius());
        serverButton.startBreatheAnimation(getBreatheOuterRadius(), startTime, DURATION_BREATHE_ANIMATION);
    }

    // Ԥ�õķ�����ֹͣ����
    public void stopServerAniamtion() {
        // ����������ɫ
        pictureBackground.stopColorAnimation();
        pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), getStopColor(), System.currentTimeMillis(), DURATION_COLOR_ANIMATION);

        // ������������
        CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
        // ͨ������repeatMode��������ѭ��������ʱ��ͻ��Զ�stop
        if (breatheAnimation != null) {
            breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_ONCE);
        }

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

	public int getBounceInnerRadius() {
		return bounceInnerRadius;
	}

	public void setBounceInnerRadius(int bounceInnerRadius) {
		this.bounceInnerRadius = bounceInnerRadius;
	}

	public int getBreatheOuterRadius() {
		return breatheOuterRadius;
	}

	public void setBreatheOuterRadius(int breatheOuterRadius) {
		this.breatheOuterRadius = breatheOuterRadius;
	}
	
	public CircleAvater getCircleAvater() {
		return circleAvater;
	}

    public void setServerButtonListener(CanvasElement.ElementOnClickListener listener) {
        serverButton.setElementOnClickListener(listener);
    }

    public void setSettingsButtonListener(CanvasElement.ElementOnClickListener listener) {
        settingsButton.setElementOnClickListener(listener);
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
				startLooping();
			}
			
			return super.onDown(e);
		}
	}

	public void startLooping() {
		Message message = refreshHandler.obtainMessage();
		message.sendToTarget();
	}

}
