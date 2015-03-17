package org.mshare.file.browser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.mshare.main.R;
import org.mshare.main.R.drawable;
import org.mshare.main.R.id;
import org.mshare.main.R.layout;


import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * TODO 如果有更好的方法用来更新FileAdapter中的内容，已经刷新GridView中的内容就更好了
 * @author HM
 */
public class MShareFileAdapter extends BaseAdapter {
	private static final String TAG = MShareFileAdapter.class.getSimpleName();
	
	// 上下文对象
	private Context context = null;
	// 所显示的文件数组
	private FileBrowserFile[] files = null;
	// 所有图标
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	// 所有图标是否加载完毕
	private static boolean DRAWABLE_PREPARED = false;
	
	public MShareFileAdapter(Context context, FileBrowserFile[] files) {
		super();
		this.context = context;
		this.files = files;
		initDrawable(context);
	}
	
	/**
	 * 初始化所有图标
	 */
	private static void initDrawable(Context context) {
		if (DRAWABLE_PREPARED) {
			Log.d(TAG, "drawables have already prepared, do nothing");
			return;
		}
		
		// 音乐文件
		DRAWABLES.put(".mp3", getResourceDrawable(context, R.drawable.music));
		DRAWABLES.put(".wav", getResourceDrawable(context, R.drawable.music));
		DRAWABLES.put(".wma", getResourceDrawable(context, R.drawable.music));
		DRAWABLES.put(".aac", getResourceDrawable(context, R.drawable.music));
		
		// 工作文件
		DRAWABLES.put(".pdf", getResourceDrawable(context, R.drawable.pdf));
		DRAWABLES.put(".doc", getResourceDrawable(context, R.drawable.doc));
		DRAWABLES.put(".ppt", getResourceDrawable(context, R.drawable.ppt));
		
		// 文本文件
		DRAWABLES.put(".txt", getResourceDrawable(context, R.drawable.txt));
		DRAWABLES.put(".xml", getResourceDrawable(context, R.drawable.xml));
		
		// 默认,所有其他的文件
		DRAWABLES.put("file", getResourceDrawable(context, R.drawable.all));
		// 文件夹
		DRAWABLES.put("directory", getResourceDrawable(context, R.drawable.folder));
		
		Log.d(TAG, "drawables have already prepared");
		// set the flag to true
		DRAWABLE_PREPARED = true;
	}
	/**
	 * 用于获得资源文件中的Drawable
	 * @return
	 */
	private static Drawable getResourceDrawable(Context context, int resId) {
		Drawable drawable = context.getResources().getDrawable(resId);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		Log.d(TAG, "get drawable");
		return drawable;
	}
	
	@Override
	public int getCount() {
		return files.length;
	}

	@Override
	public Object getItem(int position) {
		return files[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView != null) {
			ItemContainer item = (ItemContainer)convertView.getTag();
			if (item != null) {
				// 更新内容
				item.file = files[position];
				item.fileName.setText(files[position].getName());
				item.fileIcon.setImageDrawable(getDrawable(item.file));
			}
		} else { // 第一次使用的convertView
			convertView = LayoutInflater.from(context).inflate(R.layout.file_browser_item, null);
			
			// create content
			ItemContainer item = new ItemContainer();
			
			// 设置文件内容和对应的图标
			item.file = files[position];
			item.fileIcon = (ImageView)convertView.findViewById(R.id.item_file_image);
			item.fileName = (TextView)convertView.findViewById(R.id.item_file_name);
			item.fileName.setTextColor(Color.BLACK);
			item.fileName.setText(item.file.getName());
			item.fileIcon.setImageDrawable(getDrawable(item.file));
			
			// save content
			convertView.setTag(item);
		}
		
		return convertView;
	}
	
	/**
	 * 获得文件所对应的显示图标
	 * @param file
	 * @return
	 */
	private Drawable getDrawable(FileBrowserFile file) {
		if (file == null) {
			Log.e(TAG, "file is null");
			return null;
		}
		
		Drawable drawable = null;
		
		if (file.isFile()) {
			String extname = getExtname(file.getName());
			if (extname.equals("") || !DRAWABLES.containsKey(extname)) {
				drawable = DRAWABLES.get("file");
			} else {
				drawable = DRAWABLES.get(extname);
			}
		} else if (file.isDirectory()) {
			drawable = DRAWABLES.get("directory");
		}
		
		return drawable; 
	}

	/**
	 * 获得文件的扩展名
	 * @return
	 */
	public static String getExtname(String fileName) {
		int subStart = fileName.lastIndexOf(".");
		
		if (subStart != -1) {
			return fileName.substring(subStart);
		} else {
			return "";
		}
	}
	
	/**
	 * GridView中的Item所包含的内容
	 */
	public class ItemContainer {
		public ImageView fileIcon = null;
		/**
		 * 对应GridView中的TextView内容
		 */
		public TextView fileName = null;
		/**
		 * 和TextView相对应的file文件
		 */
		public FileBrowserFile file = null;
	}
	
}
