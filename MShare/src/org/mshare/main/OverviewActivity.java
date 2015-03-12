package org.mshare.main;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;


public class OverviewActivity extends Activity {
	private static final String TAG = OverviewActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overview_activity_flat);
		
		SurfaceView surfaceView = (SurfaceView)findViewById(R.id.server_overview_layout);
		
		int color_white = getResources().getColor(R.color.Color_White);
		int color_darken_blue = getResources().getColor(R.color.blue02);
		int color_light_blue = getResources().getColor(R.color.blue08);
		
		// …Ë÷√—’…´
		surfaceView.setBackgroundColor(color_darken_blue);
		
		final ValueAnimator animator = ObjectAnimator.ofInt(surfaceView, "backgroundColor", color_darken_blue, color_light_blue);
		animator.setDuration(500);
		animator.setEvaluator(new ArgbEvaluator());

	}
	
}
