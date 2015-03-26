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
		
		// ����ļ�����������ûص�����
		fileBrowser = (MShareFileBrowser)findViewById(R.id.local_file_browser);
		fileBrowser.setCallback(this);
		// ����ʹ�ö�ѡ
		fileBrowser.setMultiSelectEnabled(true);
		
		
		// ���ļ�
		LocalBrowserFile rootFile = new LocalBrowserFile(Environment.getExternalStorageDirectory().getAbsolutePath());
		fileBrowser.setRootFile(rootFile, "SD��");
		fileBrowser.refreshGridView(listFiles(rootFile));
		
		//��Ӳ˵���
		mshareFileMenu = new MshareFileMenu[4];
		linearLayout = (LinearLayout)this.findViewById(R.id.local_menus);
		setMenu1();
		setMenu2();
		setMenu3();
		
		//�ļ�������
		mshareFileManage = new MshareFileManage(this);
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, 0, 0, "����");
		
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
		// û��ˢ��Ҫ��
		if(!mshareFileManage.getSelect()) {
			changeMenu(0, 1);
		}
	}

	@Override
	public void onGridViewClick() {
		// �����˳�
		fileBrowser.quitMultiSelectMode();
	}
	
	@Override
	public void onRefreshButtonClick(FileBrowserFile file) {
		Log.d(TAG, "onRefreshButtonClick");
		
		fileBrowser.refreshGridView(listFiles(file));
		
		// �����˳�
//		fileBrowser.quitMultiSelectMode();
		
		// ���Զ�ѡ
//		if (fileBrowser.getMode() == MShareFileBrowser.MODE_MULTI_SELECT) {
//			FileBrowserFile[] files = fileBrowser.getMultiSelectedFiles();
//			// ���Ի�����б�ѡ���file������
//			for (int i = 0; i < files.length; i++) {
//				FileBrowserFile selectFile = files[i];
//				Log.v(TAG, selectFile.getName());
//			}
//		}
	}
	
	/**
	 * ����������ļ�
	 * @return һ��MShareFile������, or null if the `list()` == null
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
		
		// �����
		for (int i = 0, len = fileList.length; i < len; i++) {
			ret[i] = new LocalBrowserFile(dir + "/" + fileList[i]);
		}
		
		return ret;
	}
	//���õ�һ�˵�
	private void setMenu1() {
		this.mshareFileMenu[0] = new MshareFileMenu(this, this.linearLayout);
		MenuNewFolder menuNewFolder = new MenuNewFolder();
		this.mshareFileMenu[0].addButton(R.drawable.account, "�½��ļ���", menuNewFolder);		
	}
	
	//���õڶ��˵�
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
		this.mshareFileMenu[1].addButton(R.drawable.account, "����", menuCopy);
		this.mshareFileMenu[1].addButton(R.drawable.account, "����", menuCut);
		this.mshareFileMenu[1].addButton(R.drawable.account, "ɾ��", menuDelete);
		this.mshareFileMenu[1].addButton(R.drawable.account, "����", menuCancelOperation);
		this.mshareFileMenu[1].setRightMenu(this.mshareFileMenu[3]);
		this.mshareFileMenu[3].setLeftMenu(this.mshareFileMenu[1]);
		this.mshareFileMenu[3].addButton(R.drawable.account, "������", menuRename);
		this.mshareFileMenu[3].addButton(R.drawable.account, "����", menuShare);
		this.mshareFileMenu[3].addButton(R.drawable.account, "������", menuUnshare);
		this.mshareFileMenu[1].hide();
		this.mshareFileMenu[3].hide();
	}
	
	//���õ����˵�
	private void setMenu3() {
		this.mshareFileMenu[2] = new MshareFileMenu(this, this.linearLayout);
		MenuPaste menuPaste = new MenuPaste();
		MenuCancelPaste menuCancelPaste = new MenuCancelPaste();
		this.mshareFileMenu[2].addButton(R.drawable.account, "ճ��", menuPaste);
		this.mshareFileMenu[2].addButton(R.drawable.account, "ȡ��", menuCancelPaste);
		this.mshareFileMenu[2].hide();
	}
	
	//�л��˵�
	private void changeMenu(int fromMenu, int toMenu) {
		this.mshareFileMenu[fromMenu].hideAnimation();
		this.mshareFileMenu[toMenu].showAnimation();
	}
	
	//ˢ�µ�ǰĿ¼
	private void refreshDirectory() {
		fileBrowser.refreshGridView(listFiles(fileBrowser.getCurrentDirectory()));
	}

	
	//�½��ļ���
	class MenuNewFolder implements View.OnClickListener {
		
		@SuppressLint("InflateParams")
		@Override
		public void onClick(View arg0) {
			final TableLayout newFolderForm = (TableLayout)getLayoutInflater()
					.inflate( R.layout.new_folder, null);	
				new AlertDialog.Builder(context)
					// ���öԻ���ı���
					.setTitle("�½��ļ���")
					// ���öԻ�����ʾ��View����
					.setView(newFolderForm)
					// Ϊ�Ի�������һ����ȷ������ť
					.setPositiveButton("ȷ��" , new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							EditText editText = (EditText)newFolderForm.findViewById(R.id.editNewFolder);
							String folderName = editText.getText().toString();
							String path = fileBrowser.getCurrentDirectory().getAbsolutePath() + File.separator +folderName;
							File file = new File(path);
							if(folderName.equals("")) {
								Toast.makeText(getApplicationContext(), "�ļ���������Ϊ��", Toast.LENGTH_SHORT).show();	
							}
							else if(file.exists()) {
								Toast.makeText(getApplicationContext(), "�ļ����Ѵ���", Toast.LENGTH_SHORT).show();	
							}
							else {
								mshareFileManage.newFolder(path);
								refreshDirectory();
							}
							
						}
					})
					// Ϊ�Ի�������һ����ȡ������ť
					.setNegativeButton("ȡ��", null)
					// ����������ʾ�Ի���
					.create()
					.show();		
		}
	}
	
	//����
	class MenuCopy implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			FileBrowserFile[] file = fileBrowser.getMultiSelectedFiles();
			int length = file.length;
			if(length < 1) {
				Toast.makeText(getApplicationContext(), "��ѡ����Ҫ���Ƶ��ļ�", Toast.LENGTH_SHORT).show();
			}
			else {
				mshareFileManage.copySelect(file, false);
				fileBrowser.quitMultiSelectMode();
				changeMenu(1, 2);
				fileBrowser.setMultiSelectEnabled(false);
			}
		}
	}
	//����
	class MenuCut implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			FileBrowserFile[] file = fileBrowser.getMultiSelectedFiles();
			int length = file.length;
			if(length < 1) {
				Toast.makeText(getApplicationContext(), "��ѡ����Ҫ���е��ļ�", Toast.LENGTH_SHORT).show();
			}
			else {
				mshareFileManage.copySelect(file, true);
				fileBrowser.quitMultiSelectMode();
				changeMenu(1, 2);
				fileBrowser.setMultiSelectEnabled(false);
			}
		}
	}
	
	//������
	class MenuRename implements View.OnClickListener {
		
		@SuppressLint("InflateParams")
		@Override
		public void onClick(View arg0) {
			final FileBrowserFile[] file = fileBrowser.getMultiSelectedFiles();
			if(file.length == 1) {
				final TableLayout renameForm = (TableLayout)getLayoutInflater()
					.inflate( R.layout.rename, null);	
				new AlertDialog.Builder(context)
					// ���öԻ���ı���
					.setTitle("������")
					// ���öԻ�����ʾ��View����
					.setView(renameForm)
					// Ϊ�Ի�������һ����ȷ������ť
					.setPositiveButton("ȷ��" , new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							EditText editText = (EditText)renameForm.findViewById(R.id.editRename);
							String newName = editText.getText().toString();
							String oldPath = file[0].getAbsolutePath();
							String newPath = fileBrowser.getCurrentDirectory().getAbsolutePath() + File.separator + newName;
							if(newName.equals("") || oldPath.equals(newPath)) {
								Toast.makeText(getApplicationContext(), "���ļ�������Ϊ�ջ��ԭ��һ��", Toast.LENGTH_SHORT).show();
							}
							else {
								mshareFileManage.renameFile(oldPath, newPath);
								fileBrowser.quitMultiSelectMode();
								refreshDirectory();
								changeMenu(3, 0);
							}
						}
					})
					// Ϊ�Ի�������һ����ȡ������ť
					.setNegativeButton("ȡ��", null)
					// ����������ʾ�Ի���
					.create()
					.show();
			}
			else {
				Toast.makeText(getApplicationContext(), "ֻ��ѡ��һ���ļ�����������", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	//ɾ��
	class MenuDelete implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			FileBrowserFile[] fileBrowserFile = fileBrowser.getMultiSelectedFiles();
			if(fileBrowserFile.length < 1) {
				Toast.makeText(getApplicationContext(), "��ѡ��Ҫɾ�����ļ�", Toast.LENGTH_SHORT).show();
			}
			else {
				mshareFileManage.deleteMultiFiles(fileBrowserFile);
				fileBrowser.quitMultiSelectMode();
				refreshDirectory();
				changeMenu(1, 0);
			}
		}
	}
	
	//ȡ������
	class MenuCancelOpration implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			fileBrowser.quitMultiSelectMode();
			changeMenu(1, 0);
		}
	}
	
	//ճ��
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
	
	//ȡ��ճ��
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
            // ��õ�ǰ��ѡ������ݣ���Ҫ�����������ݱ�������
        }
    }

    class MenuUnshare implements View.OnClickListener {

        @Override
        public void onClick(View v) {

        }
    }
}
