/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mshare.scan;

import java.util.ArrayList;
import java.util.List;

import org.mshare.main.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

/**
 * TODO 做的更加逼格一点
 * 刷新的频率问题
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  private static final long ANIMATION_DELAY = 80L;
  private static final int CURRENT_POINT_OPACITY = 0xA0;
  private static final int MAX_RESULT_POINTS = 20;
  private static final int POINT_SIZE = 6;

  private CameraManager cameraManager;
  private final Paint paint;
  private Bitmap resultBitmap;
  private final int maskColor;
  private final int resultColor;
  private final int laserColor;
  private final int resultPointColor;
  private int scannerAlpha;
  private List<ResultPoint> possibleResultPoints;
  private List<ResultPoint> lastPossibleResultPoints;

  private double laserRatio;
  private boolean laserDirection;
  
  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Resources resources = getResources();
    maskColor = resources.getColor(R.color.viewfinder_mask);
    resultColor = resources.getColor(R.color.result_view);
    laserColor = resources.getColor(R.color.viewfinder_laser);
    resultPointColor = resources.getColor(R.color.possible_result_points);
    scannerAlpha = 0;
    possibleResultPoints = new ArrayList<ResultPoint>(5);
    lastPossibleResultPoints = null;
  }

  public void setCameraManager(CameraManager cameraManager) {
    this.cameraManager = cameraManager;
  }

  @Override
  public void onDraw(Canvas canvas) {
    if (cameraManager == null) {
      return; // not ready yet, early draw before done configuring
    }
    // frame所对应的是中间区域的内容
    Rect frame = cameraManager.getFramingRect();
    Rect previewFrame = cameraManager.getFramingRectInPreview();    
    if (frame == null || previewFrame == null) {
      return;
    }
    int width = canvas.getWidth();
    int height = canvas.getHeight();

    // 所绘制的是周围的灰色
    // Draw the exterior (i.e. outside the framing rect) darkened
    paint.setColor(resultBitmap != null ? resultColor : maskColor);
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);

    // 应该是Activity所发送的Thumbnail，如果存在Thumbnail的话，就将thumbnail绘制
    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(CURRENT_POINT_OPACITY);
      canvas.drawBitmap(resultBitmap, null, frame, paint);
    } else {

    // 目前应该是没有resultBitmap
    	// Draw a red "laser scanner" line through the middle to show decoding is active
    	// 绘制红色的线条，仅仅是改变了红色线条的透明度
    	// TODO 不再使用一个长方形作为线条,而是使用更加逼格的东西
    	paint.setColor(laserColor);
    	int middle = frame.height() / 2 + frame.top;
    	// 使用一个正方形来代表线条
    	double nextLaserRatio = laserRatio + 0.01;
    	if ((nextLaserRatio - 1) > 0) {
    		laserRatio = nextLaserRatio - 1;
    		// 修正移动的方向
    		laserDirection = !laserDirection;
    	} else {
    		laserRatio = nextLaserRatio;
    	}
    	// 对于不同的方向进行不同的处理
    	if (laserDirection) {
    		canvas.drawRect(frame.left - 5, (int)(frame.top + 300 * laserRatio - 1), frame.right + 4, (int)(frame.top + 300 * laserRatio + 2), paint);
    	} else {
    		canvas.drawRect(frame.left - 5, (int)(frame.bottom - 300 * laserRatio - 1), frame.right + 4, (int)(frame.bottom - 300 * laserRatio + 2), paint);
    	}
    	
    	// 计算实际缩放的内容
    	float scaleX = frame.width() / (float) previewFrame.width();
    	float scaleY = frame.height() / (float) previewFrame.height();

    	List<ResultPoint> currentPossible = possibleResultPoints;
    	List<ResultPoint> currentLast = lastPossibleResultPoints;
    	int frameLeft = frame.left;
    	int frameTop = frame.top;
    	if (currentPossible.isEmpty()) {
    		lastPossibleResultPoints = null;
    	} else {
    	possibleResultPoints = new ArrayList<ResultPoint>(5);
    	lastPossibleResultPoints = currentPossible;
    	paint.setAlpha(CURRENT_POINT_OPACITY);
    	paint.setColor(resultPointColor);
        synchronized (currentPossible) {
          for (ResultPoint point : currentPossible) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                              frameTop + (int) (point.getY() * scaleY),
                              POINT_SIZE, paint);
          }
        }
      }
      if (currentLast != null) {
        paint.setAlpha(CURRENT_POINT_OPACITY / 2);
        paint.setColor(resultPointColor);
        synchronized (currentLast) {
          float radius = POINT_SIZE / 2.0f;
          for (ResultPoint point : currentLast) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                              frameTop + (int) (point.getY() * scaleY),
                              radius, paint);
          }
        }
      }

      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY,
                            frame.left - POINT_SIZE,
                            frame.top - POINT_SIZE,
                            frame.right + POINT_SIZE,
                            frame.bottom + POINT_SIZE);
    }
  }

  public void drawViewfinder() {
    Bitmap resultBitmap = this.resultBitmap;
    this.resultBitmap = null;
    if (resultBitmap != null) {
      resultBitmap.recycle();
    }
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    List<ResultPoint> points = possibleResultPoints;
    synchronized (points) {
      points.add(point);
      int size = points.size();
      if (size > MAX_RESULT_POINTS) {
        // trim it
        points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
      }
    }
  }

  	public void setRotation(int rotation) {
  		// 先调整到360之内
  		rotation %= 360;
  		// 需要将宽和高进行对调
  		if (rotation == 90 || rotation == 270) {
  			
  		}
  	}
  
}
