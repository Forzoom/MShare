package org.mshare.main;

import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class QRCodeLogin extends Activity {
	private String contents = "12211017";
	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO ´´½¨¶þÎ¬Âë
		super.onCreate(savedInstanceState);
		setContentView(R.layout.encode);
		
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point displaySize = new Point();
		display.getSize(displaySize);
		int w = displaySize.x;
		int h = displaySize.y;
		int smallerDimension = w < h ? w : h;
		smallerDimension = smallerDimension * 7 / 8;
		
		String contentsToEncode = contents;
		Map<EncodeHintType,Object> hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);  
		hints.put(EncodeHintType.CHARACTER_SET, "GBK");
		BitMatrix result = null;
		try {
			result = new QRCodeWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, smallerDimension, smallerDimension, hints);
		} catch (IllegalArgumentException iae) {
			// Unsupported format
			iae.printStackTrace();
		} catch (WriterException we) {
			we.printStackTrace();
		}
		
		if (result != null) {
			int width = result.getWidth();
			int height = result.getHeight();
			int[] pixels = new int[width * height];
			
			for (int y = 0; y < height; y++) {
				int offset = y * width;
				for (int x = 0; x < width; x++) {
					pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
				}
			}

			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

			ImageView view = (ImageView) findViewById(R.id.image_view);
			view.setImageBitmap(bitmap);
		}
	}
}
