package org.mshare.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by huangming on 15/3/26.
 */
public class ServerAvaterImageView extends ImageView {
	private static final String TAG = ServerAvaterImageView.class.getSimpleName();

	// �жϵ�ǰ�Ƿ�ѡ��
	private boolean isAvaterSelected = false;

	public ServerAvaterImageView(Context context) {
		super(context);
	}

	public ServerAvaterImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ServerAvaterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "the canvasWidth:" + canvas.getWidth() + " canvasHeight:" + canvas.getHeight());

		// ��Ҫʹ���Լ����ж�����

		// ���Ʊ߿�
		int borderWidth = 5;
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		// ������������
		canvas.drawRect(0, 0, borderWidth, canvasWidth, paint);
		canvas.drawRect(canvasWidth - borderWidth, 0, canvasWidth, canvasHeight, paint);
		// ��������
		canvas.drawRect(borderWidth, 0, canvasWidth - borderWidth, borderWidth, paint);
		canvas.drawRect(borderWidth, canvasHeight - borderWidth, canvasWidth - borderWidth, canvasHeight, paint);
	}

	// ���õ�ǰ�Ƿ�ѡ��
	public void setAvaterSelected(boolean isSelected) {
		if (this.isAvaterSelected == isSelected) {
			Log.w(TAG, "the same condition, is there something wrong?");
			return;
		} else { // ������ͬ
			this.isAvaterSelected = isSelected;
			// ����ˢ��
			invalidate();
		}
	}

	// ��õ�ǰ�Ƿ�ѡ��
	public boolean isAvaterSelected() {
		return false;
	}
}
