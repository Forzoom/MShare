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
import android.os.Parcelable;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ��Activityֻ����������ݺͳ��֣���������������������
 * TODO �û���Ӧ��ʹ���ĸ��û�����û��ȷ����,��������û���ѡ����Щ�����أ�Ŀǰ��ʹ�õĶ���Ĭ���û�
 * TODO �ÿͻ��˱������ݣ�������ʹ��һ��UID����֤�������˵�Ψһ��
 * @author HM
 *
 */
public class QRCodeConnectActivity extends Activity {
	private static final String TAG = QRCodeConnectActivity.class.getSimpleName();
	
	// ��������ʾ��ά�����ɫ
	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;
	
	// TODO ���ڱ������ݵ�extra_key
	public static final String EXTRA_CONTENT = "extra_content_key";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qrcode);
		
		// TODO ��û�м�⵽content��ʱ�򣬸���ô��
		// ���ͨ��Intent���͵�����
		Intent intent = getIntent();
		ConnectInfo connectInfo = (ConnectInfo)intent.getParcelableExtra(EXTRA_CONTENT);
		
		// ѡ������ʾ�Ķ�ά��Ŀ�͸�
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point displaySize = new Point();
		display.getSize(displaySize);
		int w = displaySize.x;
		int h = displaySize.y;
		int smallerDimension = w < h ? w : h;
		smallerDimension = smallerDimension * 7 / 8;
		
		// ��������Ҫ���������
		String contentsToEncode = connectInfo.toString();
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
