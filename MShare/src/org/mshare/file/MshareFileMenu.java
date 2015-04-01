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
	LinearLayout linearLayout;//�˵���Ӧ��������
	Context context;//��Ӧ��Activity
	MshareFileMenu anotherMenu;
	boolean leftMenu = true;
	
	
	public MshareFileMenu(Context c, LinearLayout l) { //���캯�� �����²˵�
		this.context = c;
		this.linearLayout = (LinearLayout)LayoutInflater.from(this.context).inflate(R.layout.menu, null);
		l.addView(this.linearLayout);
	}
	
	public void addButton(int src, String str, View.OnClickListener listener) { //���һ���°�ť
		View view = LayoutInflater.from(this.context).inflate(R.layout.menu_button, null);
		// ��Ӧ���ı�
		TextView textView = (TextView)view.findViewById(R.id.menu_text);
		textView.setText(str);
		// ��Ӧ��ͼ��
		ImageView imageView = (ImageView)view.findViewById(R.id.menu_image);
		imageView.setImageResource(src);
		// LayoutParams
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		// ����Ȩ��
		this.linearLayout.setWeightSum(this.linearLayout.getWeightSum()+1);
		this.linearLayout.addView(view, lp);
		// ���ü�����
		view.setOnClickListener(listener);
		view.setBackgroundResource(R.drawable.menu_background);
	}
	
	/**
	 * ��Ӳ������ֵİ�ť
	 * @param resId
	 * @param listener
	 */
	public void addButton(int resId, View.OnClickListener listener) { //���һ���°�ť
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
	
	//�����Ҳ˵�
	public void setRightMenu(MshareFileMenu m) {
		this.anotherMenu = m;
		this.leftMenu = true;
		ToRight toRight = new ToRight();
		this.addButton(R.drawable.toright, toRight);
	}
	
	//������˵�
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
