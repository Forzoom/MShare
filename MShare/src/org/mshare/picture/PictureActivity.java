package org.mshare.picture;

import org.mshare.main.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;

/**
 * 按钮显示文本提示
 * @author HM
 *
 */
public class PictureActivity extends Activity {
// implements SurfaceHolder.Callback, Handler.Callback {
//	private static final String TAG = PictureActivity.class.getSimpleName();
//	
//	Paint mPaint = new Paint();
//	Paint buttonPaint = new Paint();
//	Paint textPaint = new Paint();
//	Paint descriptionPaint = new Paint();
//	
//	GestureDetector mGestureDetector;
//	SurfaceHolder mSurfaceHolder;
//	RefreshHandler mRefreshHandler;
//
//	private static final String KEY_PICTURE_BUTTON = "picture_button";
//
//	Bitmap backgroundBitmap;
//	
//	int duration = 200;
//	
//	boolean show = false;
//	
//	RingButton mPictureButton;
//	
//	private boolean isLooping = false;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.picture);
//		
//		SurfaceView surfaceView = (SurfaceView)findViewById(R.id.picture_surface_view);
//		SurfaceHolder surfaceHolder = surfaceView.getHolder();
//		surfaceHolder.addCallback(this);
//	
//		// 设置画笔
//		mPaint.setAntiAlias(true);
//		buttonPaint.setAntiAlias(true);
//		buttonPaint.setColor(getResources().getColor(R.color.color_light_gray));
//		buttonPaint.setAlpha(223);
//		
//		textPaint.setTextSize(36);
//		textPaint.setAntiAlias(true);
//		textPaint.setColor(getResources().getColor(R.color.Color_Black));
//		
//		descriptionPaint.setAntiAlias(true);
//		descriptionPaint.setColor(getResources().getColor(R.color.Color_White));
//		
//		mGestureDetector = new GestureDetector(this, new GestureListener());
//		mRefreshHandler = new RefreshHandler(Looper.myLooper(), this);
//		
//		// 创建PictureButton
//		mPictureButton = new RingButton();
//		mPictureButton.setStartRadius(46);
//		mPictureButton.setEndRadius(36);
//		mPictureButton.setOuterRadius(60);
//		mPictureButton.setCx(200);
//		mPictureButton.setCy(200);
//	}
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		Log.d(TAG, "surface created");
//		
//		// Holder
//		mSurfaceHolder = holder;
//		
//		Canvas canvas = holder.lockCanvas();
//		// 获得canvas的大小
//		int canvasWidth = canvas.getWidth(), canvasHeight = canvas.getHeight();
//		Log.d(TAG, "canvasWidth:" + canvasWidth + " canvasHeight:" + canvasHeight);
//		// 获得原图片大小
//		Options options = new Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeResource(getResources(), R.drawable.qrcode1, options);
//		int rawWidth = options.outWidth, rawHeight = options.outHeight;
//		Log.d(TAG, "rawWidth:" + rawWidth + " rawHeight:" + rawHeight);
//		// 尝试缩放图片
//		double scaleRatio = rawWidth * 1.0 / (canvasWidth * 1.0);
//		Log.d(TAG, "scaleRatio:" + scaleRatio);
//		options.inJustDecodeBounds = false;
////		options.inSampleSize = (int)scaleRatio;
//		Log.d(TAG, "inSampleSize:" + (int)scaleRatio);
//		Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qrcode1, options);
//		Log.d(TAG, "rawBitmap.getWidth():" + rawBitmap.getWidth() + " getHeight:" + rawBitmap.getHeight());
//		int targetWidth = canvasWidth, targetHeight = (int)(rawHeight / scaleRatio);
//		Log.d(TAG, "targetWidth:" + targetWidth + " targetHeight:" + targetHeight);
//		backgroundBitmap = Bitmap.createScaledBitmap(rawBitmap, targetWidth, targetHeight, false);
//		
//		canvas.drawBitmap(backgroundBitmap, 0, 0, mPaint);
//
//		// 绘制圆
//		mPictureButton.initDraw(canvas, buttonPaint);
//		holder.unlockCanvasAndPost(canvas);
//	}
//
//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width,
//			int height) {
//		Log.d(TAG, "surface changed");
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		Log.d(TAG, "surface destoryed");
//	}
//	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {		
//		int action = event.getAction() & MotionEvent.ACTION_MASK;
////		Log.d(TAG, "action:" + action);
//		mGestureDetector.onTouchEvent(event);
//		return super.onTouchEvent(event);
//	}
//	
////	public void drawDescription(Canvas canvas, String text, int left, int top, int right, int bottom) {
////		if (show) {
//////			int center = (top + bottom) / 2;
////			
//////			canvas.drawLine(left, center, left + 10, center - 10, mPaint);
////			// 绘制中间部分
////			descriptionPaint.setStyle(Style.FILL);
////			descriptionPaint.setColor(getResources().getColor(R.color.Color_White));
////			canvas.drawRect(left, top, right, bottom, descriptionPaint);
////			canvas.drawRoundRect(new RectF((float)left, (float)top, (float)right, (float)bottom), (float)10, (float)10, descriptionPaint);
////			// 绘制边框
////			descriptionPaint.setStrokeWidth(3);
////			descriptionPaint.setStyle(Style.STROKE);
////			descriptionPaint.setColor(getResources().getColor(R.color.Color_Black));
////			canvas.drawRoundRect(new RectF((float)left, (float)top, (float)right, (float)bottom), (float)10, (float)10, descriptionPaint);
////			
////			canvas.drawText(text, left + 30, top + textPaint.getTextSize() + 30, textPaint);
////		}
////	}
//	
//	class GestureListener extends GestureDetector.SimpleOnGestureListener {
//
//		@Override
//		public boolean onDown(MotionEvent e) {
//			Log.d(TAG, "onDown");
//			int x = (int)e.getX(), y = (int)e.getY();
//			
//			if (mPictureButton.isClicked(x, y)) {
//				mPictureButton.start();
//				if (!isLooping) {
//					// 发送PictureButton
//					Message message = mRefreshHandler.obtainMessage();				
//					message.sendToTarget();
//				}
//			}
//			
////			show = !show;
//			
//			return super.onDown(e);
//		}
//		
//	}
//
//	@Override
//	public boolean handleMessage(Message msg) {
//		Log.d(TAG, "handleMessage");
//		
//		// 获得需要刷新的区域，仅仅能够在这里刷新
//		Canvas canvas = mSurfaceHolder.lockCanvas();
//		canvas.drawBitmap(backgroundBitmap, 0, 0, mPaint);
//		
//		if (mPictureButton.isStarted()) {
//			Log.d(TAG, "draw");
//			mPictureButton.draw(canvas, buttonPaint);
//			Message message = mRefreshHandler.obtainMessage();
//			mRefreshHandler.sendMessageDelayed(message, 10);
//			isLooping = true;
//		} else {
//			// 将不会再刷新了
//			isLooping = false;
//			mPictureButton.initDraw(canvas, buttonPaint);
//		}
////			int cx = mPictureButton.getCx(), cy = mPictureButton.getCy(), outerRadius = mPictureButton.getOuterRadius();
////			String content = "LiYang";
////			int left = cx + outerRadius + 10;
////			int textSize = (int)textPaint.getTextSize();
////			int top = cy - textSize / 2 - 30;
////			TextPaint tp = new TextPaint(textPaint);
////			int right = left + (int)textPaint.measureText(content) + 60;
////			int bottom = cy + textSize / 2 + 30;
////			
////			drawDescription(canvas, content, left, top, right, bottom);
//			
//		mSurfaceHolder.unlockCanvasAndPost(canvas);
//		
//		return false;
//	}
//	
}
