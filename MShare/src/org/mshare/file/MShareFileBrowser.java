package org.mshare.file;

import java.io.File;

import org.mshare.main.*;
import org.mshare.file.FileAdapter.ItemContainer;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.app.Activity;
import android.content.Context;
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
public class MShareFileBrowser implements MShareCrumbController.OnItemClickListener {

	private static final String TAG = "MShareFileBrowser";
	
	private Context context = null;
	private ViewGroup container = null;
	/**
	 * 面包屑导航的控制器
	 */
	private MShareCrumbController mShareCrumbs = null;
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
	
	public MShareFileBrowser(Context context, ViewGroup container) {
		this.context = context;
		this.container = container;
	}
	
	public View getView() {
		// file browser view
		View view = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);

		// set back button
		backBtn = (Button)(view.findViewById(R.id.crumb_back_button));
		backBtn.setOnClickListener(new BackBtnListener(context));
		
		// 根目录路径，即扩展存储路径
		File root = Environment.getExternalStorageDirectory();
		
		LinearLayout crumbContainer = (LinearLayout)(view.findViewById(R.id.crumb_container));
		
		// 面包屑导航控制器
		mShareCrumbs = new MShareCrumbController(context, root, crumbContainer);
		mShareCrumbs.setOnItemClickListener(this);
		
		// 获得根目录下的文件列表
		MShareFile[] files = mShareCrumbs.getFiles();
		// create grid view
		gridView = (GridView)(view.findViewById(R.id.grid_view));
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
		
		// register context menu
		((Activity)context).registerForContextMenu(gridView);
		
		// check external storage useful
		if (!isExternalStorageUseful()) {
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
	 * 检测扩展存储是否有效
	 * @return 成功时返回true，否则返回false
	 */
	public boolean isExternalStorageUseful() {
		String state = Environment.getExternalStorageState();
		// 仅仅当扩展存储可读写的时候才算有效
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 刷新用的主要方法
	 */
	public void refresh() {
		if (isExternalStorageUseful()) {
			refreshGridView();
		} else {
			mShareCrumbs.clean();
			refreshGridView(new MShareFile[0]);
			Toast.makeText(context, "扩展存储不可用", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * refresh gridview
	 */
	public void refreshGridView() {
		refreshGridView(mShareCrumbs.get());
	}
	/**
	 * refresh gridview
	 * @param file
	 */
	public void refreshGridView(MShareFile file) {
		refreshGridView(file.getSubFiles());
	}
	
	/**
	 * reset the adapter of grid view 
	 * @param files
	 */
	public void refreshGridView(MShareFile[] files) {
		// 新的适配器，用于刷新GridView
		adapter = new FileAdapter(context, files);
		gridView.setAdapter(adapter);
		// 设置导航后退按钮的样式，即是否可以被按下
		if (!mShareCrumbs.canPop()) {
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
		mShareCrumbs.push(file);
	}
	/**
	 * pop top crumb
	 * @return
	 */
	public void popCrumb() {
		mShareCrumbs.pop();
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
}
