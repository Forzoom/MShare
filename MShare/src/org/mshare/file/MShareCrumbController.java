package org.mshare.file;

import java.io.File;
import java.util.ArrayList;
import org.mshare.main.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * ���м����
 * @author HM
 *
 */
public class MShareCrumbController {

	private static final String TAG = "MShareCrumbController";
	
	/**
	 * �����Ķ���
	 */
	private Context context = null;
	/**
	 * 
	 */
	private static final int POINTER_DEFAULT = -1;
	private static final int POINTER_INIT = 0;
	/**
	 * ��ǰջ������Ӧ������Ƕ���
	 */
	private int top = POINTER_DEFAULT;
	/**
	 * ��ǰ���ڱ�ѡ��ĵ������ݵ����
	 */
	private int selected = POINTER_DEFAULT;
	
	/**
	 * ������ܹ�ӵ�еĵ�����������
	 */
	private static final int COUNT_MAX = 10;
	
	/**
	 * the stack for saving files(to be exact, directory)
	 */
	private MShareFile[] stack = null;
	/**
	 * the capacity of stack
	 */
	private int maxDepth = COUNT_MAX;
	/**
	 * path the crumbs represent
	 */
	private String path = null;
	/**
	 * ������һ��HorizontalScrollView�е�LinearLayout�������������м���������ݣ����е���ʽ�����Լ�����
	 */
	private LinearLayout container = null;

	private OnItemClickListener listener = null;
	
	public MShareCrumbController(Context context, File file, LinearLayout container) {
		this.context = context;
		
		// ����ջ
		stack = new MShareFile[maxDepth];
		
		// ָ��container
		this.container = container;
		
		// ����Ŀ¼��ӵ�������
		push(new MShareFile(file.getAbsolutePath()));
	}
	
	/**
	 * ���õ����е���������
	 */
	public void clean() {
		this.top = this.selected = POINTER_INIT;
		refreshPath();
	}
	
	/**
	 * ���ѡ�����ļ�����
	 * @return
	 */
	public MShareFile get() {
		return stack[selected];
	}
	/**
	 * ���һ��button����
	 * @return
	 */
	private Button getView(int index, String name) {
		Button button = new Button(context);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		button.setLayoutParams(params);
		button.setBackgroundResource(R.drawable.crumbs_crumb_button);
		
		button.setTag(index);
		button.setText(name);
		button.setOnClickListener(new OnClickListener());
		return button;
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
	 * ��һ���µ�`MShareFile`��ջ
	 * @param file
	 */
	public void push(MShareFile file) {
		// �������ݳ�ջ
		if (selected != top) {
			popUseless();
		}
		
		// ����button
		Button button = getView(selected + 1, file.getName());
		container.addView(button, selected + 1);
		
		stack[selected + 1] = file;
		
		unselect();
		select(selected + 1);
		
		// ����top
		this.top = this.selected;	
	}
	
	/**
	 * ����ջ���������� 
	 * @return
	 */
	public void pop() {
		if (selected > 0) {
			// �������ݳ�ջ
			popUseless();
			// selected���ݳ�ջ
			stack[selected] = null;
			
			// ����container�е���ʽ
			container.removeViewAt(selected);
			
			// ���¼���top��selected
			// �����µ�ѡ�����
			select(selected - 1);
			// ��������top
			top = selected;
		}
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
		if (selected != index) {
			Log.v(TAG, "selected :" + selected);
			Log.v(TAG, "index :" + index);
			Button button = (Button)container.getChildAt(index);
			// ��Button����Ϊδѡ��
			
			button.setBackgroundResource(R.drawable.crumbs_crumb_button_pressed);
			selected = index;
		}
	}
	/**
	 * ����ǰ��ѡ�е���������Ϊ��ѡ��
	 */
	public void unselect() {
		if (selected >= 0) {
			Button button = (Button)container.getChildAt(selected);
			// ��ν�button���óɲ�ѡ��
			button.setBackgroundResource(R.drawable.crumbs_crumb_button);
		}
	}
	/**
	 * get files in current maxDepth level 
	 * @return selected files or null
	 */
	public MShareFile[] getFiles() {
		if (selected < maxDepth && stack[selected] != null) {
			return stack[selected].getSubFiles();
		}
		return null;
	}
	
	/**
	 * ��õ�ǰ��������Ӧ���ļ�·��
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * ˢ��·������
	 */
	private void refreshPath() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0, len = this.selected; i <= len; i++) {
			list.add(this.stack[i].getName());
		}
		this.path = TextUtils.join(File.separator, list);
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}
	/**
	 * ʵ�ּ������࣬��ִ�лص�����
	 * @author HM
	 *
	 */
	public interface OnItemClickListener {
		/**
		 * ����button�������ʱ�򣬽����øûص�����
		 * @param index
		 * @param name
		 */
		public void onClick(int selected, String name);
	}
	
	/**
	 * ����ÿ��button�ĵ�����������
	 * @author HM
	 *
	 */
	private class OnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Button button = (Button)v;
			int selected = (Integer)(button.getTag());
			String name = button.getText().toString();
			unselect();
			select(selected);
			if (listener != null) {
				listener.onClick(selected, name);
			}
		}
	}
}
