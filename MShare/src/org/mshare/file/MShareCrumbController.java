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
 * 面包屑导航
 * @author HM
 *
 */
public class MShareCrumbController {

	private static final String TAG = "MShareCrumbController";
	
	/**
	 * 上下文对象
	 */
	private Context context = null;
	/**
	 * 
	 */
	private static final int POINTER_DEFAULT = -1;
	private static final int POINTER_INIT = 0;
	/**
	 * 当前栈顶所对应的序号是多少
	 */
	private int top = POINTER_DEFAULT;
	/**
	 * 当前正在被选择的导航内容的序号
	 */
	private int selected = POINTER_DEFAULT;
	
	/**
	 * 最多所能够拥有的导航内容数量
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
	 * 包含在一个HorizontalScrollView中的LinearLayout，用来包含面包屑导航的内容，所有的样式都由自己控制
	 */
	private LinearLayout container = null;

	private OnItemClickListener listener = null;
	
	public MShareCrumbController(Context context, File file, LinearLayout container) {
		this.context = context;
		
		// 生成栈
		stack = new MShareFile[maxDepth];
		
		// 指定container
		this.container = container;
		
		// 将根目录添加到导航中
		push(new MShareFile(file.getAbsolutePath()));
	}
	
	/**
	 * 重置导航中的所有内容
	 */
	public void clean() {
		this.top = this.selected = POINTER_INIT;
		refreshPath();
	}
	
	/**
	 * 获得选定的文件内容
	 * @return
	 */
	public MShareFile get() {
		return stack[selected];
	}
	/**
	 * 获得一个button对象
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
	 * 将一个新的`MShareFile`入栈
	 * @param file
	 */
	public void push(MShareFile file) {
		// 部分内容出栈
		if (selected != top) {
			popUseless();
		}
		
		// 创建button
		Button button = getView(selected + 1, file.getName());
		container.addView(button, selected + 1);
		
		stack[selected + 1] = file;
		
		unselect();
		select(selected + 1);
		
		// 设置top
		this.top = this.selected;	
	}
	
	/**
	 * 弹出栈顶导航内容 
	 * @return
	 */
	public void pop() {
		if (selected > 0) {
			// 额外内容出栈
			popUseless();
			// selected内容出栈
			stack[selected] = null;
			
			// 设置container中的样式
			container.removeViewAt(selected);
			
			// 重新计算top和selected
			// 设置新的选择对象
			select(selected - 1);
			// 重新设置top
			top = selected;
		}
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
		if (selected != index) {
			Log.v(TAG, "selected :" + selected);
			Log.v(TAG, "index :" + index);
			Button button = (Button)container.getChildAt(index);
			// 将Button设置为未选中
			
			button.setBackgroundResource(R.drawable.crumbs_crumb_button_pressed);
			selected = index;
		}
	}
	/**
	 * 将当前被选中的内容设置为不选中
	 */
	public void unselect() {
		if (selected >= 0) {
			Button button = (Button)container.getChildAt(selected);
			// 如何将button设置成不选中
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
	 * 获得当前导航所对应的文件路径
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 刷新路径内容
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
	 * 实现监听器类，来执行回调内容
	 * @author HM
	 *
	 */
	public interface OnItemClickListener {
		/**
		 * 当有button被点击的时候，将调用该回调函数
		 * @param index
		 * @param name
		 */
		public void onClick(int selected, String name);
	}
	
	/**
	 * 对于每个button的单击监听器类
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
