package org.mshare.file;

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
public class FileAdapter extends BaseAdapter {
	private static final String TAG = FileAdapter.class.getSimpleName();
	
	private Context context = null;
	/**
	 * 所需要显示的文件数组
	 */
	private MShareFile[] files = null;
	private MShareFileBrowser fileBrowser;
	/**
	 * 保存所有支持类型的图片，静态类型以提供给所有的FileAdapter实例使用，因为刷新GridView的时候将生成新的FileAdapter
	 */
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	/**
	 * 所有的DRAWABLE是否都已经加载好了
	 */
	private static boolean DRAWABLE_PREPARED = false;
	
	/**
	 * 
	 * @param context 上下文对象
	 * @param fileBrowser 文件浏览器的引用，当图标LongClick事件触发的时候，设置fileBrowser当前被选定的内容
	 * @param files 所需要显示的内容
	 */
	public FileAdapter(Context context, MShareFileBrowser fileBrowser, MShareFile[] files) {
		super();
		this.context = context;
		this.fileBrowser = fileBrowser;
		this.files = files;
		initDrawable(context);
	}
	
	/**
	 * 初始化所有的drawable，即所有需要在GridView中显示的图片
	 */
	private static void initDrawable(Context context) {
		if (DRAWABLE_PREPARED) {
			return;
		}
		
		// 音乐文件
		DRAWABLES.put(".mp3", getResourceDrawable(context, R.drawable.mp3));
		DRAWABLES.put(".wav", getResourceDrawable(context, R.drawable.wav));
		DRAWABLES.put(".wma", getResourceDrawable(context, R.drawable.wma));
		DRAWABLES.put(".aac", getResourceDrawable(context, R.drawable.aac));
		
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
			convertView = (View)LayoutInflater.from(context).inflate(R.layout.grid_item, null);
			
			convertView.setClickable(true);
			// 为convertView设置LongClick响应
			convertView.setLongClickable(true);
			convertView.setOnLongClickListener(new OnItemLongClickListener());
			
			// create content
			ItemContainer item = new ItemContainer();
			
			// 设置文件内容和对应的图标
			item.file = files[position];
			item.fileIcon = (ImageView)convertView.findViewById(R.id.item_file_image);
			item.fileName = (TextView)convertView.findViewById(R.id.item_file_name);
			item.fileName.setTextColor(Color.BLACK);
			item.fileName.setText(item.file.getDisplayName());
			item.fileIcon.setImageDrawable(getDrawable(item.file));
			item.fileName.setClickable(false);
			item.fileIcon.setClickable(false);
			
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
	private Drawable getDrawable(MShareFile file) {
		if (file == null) {
			Log.e(TAG, "file is null");
			return null;
		}
		
		Drawable drawable = null;
		
		if (file.isFile()) {
			String extname = file.getExtname();
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
		public MShareFile file = null;
	}
	
	/**
	 * 当图标被长按时触发，将设置FileBrowser当前这被选中的内容
	 * @author HM
	 *
	 */
	private class OnItemLongClickListener implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			// 针对convertView进行处理
			Object tag = v.getTag();
			if (tag == null || !(tag instanceof ItemContainer)) {
				Log.e(TAG, "null tag or invalid tag");
				return false;
			}
			// 可能会出现问题
			ItemContainer item = (ItemContainer)tag;
			fileBrowser.setSelectFile(item.file);
			Log.d(TAG, "set select file : " + item.file.getAbsolutePath());
			
			// TODO 设置成false，是这样?
			return false;
		}
		
	}
	
}
