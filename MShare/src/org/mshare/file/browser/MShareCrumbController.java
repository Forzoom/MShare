package org.mshare.file.browser;

import java.io.File;
import java.util.ArrayList;
import org.mshare.main.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * ���м����
 * TODO ����ʹ��button����������������
 * TODO ʹ��ArrayList����̬����ջ������
 * @author HM
 * 
 */
public class MShareCrumbController {
	private static final String TAG = MShareCrumbController.class.getSimpleName();
	
	// Ĭ�ϵ�ָ���ַ
	private static final int POINTER_DEFAULT = -1;
	// ��ʼ��״̬�µ�ָ���ַ
	private static final int POINTER_INIT = 0;
	/**
	 * ��ǰջ����Ӧ���
	 */
	private int top = POINTER_DEFAULT;
	/**
	 * ��ǰ��ѡ��ĵ������ݵ����
	 */
	private int selected = POINTER_DEFAULT;
	// �����ӵ�еĵ�������������������ջ���
	private static final int COUNT_MAX = 10;
	// �����ļ���ջ
	private FileBrowserFile[] stack = null;
	// ջ���������
	private int maxCount = COUNT_MAX;
	/**
	 * ������һ��HorizontalScrollView�е�LinearLayout�������������м���������ݣ����е���ʽ�����Լ�����
	 */
	private LinearLayout container;
	// ���м�����ʱ�Ļص�����
	private OnCrumbClickListener listener;
	
	public MShareCrumbController(LinearLayout container, FileBrowserFile rootFile) {
		// ����ջ
		stack = new FileBrowserFile[maxCount];
		// ָ��container
		this.container = container;
		// ����Ŀ¼��ӵ�������
		push(rootFile);
		// ѡ���Ŀ¼
		select(0);
	}
	
	/**
	 * ���õ������ݣ�Ϊֻ�и�·���ĳ�ʼ״̬
	 */
	public void clean() {
		this.top = this.selected = POINTER_INIT;
	}
	/**
	 * ��ǰ���ڱ�ѡ���index
	 * @return
	 */
	public int getSelected() {
		return selected;
	}
	/**
	 * ���ѡ�����ļ�����
	 * @return
	 */
	public FileBrowserFile getSelectedFile() {
		return stack[selected];
	}
	
	/**
	 * �Ƿ���Խ�selected����������ݵ���
	 * @return
	 */
	public boolean canPop() {
		if (selected > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * �Ƿ���Խ�selected��������������ݵ���
	 * @return
	 */
	public boolean canPopUseless() {
		if (selected >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ��һ���µ�MShareFile��ջ
	 * @param file
	 */
	public int push(FileBrowserFile file) {
		// �������ݳ�ջ
		if (selected != top) {
			popUseless();
		}
		
		int index = selected + 1;
		
		// ����button
		Button button = getView(index, file.getName());
		container.addView(button, index);
		stack[index] = file;
		// ����top
		top = index;
		
		return index;	
	}
	
	/**
	 * ������ѡ��ĵ�������
	 */
	public int pop() {
		
		popUseless();

		int index = getSelected();
		unselect();
		stack[index] = null;
		container.removeViewAt(index);
		
		// ��������top
		top = index - 1;
		return index;
	}
	
	/**
	 * ��������������ݣ�ֱ����selected��������Ϊջ������
	 * @return
	 */
	public void popUseless() {
		if (selected >= 0) { // �����selected�������
			// ��ջ�е����ݳ�ջ
			for (int i = selected + 1; i <= top; i++) {
				stack[i] = null;
			}
			// ��crumb�е������˳�
			container.removeViews(selected + 1, top - selected);
			
			top = selected;
		}
	}
	
	/**
	 * ���õ�ǰ��ѡ���ĵ�������
	 * ��index����Ӧ����������Ϊѡ�еģ���������������Ϊ��ѡ�е�
	 * @param index ��Ӧ�����к�
	 */
	public void select(int index) {
		// Ҫ��û�б�ѡ�е���������Ϊѡ�е�
//		if (selected != index) {
			Log.v(TAG, "selected :" + selected);
			Log.v(TAG, "index :" + index);
			Button button = (Button)container.getChildAt(index);
			// ��Button����Ϊѡ��
			button.setBackgroundResource(R.drawable.crumbs_crumb_button_pressed);
			selected = index;
//		}
	}
	/**
	 * ����ǰ��ѡ�е���������Ϊ��ѡ��
	 */
	public void unselect() {
		if (selected >= 0) {
			Button button = (Button)container.getChildAt(selected);
			if (button != null) {
				// ��ν�button���óɲ�ѡ��
				button.setBackgroundResource(R.drawable.crumbs_crumb_button_normal);
			}
			selected = POINTER_DEFAULT;
		}
	}
	
	/**
	 * ���õ�crumb��button�������ʱ��Ļص�����
	 * @param listener �����õĻص��������ᶥ����һ���ص�����
	 */
	public void setOnCrumbClickListener(OnCrumbClickListener listener) {
		this.listener = listener;
	}
	
	/**
	 * ���һ��button�����������м������
	 * @return
	 */
	private Button getView(int index, String name) {
		Button button = new Button(container.getContext());
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		button.setLayoutParams(params);
		button.setBackgroundResource(R.drawable.crumbs_crumb_button_normal);
		
		button.setTag(index);
		button.setText(name);
		button.setOnClickListener(new OnClickListener());
		return button;
	}
	
	/**
	 * ʹ������ʵ�ּ������࣬��ִ�лص�����
	 */
	public interface OnCrumbClickListener {
		/**
		 * ����button�������ʱ�򣬽����øûص�����
		 * @param index
		 * @param name
		 */
		public void onCrumbClick(int selected, String name);
	}
	
	/**
	 * �������м�е�ÿ��Button�ĵ�����������
	 */
	private class OnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// 
			Button button = (Button)v;
			int selected = (Integer)(button.getTag());
			String name = button.getText().toString();
			unselect();
			select(selected);
			if (listener != null) {
				listener.onCrumbClick(selected, name);
			}
		}
	}
}
