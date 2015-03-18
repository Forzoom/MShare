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
	// 保存文件的栈
	private ArrayList<FileBrowserFile> stack = new ArrayList<FileBrowserFile>();;
	/**
	 * 包含在一个HorizontalScrollView中的LinearLayout，用来包含面包屑导航的内容，所有的样式都由自己控制
	 */
	private LinearLayout layout;
	// 面包屑被点击时的回调函数
	private FileBrowserCallback callback;
	
	private MShareFileBrowser fileBrowser;
	
	/**
	 * 
	 * @param layout
	 * @param rootFile 存在是因为当rootFile的面包屑被点击的时候，需要相应onCrumbClick事件
	 */
	public MShareCrumbController(LinearLayout layout, MShareFileBrowser fileBrowser) {
		this.fileBrowser = fileBrowser;
		// 指定container
		this.layout = layout;
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
		return stack.get(selected);
	}
	
	/**
	 * 是否可以将selected所代表的内容弹出
	 * @return
	 */
	public boolean canPop() {
		return selected > 0;
	}
	
	/**
	 * 是否可以将selected后面所代表的内容弹出
	 * @return
	 */
	public boolean canPopUseless() {
		return selected > 0;
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
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		
		Log.d(TAG, "detect the layout weight sum : " + layout.getWeightSum());
		
		layout.addView(button, index, params);
		stack.add(index, file);
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
		stack.remove(index);
		layout.removeViewAt(index);
		
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
			Log.d(TAG, "try pop useless");
			// 将栈中的内容出栈
			for (int i = selected + 1; i <= top; i++) {
				stack.remove(i);
			}
			// 将crumb中的内容退出
			layout.removeViews(selected + 1, top - selected);
			
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
			Button button = (Button)layout.getChildAt(index);
			// 将Button设置为选中
			button.setTextColor(layout.getContext().getResources().getColor(R.color.Color_White));
			button.setBackgroundColor(layout.getContext().getResources().getColor(R.color.blue08));
			selected = index;
//		}
	}
	/**
	 * 将当前被选中的内容设置为不选中
	 */
	public void unselect() {
		if (selected >= 0) {
			Button button = (Button)layout.getChildAt(selected);
			if (button != null) {
				// 如何将button设置成不选中
				button.setTextColor(layout.getContext().getResources().getColor(R.color.Color_Black));
				button.setBackgroundColor(layout.getContext().getResources().getColor(R.color.Color_White));
			}
			selected = POINTER_DEFAULT;
		}
	}
	
	/**
	 * 设置当crumb的button被点击的时候的回调函数
	 * @param callback 所设置的回调函数，会顶替上一个回调内容
	 */
	public void setCallback(FileBrowserCallback callback) {
		this.callback = callback;
	}
	
	/**
	 * 获得一个button对象，用于面包屑的内容
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
			
			fileBrowser.waitForRefresh();
			
			if (callback != null) {
				callback.onCrumbClick(stack.get(MShareCrumbController.this.selected));
			}
		}
	}
}
