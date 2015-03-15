package org.mshare.main;

import java.util.ArrayList;

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

public class OverviewActivity extends Activity implements SurfaceHolder.Callback, Handler.Callback {
	private static final String TAG = OverviewActivity.class.getSimpleName();
	
	private ArrayList<CanvasElement> canvasElements = new ArrayList<CanvasElement>();
	
	SurfaceHolder mSurfaceHolder; 
	private GestureDetector mGestureDetector;
	RefreshHandler mRefreshHandler;
	
	// 所使用的统一画笔
	private Paint canvasPaint = new Paint();
	
	boolean isLooping = false;
	
	int colorBlueDarken;
	int colorBlueLighten;
	
	private PictureBackground pictureBackground = new PictureBackground();
	private RingButton serverButton = new RingButton();
	// 所设置的缩小的内半径
	int serverInnerRadius;
	
	private ViewSwitcher viewSwitcher;
	
	private StatusController statusController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overview_activity_container);
		
		viewSwitcher = (ViewSwitcher)findViewById(R.id.view_switcher);
		
		// 服务器相关内容
		FrameLayout.LayoutParams serverParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewGroup serverOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_flat, null);
		FrameLayout.LayoutParams clientParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewGroup clientOverview = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.overview_activity_other, null);
		
		viewSwitcher.addView(serverOverview, serverParams);
		viewSwitcher.addView(clientOverview, clientParams);
		
		ServerOverviewSurfaceView surfaceView = (ServerOverviewSurfaceView)findViewById(R.id.server_overview_surface_view);
		surfaceView.setGestureDetector(new GestureDetector(this, new GestureListener()));
		mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		mRefreshHandler = new RefreshHandler(Looper.myLooper(), this);
		mGestureDetector = new GestureDetector(this, new SwitchListener());
		
		statusController = new StatusController();
		
		canvasPaint.setAntiAlias(true);
		canvasPaint.setColor(getResources().getColor(R.color.color_light_gray));
		canvasPaint.setAlpha(223);
		
	}

	@Override
	public boolean handleMessage(Message msg) {
		Log.d(TAG, "handleMessage");

		// 获得需要刷新的区域，仅仅能够在这里刷新
		Canvas canvas = mSurfaceHolder.lockCanvas();
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
			Message message = mRefreshHandler.obtainMessage();
			mRefreshHandler.sendMessageDelayed(message, 10);
		}

		mSurfaceHolder.unlockCanvasAndPost(canvas);

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 用于检测Fling
		mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surface created");
		
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(getResources().getColor(R.color.blue08));
		int canvasWidth = canvas.getWidth(), canvasHeight = canvas.getHeight();

		// 会被首先绘制
		colorBlueDarken = getResources().getColor(R.color.blue01);
		colorBlueLighten = getResources().getColor(R.color.blue08);
		pictureBackground.setCurrentColor(statusController.getServerStatus() == StatusController.STATUS_SERVER_STOPPED ? colorBlueDarken : colorBlueLighten);
		canvasElements.add(pictureBackground);
		
		// 圆环的参数设置不得不放在这里，因为要使用canvasWidth
		
		// 圆环
		serverInnerRadius = canvasWidth / 4 - 50;
		serverButton.setInnerRadius(canvasWidth / 4 - 20);
		serverButton.setOuterRadius(canvasWidth / 4);
		serverButton.setCx(canvasWidth / 2);
		serverButton.setCy(canvasHeight / 2);
		canvasElements.add(serverButton);
		
		// 绘制基本内容
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
	
	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown");
			int x = (int)e.getX(), y = (int)e.getY();
			
			// 使用bounce效果
			// 不允许有任何的animation
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
				
				// TODO 修改成函数
				if (!isLooping) {
					// 发送PictureButton
					Message message = mRefreshHandler.obtainMessage();				
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
			// 如何判断左右
			// 右
			if (velocityX > 0.0f) {
				viewSwitcher.setInAnimation(OverviewActivity.this, R.animator.slide_in_left);
				viewSwitcher.setOutAnimation(OverviewActivity.this, R.animator.slide_out_right);
				viewSwitcher.showPrevious();
			} else {
				viewSwitcher.setInAnimation(OverviewActivity.this, R.animator.slide_in_right);
				viewSwitcher.setOutAnimation(OverviewActivity.this, R.animator.slide_out_left);
				viewSwitcher.showNext();
			}
			
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}
}
