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
	 * @param resId ��ǰû�а취�ж�resId�Ƿ���ȷ
	 * @param radius ��ϣ��������ͷ��İ뾶��С
	 * @return ʧ��ʱ����null
	 */
	public static Bitmap createAvater(int resId, int radius) {

		// ��ʱ����ɫ��÷�������
		Context context = MShareApp.getAppContext();
        Resources res = context.getResources();
		int blackColor = res.getColor(R.color.Color_Black);
		int transparentColor = res.getColor(R.color.color_transparent);

        // ʹ��decode����
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opt);
        int resWidth = opt.outWidth, resHeight = opt.outHeight;
        int shortWidth = resWidth < resHeight ? resWidth : resHeight;

        opt.inJustDecodeBounds = false;
        opt.inSampleSize = (shortWidth / (radius * 2));
        Bitmap sampleBitmap = BitmapFactory.decodeResource(res, resId, opt);

		// ���������ͼƬ
        Bitmap rectBitmap = getRectBitmap(sampleBitmap);

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



    /**
     * ���һ��������ͼƬ
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
