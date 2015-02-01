package org.mshare.file;

import java.io.File;
import java.util.Map;

import org.mshare.main.*;
import org.mshare.file.FileAdapter.ItemContainer;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.mshare.main.MShareUtil;
/**
 * 入口类
 * @author HM
 *
 */
public class MShareFileBrowser extends BroadcastReceiver implements MShareCrumbController.OnCrumbClickListener {

	// TODO 暂时放置在这里，使用内容在MainActivity中
	public static final int CM_ITEM_ID_SHARE = 4;
	public static final int CM_ITEM_ID_UNSHARE = 5;
	
	private static final String TAG = MShareFileBrowser.class.getSimpleName();
	
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
	// TODO rootFile的设定必须和服务器中的内容相同
	private MShareFile rootFile;
	private boolean enable = false;
	
	public MShareFileBrowser(Context context, ViewGroup container) {
		this.context = context;
		this.container = container;
	}
	
	public View getView() {
		// 文件浏览器布局
		View fileBrowserLayout = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);

		// 设置后退按钮
		backBtn = (Button)(fileBrowserLayout.findViewById(R.id.crumb_back_button));
		backBtn.setOnClickListener(new BackBtnListener(context));
		
		// 根目录路径，即扩展存储路径
		// TODO FsSettings.getChrootDir可能并不能获得正确的路径,虽然在使用FileBrowser之前已经判断了扩展存储是否可用
		rootFile = new MShareFile(FsSettings.getChrootDir());
		LinearLayout crumbContainer = (LinearLayout)(fileBrowserLayout.findViewById(R.id.crumb_container));
		
		// 面包屑导航控制器
		crumbController = new MShareCrumbController(context, rootFile, crumbContainer);
		crumbController.setOnItemClickListener(this);
		
		// 获得根目录下的文件列表
		MShareFile[] files = crumbController.getFiles();
		// create grid view
		gridView = (GridView)(fileBrowserLayout.findViewById(R.id.grid_view));
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
		
		// TODO 可能并不是很好的注册ContextMenu的方法
		((Activity)context).registerForContextMenu(gridView);
		
		// 检测扩展存储是否可用
		setEnabled(MShareUtil.isExternalStorageUsable());
		if (!isEnabled()) {
			Toast.makeText(context, R.string.external_storage_removed, Toast.LENGTH_SHORT).show();
			return null;
		} else {
			// set adapter
			adapter = new FileAdapter(context, files); 
			gridView.setAdapter(adapter);
			return fileBrowserLayout;
		}
	}
	
	/**
	 * 检测扩展存储是否可用
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
	 * 设置一批文件是否共享
	 * @param files
	 * @param shared
	 */
	public void setFilesShared(MShareFile[] files, boolean shared) {
		
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
			
			Object tag = view.getTag();
			
			if (tag != null) {
				ItemContainer item = (ItemContainer)tag; 
				MShareFile file = item.file;
				if (file.isDirectory()) { // whether is a directory
					
					if (file != null && file.canRead()) { // 文件夹可以打开 
						pushCrumb(file);
						refreshGridView(file);
					} else { // 文件夹无法打开
						Toast.makeText(context, "文件夹无法访问", Toast.LENGTH_SHORT).show();
					}
				} else {
					// 是文件
				}
			} else {
				// error
			}
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
	 * 监听扩展存储的状态
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
