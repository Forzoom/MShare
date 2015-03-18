package org.mshare.file;

import org.mshare.main.R;

import android.content.Context;
import android.location.GpsStatus.Listener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MshareFileMenu {
	LinearLayout linearLayout;//菜单对应的主布局
	Context context;//对应的Activity
	
	
	public MshareFileMenu(Context c) { //构造函数 创建新菜单
		this.context = c;
		this.linearLayout = (LinearLayout)LayoutInflater.from(this.context).inflate(R.layout.menu, null);
	}
	
	public void addButton(int src, String str, View.OnClickListener listener) { //添加一个新按钮
		View view = LayoutInflater.from(this.context).inflate(R.layout.menu_button, null);
		
		TextView textView = (TextView)view.findViewById(R.id.menu_text);
		textView.setText(str);
		
		ImageView imageView = (ImageView)view.findViewById(R.id.menu_image);
		imageView.setBackgroundResource(src);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		
		this.linearLayout.setWeightSum(this.linearLayout.getWeightSum()+1);
		this.linearLayout.addView(view, lp);
		this.linearLayout.setOnClickListener(listener);
	}
	
	public void removeButton() { //删除所有按钮（貌似没用到）
		this.linearLayout.removeAllViews();
		this.linearLayout.setWeightSum(0);
	}
	
	public void hideAnimation() { //动画隐藏菜单
		AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
		aa.setDuration(500);
		aa.setFillAfter(true);
		this.linearLayout.startAnimation(aa);
	}
	
	public void showAnimation() { //动画显示菜单
		AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
		aa.setDuration(500);
		aa.setFillAfter(true);
		this.linearLayout.startAnimation(aa);
	}
	
	public void hide() { //隐藏菜单
		this.linearLayout.setVisibility(View.GONE);
	}
}
