package org.mshare.main;

import org.mshare.file.MshareFileMenu;
import org.mshare.file.browser.FileBrowserCallback;
import org.mshare.file.browser.FileBrowserFile;
import org.mshare.file.browser.LocalBrowserFile;
import org.mshare.file.browser.MShareFileBrowser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.GridView;
import android.widget.LinearLayout;

public class FileBrowserActivity extends Activity implements FileBrowserCallback {
	private static final String TAG = FileBrowserActivity.class.getSimpleName();
	
	private MShareFileBrowser fileBrowser;
	
	private LinearLayout linearLayout;
	
	private MshareFileMenu mshareFileMenu1;
	private MshareFileMenu mshareFileMenu2;
	private MshareFileMenu mshareFileMenu3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_file_browser_activity);
		
		// 获得文件浏览器并设置回调函数
		fileBrowser = (MShareFileBrowser)findViewById(R.id.local_file_browser);
		fileBrowser.setCallback(this);
		// 允许使用多选
		fileBrowser.setMultiSelectEnabled(true);
		
		// 根文件
		LocalBrowserFile rootFile = new LocalBrowserFile(Environment.getExternalStorageDirectory().getAbsolutePath());
		fileBrowser.setRootFile(rootFile);
		fileBrowser.refreshGridView(listFiles(rootFile));
		
		//添加菜单栏
		linearLayout = (LinearLayout)this.findViewById(R.id.local_menus);
		setMenu1();
		setMenu2();
		setMenu3();
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, 0, 0, "测试");
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onCrumbClick(FileBrowserFile file) {
		Log.d(TAG, "onCrumbClick");
		fileBrowser.refreshGridView(listFiles(file));
	}

	@Override
	public void onBackButtonClick(FileBrowserFile file) {
		Log.d(TAG, "onBackButtonClick");
		fileBrowser.refreshGridView(listFiles(file));
	}

	@Override
	public void onItemClick(FileBrowserFile file) {
		Log.d(TAG, "onItemClick");
		fileBrowser.refreshGridView(listFiles(file));
	}

	@Override
	public void onItemLongClick(FileBrowserFile file) {
		Log.d(TAG, "onItemLongClick");
		// 没有刷新要求
	}

	@Override
	public void onGridViewClick() {
		// 尝试退出
		fileBrowser.quitMultiSelectMode();
	}
	
	@Override
	public void onRefreshButtonClick(FileBrowserFile file) {
		Log.d(TAG, "onRefreshButtonClick");
		fileBrowser.refreshGridView(listFiles(file));
		
		// 测试多选
//		if (fileBrowser.getMode() == MShareFileBrowser.MODE_MULTI_SELECT) {
//			FileBrowserFile[] files = fileBrowser.getMultiSelectedFiles();
//			// 测试获得所有被选择的file的内容
//			for (int i = 0; i < files.length; i++) {
//				FileBrowserFile selectFile = files[i];
//				Log.v(TAG, selectFile.getName());
//			}
//		}
	}
	
	/**
	 * 获得所有子文件
	 * @return 一个MShareFile的数组, or null if the `list()` == null
	 */
	public static FileBrowserFile[] listFiles(FileBrowserFile file) {
		Log.d(TAG, "list files");
		
		if (!(file instanceof LocalBrowserFile)) {
			Log.e(TAG, "the file is not a LocalBrowserFile");
			return new FileBrowserFile[0];
		}
		LocalBrowserFile lbFile = (LocalBrowserFile)file;
		
		if (!file.isDirectory()) { // is directory
			Log.e(TAG, "is not a directory");
			return null;
		}

		String[] fileList = lbFile.getFile().list();
		if (fileList == null) {
			 return null;
		}
		
		String dir = lbFile.getAbsolutePath();
		LocalBrowserFile[] ret = new LocalBrowserFile[fileList.length];
		
		// 填充结果
		for (int i = 0, len = fileList.length; i < len; i++) {
			ret[i] = new LocalBrowserFile(dir + "/" + fileList[i]);
		}
		
		return ret;
	}
	//设置第一菜单
	private void setMenu1() {
		this.mshareFileMenu1 = new MshareFileMenu(this, this.linearLayout);
		MenuNewFolder menuNewFolder = new MenuNewFolder();
		MenuRefresh menuRefresh = new MenuRefresh();
		this.mshareFileMenu1.addButton(R.drawable.account, "新建文件夹", menuNewFolder);		
		this.mshareFileMenu1.addButton(R.drawable.account, "刷新", menuRefresh);
	}
	
	//设置第二菜单
	private void setMenu2() {
		this.mshareFileMenu2 = new MshareFileMenu(this, this.linearLayout);
		MenuCopy menuCopy = new MenuCopy();
		MenuCut menuCut = new MenuCut();
		MenuRename menuRename = new MenuRename();
		MenuDelete menuDelete = new MenuDelete();
		MenuCancel menuCancel = new MenuCancel();
		this.mshareFileMenu2.addButton(R.drawable.account, "复制", menuCopy);
		this.mshareFileMenu2.addButton(R.drawable.account, "剪切", menuCut);
		this.mshareFileMenu2.addButton(R.drawable.account, "重命名", menuRename);
		this.mshareFileMenu2.addButton(R.drawable.account, "删除", menuDelete);
		this.mshareFileMenu2.addButton(R.drawable.account, "撤消", menuCancel);
		this.mshareFileMenu2.hide();
	}
	
	//设置第三菜单
	private void setMenu3() {
		this.mshareFileMenu3 = new MshareFileMenu(this, this.linearLayout);
		MenuPaste menuPaste = new MenuPaste();
		MenuCancel menuCancel = new MenuCancel();
		this.mshareFileMenu3.addButton(R.drawable.account, "粘贴", menuPaste);
		this.mshareFileMenu3.addButton(R.drawable.account, "取消", menuCancel);
		this.mshareFileMenu3.hide();
	}
	
	//新建文件夹
	class MenuNewFolder implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			mshareFileMenu1.hideAnimation();
			mshareFileMenu2.showAnimation();
			
		}
	}
	
	//刷新
	class MenuRefresh implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//复制
	class MenuCopy implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	//剪切
	class MenuCut implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//重命名
	class MenuRename implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//删除
	class MenuDelete implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//取消
	class MenuCancel implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//粘贴
	class MenuPaste implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
}
