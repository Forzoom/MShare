package org.mshare.file.browser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.mshare.main.MShareApp;
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
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * TODO 如果有更好的方法用来更新FileAdapter中的内容，已经刷新GridView中的内容就更好了
 * @author HM
 */
public class MShareFileAdapter extends BaseAdapter {
	private static final String TAG = MShareFileAdapter.class.getSimpleName();
	
	private Context context;
	// 所显示的文件数组
	private FileBrowserFile[] files = null;
	// 所有图标
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	// 所有已选择内容图标
	private static HashMap<String, Drawable> DRAWABLES_SELECTED = new HashMap<String, Drawable>();
	// 所有图标是否加载完毕
	private static boolean DRAWABLE_PREPARED = false;
	// 用于保存ItemContainer
	private ItemContainer[] itemContainers;
	
	public MShareFileAdapter(Context context, FileBrowserFile[] files) {
		super();
		this.context = context;
		this.files = files;
		this.itemContainers = new ItemContainer[files.length];
		initDrawable();
	}
	
	/**
	 * 初始化所有图标
	 */
	private static void initDrawable() {
		if (DRAWABLE_PREPARED) {
			Log.d(TAG, "drawables have already prepared, do nothing");
			return;
		}
		
		// 音乐文件
		DRAWABLES.put(".mp3", getResourceDrawable(R.drawable.music));
		DRAWABLES.put(".wav", getResourceDrawable(R.drawable.music));
		DRAWABLES.put(".wma", getResourceDrawable(R.drawable.music));
		DRAWABLES.put(".aac", getResourceDrawable(R.drawable.music));
		
		// 工作文件
//		DRAWABLES.put(".pdf", getResourceDrawable(R.drawable.pdf));
//		DRAWABLES.put(".doc", getResourceDrawable(R.drawable.doc));
//		DRAWABLES.put(".ppt", getResourceDrawable(R.drawable.ppt));
		
		// 文本文件
//		DRAWABLES.put(".txt", getResourceDrawable(R.drawable.txt));
//		DRAWABLES.put(".xml", getResourceDrawable(R.drawable.xml));
		
		// 默认,所有其他的文件
		DRAWABLES.put("file", getResourceDrawable(R.drawable.all));
		// 文件夹
		DRAWABLES.put("directory", getResourceDrawable(R.drawable.folder));
		
		// 音乐文件
		DRAWABLES_SELECTED.put(".mp3", getResourceDrawable(R.drawable.music_selected));
		DRAWABLES_SELECTED.put(".wav", getResourceDrawable(R.drawable.music_selected));
		DRAWABLES_SELECTED.put(".wma", getResourceDrawable(R.drawable.music_selected));
		DRAWABLES_SELECTED.put(".aac", getResourceDrawable(R.drawable.music_selected));
		
		// 工作文件
//		DRAWABLES_SELECTED.put(".pdf", getResourceDrawable(R.drawable.pdf));
//		DRAWABLES_SELECTED.put(".doc", getResourceDrawable(R.drawable.doc));
//		DRAWABLES_SELECTED.put(".ppt", getResourceDrawable(R.drawable.ppt));
		
		// 文本文件
//		DRAWABLES_SELECTED.put(".txt", getResourceDrawable(R.drawable.txt));
//		DRAWABLES_SELECTED.put(".xml", getResourceDrawable(R.drawable.xml));
		
		// 默认,所有其他的文件
		DRAWABLES_SELECTED.put("file", getResourceDrawable(R.drawable.all_selected));
		// 文件夹
		DRAWABLES_SELECTED.put("directory", getResourceDrawable(R.drawable.folder_selected));
		
		Log.d(TAG, "drawables have already prepared");
		// set the flag to true
		DRAWABLE_PREPARED = true;
	}
	/**
	 * 用于获得资源文件中的Drawable
	 * @return
	 */
	private static Drawable getResourceDrawable(int resId) {
		Drawable drawable = MShareApp.getAppContext().getResources().getDrawable(resId);
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
			// TODO 需要测试，是否可以使用？
			int lastPosition = (Integer)convertView.getTag();
			// 获得原本的ItemContainer
			ItemContainer item = itemContainers[lastPosition];
			
			// 更新内容
			item.file = files[position];
			item.fileName.setText(files[position].getName());
			// TODO 在这里的刷新是有问题的
			item.fileIcon.setImageDrawable(getCommonDrawable(item.file));
			
			// 放在新的位置
			itemContainers[lastPosition] = null;
			itemContainers[position] = item;
			
			// 保存新的position
			convertView.setTag(position);
		} else { // 第一次使用的convertView
			convertView = LayoutInflater.from(context).inflate(R.layout.file_browser_item, null);
			
			// 创建ItemContainer
			ItemContainer item = new ItemContainer();
			
			// 设置文件内容和对应的图标
			item.file = files[position];
			item.fileIcon = (ImageView)convertView.findViewById(R.id.item_file_image);
			item.fileName = (TextView)convertView.findViewById(R.id.item_file_name);
			item.fileName.setTextColor(Color.BLACK);
			item.fileName.setText(item.file.getName());
			item.fileIcon.setImageDrawable(getCommonDrawable(item.file));
			
			// 保存ItemContainer
			itemContainers[position] = item;
			convertView.setTag(position);
		}
		
		return convertView;
	}
	
	public static Drawable getCommonDrawable(FileBrowserFile file) {
		return getDrawable(file, DRAWABLES);
	}
	
	public static Drawable getUnselectedDrawable(FileBrowserFile file) {
		return getDrawable(file, DRAWABLES);
	}
	
	public static Drawable getSelectedDrawable(FileBrowserFile file) {
		return getDrawable(file, DRAWABLES_SELECTED);
	}
	
	/**
	 * 获得文件所对应的显示图标
	 * @param file
	 * @return
	 */
	public static Drawable getDrawable(FileBrowserFile file, HashMap<String, Drawable> drawables) {
		if (file == null) {
			Log.e(TAG, "file is null");
			return null;
		}
		
		Drawable drawable = null;
		
		if (file.isFile()) {
			String extname = getExtname(file.getName());
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

	// 获得ItemContainer
	public ItemContainer getItemContainers(int position) {
		if (position < itemContainers.length) {
			return itemContainers[position];
		} else {
			return null;
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
