package org.mshare.file;

import org.mshare.main.R;

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
	LinearLayout linearLayout;//�˵���Ӧ��������
	Context context;//��Ӧ��Activity
	
	
	public MshareFileMenu(Context c, LinearLayout l) { //���캯�� �����²˵�
		this.context = c;
		this.linearLayout = (LinearLayout)LayoutInflater.from(this.context).inflate(R.layout.menu, null);
		l.addView(this.linearLayout);
	}
	
	public void addButton(int src, String str, View.OnClickListener listener) { //���һ���°�ť
		View view = LayoutInflater.from(this.context).inflate(R.layout.menu_button, null);
		
		TextView textView = (TextView)view.findViewById(R.id.menu_text);
		textView.setText(str);
		
		ImageView imageView = (ImageView)view.findViewById(R.id.menu_image);
		imageView.setBackgroundResource(src);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		
		this.linearLayout.setWeightSum(this.linearLayout.getWeightSum()+1);
		this.linearLayout.addView(view, lp);
		view.setOnClickListener(listener);
	}
	
	/**
	 * ��Ӳ������ֵİ�ť
	 * @param resId
	 * @param listener
	 */
	public void addButton(int resId, View.OnClickListener listener) { //���һ���°�ť
		View menuItem = LayoutInflater.from(this.context).inflate(R.layout.menu_button_no_text, null);
		
		ImageView imageView = (ImageView)menuItem.findViewById(R.id.menu_image);
		imageView.setBackgroundResource(resId);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		
		this.linearLayout.setWeightSum(this.linearLayout.getWeightSum() + 1);
		this.linearLayout.addView(menuItem, lp);
		menuItem.setOnClickListener(listener);
	}
	
	public void removeButton() { //ɾ�����а�ť��ò��û�õ���
		this.linearLayout.removeAllViews();
		this.linearLayout.setWeightSum(0);
	}
	
	public void hideAnimation() { //�������ز˵�
		Animation animation = AnimationUtils.loadAnimation(context, R.anim.menu_hide);
		this.linearLayout.startAnimation(animation);
		this.linearLayout.setVisibility(View.GONE);
	}
	
	public void showAnimation() { //������ʾ�˵�
		this.linearLayout.setVisibility(View.VISIBLE);
		Animation animation = AnimationUtils.loadAnimation(context, R.anim.menu_show);
		this.linearLayout.startAnimation(animation);
	}
	
	public void hide() { //���ز˵�
		this.linearLayout.setVisibility(View.GONE);
	}
}
