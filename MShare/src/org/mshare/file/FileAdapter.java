package org.mshare.file;

import java.io.File;
import java.util.HashMap;

import org.mshare.main.R;
import org.mshare.main.R.drawable;
import org.mshare.main.R.id;
import org.mshare.main.R.layout;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * how to refresh the content
 * @author HM
 *
 */
public class FileAdapter extends BaseAdapter {

	private Context context = null;
	/**
	 * 
	 */
	private MShareFile[] files = null;
	/**
	 * 
	 */
	private Drawable drawable = null;
	/**
	 * save all drawables
	 */
	private static HashMap<String, Drawable> drawables = new HashMap<String, Drawable>();
	
	private static boolean drawablePrepared = false;
	
	/**
	 * The grid item container
	 * @author HM
	 *
	 */
	public class ItemContainer {
		/**
		 * attached text view
		 */
		public TextView fileNameView = null;
		/**
		 * data container
		 */
		public MShareFile file = null;
	}
	
	/**
	 * create files content and picture
	 * @param context
	 * @param files
	 */
	public FileAdapter(Context context, MShareFile[] files) {
		super();
		this.context = context;
		this.files = files;
		initDrawable(context);
	}
	
	/**
	 * initial all drawables
	 */
	private static void initDrawable(Context context) {
		if (drawablePrepared) {
			return;
		}
		Drawable mp3Drawable = context.getResources().getDrawable(R.drawable.mp3);
		mp3Drawable.setBounds(0, 0, mp3Drawable.getIntrinsicWidth(), mp3Drawable.getIntrinsicHeight());
		drawables.put(".mp3", mp3Drawable);
		
		Drawable pdfDrawable = context.getResources().getDrawable(R.drawable.pdf);
		pdfDrawable.setBounds(0, 0, pdfDrawable.getIntrinsicWidth(), pdfDrawable.getIntrinsicHeight());
		drawables.put(".pdf", pdfDrawable);
		
		Drawable docDrawable = context.getResources().getDrawable(R.drawable.doc);
		docDrawable.setBounds(0, 0, docDrawable.getIntrinsicWidth(), docDrawable.getIntrinsicHeight());
		drawables.put(".doc", docDrawable);
		
		Drawable txtDrawable = context.getResources().getDrawable(R.drawable.txt);
		txtDrawable.setBounds(0, 0, txtDrawable.getIntrinsicWidth(), txtDrawable.getIntrinsicHeight());
		drawables.put(".txt", txtDrawable);
		
		Drawable pptDrawable = context.getResources().getDrawable(R.drawable.ppt);
		pptDrawable.setBounds(0, 0, pptDrawable.getIntrinsicWidth(), pptDrawable.getIntrinsicHeight());
		drawables.put(".ppt", pptDrawable);
		
		Drawable xmlDrawable = context.getResources().getDrawable(R.drawable.xml);
		xmlDrawable.setBounds(0, 0, xmlDrawable.getIntrinsicWidth(), xmlDrawable.getIntrinsicHeight());
		drawables.put(".xml",xmlDrawable);
		
		Drawable fileDrawable = context.getResources().getDrawable(R.drawable.all);
		fileDrawable.setBounds(0, 0, fileDrawable.getIntrinsicWidth(), fileDrawable.getIntrinsicHeight());
		drawables.put("file", fileDrawable);
		
		Drawable DirectoryDrawable = context.getResources().getDrawable(R.drawable.folder);
		DirectoryDrawable.setBounds(0, 0, DirectoryDrawable.getIntrinsicWidth(), DirectoryDrawable.getIntrinsicHeight());
		drawables.put("directory", DirectoryDrawable);
		
		// set the flag to true
		drawablePrepared = true;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return files.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return files[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView != null) {
			ItemContainer item = (ItemContainer)convertView.getTag();
			if (item != null) {
				item.fileNameView.setText(files[position].getName());
			}
		} else { // if convertView is null
			convertView = (View)LayoutInflater.from(context).inflate(R.layout.grid_item, null);
			
			// create content
			ItemContainer item = new ItemContainer();
			
			// set file and get drawable
			item.file = files[position];
			Drawable drawable = getDrawable(item.file);
			
			item.fileNameView = (TextView)convertView.findViewById(R.id.item_file_name);
			item.fileNameView.setWidth(drawable.getIntrinsicWidth()); // max width
			item.fileNameView.setText(item.file.getDisplayName());
			item.fileNameView.setCompoundDrawables(null, drawable, null, null);
			
			// register for long click
			
			
			// save content
			convertView.setTag(item);
		}
		
		return convertView;
	}
	
	/**
	 * get match Drawable
	 * @param file
	 * @return
	 */
	private Drawable getDrawable(MShareFile file) {
		if (file == null) {
			return null;
		}
		
		Drawable drawable = null;
		
		if (file.isFile()) {
			String extname = file.getExtname();
			if (extname.equals("") || !drawables.containsKey(extname)) {
				drawable = drawables.get("file");
			} else {
				drawable = drawables.get(extname);
			}
		} else if (file.isDirectory()) {
			drawable = drawables.get("directory");
		}
		
		return drawable; 
	}
	
}
