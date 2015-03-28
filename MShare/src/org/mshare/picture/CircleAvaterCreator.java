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
	 * @param resId 当前没有办法判断resId是否正确
	 * @param radius 所希望创建的头像的半径大小
	 * @return 失败时返回null
	 */
	public static Bitmap createAvater(int resId, int radius) {

		// 暂时将颜色获得放在这里
		Context context = MShareApp.getAppContext();
        Resources res = context.getResources();
		int blackColor = res.getColor(R.color.Color_Black);
		int transparentColor = res.getColor(R.color.color_transparent);

        // 使用decode创建
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opt);
        int resWidth = opt.outWidth, resHeight = opt.outHeight;
        int shortWidth = resWidth < resHeight ? resWidth : resHeight;

        opt.inJustDecodeBounds = false;
        opt.inSampleSize = (shortWidth / (radius * 2));
        Bitmap sampleBitmap = BitmapFactory.decodeResource(res, resId, opt);

		// 获得正方形图片
        Bitmap rectBitmap = getRectBitmap(sampleBitmap);

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



    /**
     * 获得一个正方形图片
     * @param source
     * @return
     */
    private static Bitmap getRectBitmap(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int shortOne = width < height ? width : height;
        Bitmap rectBitmap = Bitmap.createBitmap(source, (width - shortOne) / 2, (height - shortOne) / 2, shortOne, shortOne);
        return rectBitmap;
    }

}
