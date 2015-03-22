package org.mshare.main;

import org.mshare.file.MshareFileMenu;
import org.mshare.file.browser.FileBrowserCallback;
import org.mshare.file.browser.FileBrowserFile;
import org.mshare.file.browser.LocalBrowserFile;
import org.mshare.file.browser.MShareFileBrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TableLayout;

public class FileBrowserActivity extends Activity implements FileBrowserCallback {
	private static final String TAG = FileBrowserActivity.class.getSimpleName();
	
	private MShareFileBrowser fileBrowser;
	
	private Context context;
	
	private LinearLayout linearLayout;
	
	private MshareFileMenu mshareFileMenu1;
	private MshareFileMenu mshareFileMenu2;
	private MshareFileMenu mshareFileMenu3;
	
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
		linearLayout = (LinearLayout)this.findViewById(R.id.local_menus);
		setMenu1();
		setMenu2();
		setMenu3();
		
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
		fileBrowser.refreshGridView(listFiles(file));
	}

	@Override
	public void onItemLongClick(FileBrowserFile file) {
		Log.d(TAG, "onItemLongClick");
		// û��ˢ��Ҫ��
		mshareFileMenu1.hideAnimation();
		mshareFileMenu2.showAnimation();
	}

	@Override
	public void onGridViewClick() {
		// �����˳�
		fileBrowser.quitMultiSelectMode();
	}
	
	@Override
	public void onRefreshButtonClick(FileBrowserFile file) {
		Log.d(TAG, "onRefreshButtonClick");
		
//		fileBrowser.refreshGridView(listFiles(file));
		
		// �����˳�
		fileBrowser.quitMultiSelectMode();
		
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
		this.mshareFileMenu1 = new MshareFileMenu(this, this.linearLayout);
		MenuNewFolder menuNewFolder = new MenuNewFolder();
		this.mshareFileMenu1.addButton(R.drawable.account, "�½��ļ���", menuNewFolder);		
	}
	
	//���õڶ��˵�
	private void setMenu2() {
		this.mshareFileMenu2 = new MshareFileMenu(this, this.linearLayout);
		MenuCopy menuCopy = new MenuCopy();
		MenuCut menuCut = new MenuCut();
		MenuRename menuRename = new MenuRename();
		MenuDelete menuDelete = new MenuDelete();
		MenuCancel menuCancel = new MenuCancel();
		this.mshareFileMenu2.addButton(R.drawable.account, "����", menuCopy);
		this.mshareFileMenu2.addButton(R.drawable.account, "����", menuCut);
		this.mshareFileMenu2.addButton(R.drawable.account, "������", menuRename);
		this.mshareFileMenu2.addButton(R.drawable.account, "ɾ��", menuDelete);
		this.mshareFileMenu2.addButton(R.drawable.account, "����", menuCancel);
		this.mshareFileMenu2.hide();
	}
	
	//���õ����˵�
	private void setMenu3() {
		this.mshareFileMenu3 = new MshareFileMenu(this, this.linearLayout);
		MenuPaste menuPaste = new MenuPaste();
		MenuCancel menuCancel = new MenuCancel();
		this.mshareFileMenu3.addButton(R.drawable.account, "ճ��", menuPaste);
		this.mshareFileMenu3.addButton(R.drawable.account, "ȡ��", menuCancel);
		this.mshareFileMenu3.hide();
	}
	
	//�½��ļ���
	class MenuNewFolder implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			TableLayout loginForm = (TableLayout)getLayoutInflater()
					.inflate( R.layout.new_folder, null);	
				new AlertDialog.Builder(context)
					// ���öԻ���ı���
					.setTitle("�½��ļ���")
					// ���öԻ�����ʾ��View����
					.setView(loginForm)
					// Ϊ�Ի�������һ����ȷ������ť
					.setPositiveButton("ȷ��" , new OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,
								int which)
						{
							
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
			
		}
	}
	//����
	class MenuCut implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//������
	class MenuRename implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//ɾ��
	class MenuDelete implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//ȡ��
	class MenuCancel implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
	
	//ճ��
	class MenuPaste implements View.OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			
		}
	}
}
