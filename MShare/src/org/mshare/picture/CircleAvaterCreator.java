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

// ���ڴ���Բ�ε�ͷ��
public class CircleAvaterCreator {
	private static final String TAG = CircleAvaterCreator.class.getSimpleName();
	
	/**
	 * ����ͼƬ������ϣ��ÿ�ζ���������Ҫ������ֶ�
	 * @param source
	 * @param radius
	 * @return ʧ��ʱ����null
	 */
	public static Bitmap createAvater(Bitmap source, int radius) {

		if (source == null) {
			Log.e(TAG, "source is null, stop creating!");
			return null;
		}
		
		// ��ʱ����ɫ��÷�������
		Context context = MShareApp.getAppContext();
		int blackColor = context.getResources().getColor(R.color.Color_Black);
		int transparentColor = context.getResources().getColor(R.color.color_transparent);
		
		// �ļ��Ķ�ȡ������Ҫ����ķ�Χ֮��(ʹ��inSampleSize����ý�С��ͼƬ);
		
		// ���������ͼƬ
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		int shortWidth = sourceWidth < sourceHeight ? sourceWidth : sourceHeight;
		Bitmap rectBitmap = Bitmap.createBitmap(source, (sourceWidth - shortWidth) / 2, (sourceHeight - shortWidth) / 2, shortWidth, shortWidth);
		
		// ��������ͼƬ(�������ٻ�������αȽϺã�)
		Bitmap avater = Bitmap.createScaledBitmap(rectBitmap, radius * 2, radius * 2, false);
		// �ͷ�rectBitmap
		rectBitmap.recycle();
		rectBitmap = null;
		
		// ��������ͼƬ
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
