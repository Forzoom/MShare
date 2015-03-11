/*
 * Copyright (C) 2010 ZXing authors
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
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
final class CameraConfigurationManager {

  private static final String TAG = "CameraConfiguration";

  // This is bigger than the size of a small screen, which is still supported. The routine
  // below will still select the default (presumably 320x240) size for these. This prevents
  // accidental selection of very low resolution on some devices.
  private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
  //private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
  //private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
  // 原本是0.15
  private static final double MAX_ASPECT_DISTORTION = 0.2;

  private final Context context;
  private Point screenResolution;
  private Point cameraResolution;

  CameraConfigurationManager(Context context) {
    this.context = context;
  }

  /**
   * Reads, one time, values from the camera that are needed by the app.
   * 检测屏幕的尺寸和相机预览的尺寸
   * 并不保证宽一定小于高
   */
  void initFromCameraParameters(Camera camera) {
    Camera.Parameters parameters = camera.getParameters();
    WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = manager.getDefaultDisplay();
    DisplayMetrics metrics = new DisplayMetrics();
    display.getMetrics(metrics);
    // 不知道相机的Size是什么样的，是宽大于高还是高大于宽
    Point theScreenResolution = new Point(metrics.widthPixels, metrics.heightPixels);
    screenResolution = theScreenResolution;
    Log.i(TAG, "Screen resolution: " + screenResolution);
    cameraResolution = findBestPreviewSizeValue(parameters, screenResolution);
    Log.i(TAG, "Camera resolution: " + cameraResolution);
  }

  void setDesiredCameraParameters(Camera camera, boolean safeMode) {
    Camera.Parameters parameters = camera.getParameters();

    if (parameters == null) {
      Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
      return;
    }

    Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

    if (safeMode) {
      Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
    }

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    initializeTorch(parameters, prefs, safeMode);

    String focusMode = null;
//    if (prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true)) {
//      if (safeMode || prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, false)) {
//        focusMode = findSettableValue(parameters.getSupportedFocusModes(),
//                                      Camera.Parameters.FOCUS_MODE_AUTO);
//      } else {
//        focusMode = findSettableValue(parameters.getSupportedFocusModes(),
//                                      Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
//                                      Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
//                                      Camera.Parameters.FOCUS_MODE_AUTO);
//      }
//    }
    
    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    
    // Maybe selected auto-focus but not available, so fall through here:
//    if (!safeMode && focusMode == null) {
//      focusMode = findSettableValue(parameters.getSupportedFocusModes(),
//                                    Camera.Parameters.FOCUS_MODE_MACRO,
//                                    Camera.Parameters.FOCUS_MODE_EDOF);
//    }
//    if (focusMode != null) {
//      parameters.setFocusMode(focusMode);
//    }

//    if (prefs.getBoolean(PreferencesActivity.KEY_INVERT_SCAN, false)) {
//      String colorMode = findSettableValue(parameters.getSupportedColorEffects(),
//                                           Camera.Parameters.EFFECT_NEGATIVE);
//      if (colorMode != null) {
//        parameters.setColorEffect(colorMode);
//      }
//    }

    parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
    camera.setParameters(parameters);

    Camera.Parameters afterParameters = camera.getParameters();
    Camera.Size afterSize = afterParameters.getPreviewSize();
    if (afterSize!= null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
      Log.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y +
                 ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
      cameraResolution.x = afterSize.width;
      cameraResolution.y = afterSize.height;
    }
  }

  Point getCameraResolution() {
    return cameraResolution;
  }

  Point getScreenResolution() {
    return screenResolution;
  }

  boolean getTorchState(Camera camera) {
    if (camera != null) {
      Camera.Parameters parameters = camera.getParameters();
      if (parameters != null) {
        String flashMode = camera.getParameters().getFlashMode();
        return flashMode != null &&
            (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
             Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
      }
    }
    return false;
  }

  void setTorch(Camera camera, boolean newSetting) {
    Camera.Parameters parameters = camera.getParameters();
    doSetTorch(parameters, newSetting, false);
    camera.setParameters(parameters);
  }

  private void initializeTorch(Camera.Parameters parameters, SharedPreferences prefs, boolean safeMode) {
//    boolean currentSetting = FrontLightMode.readPref(prefs) == FrontLightMode.ON;
    doSetTorch(parameters, false, safeMode);
  }

  private void doSetTorch(Camera.Parameters parameters, boolean newSetting, boolean safeMode) {
    String flashMode;
    if (newSetting) {
      flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                                    Camera.Parameters.FLASH_MODE_TORCH,
                                    Camera.Parameters.FLASH_MODE_ON);
    } else {
      flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                                    Camera.Parameters.FLASH_MODE_OFF);
    }
    if (flashMode != null) {
      parameters.setFlashMode(flashMode);
    }

    /*
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    if (!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_EXPOSURE, false)) {
      if (!safeMode) {
        int minExposure = parameters.getMinExposureCompensation();
        int maxExposure = parameters.getMaxExposureCompensation();
        if (minExposure != 0 || maxExposure != 0) {
          float step = parameters.getExposureCompensationStep();
          int desiredCompensation;
          if (newSetting) {
            // Light on; set low exposue compensation
            desiredCompensation = Math.max((int) (MIN_EXPOSURE_COMPENSATION / step), minExposure);
          } else {
            // Light off; set high compensation
            desiredCompensation = Math.min((int) (MAX_EXPOSURE_COMPENSATION / step), maxExposure);
          }
          Log.i(TAG, "Setting exposure compensation to " + desiredCompensation + " / " + (step * desiredCompensation));
          parameters.setExposureCompensation(desiredCompensation);
        } else {
          Log.i(TAG, "Camera does not support exposure compensation");
        }
      }
    }
     */
  }

  /**
   * 寻找最合适的PreviewSize，用于显示相机的预览内容
   * @param parameters
   * @param screenResolution
   * @return
   */
  private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {

    List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
    // 没有支持的PreviewSize，所以使用默认的Size
    if (rawSupportedSizes == null) {
      Log.w(TAG, "Device returned no supported preview sizes; using default");
      Camera.Size defaultSize = parameters.getPreviewSize();
      return new Point(defaultSize.width, defaultSize.height);
    }

    // Sort by size, descending
    // 对所有的Size进行排序，按照显示面积的大小
    List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);
    Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
      @Override
      public int compare(Camera.Size a, Camera.Size b) {
        int aPixels = a.height * a.width;
        int bPixels = b.height * b.width;
        if (bPixels < aPixels) {
          return -1;
        }
        if (bPixels > aPixels) {
          return 1;
        }
        return 0;
      }
    });

    // TODO 需要了解为什么要这么做
    // 仅仅是为了打Log
    if (Log.isLoggable(TAG, Log.INFO)) {
      StringBuilder previewSizesString = new StringBuilder();
      for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
        previewSizesString.append(supportedPreviewSize.width).append('x')
            .append(supportedPreviewSize.height).append(' ');
      }
      Log.i(TAG, "Supported preview sizes: " + previewSizesString);
    }

    // TODO 标记一下，在这里重新调整了宽高比
    // 屏幕的宽高比，且获得的ratio一定会大于1
    double screenAspectRatio = 0.0;
    double screenWidth = (double)screenResolution.x, screenHeight = (double)screenResolution.y;
    if (screenWidth > screenHeight) {
    	screenAspectRatio = screenWidth / screenHeight;
    } else {
    	screenAspectRatio = screenHeight / screenWidth;
    }

    // 筛选以及除去不合适的相机Size
    // Remove sizes that are unsuitable
    Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
    while (it.hasNext()) {
    	
      Camera.Size supportedPreviewSize = it.next();
      int realWidth = supportedPreviewSize.width;
      int realHeight = supportedPreviewSize.height;
      Log.d(TAG, "当前处理的Siz:width:" + realWidth + " height:" +realHeight);
      // 需要排除过小的size
      if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
        it.remove();
        Log.d(TAG, "remove Size too small: width:" + realWidth + " height:" +realHeight);
        continue;
      }

      // TODO
      boolean isCandidatePortrait = realWidth < realHeight;
      Log.d(TAG, "isCandidatePortrait:" + isCandidatePortrait);
      // 可能轻率的宽度和高度，意思是仅仅使用Heigth和Width来判断横竖屏？
      // 所选择的宽度一定是大于高度的，所以宽高比一定是大于1的
      int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
      int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
      // 宽高比
      double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
      // 排除和屏幕比例相差过多的Size
      double distortion = Math.abs(aspectRatio - screenAspectRatio);
      if (distortion > MAX_ASPECT_DISTORTION) {
        it.remove(); 
        Log.d(TAG, "remove Size aspect fail: width:" + realWidth + " height:" +realHeight);
        continue;
      }

      // 检测到和屏幕宽高相同的PreviewSize
      if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
        Point exactPoint = new Point(realWidth, realHeight);
        Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
        return exactPoint;
      }
    }

    // If no exact match, use largest preview size. This was not a great idea on older devices because
    // of the additional computation needed. We're likely to get here on newer Android 4+ devices, where
    // the CPU is much more powerful.
    // 没有完全匹配的Size的情况下，将选取最大的Size
    // 意思就是说Size不可能大于屏幕是吗?
    if (!supportedPreviewSizes.isEmpty()) {
      Camera.Size largestPreview = supportedPreviewSizes.get(0);
      Point largestSize = new Point(largestPreview.width, largestPreview.height);
      Log.i(TAG, "Using largest suitable preview size: " + largestSize);
      return largestSize;
    }

    // If there is nothing at all suitable, return current preview size
    // 没有合适的情况下，将返回默认的Size
    Camera.Size defaultPreview = parameters.getPreviewSize();
    Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
    Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);
    return defaultSize;
  }

  private static String findSettableValue(Collection<String> supportedValues,
                                          String... desiredValues) {
    Log.i(TAG, "Supported values: " + supportedValues);
    String result = null;
    if (supportedValues != null) {
      for (String desiredValue : desiredValues) {
        if (supportedValues.contains(desiredValue)) {
          result = desiredValue;
          break;
        }
      }
    }
    Log.i(TAG, "Settable value: " + result);
    return result;
  }

}
