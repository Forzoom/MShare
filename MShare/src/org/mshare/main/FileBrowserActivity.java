package org.mshare.main;

import org.mshare.file.browser.MShareFile;
import org.mshare.file.browser.MShareFileBrowser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.GridView;

public class FileBrowserActivity extends Activity {
	private static final String TAG = FileBrowserActivity.class.getSimpleName();
	
	private MShareFileBrowser fileBrowser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fileBrowser = new MShareFileBrowser(this, null, new MShareFile(Environment.getExternalStorageDirectory().getAbsolutePath()));
		View fileBrowserView = fileBrowser.getView();
		GridView gridView = fileBrowser.getGridView();
		
		registerForContextMenu(gridView);
		
		setContentView(fileBrowserView);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		menu.add(0, 0, 0, "≤‚ ‘");
		
//		int position = ((GridView)v).getSelected
		
		int position = ((GridView)v).getSelectedItemPosition();
		long id = ((GridView)v).getSelectedItemId();
		Object obj = ((GridView)v).getSelectedItem();
		Log.d(TAG, "the selected position : " + position + " id : " + id + " obj : " + obj);
		
		if (position != -1) {
			String path = fileBrowser.getCurrentFiles()[position].getAbsolutePath();
			Log.d(TAG, "selected path : " + path);
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
}
