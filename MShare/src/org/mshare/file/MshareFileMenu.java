package org.mshare.file;

import org.mshare.main.R;

import android.R.bool;
import android.content.Context;
import android.location.GpsStatus.Listener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MshareFileMenu {
	LinearLayout linearLayout;//菜单对应的主布局
	Context context;//对应的Activity
	MshareFileMenu anotherMenu;
	boolean leftMenu = true;
	
	
	public MshareFileMenu(Context c, LinearLayout l) { //构造函数 创建新菜单
		this.context = c;
		this.linearLayout = (LinearLayout)LayoutInflater.from(this.context).inflate(R.layout.menu, null);
		l.addView(this.linearLayout);
	}
	
	public void addButton(int src, String str, View.OnClickListener listener) { //添加一个新按钮
		View view = LayoutInflater.from(this.context).inflate(R.layout.menu_button, null);
		// 对应的文本
		TextView textView = (TextView)view.findViewById(R.id.menu_text);
		textView.setText(str);
		// 对应的图标
		ImageView imageView = (ImageView)view.findViewById(R.id.menu_image);
		imageView.setImageResource(src);
		// LayoutParams
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		// 设置权重
		this.linearLayout.setWeightSum(this.linearLayout.getWeightSum()+1);
		this.linearLayout.addView(view, lp);
		// 设置监听器
		view.setOnClickListener(listener);
		view.setBackgroundResource(R.drawable.menu_background);
	}
	
	/**
	 * 添加不带文字的按钮
	 * @param resId
	 * @param listener
	 */
	public void addButton(int resId, View.OnClickListener listener) { //添加一个新按钮
		View menuItem = LayoutInflater.from(this.context).inflate(R.layout.menu_button_no_text, null);
		
		ImageView imageView = (ImageView)menuItem.findViewById(R.id.menu_image);
		imageView.setImageResource(resId);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		
		this.linearLayout.setWeightSum(this.linearLayout.getWeightSum() + 1);
		this.linearLayout.addView(menuItem, lp);
		menuItem.setOnClickListener(listener);
		menuItem.setBackgroundResource(R.drawable.menu_background);
	}
	
	public void removeButton() { //删除所有按钮（貌似没用到）
		this.linearLayout.removeAllViews();
		this.linearLayout.setWeightSum(0);
	}
	
	public void hideAnimation() { //动画隐藏菜单
		Animation animation = AnimationUtils.loadAnimation(context, R.anim.menu_hide);
		this.linearLayout.startAnimation(animation);
		this.linearLayout.setVisibility(View.GONE);
	}
	
	public void showAnimation() { //动画显示菜单
		this.linearLayout.setVisibility(View.VISIBLE);
		Animation animation = AnimationUtils.loadAnimation(context, R.anim.menu_show);
		this.linearLayout.startAnimation(animation);
	}
	
	public void hide() { //隐藏菜单
		this.linearLayout.setVisibility(View.GONE);
	}
	
	//设置右菜单
	public void setRightMenu(MshareFileMenu m) {
		this.anotherMenu = m;
		this.leftMenu = true;
		ToRight toRight = new ToRight();
		this.addButton(R.drawable.toright, toRight);
	}
	
	//设置左菜单
	public void setLeftMenu(MshareFileMenu m) {
		this.anotherMenu = m;
		this.leftMenu = false;
		ToLeft toLeft = new ToLeft();
		this.addButton(R.drawable.toleft, toLeft);
	}
	
	class ToRight implements View.OnClickListener {
		
		@Override
        public void onClick(View v) {
			hideAnimation();
			anotherMenu.showAnimation();
        }
	}
	
	class ToLeft implements View.OnClickListener {
		
		@Override
        public void onClick(View v) {
			hideAnimation();
			anotherMenu.showAnimation();
        }
	}
}
