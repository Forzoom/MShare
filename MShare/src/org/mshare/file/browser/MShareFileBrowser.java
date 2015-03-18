package org.mshare.file.browser;

import java.util.Map;

import org.mshare.main.*;
import org.mshare.file.browser.MShareFileAdapter.ItemContainer;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * 文件浏览器
 * 包含GridView中图标的点击事件 {@link GridViewItemClickListener}
 * 完成了面包屑导航中事件，当点击文件夹的时候自动进入文件夹
 * @author HM
 *
 */
public class MShareFileBrowser extends LinearLayout {
	private static final String TAG = MShareFileBrowser.class.getSimpleName();

	private Context context;
	
	// 用于包含文件浏览器的container
	private ViewGroup container = null;
	/**
	 * 面包屑导航的控制器
	 */
	private MShareCrumbController crumbController;
	/**
	 * GridView所对应的适配器
	 */
	private MShareFileAdapter adapter;
	/**
	 * 主要显示的GridView
	 */
	private GridView gridView;
	/**
	 * 后退按钮
	 */
	private ImageView backButton;
	/**
	 * 当前显示的内容
	 */
	private FileBrowserFile[] currentFiles;
	
	// 回调函数
	private FileBrowserCallback callback;

	private View fileBrowserLayout;
	
	private RelativeLayout.LayoutParams coverLayoutParam;
	private LinearLayout cover;
	private RelativeLayout gridViewContainer;
	
	private boolean enable;
	private boolean isWaitForRefresh = false;
	
	public MShareFileBrowser(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		prepare();
	}

	public MShareFileBrowser(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		prepare();
	}

	public MShareFileBrowser(Context context) {
		super(context);
		this.context = context;
		prepare();
	}
	
	// 需要手动调用
	public void prepare() {
		// 需要调用
		setEnabled(true);
		
		// 文件浏览器布局
		fileBrowserLayout = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);
		
		// 设置后退按钮
		backButton = (ImageView)(fileBrowserLayout.findViewById(R.id.crumb_back_button));
		backButton.setOnClickListener(new BackButtonListener());
		
		// 面包屑导航布局
		LinearLayout crumbContainer = (LinearLayout)(fileBrowserLayout.findViewById(R.id.crumb_container));
		// 面包屑导航控制器
		crumbController = new MShareCrumbController(crumbContainer, this);
		if (callback != null) {
			crumbController.setCallback(callback);
		}
		
		// 创建GridView
		gridView = (GridView)(fileBrowserLayout.findViewById(R.id.file_browser_grid_view));
		gridView.setOnItemLongClickListener(new GridViewItemLongClickListener());
		gridView.setOnItemClickListener(new GridViewItemClickListener());
		
		// GridView的容器
		gridViewContainer = (RelativeLayout)fileBrowserLayout.findViewById(R.id.file_browser_grid_view_container);
		
		// 创建cover
		cover = new LinearLayout(context);
		coverLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

		// fileBrowserLayout中所有的view都处理过后再添加
		addView(fileBrowserLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
	}
	
	/**
	 * 设置后会等待刷新
	 * @param file
	 */
	public void setRootFile(FileBrowserFile file) {
		crumbController.push(file);
		crumbController.select(0);
		waitForRefresh();
	}
	
	/**
	 * 设置当前的文件浏览器是否可用
	 * 一般在扩展存储不可用的时候，将文件浏览器设置为不可用
	 * @return
	 */
	public boolean isEnabled() {
		return this.enable;
	}
	/**
	 * TODO 需要一个机制来设置Activity并不能使用，当扩展存储没有的时候该怎么办？
	 * @param enable
	 */
	public void setEnabled(boolean enable) {
		this.enable = enable;
	}

	public void waitForRefresh() {
		gridViewContainer.addView(cover, coverLayoutParam);
		isWaitForRefresh = true;
	}
	
	/**
	 * 刷新GridView，重置适配器
	 * @param currentFiles
	 */
	public void refreshGridView(FileBrowserFile[] files) {
		
		if (!isEnabled()) {
			crumbController.clean();
			// 将GridView中的内容置空
			files = new FileBrowserFile[0];
		} else {
			// 暂时先放在这里
			if (isWaitForRefresh) {
				gridViewContainer.removeView(cover);
			}
		}
		
		// 设置当前正在文件浏览器中的内容
		this.currentFiles = files;
		
		// 新的适配器，用于刷新GridView
		
		// 使用的可能是不合适的Context
		adapter = new MShareFileAdapter(MShareApp.getAppContext(), files);
		gridView.setAdapter(adapter);
		
		// 设置导航后退按钮的样式，即是否可以被按下
		if (!crumbController.canPop()) {
			backButton.setClickable(false);
		} else {
			backButton.setClickable(true);
		}
	}
	
	/**
	 * 向面包屑导航中添加新的导航内容
	 * @param file 添加到面包屑导航中的新内容
	 */
	public void pushCrumb(FileBrowserFile file) {
		int index = crumbController.push(file);
		crumbController.unselect();
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
	
	public GridView getGridView() {
		return gridView;
	}

	public FileBrowserFile[] getCurrentFiles() {
		return currentFiles;
	}
	
	// 设置callback
	public void setCallback(FileBrowserCallback callback) {
		this.callback = callback;
		// 暂时先这样，为了和init中无论是谁被调用的情况下，都能让crumbController被设置callback
		if (crumbController != null) {
			crumbController.setCallback(callback);
		}
	}
	
	/**
	 * 用于相应GridView中Item的响应事件
	 */
	private class GridViewItemClickListener implements AdapterView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemClick invoke!");
			Object tag = view.getTag();
			
			if (tag == null) {
				Log.e(TAG, "tag是null");
				return;
			}

			ItemContainer item = (ItemContainer)tag; 
			FileBrowserFile file = item.file;
			if (file.isDirectory()) { // whether is a directory
				
				if (file != null && file.canRead()) { // 文件夹可以打开
					pushCrumb(file);
					waitForRefresh();
					if (callback != null) {
						callback.onItemClick(file);
					}
				} else {
					Log.e(TAG, "文件夹无法访问");
				}
			} else {
				Log.d(TAG, "所点击的是一个文件");
				if (callback != null) {
					callback.onItemClick(file);
				}
			}
			
		}
	}
	
	private class GridViewItemLongClickListener implements AdapterView.OnItemLongClickListener {

		// 设置当前被选择的文件
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemLongClick invoke!");
			FileBrowserFile file = currentFiles[position];
			Log.d(TAG, "set select file : " + file.getAbsolutePath());

			if (callback != null) {
				callback.onItemLongClick(file);
			}
			
			return false;
		}
		
	}
	
	/**
	 * 后退按钮的监听器
	 * @author HM
	 *
	 */
	private class BackButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Log.v(TAG, "crumb item is clicked");
			popCrumb();
			waitForRefresh();
			if (callback != null) {
				callback.onBackButtonClick(crumbController.getSelectedFile());
			}
		}
		
	}

	// TODO 需要修正了
	/**
	 * <p>监听扩展存储的状态</p>
	 * TODO 将来将使用{@link ExternalStorageStatusReceiver}来代替
	 */
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // 扩展卡被拔出
			setEnabled(false);
			refreshGridView(new FileBrowserFile[0]);
		} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // 扩展卡可以使用
			setEnabled(true);
			refreshGridView(FileBrowserActivity.listFiles(crumbController.getSelectedFile()));
		}
	}	
}
