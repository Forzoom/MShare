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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 对于传递来的图片内容进行解码
 * @author HM
 *
 */
final class DecodeHandler extends Handler {

  private static final String TAG = DecodeHandler.class.getSimpleName();

  // 主要是用来获得cameraManager
//  private final CaptureActivity activity;
  private ScanActivity activity;
  private final QRCodeReader qrCodeReader;
  private boolean running = true;

  public static final int WHAT_DECODE = 1;
  public static final int WHAT_QUIT = 2;
  private int index = 0;
  
  DecodeHandler(ScanActivity activity) {
    qrCodeReader = new QRCodeReader();
    this.activity = activity;
  }

  @Override
  public void handleMessage(Message message) {
    if (!running) {
      return;
    }
    // 需要设置 what来启动或者停止decode
    switch (message.what) {
      case WHAT_DECODE:
        decode((byte[]) message.obj, message.arg1, message.arg2);
        break;
      case WHAT_QUIT:
        running = false;
        Looper.myLooper().quit();
        break;
    }
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
	private void decode(byte[] data, int width, int height) {
	  
		long start = System.currentTimeMillis();
		Result rawResult = null;
		PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
		if (source != null) {
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			// 需要将bitmap发送到Activity中进行浏览,或者直接将bitmap保存下来
      
			int[] pixels = source.renderThumbnail();
			int thumbnailWidth = source.getThumbnailWidth();
			int thumbnailHeight = source.getThumbnailHeight();
			Bitmap bmp = Bitmap.createBitmap(pixels, 0, thumbnailWidth, thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888);
			String path = Environment.getExternalStorageDirectory().getAbsolutePath();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(path + File.separator + "capture" + index + ".jpg");
				index++;
				boolean result = bmp.compress(Bitmap.CompressFormat.JPEG, 70, fos);
				Log.d(TAG, "compress result " + result);
				// 将fos关闭
				fos.flush();
				fos.close();
				fos = null;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					fos = null;
				}
			}
      
      // 尝试将bitmap中的内容得到
//      try {
//		BitMatrix matrix = bitmap.getBlackMatrix();
//		int area = matrix.getWidth() * matrix.getHeight();
//		int[] container = new int[area];
//		
//		for (int x = 0; x < matrix.getWidth(); x++) {
//			for (int y = 0; y < matrix.getHeight(); y++) {
//				if (matrix.get(x, y)) {
//					container[x * width + y] = 0x000000;
//				} else {
//					container[x * width + y] = 0xffffff;
//				}
//			}
//		}
//		
//		// TODO 这里永远是失败，不知道为什么
//		Bitmap getBitmap = Bitmap.createBitmap(container, matrix.getWidth(), matrix.getHeight(), Bitmap.Config.ARGB_8888);
//		FileOutputStream fos = new FileOutputStream(path + File.separator + "get_capture" + index + ".jpg");
//		boolean result = getBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
//		Log.d(TAG, "get compress result " + result);
//		
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		Log.e(TAG, "get compress fail");
//		e.printStackTrace();
//	}
      
      try {
        rawResult = qrCodeReader.decode(bitmap, null);
      } catch (ReaderException re) {
        // continue
      } finally {
    	  qrCodeReader.reset();
      }
    }

    // 将内容发送到CaptureActivityHandler中进行处理
    // TODO 需要修正
    
    // 成功的时候发送内容，失败的时候调用request继续尝试获得内容
    if (rawResult != null) {
      // Don't log the barcode contents for security.
      long end = System.currentTimeMillis();
      Log.d(TAG, "Found barcode in " + (end - start) + " ms");
      Toast.makeText(activity, rawResult.toString(), Toast.LENGTH_SHORT).show();
    } else {
    	Log.e(TAG, "fail, request again!");
    	Log.d(TAG, "request again preview frame in DecodeHandler, handler:" + this + " message:" + WHAT_DECODE);
//    	activity.getCameraManager().requestPreviewFrame(this, WHAT_DECODE);
    }
  }

  private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
    int[] pixels = source.renderThumbnail();
    // 获得缩放后的图片的宽和高
    int width = source.getThumbnailWidth();
    int height = source.getThumbnailHeight();
    // 创建图片
    Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
    // 将图片内容转换成ByteArray,后绑定在bundle中，并将一些其他的内容发送出去
    ByteArrayOutputStream out = new ByteArrayOutputStream();    
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
    bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
  }

}
