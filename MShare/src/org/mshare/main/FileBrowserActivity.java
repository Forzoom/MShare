package org.mshare.main;

import java.io.File;

import org.mshare.file.MshareFileManage;
import org.mshare.file.MshareFileMenu;
import org.mshare.file.browser.FileBrowserCallback;
import org.mshare.file.browser.FileBrowserFile;
import org.mshare.file.browser.LocalBrowserFile;
import org.mshare.file.browser.MShareFileBrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

public class FileBrowserActivity extends Activity implements FileBrowserCallback {
	private static final String TAG = FileBrowserActivity.class.getSimpleName();
	
	private MShareFileBrowser fileBrowser;
	
	private Context context;
	
	private LinearLayout linearLayout;
	
	private MshareFileMenu[] mshareFileMenu;
	
	private MshareFileManage mshareFileManage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_file_browser_activity);
		this.context = this;
		
		// 获得文件浏览器并设置回调函数
		fileBrowser = (MShareFileBrowser)findViewById(R.id.local_file_browser);
		fileBrowser.setCallback(this);
		// 允许使用多选
		fileBrowser.setMultiSelectEnabled(true);
		
		
		// 根文件
		LocalBrowserFile rootFile = new LocalBrowserFile(Environment.getExternalStorageDirectory().getAbsolutePath());
		fileBrowser.setRootFile(rootFile, "SD卡");
		fileBrowser.refreshGridView(listFiles(rootFile));
		
		//添加菜单栏
		mshareFileMenu = new MshareFileMenu[4];
		linearLayout = (LinearLayout)this.findViewById(R.id.local_menus);
		setMenu1();
		setMenu2();
		setMenu3();
		
		//文件处理类
		mshareFileManage = new MshareFileManage(this);
		
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
		if(file.isDirectory()) {
			fileBrowser.refreshGridView(listFiles(file));
		}
		else {
			fileBrowser.openFile(file);
		}
		
	}

	@Override
	public void onItemLongClick(FileBrowserFile file) {
		Log.d(TAG, "onItemLongClick");
		// 没有刷新要求
		if(!mshareFileManage.getSelect()) {
			changeMenu(0, 1);
		}
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
		
		// 测试退出
//		fileBrowser.quitMultiSelectMode();
		
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
		this.mshareFileMenu[0] = new MshareFileMenu(this, this.linearLayout);
		MenuNewFolder menuNewFolder = new MenuNewFolder();
		this.mshareFileMenu[0].addButton(R.drawable.account, "新建文件夹", menuNewFolder);		
	}
	
	//设置第二菜单
	private void setMenu2() {
		this.mshareFileMenu[1] = new MshareFileMenu(this, this.linearLayout);
		this.mshareFileMenu[3] = new MshareFileMenu(this, this.linearLayout);
		MenuCopy menuCopy = new MenuCopy();
		MenuCut menuCut = new MenuCut();
		MenuRename menuRename = new MenuRename();
		MenuDelete menuDelete = new MenuDelete();
		MenuCancelOpration menuCancelOperation = new MenuCancelOpration();
		MenuShare menuShare = new MenuShare();
		MenuUnshare menuUnshare = new MenuUnshare();
		this.mshareFileMenu[1].addButton(R.drawable.account, "复制", menuCopy);
		this.mshareFileMenu[1].addButton(R.drawable.account, "剪切", menuCut);
		this.mshareFileMenu[1].addButton(R.drawable.account, "删除", menuDelete);
		this.mshareFileMenu[1].addButton(R.drawable.account, "撤消", menuCancelOperation);
		this.mshareFileMenu[1].setRightMenu(this.mshareFileMenu[3]);
		this.mshareFileMenu[3].setLeftMenu(this.mshareFileMenu[1]);
		this.mshareFileMenu[3].addButton(R.drawable.account, "重命名", menuRename);
		this.mshareFileMenu[3].addButton(R.drawable.account, "共享", menuShare);
		this.mshareFileMenu[3].addButton(R.drawable.account, "不共享", menuUnshare);
		this.mshareFileMenu[1].hide();
		this.mshareFileMenu[3].hide();
	}
	
	//设置第三菜单
	private void setMenu3() {
		this.mshareFileMenu[2] = new MshareFileMenu(this, this.linearLayout);
		MenuPaste menuPaste = new MenuPaste();
		MenuCancelPaste menuCancelPaste = new MenuCancelPaste();
		this.mshareFileMenu[2].addButton(R.drawable.account, "粘贴", menuPaste);
		this.mshareFileMenu[2].addButton(R.drawable.account, "取消", menuCancelPaste);
		this.mshareFileMenu[2].hide();
	}
	
	//切换菜单
	private void changeMenu(int fromMenu, int toMenu) {
		this.mshareFileMenu[fromMenu].hideAnimation();
		this.mshareFileMenu[toMenu].showAnimation();
	}
	
	//刷新当前目录
	private void refreshDirectory() {
		fileBrowser.refreshGridView(listFiles(fileBrowser.getCurrentDirectory()));
	}

	
	//新建文件夹
	class MenuNewFolder implements View.OnClickListener {
		
		@SuppressLint("InflateParams")
		@Override
		public void onClick(View arg0) {
			final TableLayout newFolderForm = (TableLayout)getLayoutInflater()
					.inflate( R.layout.new_folder, null);	
				new AlertDialog.Builder(context)
					// 设置对话框的标题
					.setTitle("新建文件夹")
					// 设置对话框显示的View对象
					.setView(newFolderForm)
					// 为对话框设置一个“确定”按钮
					.setPositiveButton("确定" , new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							EditText editText = (EditText)newFolderForm.findViewById(R.id.editNewFolder);
							String folderName = editText.getText().toString();
							String path = fileBrowser.getCurrentDirectory().getAbsolutePath() + File.separator +folderName;
							File file = new File(path);
							if(folderName.equals("")) {
								Toast.makeText(getApplicationContext(), "文件夹名不能为空", Toast.LENGTH_SHORT).show();	
							}
							else if(file.exists()) {
								Toast.makeText(getApplicationContext(), "文件夹已存在", Toast.LENGTH_SHORT).show();	
							}
							else {
								mshareFileManage.newFolder(path);
								refreshDirectory();
							}
							
						}
					})
					// 为对话框设置一个“取消”按钮
					.setNegativeButton("取消", null)
					// 创建、并显示对话框
					.create()
					.show();		
		}
	}
	
	//复制
	class MenuCopy implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			FileBrowserFile[] file = fileBrowser.getMultiSelectedFiles();
			int length = file.length;
			if(length < 1) {
				Toast.makeText(getApplicationContext(), "请选择需要复制的文件", Toast.LENGTH_SHORT).show();
			}
			else {
				mshareFileManage.copySelect(file, false);
				fileBrowser.quitMultiSelectMode();
				changeMenu(1, 2);
				fileBrowser.setMultiSelectEnabled(false);
			}
		}
	}
	//剪切
	class MenuCut implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			FileBrowserFile[] file = fileBrowser.getMultiSelectedFiles();
			int length = file.length;
			if(length < 1) {
				Toast.makeText(getApplicationContext(), "请选择需要剪切的文件", Toast.LENGTH_SHORT).show();
			}
			else {
				mshareFileManage.copySelect(file, true);
				fileBrowser.quitMultiSelectMode();
				changeMenu(1, 2);
				fileBrowser.setMultiSelectEnabled(false);
			}
		}
	}
	
	//重命名
	class MenuRename implements View.OnClickListener {
		
		@SuppressLint("InflateParams")
		@Override
		public void onClick(View arg0) {
			final FileBrowserFile[] file = fileBrowser.getMultiSelectedFiles();
			if(file.length == 1) {
				final TableLayout renameForm = (TableLayout)getLayoutInflater()
					.inflate( R.layout.rename, null);	
				new AlertDialog.Builder(context)
					// 设置对话框的标题
					.setTitle("重命名")
					// 设置对话框显示的View对象
					.setView(renameForm)
					// 为对话框设置一个“确定”按钮
					.setPositiveButton("确定" , new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							EditText editText = (EditText)renameForm.findViewById(R.id.editRename);
							String newName = editText.getText().toString();
							String oldPath = file[0].getAbsolutePath();
							String newPath = fileBrowser.getCurrentDirectory().getAbsolutePath() + File.separator + newName;
							if(newName.equals("") || oldPath.equals(newPath)) {
								Toast.makeText(getApplicationContext(), "新文件名不能为空或和原名一样", Toast.LENGTH_SHORT).show();
							}
							else {
								mshareFileManage.renameFile(oldPath, newPath);
								fileBrowser.quitMultiSelectMode();
								refreshDirectory();
								changeMenu(3, 0);
							}
						}
					})
					// 为对话框设置一个“取消”按钮
					.setNegativeButton("取消", null)
					// 创建、并显示对话框
					.create()
					.show();
			}
			else {
				Toast.makeText(getApplicationContext(), "只能选中一个文件进行重命名", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	//删除
	class MenuDelete implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			FileBrowserFile[] fileBrowserFile = fileBrowser.getMultiSelectedFiles();
			if(fileBrowserFile.length < 1) {
				Toast.makeText(getApplicationContext(), "请选择要删除的文件", Toast.LENGTH_SHORT).show();
			}
			else {
				mshareFileManage.deleteMultiFiles(fileBrowserFile);
				fileBrowser.quitMultiSelectMode();
				refreshDirectory();
				changeMenu(1, 0);
			}
		}
	}
	
	//取消操作
	class MenuCancelOpration implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			fileBrowser.quitMultiSelectMode();
			changeMenu(1, 0);
		}
	}
	
	//粘贴
	class MenuPaste implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			if(mshareFileManage.getCut()) {
				mshareFileManage.moveMultiFiles(fileBrowser.getCurrentDirectory().getAbsolutePath());
			}
			else {
				mshareFileManage.CopyMultiFiles(fileBrowser.getCurrentDirectory().getAbsolutePath());
			}
			refreshDirectory();
			changeMenu(2, 0);
			fileBrowser.setMultiSelectEnabled(true);
		}
	}
	
	//取消粘贴
	class MenuCancelPaste implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			mshareFileManage.pasteCancel();
			changeMenu(2, 0);
			fileBrowser.setMultiSelectEnabled(true);
		}
	}

    class MenuShare implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // 获得当前所选择的内容，需要将长按的内容保存下来
        }
    }

    class MenuUnshare implements View.OnClickListener {

        @Override
        public void onClick(View v) {

        }
    }
}
