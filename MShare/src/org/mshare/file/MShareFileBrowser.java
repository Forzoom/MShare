package org.mshare.file;

import java.io.File;
import org.mshare.main.*;
import org.mshare.file.FileAdapter.ItemContainer;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 入口类
 * @author HM
 *
 */
public class MShareFileBrowser extends BroadcastReceiver implements MShareCrumbController.OnItemClickListener {

	private static final String TAG = "MShareFileBrowser";
	
	private Context context = null;
	private ViewGroup container = null;
	/**
	 * 面包屑导航的控制器
	 */
	private MShareCrumbController crumbController = null;
	/**
	 * GridView所对应的适配器
	 */
	private FileAdapter adapter = null;
	/**
	 * 主要显示的GridView
	 */
	private GridView gridView = null;
	/**
	 * 后退按钮
	 */
	private Button backBtn = null;
	private MShareFile rootFile;
	private boolean enable = false;
	
	public MShareFileBrowser(Context context, ViewGroup container) {
		this.context = context;
		this.container = container;
	}
	
	public View getView() {
		// 文件浏览器布局
		View view = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);

		// 设置后退按钮
		backBtn = (Button)(view.findViewById(R.id.crumb_back_button));
		backBtn.setOnClickListener(new BackBtnListener(context));
		
		// 根目录路径，即扩展存储路径
		rootFile = new MShareFile(Environment.getExternalStorageDirectory());
		
		LinearLayout crumbContainer = (LinearLayout)(view.findViewById(R.id.crumb_container));
		
		// 面包屑导航控制器
		crumbController = new MShareCrumbController(context, rootFile, crumbContainer);
		crumbController.setOnItemClickListener(this);
		
		// 获得根目录下的文件列表
		MShareFile[] files = crumbController.getFiles();
		// create grid view
		gridView = (GridView)(view.findViewById(R.id.grid_view));
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
		
		// register context menu
		((Activity)context).registerForContextMenu(gridView);
		
		// check external storage useful
		if (!isEnabled()) {
			Toast.makeText(context, "扩展存储不可用", Toast.LENGTH_SHORT).show();
			return null;
		} else {
			// set adapter
			adapter = new FileAdapter(context, files); 
			gridView.setAdapter(adapter);
			return view;
		}
	}
	/**
	 * 检查是否可用
	 * @return
	 */
	public boolean checkEnable() {
		String state = Environment.getExternalStorageState();
		this.enable = state.equals(Environment.MEDIA_MOUNTED);
		// 仅仅当扩展存储可读写的时候才算有效
		return isEnabled();
	}
	
	/**
	 * 检测扩展存储是否可用
	 * @return
	 */
	public boolean isEnabled() {
		return this.enable;
	}
	/**
	 * 手动设置是否可用
	 * @param enable
	 */
	public void setEnabled(boolean enable) {
		this.enable = enable;
	}
	
	/**
	 * 刷新用的主要方法
	 */
	public void refresh() {
		if (isEnabled()) {
			refreshGridView();
		} else {
			
			crumbController.clean();
			// 将GridView中的内容设置为空
			refreshGridView(new MShareFile[0]);
			Toast.makeText(context, "扩展存储不可用", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 刷新GridView，默认使用面包屑导航中的内容
	 */
	public void refreshGridView() {
		refreshGridView(crumbController.getSelectedFile());
	}
	/**
	 * 刷新GridView
	 * @param file
	 */
	public void refreshGridView(MShareFile file) {
		refreshGridView(file.getSubFiles());
	}
	
	/**
	 * 刷新GridView，重置适配器 
	 * @param files
	 */
	public void refreshGridView(MShareFile[] files) {
		// 新的适配器，用于刷新GridView
		adapter = new FileAdapter(context, files);
		gridView.setAdapter(adapter);
		
		// 设置导航后退按钮的样式，即是否可以被按下
		if (!crumbController.canPop()) {
			backBtn.setClickable(false);
		} else {
			backBtn.setClickable(true);
		}
	}
	
	/**
	 * 向面包屑导航中添加新的导航内容
	 * @param file 添加到面包屑导航中的新内容
	 */
	public void pushCrumb(MShareFile file) {
		int index = crumbController.push(file);
		crumbController.select(index);
	}
	/**
	 * 弹出当前选择的面包屑导航
	 * @return
	 */
	public void popCrumb() {
		int index = crumbController.pop();
		crumbController.select(index - 1);
	}
	
	/**
	 * 用于响应当面包屑导航中的内容被点击时的事件
	 * @param selected
	 * @param name
	 */
	@Override
	public void onClick(int selected, String name) {
		// TODO Auto-generated method stub
//		this.selected = selected;
		refreshGridView();
	}
	
	
	/**
	 * 用于相应GridView中的button的响应事件
	 * @author HM
	 *
	 */
	private class GridViewItemClickListener implements AdapterView.OnItemClickListener {
		
		private Context context = null;
		
		public GridViewItemClickListener(Context context) {
			this.context = context;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			Object tag = view.getTag();
			
			if (tag != null) {
				ItemContainer item = (ItemContainer)tag; 
				MShareFile file = item.file;
				if (file.isDirectory()) { // whether is a directory
					if (file.getSubFiles() != null) {
						pushCrumb(file);
						refreshGridView(file.getSubFiles());
					} else {
						// cannot open the directory
						Toast.makeText(context, "文件夹无法访问", Toast.LENGTH_LONG).show();
					}
				} else {
					// is file, do nothing
				}
			} else {
				// error
			}
		}
	}
	
	/**
	 * temp listener for crumb last button
	 * @author HM
	 *
	 */
	private class BackBtnListener implements View.OnClickListener {

		private Context context = null;
		
		public BackBtnListener(Context context) {
			this.context = context;
		}
		
		@Override
		public void onClick(View v) {
			Log.v(TAG, "CrumbClickListener");
			popCrumb();
			refreshGridView();
		}
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // 扩展卡被拔出
			setEnabled(false);
			refresh();
		} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // 扩展卡可以使用
			setEnabled(true);
			refresh();
		}
	}	
}
