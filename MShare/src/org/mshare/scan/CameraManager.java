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

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

  private static final String TAG = CameraManager.class.getSimpleName();

  private static final int MIN_FRAME_WIDTH = 240;
  private static final int MIN_FRAME_HEIGHT = 240;
  private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
  private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

  private final ScanActivity activity;
  private final CameraConfigurationManager configManager;
  private Camera camera;
  private Rect framingRect;
  private Rect framingRectInPreview;
  private boolean initialized;
  private boolean previewing; // 表明当前相机正在工作，预览正在进行，在startPreview和stopPreview中被处理
  private int requestedFramingRectWidth;
  private int requestedFramingRectHeight;
  private int displayOrientation = -1;
  
  /**
   * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
   * clear the handler so it will only receive one message.
   * 数据被发送到这里来解码，每次解码后handler都会被清空，以保证只会接受到一次消息，后面消息中的信息不会覆盖前面的内容，因为使用OneShot时，后面
   * 的Preview内容会覆盖前面的Preview内容
   * 每次使用OneShotCallback，callback对象都会被清除
   */
  private final PreviewCallback previewCallback;

  public CameraManager(ScanActivity activity) {
    this.activity = activity;
    // configManager也是在这里被创建
    this.configManager = new CameraConfigurationManager(activity);
    // previewCallback被创建
    previewCallback = new PreviewCallback(configManager);
  }

  /**
   * Opens the camera driver and initializes the hardware parameters.
   * 启动相机，并且
   *
   * @param holder The surface object which the camera will draw preview frames into.
   * @throws IOException Indicates the camera driver failed to open.
   */
  public synchronized void openDriver(SurfaceHolder holder) throws IOException {
    Camera theCamera = camera;
    if (theCamera == null) {
      theCamera = OpenCameraInterface.open();
//    	theCamera = Camera.open();
      if (theCamera == null) {
        throw new IOException();
      }
      camera = theCamera;
    }
    
    displayOrientation = getDisplayOrientationResult();
    // 设置Preview的旋转
    if (displayOrientation != -1) {
    	theCamera.setDisplayOrientation(displayOrientation);
    }
	
    theCamera.setPreviewDisplay(holder);

    if (!initialized) {
      initialized = true;
      configManager.initFromCameraParameters(theCamera);
      if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
        setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
        requestedFramingRectWidth = 0;
        requestedFramingRectHeight = 0;
      }
    }

    Camera.Parameters parameters = theCamera.getParameters();
    String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
    try {
      configManager.setDesiredCameraParameters(theCamera, false);
    } catch (RuntimeException re) {
      // Driver failed
      Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
      Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
      // Reset:
      if (parametersFlattened != null) {
        parameters = theCamera.getParameters();
        parameters.unflatten(parametersFlattened);
        try {
          theCamera.setParameters(parameters);
          configManager.setDesiredCameraParameters(theCamera, true);
        } catch (RuntimeException re2) {
          // Well, darn. Give up
          Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
        }
      }
    }

  }

  	/**
  	 * 通过计算获得当前Preview的内容应该怎样旋转
  	 * 并不是所有的手机都对setRotation支持
  	 * @return
  	 */
  	public int getDisplayOrientationResult() {
  		// TODO 在这里尝试修正相机的预览方向,在这里也许并不是一个很好的选择，因为没有Activity
  	    CameraInfo info = new android.hardware.Camera.CameraInfo();
  	    // TODO 需要修正Camera的id
  	    Camera.getCameraInfo(0, info);
  	    int rotation = activity.getWindowManager().getDefaultDisplay()
  	            .getRotation();
  	    int degrees = 0;
  	    switch (rotation) {
  	        case Surface.ROTATION_0: degrees = 0; break;
  	        case Surface.ROTATION_90: degrees = 90; break;
  	        case Surface.ROTATION_180: degrees = 180; break;
  	        case Surface.ROTATION_270: degrees = 270; break;
  	    }
  	    return (info.orientation - degrees + 360) % 360;
  	}
  
  public synchronized boolean isOpen() {
    return camera != null;
  }

  /**
   * Closes the camera driver if still in use.
   */
  public synchronized void closeDriver() {
    if (camera != null) {
      camera.release();
      camera = null;
      // Make sure to clear these each time we close the camera, so that any scanning rect
      // requested by intent is forgotten.
      framingRect = null;
      framingRectInPreview = null;
    }
  }

  /**
   * Asks the camera hardware to begin drawing preview frames to the screen.
   */
  public synchronized void startPreview() {
	  Log.d(TAG, "start preview");
    Camera theCamera = camera;
    if (theCamera != null && !previewing) {
      theCamera.startPreview();
      previewing = true;
    }
    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(0, info);
    Log.d(TAG, "now rotation " + info.orientation);
  }

  /**
   * Tells the camera to stop drawing preview frames.
   */
  public synchronized void stopPreview() {
	  Log.d(TAG, "stop preview");
    if (camera != null && previewing) {
      camera.stopPreview();
      previewCallback.setHandler(null, 0);
      previewing = false;
    }
  }

  /**
   * Convenience method for {@link com.google.zxing.client.android.CaptureActivity}
   */
  public synchronized void setTorch(boolean newSetting) {
    if (newSetting != configManager.getTorchState(camera)) {
      if (camera != null) {
        configManager.setTorch(camera, newSetting);
      }
    }
  }

  /**
   * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
   * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
   * respectively.
   * 可以说是每次preview的入口，设置了处理Preview内容的Handler，同时让下一个Preview内容被调用
   *
   * @param handler The handler to send the message to.
   * @param message The what field of the message to be sent.
   */
  public synchronized void requestPreviewFrame(Handler handler, int message) {
    Camera theCamera = camera;
    if (theCamera != null && previewing) {
      previewCallback.setHandler(handler, message);
      theCamera.setOneShotPreviewCallback(previewCallback);
    }
  }

  /**
   * 二维码所应该放置的区域
   * Calculates the framing rect which the UI should draw to show the user where to place the
   * barcode. This target helps with alignment as well as forces the user to hold the device
   * far enough away to ensure the image will be in focus.
   *
   * @return The rectangle to draw on screen in window coordinates.
   */
  public synchronized Rect getFramingRect() {
    if (framingRect == null) {
      if (camera == null) {
        return null;
      }
      Point screenResolution = configManager.getScreenResolution();
      if (screenResolution == null) {
        // Called early, before init even finished
        return null;
      }

      // 设定为较短边的八分之五
      int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
      int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);
      // 调整为正方形
      if (height < width) {
    	  width = height;
      } else {
    	  height = width;
      }
      
      int leftOffset = (screenResolution.x - width) / 2;
      int topOffset = (screenResolution.y - height) / 2;
      framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
      Log.d(TAG, "Calculated framing rect: " + framingRect);
    }
    return framingRect;
  }
  
  private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
    int dim = 5 * resolution / 8; // Target 5/8 of each dimension
    if (dim < hardMin) {
      return hardMin;
    }
    if (dim > hardMax) {
      return hardMax;
    }
    return dim;
  }

  /**
   * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
   * not UI / screen.
   * 不知道这个是干什么的，是在preview的控件中的中间正方形的位置，用于将正方形修正为正方形所使用的?
   */
  public synchronized Rect getFramingRectInPreview() {
	  // 有人一直在调用该函数
    if (framingRectInPreview == null) {
      Rect framingRect = getFramingRect();
      if (framingRect == null) {
        return null;
      }
      Rect rect = new Rect(framingRect);
      Point cameraResolution = configManager.getCameraResolution();
      Point screenResolution = configManager.getScreenResolution();
      if (cameraResolution == null || screenResolution == null) {
    	Log.d(TAG, "cameraResolution and screenResolution is null");
        // Called early, before init even finished
        return null;
      }
      Log.d(TAG, "get resolution");
      
      // 对于cameraResolution和screenResolution来说，保证宽大于高
      int cameraX = cameraResolution.x, cameraY = cameraResolution.y, screenX = screenResolution.x, screenY = screenResolution.y;
      boolean cameraLandscape = cameraX > cameraY;
      int cameraWidth = cameraLandscape ? cameraX : cameraY;
      int cameraHeight = cameraLandscape ? cameraY : cameraX;
      boolean screenLandscape = screenX > screenY;
      int screenWidth = screenLandscape ? screenX : screenY;
      int screenHeight = screenLandscape ? screenY : screenX;
      
      // 不再调整，只有这样才是正确的
      rect.left = rect.left * cameraWidth / screenWidth;
      rect.right = rect.right * cameraWidth / screenWidth;
      rect.top = rect.top * cameraHeight / screenHeight;
      rect.bottom = rect.bottom * cameraHeight / screenHeight;
      framingRectInPreview = rect;
//      Log.d(TAG, "cameraX:" + cameraX + " cameraY:" + cameraY + " screenX:" + screenX + " screenY:" + screenY);
      Log.d(TAG, "Calculated framing rect in preview: " + framingRectInPreview);
    }
    return framingRectInPreview;
  }

  /**
   * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
   * them automatically based on screen resolution.
   *
   * @param width The width in pixels to scan.
   * @param height The height in pixels to scan.
   */
  public synchronized void setManualFramingRect(int width, int height) {
    if (initialized) {
      Point screenResolution = configManager.getScreenResolution();
      if (width > screenResolution.x) {
        width = screenResolution.x;
      }
      if (height > screenResolution.y) {
        height = screenResolution.y;
      }
      int leftOffset = (screenResolution.x - width) / 2;
      int topOffset = (screenResolution.y - height) / 2;
      framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
      Log.d(TAG, "Calculated manual framing rect: " + framingRect);
      framingRectInPreview = null;
    } else {
      requestedFramingRectWidth = width;
      requestedFramingRectHeight = height;
    }
  }

  /**
   * A factory method to build the appropriate LuminanceSource object based on the format
   * of the preview buffers, as described by Camera.Parameters.
   *
   * @param data A preview frame.
   * @param width The width of the image.
   * @param height The height of the image.
   * @return A PlanarYUVLuminanceSource instance.
   */
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
    Rect rect = getFramingRectInPreview();
    if (rect == null) {
      return null;
    }
    
    // 需要在这里调整方向，相机的方向对于dataWidth和dataHeight，但是width和height对应的是屏幕上的内容
    Log.d(TAG, "buildLuminanceSource : dataWidth:" + width + " dataHeight:" + height);
    Log.d(TAG, "buildLuminanceSource : width:" + rect.width() + " height:" + rect.height());
    // Go ahead and assume it's YUV rather than die.
    // 需要根据相机显示的旋转情况来判断,因为rect所对应的是屏幕内容，也就是相机旋转后的内容
    	if (displayOrientation == 90 || displayOrientation == 270) {
    		Rect r = new Rect(rect.top, rect.left, rect.top + rect.height(), rect.left + rect.width());
    		Log.d(TAG, "try rotate 90 " + r);
    		// 将left和top，宽和高进行了交换
    		return new PlanarYUVLuminanceSource(data, width, height, rect.top, rect.left, rect.height(), rect.width(), false);
    	} else {
    		Log.d(TAG, "try no rotate " + rect);
    		return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
    	}
	}

}
