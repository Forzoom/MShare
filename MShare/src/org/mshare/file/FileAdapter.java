package org.mshare.file;

import java.io.File;
import java.util.HashMap;

import org.mshare.main.R;
import org.mshare.main.R.drawable;
import org.mshare.main.R.id;
import org.mshare.main.R.layout;


import android.R.color;
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
	 * 显示在文件浏览器中的图片
	 */
	private Drawable drawable = null;
	/**
	 * 保存所有支持类型的图片，静态类型以提供给所有的FileAdapter实例使用，因为刷新GridView的时候将生成新的FileAdapter
	 */
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	/**
	 * 
	 */
	private static boolean DRAWABLE_PREPARED = false;
	
	/**
	 * GridView中的Item所包含的内容
	 * @author HM
	 *
	 */
	public class ItemContainer {
		/**
		 * 对应GridView中的TextView内容
		 */
		public TextView fileNameView = null;
		/**
		 * 和TextView相对应的file文件
		 */
		public MShareFile file = null;
	}
	
	public FileAdapter(Context context, MShareFile[] files) {
		super();
		this.context = context;
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
		} else { // 第一次使用的convertView
			convertView = (View)LayoutInflater.from(context).inflate(R.layout.grid_item, null);
			
			// create content
			ItemContainer item = new ItemContainer();
			
			// 设置文件内容和对应的图标
			item.file = files[position];
			item.fileNameView = (TextView)convertView.findViewById(R.id.item_file_name);
			
			Drawable icon = getDrawable(item.file);
			
			String displayName = item.file.getDisplayName();
//			paint
			
			Rect bound = new Rect();
			item.fileNameView.getPaint().getTextBounds(displayName, 0, displayName.length(), bound);
			
			// 将定padding所设置的就是字的内容的偏移
			
			int iconWidth = icon.getIntrinsicWidth();
			// 最大的长度
			int maxTextWidth = (int)(iconWidth * 2);
			// 最小长度
			int minTextWidth = (int)(iconWidth * 1.1);
			// 所测试的文件的长度
			int textWidth = bound.width();
			// 所要设置的长度
			int resultWidth = (int)(iconWidth * 1.1);
			if (iconWidth < textWidth && textWidth < maxTextWidth) {
				resultWidth = (int)(textWidth * 1.1);
			} else if (textWidth >= maxTextWidth) {
				resultWidth = maxTextWidth;
			} else if (textWidth <= iconWidth) {
				resultWidth = minTextWidth;
				// 需要用空格补齐内容
				Rect blankBounds = new Rect();
				String _fix = " a";
				item.fileNameView.getPaint().getTextBounds(_fix, 0, _fix.length(), blankBounds);
				int blankWidth = blankBounds.width();
				Log.v(TAG, "blankWidth : " + blankWidth);
				Log.v(TAG, "blankHeight : " + blankBounds.height());

				_fix = " a a  a";
				item.fileNameView.getPaint().getTextBounds(_fix, 0, _fix.length(), blankBounds);
				Log.v(TAG, "blankWidth : " + blankBounds.width());
				Log.v(TAG, "blankHeight : " + blankBounds.height());
				
				int fixCount = (iconWidth - textWidth) / 2 / blankWidth;
				int fixIndex = 1;
				String fix = " ";
				while (fixIndex < fixCount) {
					fix += " ";
				}
				item.file.setDisplayName(fix);
			}
			
			item.fileNameView.setWidth(resultWidth);
			
			item.fileNameView.setText(displayName);
			item.fileNameView.setCompoundDrawables(null, icon, null, null);
			item.fileNameView.setPadding(0, 20, 0, 0);
			item.fileNameView.setTextColor(Color.BLACK);
			
			Log.v(TAG, "displayName : " + displayName + bound.width() + " " + iconWidth + " " + item.fileNameView.getTextSize());
			
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
	
}
