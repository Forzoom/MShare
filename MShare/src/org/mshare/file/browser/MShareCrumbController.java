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
	// �����ļ���ջ
	private ArrayList<FileBrowserFile> stack = new ArrayList<FileBrowserFile>();;
	/**
	 * ������һ��HorizontalScrollView�е�LinearLayout�������������м���������ݣ����е���ʽ�����Լ�����
	 */
	private LinearLayout layout;
	// ���м�����ʱ�Ļص�����
	private FileBrowserCallback callback;
	
	private MShareFileBrowser fileBrowser;
	
	/**
	 * 
	 * @param layout
	 * @param rootFile ��������Ϊ��rootFile�����м�������ʱ����Ҫ��ӦonCrumbClick�¼�
	 */
	public MShareCrumbController(LinearLayout layout, MShareFileBrowser fileBrowser) {
		this.fileBrowser = fileBrowser;
		// ָ��container
		this.layout = layout;
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
		return stack.get(selected);
	}
	
	/**
	 * �Ƿ���Խ�selected����������ݵ���
	 * @return
	 */
	public boolean canPop() {
		return selected > 0;
	}
	
	/**
	 * �Ƿ���Խ�selected��������������ݵ���
	 * @return
	 */
	public boolean canPopUseless() {
		return selected > 0;
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
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		
		Log.d(TAG, "detect the layout weight sum : " + layout.getWeightSum());
		
		layout.addView(button, index, params);
		stack.add(index, file);
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
		stack.remove(index);
		layout.removeViewAt(index);
		
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
			Log.d(TAG, "try pop useless");
			// ��ջ�е����ݳ�ջ
			for (int i = selected + 1; i <= top; i++) {
				stack.remove(i);
			}
			// ��crumb�е������˳�
			layout.removeViews(selected + 1, top - selected);
			
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
			Button button = (Button)layout.getChildAt(index);
			// ��Button����Ϊѡ��
			button.setTextColor(layout.getContext().getResources().getColor(R.color.Color_White));
			button.setBackgroundColor(layout.getContext().getResources().getColor(R.color.blue08));
			selected = index;
//		}
	}
	/**
	 * ����ǰ��ѡ�е���������Ϊ��ѡ��
	 */
	public void unselect() {
		if (selected >= 0) {
			Button button = (Button)layout.getChildAt(selected);
			if (button != null) {
				// ��ν�button���óɲ�ѡ��
				button.setTextColor(layout.getContext().getResources().getColor(R.color.Color_Black));
				button.setBackgroundColor(layout.getContext().getResources().getColor(R.color.Color_White));
			}
			selected = POINTER_DEFAULT;
		}
	}
	
	/**
	 * ���õ�crumb��button�������ʱ��Ļص�����
	 * @param callback �����õĻص��������ᶥ����һ���ص�����
	 */
	public void setCallback(FileBrowserCallback callback) {
		this.callback = callback;
	}
	
	/**
	 * ���һ��button�����������м������
	 * @return
	 */
	private Button getView(int index, String name) {
		Button button = new Button(layout.getContext());

		button.setTextColor(layout.getContext().getResources().getColor(R.color.Color_Black));
		button.setBackgroundColor(layout.getContext().getResources().getColor(R.color.Color_White));

		button.setTag(index);
		button.setText(name);
		button.setSingleLine(true);
		button.setOnClickListener(new OnClickListener());
		return button;
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
			
			fileBrowser.waitForRefresh();
			
			if (callback != null) {
				callback.onCrumbClick(stack.get(MShareCrumbController.this.selected));
			}
		}
	}
}
