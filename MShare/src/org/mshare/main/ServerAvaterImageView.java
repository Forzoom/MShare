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

	// 判断当前是否被选择
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

		// 需要使用自己的判断内容

		// 绘制边框
		int borderWidth = 5;
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		// 绘制左右两竖
		canvas.drawRect(0, 0, borderWidth, canvasWidth, paint);
		canvas.drawRect(canvasWidth - borderWidth, 0, canvasWidth, canvasHeight, paint);
		// 绘制上下
		canvas.drawRect(borderWidth, 0, canvasWidth - borderWidth, borderWidth, paint);
		canvas.drawRect(borderWidth, canvasHeight - borderWidth, canvasWidth - borderWidth, canvasHeight, paint);
	}

	// 设置当前是否被选择
	public void setAvaterSelected(boolean isSelected) {
		if (this.isAvaterSelected == isSelected) {
			Log.w(TAG, "the same condition, is there something wrong?");
			return;
		} else { // 当不相同
			this.isAvaterSelected = isSelected;
			// 请求刷新
			invalidate();
		}
	}

	// 获得当前是否被选择
	public boolean isAvaterSelected() {
		return false;
	}
}
