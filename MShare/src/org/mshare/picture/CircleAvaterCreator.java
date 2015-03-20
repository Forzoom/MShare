package org.mshare.picture;

import org.mshare.main.MShareApp;
import org.mshare.main.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.util.Log;

// 用于创建圆形的头像
public class CircleAvaterCreator {
	private static final String TAG = CircleAvaterCreator.class.getSimpleName();
	
	/**
	 * 创建图片，并不希望每次都创建，需要保存的手段
	 * @param source
	 * @param radius
	 * @return 失败时返回null
	 */
	public static Bitmap createAvater(Bitmap source, int radius) {

		if (source == null) {
			Log.e(TAG, "source is null, stop creating!");
			return null;
		}
		
		// 暂时将颜色获得放在这里
		Context context = MShareApp.getAppContext();
		int blackColor = context.getResources().getColor(R.color.Color_Black);
		int transparentColor = context.getResources().getColor(R.color.color_transparent);
		
		// 文件的读取不在需要处理的范围之内(使用inSampleSize来获得较小的图片);
		
		// 获得正方形图片
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		int shortWidth = sourceWidth < sourceHeight ? sourceWidth : sourceHeight;
		Bitmap rectBitmap = Bitmap.createBitmap(source, (sourceWidth - shortWidth) / 2, (sourceHeight - shortWidth) / 2, shortWidth, shortWidth);
		
		// 创建缩放图片(先缩放再获得正方形比较好？)
		Bitmap avater = Bitmap.createScaledBitmap(rectBitmap, radius * 2, radius * 2, false);
		// 释放rectBitmap
		rectBitmap.recycle();
		rectBitmap = null;
		
		// 创建最后的图片
		Bitmap result = Bitmap.createBitmap(radius * 2, radius * 2, Config.ARGB_8888);
		Canvas resultCanvas = new Canvas(result);
		
		Paint resultPaint = new Paint();
		resultPaint.setAntiAlias(true);
		resultCanvas.drawColor(transparentColor);
		resultPaint.setColor(blackColor);
		resultCanvas.drawCircle(radius, radius, radius, resultPaint);
		resultPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		resultCanvas.drawBitmap(avater, 0, 0, resultPaint);
		
		return result;
	}
	
}
