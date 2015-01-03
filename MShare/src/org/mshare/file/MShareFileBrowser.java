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

public class MShareFileBrowser {

	private static final String TAG = "MShareFileBrowser";
	
	private Context context = null;
	private ViewGroup container = null;
	/**
	 * show current file path
	 */
	private EditText crumbs = null;
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
	private Button crumbLastButton = null;
	
	public MShareFileBrowser(Context context, ViewGroup container) {
		this.context = context;
		if (context == null) Log.e(TAG, "context is null");
		this.container = container;
	}
	
	public View getView() {
		View view = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);

		crumbLastButton = (Button)(view.findViewById(R.id.crumb_last_button));
		crumbLastButton.setOnClickListener(new CrumbLastButtonListener(context));
		
		// get sd card root path and list files
		File root = Environment.getExternalStorageDirectory();
		
		// create `MShareCrumbs`
		mShareCrumbs = new MShareCrumbs(root);
		
		MShareFile[] files = mShareCrumbs.getFiles();
		
		// root does not have parent
		adapter = new FileAdapter(context, files);
		
		// get crumbs and set the path
		crumbs = (EditText)(view.findViewById(R.id.crumb_text));
		crumbs.setText(mShareCrumbs.getPath());
		
		// create grid view
		gridView = (GridView)(view.findViewById(R.id.grid_view));
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
//		gridView.setOnItemLongClickListener(new GridViewItemLongClickListener2());
		
		((Activity)context).registerForContextMenu(gridView);
		
		gridView.setAdapter(adapter);
		
		return view;
	}
	/**
	 * reset
	 */
	public void refreshGridView() {
		refreshGridView(mShareCrumbs.get());
	}
	/**
	 * reset
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
			crumbLastButton.setClickable(false);
		} else {
			crumbLastButton.setClickable(true);
		}
	}
	
	/**
	 * set the path
	 * @param text content to be shown in crumb
	 */
	private void setPath(String text) {
		this.crumbs.setText(text);
	}
	/**
	 * refresh the MainActivity path
	 */
	public void refreshPath() {
		setPath(mShareCrumbs.getPath());
	}
	
	/**
	 * new level
	 * @param file
	 */
	public void pushCrumb(MShareFile file) {
		mShareCrumbs.push(file);
		refreshPath();
	}
	/**
	 * crumb operation
	 * @return
	 */
	public MShareFile popCrumb() {
		MShareFile file = mShareCrumbs.pop();
		refreshPath();
		return file;
	}
	
	/**
	 * default index
	 */
	public void goParent() {
		goParent(mShareCrumbs.getDepth());
	}
	
	/**
	 * parent directory
	 */
	public void goParent(int index) {
		mShareCrumbs.setDepth(index);
		refreshGridView(mShareCrumbs.get());
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
					pushCrumb(file);
					refreshGridView(file);
				} else {
					// is file, do nothing
				}
			} else {
				// error
			}
		}
	}
	
	private class GridViewItemLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.v(TAG, "long click :" + mShareCrumbs.getFiles()[position].getName());
			return false;
		}
		
	}
	
	private class GridViewItemLongClickListener2 implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			return false;
		}

	}
	
	/**
	 * temp listener for crumb last button
	 * @author HM
	 *
	 */
	private class CrumbLastButtonListener implements View.OnClickListener {

		private Context context = null;
		
		public CrumbLastButtonListener(Context context) {
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
