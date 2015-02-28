package org.mshare.file;

import java.io.File;
import java.util.Map;

import org.mshare.main.*;
import org.mshare.file.MShareFileAdapter.ItemContainer;
import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.mshare.main.MShareUtil;
/**
 * 文件浏览器
 * 包含GridView中图标的点击事件 {@link GridViewItemClickListener}
 * @author HM
 *
 */
public class MShareFileBrowser extends BroadcastReceiver implements MShareCrumbController.OnCrumbClickListener {

	private static final String TAG = MShareFileBrowser.class.getSimpleName();
	// 用于ContextMenu中的groupId
	// TODO 暂时放置在这里，使用内容在MainActivity中
	public static final int CONTEXT_MENU_ITEM_ID_SHARE = 4;
	public static final int CONTEXT_MENU_ITEM_ID_UNSHARE = 5;
	
	private Context context = null;
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
	private Button backBtn;
	/**
	 * 根目录路径，即扩展存储路径
	 */
	private MShareFile rootFile;
	
	/**
	 * 正在显示的内容
	 */
	private MShareFile[] files;
	
	private boolean isLongClicked = true;
	
	private boolean enable;
	/**
	 * 当前被长按选择的内容
	 * 需要保证得到及时的更新
	 * TODO 在何时删除内容
	 */
	private MShareFile selectFile = null;
	
	public MShareFileBrowser(Context context, ViewGroup container, String rootPath) {
		this.context = context;
		this.container = container;
		this.rootFile = new MShareFile(rootPath);
	}
	
	public View getView() {
		// 文件浏览器布局
		View fileBrowserLayout = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);
		
		fileBrowserLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "file browser is clicked");
			}
		});
		
		// 设置后退按钮
		backBtn = (Button)(fileBrowserLayout.findViewById(R.id.crumb_back_button));
		backBtn.setOnClickListener(new BackBtnListener(context));
		
		// TODO 使用include标签
		LinearLayout crumbContainer = (LinearLayout)(fileBrowserLayout.findViewById(R.id.crumb_container));
		
		// 面包屑导航控制器
		crumbController = new MShareCrumbController(context, rootFile, crumbContainer);
		crumbController.setOnCrumbClickListener(this);
		
		// 获得根目录下的文件列表
		MShareFile[] files = crumbController.getFiles();
		// create grid view
		gridView = (GridView)(fileBrowserLayout.findViewById(R.id.grid_view));
		gridView.setOnItemLongClickListener(new LongListener());
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
		
		
		// 有setOnContextMenuCreateListener
		
		// TODO 可能并不是很好的注册ContextMenu的方法，因为需要将context作为Activity来使用
		((Activity)context).registerForContextMenu(gridView);
		
		// 检测扩展存储是否可用
		setEnabled(StateController.getExternalStorageState() == StateController.STATE_EXTERNAL_STORAGE_ENABLE);
		if (!isEnabled()) {
			Toast.makeText(context, R.string.external_storage_removed, Toast.LENGTH_SHORT).show();
			return null;
		} else {
			// set adapter
			adapter = new MShareFileAdapter(context, this, files); 
			gridView.setAdapter(adapter);
			return fileBrowserLayout;
		}
	}
	
	/**
	 * 设置当前被选中的文件内容
	 * @param file
	 */
	public void setSelectFile(MShareFile file) {
		selectFile = file;
	}
	
	/**
	 * 获得当前正在共享的文件，仅仅用于ContextMenu中，其他情况下不保证信息的及时性
	 * @return 在非ContextMenu的情况下调用可能是null
	 */
	public MShareFile getSelectFile() {
		return selectFile;
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
	 * 设置扩展存储是否可用
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
			Toast.makeText(context, "扩展存储不可用", Toast.LENGTH_SHORT).show();
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
		refreshGridView(file.getFiles());
	}
	
	/**
	 * 刷新GridView，重置适配器
	 * 刷新的主要函数 
	 * @param files
	 */
	public void refreshGridView(MShareFile[] files) {
		// 设置当前正在文件浏览器中的内容
		this.files = files;
		
		// 设置当前正在刷新的是否是共享的文件
		for (int i = 0; i < files.length; i++) {
			MShareFile file = files[i];
//			FsService.isFile
		}
		
		// 新的适配器，用于刷新GridView
		adapter = new MShareFileAdapter(context, this, files);
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
	
	/**
	 * 将一个文件设置为共享的
	 * @param file
	 */
	public void setFileShared(MShareFile file, boolean shared) {
		String filePath = file.getAbsolutePath();
		// TODO 不适用SharedPreference来记录
		// 当前登录用户的设置对象
		SharedPreferences sp = context.getSharedPreferences("username", Context.MODE_PRIVATE);
		// 判断当前文件路径是否是共享的
		// 尝试获得所有的内容
		Map<String, Boolean> map = (Map<String, Boolean>)sp.getAll();
		
		boolean isShared = sp.getBoolean(filePath, false);
		
		if (isShared != shared) {
			Editor editor = sp.edit();
			editor.putBoolean(filePath, shared);
			editor.commit();
		}
	}
	
	/**
	 * 用于响应当面包屑导航中的内容被点击时的事件
	 * @param selected
	 * @param name
	 */
	@Override
	public void onCrumbClick(int selected, String name) {
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
			if (isLongClicked) {
				Log.d(TAG, "is LongClick!");
				isLongClicked = false;
				return;
			}
			Log.d(TAG, "item click!");
			Object tag = view.getTag();
			
			if (tag != null) {
				ItemContainer item = (ItemContainer)tag; 
				MShareFile file = item.file;
				if (file.isDirectory()) { // whether is a directory
					
					if (file != null && file.canRead()) { // 文件夹可以打开
						pushCrumb(file);
						refreshGridView(file);
					} else {
						// TODO 文件夹无法打开，可能是权限问题
						Toast.makeText(context, "文件夹无法访问", Toast.LENGTH_SHORT).show();
					}
				} else {
					Log.d(TAG, "所点击的是一个文件");
					// 是文件
				}
			} else {
				Log.e(TAG, "tag是null");
				// error
			}
		}
	}
	
	private class LongListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {

			// 设置所
			MShareFile file = files[position];
			setSelectFile(file);
			Log.d(TAG, "set select file : " + file.getAbsolutePath());
			
			// 设置longclick，让click不被触发
			isLongClicked = true;
			
			// TODO 设置成false，是这样?
			return false;
		}
		
	}
	
	/**
	 * 后退按钮的监听器
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

	/**
	 * <p>监听扩展存储的状态</p>
	 * TODO 将来将使用{@link ExternalStorageStateReceiver}来代替
	 */
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
