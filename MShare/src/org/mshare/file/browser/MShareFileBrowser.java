package org.mshare.file.browser;

import java.util.ArrayList;
import java.util.Map;

import org.mshare.main.*;
import org.mshare.file.browser.MShareFileAdapter.ItemContainer;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.app.Activity;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * 文件浏览器
 * 包含GridView中图标的点击事件 {@link GridViewItemClickListener}
 * 完成了面包屑导航中事件，当点击文件夹的时候自动进入文件夹
 * 
 * contextmenu可能在single_select的模式下可以支持
 * 
 * 该如何退出multi_select_mode的时候将所有的fileIcon都还原呢？
 * 
 * 目前对于单选择来说并没有合适的方法，可能只需要几个回调方法就可以了
 * 
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
	private ImageButton backButton;
	
	private ImageButton refreshButton;
	
	private Animation refreshAnimation;
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
	
	private boolean isMultiSelectEnabled = false;

	public static final int MODE_SINGLE_SELECT = 1;
	public static final int MODE_MULTI_SELECT = 2;
	
	private int mode = MODE_SINGLE_SELECT;
	
	// 只有在LongClick的时候才会被处理
	private int selectPosition;
	// 暂时先使用boolean
	private boolean[] multiSelectPosition;
	
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
		backButton = (ImageButton)(fileBrowserLayout.findViewById(R.id.crumb_back_button));
		backButton.setOnClickListener(new BackButtonListener());
		backButton.setClickable(false);
		
		// 刷新按钮
		refreshButton = (ImageButton)(fileBrowserLayout.findViewById(R.id.file_browser_refresh_button));
		refreshButton.setOnClickListener(new RefreshButtonListener());
		
		// 刷新动画
		refreshAnimation = AnimationUtils.loadAnimation(context, R.anim.file_browser_refresh);
		
		// 面包屑导航布局
		LinearLayout crumbContainer = (LinearLayout)(fileBrowserLayout.findViewById(R.id.crumb_container));

		// TODO 用于处理smoothScroll的位置
		HorizontalScrollView scrollView = (HorizontalScrollView)fileBrowserLayout.findViewById(R.id.crumb_scroller);
		
		// 面包屑导航控制器
		crumbController = new MShareCrumbController(scrollView, crumbContainer, this);
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
		cover.setClickable(true);
		cover.setLongClickable(true);
		coverLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

		// fileBrowserLayout中所有的view都处理过后再添加
		addView(fileBrowserLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
	}
	
	/**
	 * 设置后会等待刷新
	 * @param file
	 */
	public void setRootFile(FileBrowserFile file) {
		setRootFile(file, file.getName());
	}
	
	// 设置自定的名字
	public void setRootFile(FileBrowserFile file, String fileRootName) {
		crumbController.push(file, fileRootName);
		crumbController.selectCrumb(0);
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

	// 调用刷新动画
	public void waitForRefresh() {
		if (isWaitForRefresh) {
			Log.e(TAG, "is already waiting!");
			return;
		}
		// 启动刷新动画
		refreshButton.startAnimation(refreshAnimation);
		gridViewContainer.addView(cover, coverLayoutParam);
		
		Log.d(TAG, "waiting for refresh data");
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
			// 除去cover
			if (isWaitForRefresh) {
				refreshButton.clearAnimation();
				gridViewContainer.removeView(cover);
				isWaitForRefresh = false;
			}
		}
		
		// 设置当前正在文件浏览器中的内容
		this.currentFiles = files;
		
		// 重置multiSelect的内容
		multiSelectPosition = new boolean[files.length];
		// 重置mode
		setMode(MODE_SINGLE_SELECT);
		
		// 新的适配器，用于刷新GridView
		// 使用的可能是不合适的Context
		adapter = new MShareFileAdapter(MShareApp.getAppContext(), this);
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
		crumbController.unselectCrumb();
		crumbController.selectCrumb(index);
	}
	/**
	 * 弹出当前选择的面包屑导航
	 * @return
	 */
	public void popCrumb() {
		int index = crumbController.pop();
		crumbController.selectCrumb(index - 1);
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
	// 是否启动了multiSelect
	public boolean isMultiSelectEnabled() {
		return isMultiSelectEnabled;
	}
	// 设置是否启动
	public void setMultiSelectEnabled(boolean isMultiSelectEnabled) {
		
		// 当前正在多选模式,将退出多选模式
		if (isMultiSelectEnabled == false && mode == MODE_MULTI_SELECT) {
			for (int i = 0, len = multiSelectPosition.length; i < len; i++) {
				multiSelectPosition[i] = false;
			}
		}
		
		this.isMultiSelectEnabled = isMultiSelectEnabled;
	}
	
//	pub
	
	// TODO 需要保证在正确的模式下获得正确的内容
	// 应该只能在SINGLE_MODE下调用
	public int getSelectPosition() {
		return selectPosition;
	}

	public FileBrowserFile getSelectFile() {
		return currentFiles[selectPosition];
	}
	
	public FileBrowserFile[] getMultiSelectedFiles() {
		ArrayList<FileBrowserFile> arrayList = new ArrayList<FileBrowserFile>();
		for (int i = 0; i < multiSelectPosition.length; i++) {
			// 不知道没有初始化的情况下是不是false
			if (multiSelectPosition[i]) {
				arrayList.add(currentFiles[i]);
			}
		}
		FileBrowserFile[] ret = new FileBrowserFile[arrayList.size()];
		arrayList.toArray(ret);
		
		return ret;
	}
	
	public Integer[] getMultiSelectedPosition() {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int i = 0; i < multiSelectPosition.length; i++) {
			// 不知道没有初始化的情况下是不是false
			if (multiSelectPosition[i]) {
				arrayList.add(i);
			}
		}
		Integer[] ret = new Integer[arrayList.size()];
		arrayList.toArray(ret);
		return ret;
	}
	/**
	 * 判断文件是否被选择了，使用SINGLE和MULTI模式
	 * @param position
	 * @return
	 */
	public boolean isFileSelected(int position) {
		if (mode == MODE_MULTI_SELECT) {
			
			if (position < multiSelectPosition.length) {
				return multiSelectPosition[position];
			} else {
				Log.e(TAG, "something wrong may happen! the target position is not exists");
				return false;
			}
			
		} else if (mode == MODE_SINGLE_SELECT) {
			// TODO 对于单选确实不知道该怎么办才好,或者说将多选和单选合并
			return position == selectPosition;
		} else {
			Log.e(TAG, "unknown mode");
			return false;
		}
	}
	
	public void quitMultiSelectMode() {
		// 当前是多选模式
		if (this.mode == MODE_MULTI_SELECT) {
			if (adapter == null) {
				Log.e(TAG, "something must be wrong, the adapter is null!");
				return;
			}
			
			// 将当前已经选择的内容设置为Common
			for (int position = 0; position < multiSelectPosition.length; position++) {
				if (multiSelectPosition[position]) {
					ItemContainer item = adapter.getItemContainers(position);
					item.fileIcon.setImageDrawable(MShareFileAdapter.getCommonDrawable(currentFiles[position]));
				}
			}
			
			// 调整mode
			setMode(MODE_SINGLE_SELECT);
		}
	}

	// 获得当前文件浏览器在什么模式下
	public int getMode() {
		return mode;
	}
	
	private void setMode(int mode) {
		int currentMode = this.mode;
		if (mode == currentMode) {
			Log.w(TAG, "the same mode, and do nothing!");
			return;
		}
		
		if (mode == MODE_MULTI_SELECT) {
			if (isMultiSelectEnabled()) {
				// 清空选择内容
				selectPosition = -1;
				
				// 设置为多选
				this.mode = mode;
			} else {
				Log.e(TAG, "multiSelect mode is disabled");
				return;
			}
		} else if (mode == MODE_SINGLE_SELECT) {
			// 清空选择内容
			// 暂时先这样清空
			for (int i = 0; i < multiSelectPosition.length; i++) {
				multiSelectPosition[i] = false;
			}
			
			// 设置为单选
			this.mode = mode;
		}
	}
	
	// 将状态设置为选中，仅仅支持在多选模式下
	public boolean selectFile(int position) {
		if (getMode() == MODE_SINGLE_SELECT) {
			Log.w(TAG, "cannot invoke select in single select mode");
			return false;
		}
		if (multiSelectPosition[position]) {
			Log.w(TAG, "the file is already select! do nothing");
			return false;
		}
		
		// TODO 判断使用正确的tag/需要判断adapter是否是null?
		ItemContainer item = adapter.getItemContainers(position);
		FileBrowserFile file = currentFiles[position];
		ImageView fileIcon = item.fileIcon;
		
		fileIcon.setImageDrawable(MShareFileAdapter.getSelectedDrawable(file));
		multiSelectPosition[position] = true;
		return true;
	}
	
	// 将状态设置为未选中，仅仅支持在多选模式下
	public boolean unselectFile(int position) {
		if (getMode() == MODE_SINGLE_SELECT) {
			Log.w(TAG, "cannot invoke unselect in single select mode");
			return false;
		}
		if (!multiSelectPosition[position]) {
			Log.w(TAG, "the file is already unselect! do nothing");
			return false;
		}
		
		// TODO 判断使用正确的tag/需要判断adapter是否是null?
		ItemContainer item = adapter.getItemContainers(position);
		FileBrowserFile file = currentFiles[position];
		ImageView fileIcon = item.fileIcon;
		
		fileIcon.setImageDrawable(MShareFileAdapter.getUnselectedDrawable(file));
		multiSelectPosition[position] = false;
		return true;
	}
		
	/**
	 * 用于相应GridView中Item的响应事件
	 */
	private class GridViewItemClickListener implements AdapterView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemClick invoke!");
			
			FileBrowserFile file = currentFiles[position];
			
			if (isMultiSelectEnabled() && mode == MODE_MULTI_SELECT) {
				
				Log.d(TAG, "operate multi select mode");
				if (multiSelectPosition[position]) {
					// 文件已经被选中，所以将其设置为未选中，并从ArrayList中移除
					unselectFile(position);
				} else {
					// 文件未被选中，所以将其设置为选中，加入ArrayList
					selectFile(position);
				}

			} else {
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
	}
	
	private class GridViewItemLongClickListener implements AdapterView.OnItemLongClickListener {

		// 设置当前被选择的文件
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemLongClick invoke!");
			FileBrowserFile longClickFile = currentFiles[position];
			Log.d(TAG, "set select file : " + longClickFile.getAbsolutePath());

			// 判断是否要启动multiSelect
			// 当multiSelected被启动的时候，OnItemClick事件将不被调用
			if (isMultiSelectEnabled()) {
				
				if (mode == MODE_SINGLE_SELECT) {
					setMode(MODE_MULTI_SELECT);
					// 修改图片
					ItemContainer item = adapter.getItemContainers(position);
					item.fileIcon.setImageDrawable(MShareFileAdapter.getSelectedDrawable(longClickFile));
					// 记录被选择情况
					multiSelectPosition[position] = true;
					
					// 回调函数
					if (callback != null) {
						callback.onItemLongClick(currentFiles[position]);
					}
				} else if (mode == MODE_MULTI_SELECT) {
					
					if (multiSelectPosition[position]) {
						unselectFile(position);
					} else {
						selectFile(position);
					}
				}
				
			} else {// 不允许MultiSelected
				
				// TODO 这里设置的selectPosition好像没有用,OnItemClick的情况下也要修改selectPosition
				selectPosition = position;
				if (callback != null) {
					callback.onItemLongClick(longClickFile);
				}
			}
			
			return true;
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

	// 刷新按钮的点击事件
	private class RefreshButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "refresh button is clicked");
			
			// 尝试刷新
			refreshButton.startAnimation(refreshAnimation);
			waitForRefresh();
			
			if (callback != null) {
				// 仅仅只是把当前应该显示的文件夹文件传出而已
				callback.onRefreshButtonClick(crumbController.getSelectedFile());
			}
		}
	}
	
	// 指明selected == null
	private class GridViewClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// 撤销当前的状态
			if (mode == MODE_SINGLE_SELECT) {
				selectPosition = -1;
			}
			
			if (callback != null) {
				callback.onGridViewClick();
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
