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
 * how to refresh the content
 * @author HM
 * TODO drawable的线条宽度设置为3的情况下可能不太好，设置成4吧，到底需要考虑一些什么因素呢？
 */
public class FileAdapter extends BaseAdapter {
	private static final String TAG = "FileAdapter";
	
	/**
	 * 上下文对象
	 */
	private Context context = null;
	/**
	 * 所需要显示的文件数组
	 */
	private MShareFile[] files = null;
	/**
	 * 保存所有支持类型的图片，静态类型以提供给所有的FileAdapter实例使用，因为刷新GridView的时候将生成新的FileAdapter
	 */
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	/**
	 * 所有的DRAWABLE是否都已经加载好了
	 */
	private static boolean DRAWABLE_PREPARED = false;
	
	private ArrayList<View> convertViews;
	
	/**
	 * GridView中的Item所包含的内容
	 * @author HM
	 *
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
	
	public FileAdapter(Context context, MShareFile[] files) {
		super();
		this.context = context;
		this.files = files;
		
		convertViews = new ArrayList<View>();
		
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
		
		// 默认
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
				// 更新file
				item.file = files[position];
				item.fileName.setText(files[position].getName());
				item.fileIcon.setImageDrawable(getDrawable(item.file));
			}
		} else { // 第一次使用的convertView
			convertView = (View)LayoutInflater.from(context).inflate(R.layout.grid_item, null);
			
			// TODO 添加到ArrayList中以便释放，如果有更好的方法来使用LongClickListener就好了
			convertViews.add(convertView);
			
			// TODO 为convertView设置长按响应，但是资源没有办法得到释放，而且如果不是一个Activity注册的，那么就会出问题
			// 因为没有办法手动开启一个ContextMenu
			((Activity)context).registerForContextMenu(convertView);
			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					
					return false;
				}
			});
			
			// create content
			ItemContainer item = new ItemContainer();
			
			// 设置文件内容和对应的图标
			item.file = files[position];
			item.fileIcon = (ImageView)convertView.findViewById(R.id.item_file_image);
			item.fileName = (TextView)convertView.findViewById(R.id.item_file_name);
			item.fileName.setTextColor(Color.BLACK);
			Drawable icon = getDrawable(item.file);
			String displayName = item.file.getDisplayName();
			item.fileName.setText(displayName);
			item.fileIcon.setImageDrawable(getDrawable(item.file));
			int maxTextWidth = (int)(icon.getIntrinsicWidth() * 1.5);
			item.fileName.setWidth(maxTextWidth);
			
			// register for long click
			
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
	 * 释放所有的convertView的LongClick
	 * 在确认该FileAdapter不使用前不要调用
	 */
	public void release() {
		// 上下文
		Activity activity = (Activity)context;
		for (int i = 0, len = convertViews.size(); i < len; i++) {
			View view = convertViews.get(i);
			activity.unregisterForContextMenu(view);
		}
	}
}
