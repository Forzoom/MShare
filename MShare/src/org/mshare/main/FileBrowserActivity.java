package org.mshare.main;

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

public class FileBrowserActivity extends Activity implements FileBrowserCallback {
	private static final String TAG = FileBrowserActivity.class.getSimpleName();
	
	private MShareFileBrowser fileBrowser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_file_browser_activity);
		
		// ����ļ�����������ûص�����
		fileBrowser = (MShareFileBrowser)findViewById(R.id.local_file_browser);
		fileBrowser.setCallback(this);
		
		// ���ļ�
		LocalBrowserFile rootFile = new LocalBrowserFile(Environment.getExternalStorageDirectory().getAbsolutePath());
		fileBrowser.setRootFile(rootFile);
		fileBrowser.refreshGridView(listFiles(rootFile));
		
		// ע��ContextMenu
		GridView gridView = fileBrowser.getGridView();
		registerForContextMenu(gridView);
		
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
		fileBrowser.refreshGridView(listFiles(file));
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
}
