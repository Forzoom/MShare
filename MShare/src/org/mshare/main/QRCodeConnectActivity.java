package org.mshare.main;

import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 该Activity只负责接受内容和呈现，并不参与服务器相关内容
 * TODO 用户名应该使用哪个用户名还没有确定把,该如何让用户来选择这些内容呢？目前所使用的都是默认用户
 * TODO 让客户端保存内容，或者是使用一个UID来保证服务器端的唯一性
 * @author HM
 *
 */
public class QRCodeConnectActivity extends Activity {
	private static final String TAG = QRCodeConnectActivity.class.getSimpleName();
	
	private String contents = "12211017";
	
	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;
	
	// TODO 用于保存内容的extra_key
	public static final String EXTRA_CONTENT = "extra_content_key";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qrcode);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// TODO 当没有检测到content的时候，该怎么办
		// 获得通过Intent发送的内容
		Intent intent = getIntent();
		contents = intent.getStringExtra(EXTRA_CONTENT);
		// 并没有接受到需要显示的内容，设置默认内容
		if (contents == null) {
			contents = "default value";
		}
		
		// 选择所显示的二维码的宽和高s
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point displaySize = new Point();
		display.getSize(displaySize);
		int w = displaySize.x;
		int h = displaySize.y;
		int smallerDimension = w < h ? w : h;
		smallerDimension = smallerDimension * 7 / 8;
		
		// 设置所需要编码的内容
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
