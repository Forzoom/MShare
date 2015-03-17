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
 * 面包屑导航
 * TODO 可以使用button池来减少性能消耗
 * TODO 使用ArrayList来动态分配栈的容量
 * @author HM
 * 
 */
public class MShareCrumbController {
	private static final String TAG = MShareCrumbController.class.getSimpleName();
	
	// 默认的指针地址
	private static final int POINTER_DEFAULT = -1;
	// 初始化状态下的指针地址
	private static final int POINTER_INIT = 0;
	/**
	 * 当前栈顶对应序号
	 */
	private int top = POINTER_DEFAULT;
	/**
	 * 当前被选择的导航内容的序号
	 */
	private int selected = POINTER_DEFAULT;
	// 最多能拥有的导航内容数量，即最大的栈深度
	private static final int COUNT_MAX = 10;
	// 保存文件的栈
	private FileBrowserFile[] stack = null;
	// 栈的最大容量
	private int maxCount = COUNT_MAX;
	/**
	 * 包含在一个HorizontalScrollView中的LinearLayout，用来包含面包屑导航的内容，所有的样式都由自己控制
	 */
	private LinearLayout container;
	// 面包屑被点击时的回调函数
	private OnCrumbClickListener listener;
	
	public MShareCrumbController(LinearLayout container, FileBrowserFile rootFile) {
		// 生成栈
		stack = new FileBrowserFile[maxCount];
		// 指定container
		this.container = container;
		// 将根目录添加到导航中
		push(rootFile);
		// 选择根目录
		select(0);
	}
	
	/**
	 * 重置导航内容，为只有根路径的初始状态
	 */
	public void clean() {
		this.top = this.selected = POINTER_INIT;
	}
	/**
	 * 当前正在被选择的index
	 * @return
	 */
	public int getSelected() {
		return selected;
	}
	/**
	 * 获得选定的文件内容
	 * @return
	 */
	public FileBrowserFile getSelectedFile() {
		return stack[selected];
	}
	
	/**
	 * 是否可以将selected所代表的内容弹出
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
	 * 是否可以将selected后面所代表的内容弹出
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
	 * 将一个新的MShareFile入栈
	 * @param file
	 */
	public int push(FileBrowserFile file) {
		// 部分内容出栈
		if (selected != top) {
			popUseless();
		}
		
		int index = selected + 1;
		
		// 创建button
		Button button = getView(index, file.getName());
		container.addView(button, index);
		stack[index] = file;
		// 设置top
		top = index;
		
		return index;	
	}
	
	/**
	 * 弹出被选择的导航内容
	 */
	public int pop() {
		
		popUseless();

		int index = getSelected();
		unselect();
		stack[index] = null;
		container.removeViewAt(index);
		
		// 重新设置top
		top = index - 1;
		return index;
	}
	
	/**
	 * 弹出多个导航内容，直到将selected的内容作为栈顶内容
	 * @return
	 */
	public void popUseless() {
		if (selected >= 0) { // 合理的selected的情况下
			// 将栈中的内容出栈
			for (int i = selected + 1; i <= top; i++) {
				stack[i] = null;
			}
			// 将crumb中的内容退出
			container.removeViews(selected + 1, top - selected);
			
			top = selected;
		}
	}
	
	/**
	 * 设置当前被选定的导航内容
	 * 将index所对应的内容设置为选中的，其他的内容设置为不选中的
	 * @param index 对应的序列号
	 */
	public void select(int index) {
		// 要将没有被选中的内容设置为选中的
//		if (selected != index) {
			Log.v(TAG, "selected :" + selected);
			Log.v(TAG, "index :" + index);
			Button button = (Button)container.getChildAt(index);
			// 将Button设置为选中
			button.setBackgroundResource(R.drawable.crumbs_crumb_button_pressed);
			selected = index;
//		}
	}
	/**
	 * 将当前被选中的内容设置为不选中
	 */
	public void unselect() {
		if (selected >= 0) {
			Button button = (Button)container.getChildAt(selected);
			if (button != null) {
				// 如何将button设置成不选中
				button.setBackgroundResource(R.drawable.crumbs_crumb_button_normal);
			}
			selected = POINTER_DEFAULT;
		}
	}
	
	/**
	 * 设置当crumb的button被点击的时候的回调函数
	 * @param listener 所设置的回调函数，会顶替上一个回调内容
	 */
	public void setOnCrumbClickListener(OnCrumbClickListener listener) {
		this.listener = listener;
	}
	
	/**
	 * 获得一个button对象，用于面包屑的内容
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
	 * 使用者所实现监听器类，来执行回调内容
	 */
	public interface OnCrumbClickListener {
		/**
		 * 当有button被点击的时候，将调用该回调函数
		 * @param index
		 * @param name
		 */
		public void onCrumbClick(int selected, String name);
	}
	
	/**
	 * 对于面包屑中的每个Button的单击监听器类
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
