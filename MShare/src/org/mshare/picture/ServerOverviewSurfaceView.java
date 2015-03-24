package org.mshare.picture;

import java.util.ArrayList;

import org.mshare.main.MShareApp;
import org.mshare.main.R;
import org.mshare.main.StatusController;
import org.mshare.picture.SettingsButton.AlphaAnimation;

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
	// 用于surfaceView上的事件检测
	private GestureDetector gestureDetector;
	// SurfaceHolder
	private SurfaceHolder surfaceHolder;
	
	// 用于
	private boolean isSurfaceCreated = false;

	// 所有将被绘制的内容
	private ArrayList<CanvasElement> canvasElements = new ArrayList<CanvasElement>();
	
	// 统一画笔
	private Paint canvasPaint = new Paint();
	// 当前是否在循环绘制
	private boolean isLooping = false;
	
	// 刷新SurfaceView所用的Handler
	private RefreshHandler refreshHandler;
	
	// 背景颜色
    private int ringColor;
	private int stopColor;
	private int startColor;
	private int operatingColor;
	private int transparentColor;
	
	// 所设置的缩小的内半径
	private int serverInnerRadius;
	
	// 所设置的放大的呼吸外半径
	private int serverOuterRadius;
		
	private StatusController statusController;

    // 所需要在Canvas上绘制的内容
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

	// 初始化
	private void init() {
		// 创建holder
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		
		// 设置GestureDetector和refreshHandler
		gestureDetector = new GestureDetector(getContext(), new GestureListener());
		refreshHandler = new RefreshHandler(Looper.myLooper(), this);
		
		// 设置画笔
		canvasPaint.setAntiAlias(true);
		canvasPaint.setColor(getResources().getColor(R.color.color_light_gray));
		canvasPaint.setAlpha(223);
		
		// 创建背景颜色
        Resources res = getResources();
        ringColor = res.getColor(R.color.Color_White);
		stopColor = res.getColor(R.color.blue01);
		startColor = res.getColor(R.color.blue08);
		operatingColor = res.getColor(R.color.blue00);
		transparentColor = res.getColor(R.color.color_transparent);

        // 希望在这里创建所有需要在Canvas上进行绘制的内容
        // 背景
        if (pictureBackground == null) {
            pictureBackground = new PictureBackground();
            addElement(pictureBackground);
        }

        // 头像
        if (circleAvater == null) {
            circleAvater = new CircleAvater();
            addElement(circleAvater);
        }

        // 设置按钮
        if (settingsButton == null) {
            Bitmap settingsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
            settingsButton = new SettingsButton(settingsBitmap);
            addElement(settingsButton);
        }

        // 服务器按钮
        if (serverButton == null) {// null情况下，仅仅是添加而已
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

        /* 创建所需要绘制的元素，在前调用addElement方法的将被先绘制，先绘制的内容被后绘制的内容覆盖 */

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

        int x = canvasWidth - settingsButton.getBitmap().getWidth() - 12;
        int paddingTop = 12;
        settingsButton.setX(x);
        settingsButton.setY(paddingTop);
        settingsButton.setPadding(12, paddingTop, 12, 12);

        // 圆环的参数设置不得不放在这里，因为要使用canvasWidth
        serverInnerRadius = canvasWidth / 4 - 50;
        serverOuterRadius = canvasWidth / 4 + 30;
        Point center = new Point(canvasWidth / 2, canvasHeight / 2);
        serverButton.setRingColor(ringColor);
        serverButton.setCenter(center);
        serverButton.setRadius(canvasWidth / 4 - 20, canvasWidth / 4);

		int avaterRadius = canvasWidth / 4;
		Bitmap avaterBitmap = CircleAvaterCreator.createAvater(R.drawable.avater_1, avaterRadius);
		
		circleAvater.setCenter(new Point(canvasWidth / 2, canvasHeight / 2));
		circleAvater.setRadius(avaterRadius);
		circleAvater.setAvater(avaterBitmap);

		// 绘制基本内容
		for (int i = 0, len = canvasElements.size(); i < len; i++) {
			CanvasElement canvasElement = canvasElements.get(i);
			canvasElement.draw(canvas, canvasPaint);
		}
		
		holder.unlockCanvasAndPost(canvas);
		
		// 临时用于启动呼吸效果
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
		// 清空所有的elements，暂时先这样
//		canvasElements.clear();
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
	
	//判断surface是否创建
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

    // 预置的服务器启动动画
    public void startServerAnimation() {
        // 处理SurfaceView中的动画效果
        long startTime = System.currentTimeMillis();

        pictureBackground.stopColorAnimation();
        pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), getStartColor(), startTime, 500);

        // 处理呼吸动画效果
        serverButton.stopBreatheAnimation();
        // 等待这一次的动画结束
        CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
        if (breatheAnimation != null) {
            breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_INFINITE);
        }
        serverButton.startBreatheAnimation(getServerOuterRadius(), startTime, 3000);

    }

    // 预置的服务器停止动画
    public void stopServerAniamtion() {
        // 调整背景颜色
        pictureBackground.stopColorAnimation();
        pictureBackground.startColorAnimation(pictureBackground.getCurrentColor(), getStopColor(), System.currentTimeMillis(), 500);

        // 调整呼吸动画
        CanvasAnimation breatheAnimation = serverButton.getBreatheAnimation();
        // 通过设置repeatMode，当动画循环结束的时候就会自动stop
        if (breatheAnimation != null) {
            breatheAnimation.setRepeatMode(CanvasAnimation.REPEAT_MODE_ONCE);
        }

    }

	// 设置GestureDetector
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
			
			// TODO 修改成函数
			if (!isLooping) {
				Message message = refreshHandler.obtainMessage();				
				message.sendToTarget();
			}
			
			return super.onDown(e);
		}
	}

}
