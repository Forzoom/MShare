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
import android.widget.HorizontalScrollView;
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
	
	private HorizontalScrollView scrollView;
	
	/**
	 * 
	 * @param layout
	 * @param rootFile ��������Ϊ��rootFile�����м�������ʱ����Ҫ��ӦonCrumbClick�¼�
	 */
	public MShareCrumbController(HorizontalScrollView scrollView, LinearLayout layout, MShareFileBrowser fileBrowser) {
		
		Log.d(TAG, scrollView + "");
		
		this.fileBrowser = fileBrowser;
		this.scrollView = scrollView;
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
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		layout.addView(button, index, params);

		Log.d(TAG, "scrollView : " + scrollView.getWidth());
		Log.d(TAG, "layout width : " + layout.getWidth());
		// �����Ƿ��ܹ����button��x��Ϣ
		Log.d(TAG, "new button x : " + button.getX() + " and button width : " + button.getWidth());
		
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
		unselectCrumb();
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
	public void selectCrumb(int index) {
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
	public void unselectCrumb() {
		if (selected >= 0) {
			Button button = (Button)layout.getChildAt(selected);
			if (button != null) {
				// ��ν�button���óɲ�ѡ��
				button.setTextColor(layout.getContext().getResources().getColor(R.color.Color_Black));
				button.setBackgroundColor(layout.getContext().getResources().getColor(R.color.color_transparent));
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
		button.setBackgroundColor(layout.getContext().getResources().getColor(R.color.color_transparent));

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
			unselectCrumb();
			selectCrumb(selected);
			
			float measureWidthSequence = button.getPaint().measureText(button.getText(), 0, button.getText().length()) + button.getPaddingLeft() + button.getPaddingRight();
			float measureWidth = button.getPaint().measureText(button.getText().toString()) + button.getPaddingLeft() + button.getPaddingRight();
			Log.d(TAG, "button padding left " + button.getPaddingLeft() + " padding right : " + button.getPaddingRight());
			Log.d(TAG, "scrollView : " + scrollView.getWidth());
			Log.d(TAG, "try for button x : " + v.getX() + " and button width : " + v.getWidth());
			Log.d(TAG, "widthSequence : " + measureWidthSequence + " width : " + measureWidth);
			
			fileBrowser.waitForRefresh();
			
			if (callback != null) {
				callback.onCrumbClick(stack.get(MShareCrumbController.this.selected));
			}
		}
	}
}
