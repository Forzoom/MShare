package org.mshare.file;

import java.io.File;

import org.mshare.main.*;
import org.mshare.file.FileAdapter.ItemContainer;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class MShareFileBrowser {

	private static final String TAG = "MShareFileBrowser";
	
	private Context context = null;
	private ViewGroup container = null;
	/**
	 * show current file path
	 */
	private TextView crumbsView = null;
	/**
	 * crumb controller
	 */
	private MShareCrumbs mShareCrumbs = null;
	/**
	 * try to use new adapter to refresh the content of gridview
	 */
	private FileAdapter adapter = null;
	/**
	 * the main view
	 */
	private GridView gridView = null;
	/**
	 * the button show parent directory content
	 */
	private Button backBtn = null;
	
	public MShareFileBrowser(Context context, ViewGroup container) {
		this.context = context;
		this.container = container;
	}
	
	public View getView() {
		// file browser view
		View view = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);

		// set back button
		backBtn = (Button)(view.findViewById(R.id.crumb_last_button));
		backBtn.setOnClickListener(new BackBtnListener(context));
		
		// get sd card root path and list files
		File root = Environment.getExternalStorageDirectory();
		
		// create `MShareCrumbs`
		mShareCrumbs = new MShareCrumbs(root);
		
		// get crumbsView and set the path
		crumbsView = (TextView)(view.findViewById(R.id.crumb_text));
		crumbsView.setText(mShareCrumbs.getPath());
		
		// all files in root directory
		MShareFile[] files = mShareCrumbs.getFiles();
		// create grid view
		gridView = (GridView)(view.findViewById(R.id.grid_view));
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
		
		// register context menu
		((Activity)context).registerForContextMenu(gridView);
		
		// check external storage useful
		if (!isExternalStorageUseful()) {
			Toast.makeText(context, "扩展存储不可用", Toast.LENGTH_SHORT).show();
			return null;
		} else {
			// set adapter
			adapter = new FileAdapter(context, files); 
			gridView.setAdapter(adapter);
			return view;
		}
	}
	
	/**
	 * check whether the external storage is useful
	 * @return
	 */
	public boolean isExternalStorageUseful() {
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * main refresh method
	 */
	public void refresh() {
		if (isExternalStorageUseful()) {
			refreshGridView();
		} else {
			mShareCrumbs.clean();
			refreshGridView(new MShareFile[0]);
			Toast.makeText(context, "扩展存储不可用", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * refresh gridview
	 */
	public void refreshGridView() {
		refreshGridView(mShareCrumbs.get());
	}
	/**
	 * refresh gridview
	 * @param file
	 */
	public void refreshGridView(MShareFile file) {
		refreshGridView(file.getSubFiles());
	}
	
	/**
	 * reset the adapter of grid view 
	 * @param files
	 */
	public void refreshGridView(MShareFile[] files) {
		// new Adapter
		adapter = new FileAdapter(context, files);
		gridView.setAdapter(adapter);
		// set crumb path
		refreshPath();
		// set the last button style
		if (!mShareCrumbs.canPop()) {
			backBtn.setClickable(false);
		} else {
			backBtn.setClickable(true);
		}
	}
	
	/**
	 * set the path
	 * @param text content to be shown in crumb
	 */
	private void setPath(String text) {
		this.crumbsView.setText(text);
	}
	/**
	 * refresh the MainActivity path
	 */
	public void refreshPath() {
		setPath(mShareCrumbs.getPath());
	}
	
	/**
	 * push a new crumb
	 * @param file
	 */
	public void pushCrumb(MShareFile file) {
		mShareCrumbs.push(file);
		refreshPath();
	}
	/**
	 * pop top crumb
	 * @return
	 */
	public MShareFile popCrumb() {
		MShareFile file = mShareCrumbs.pop();
		refreshPath();
		return file;
	}
	
	/**
	 * GridView item click listener, now just change the crumb content
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
					if (file.getSubFiles() != null) {
						pushCrumb(file);
						refreshGridView(file.getSubFiles());
					} else {
						// cannot open the directory
						Toast.makeText(context, "文件夹无法访问", Toast.LENGTH_LONG).show();
					}
				} else {
					// is file, do nothing
				}
			} else {
				// error
			}
		}
	}
	
	/**
	 * temp listener for crumb last button
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
}
