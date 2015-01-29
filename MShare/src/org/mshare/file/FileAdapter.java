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
 * TODO drawable�������������Ϊ3������¿��ܲ�̫�ã����ó�4�ɣ�������Ҫ����һЩʲô�����أ�
 */
public class FileAdapter extends BaseAdapter {
	private static final String TAG = "FileAdapter";
	
	/**
	 * �����Ķ���
	 */
	private Context context = null;
	/**
	 * ����Ҫ��ʾ���ļ�����
	 */
	private MShareFile[] files = null;
	/**
	 * ��ʾ���ļ�������е�ͼƬ
	 */
	private Drawable drawable = null;
	/**
	 * ��������֧�����͵�ͼƬ����̬�������ṩ�����е�FileAdapterʵ��ʹ�ã���Ϊˢ��GridView��ʱ�������µ�FileAdapter
	 */
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	/**
	 * 
	 */
	private static boolean DRAWABLE_PREPARED = false;
	
	/**
	 * GridView�е�Item������������
	 * @author HM
	 *
	 */
	public class ItemContainer {
		/**
		 * ��ӦGridView�е�TextView����
		 */
		public TextView fileNameView = null;
		/**
		 * ��TextView���Ӧ��file�ļ�
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
	 * ��ʼ�����е�drawable����������Ҫ��GridView����ʾ��ͼƬ
	 */
	private static void initDrawable(Context context) {
		if (DRAWABLE_PREPARED) {
			return;
		}
		
		// �����ļ�
		DRAWABLES.put(".mp3", getResourceDrawable(context, R.drawable.mp3));
		DRAWABLES.put(".wav", getResourceDrawable(context, R.drawable.wav));
		DRAWABLES.put(".wma", getResourceDrawable(context, R.drawable.wma));
		DRAWABLES.put(".aac", getResourceDrawable(context, R.drawable.aac));
		
		// �����ļ�
		DRAWABLES.put(".pdf", getResourceDrawable(context, R.drawable.pdf));
		DRAWABLES.put(".doc", getResourceDrawable(context, R.drawable.doc));
		DRAWABLES.put(".ppt", getResourceDrawable(context, R.drawable.ppt));
		
		// �ı��ļ�
		DRAWABLES.put(".txt", getResourceDrawable(context, R.drawable.txt));
		DRAWABLES.put(".xml", getResourceDrawable(context, R.drawable.xml));
		
		// Ĭ��
		DRAWABLES.put("file", getResourceDrawable(context, R.drawable.all));
		// �ļ���
		DRAWABLES.put("directory", getResourceDrawable(context, R.drawable.folder));
		
		// set the flag to true
		DRAWABLE_PREPARED = true;
	}
	/**
	 * ���ڻ����Դ�ļ��е�Drawable
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
		} else { // ��һ��ʹ�õ�convertView
			convertView = (View)LayoutInflater.from(context).inflate(R.layout.grid_item, null);
			
			// create content
			ItemContainer item = new ItemContainer();
			
			// �����ļ����ݺͶ�Ӧ��ͼ��
			item.file = files[position];
			item.fileNameView = (TextView)convertView.findViewById(R.id.item_file_name);
			
			Drawable icon = getDrawable(item.file);
			
			String displayName = item.file.getDisplayName();
//			paint
			
			Rect bound = new Rect();
			item.fileNameView.getPaint().getTextBounds(displayName, 0, displayName.length(), bound);
			
			// ����padding�����õľ����ֵ����ݵ�ƫ��
			
			int iconWidth = icon.getIntrinsicWidth();
			// ���ĳ���
			int maxTextWidth = (int)(iconWidth * 2);
			// ��С����
			int minTextWidth = (int)(iconWidth * 1.1);
			// �����Ե��ļ��ĳ���
			int textWidth = bound.width();
			// ��Ҫ���õĳ���
			int resultWidth = (int)(iconWidth * 1.1);
			if (iconWidth < textWidth && textWidth < maxTextWidth) {
				resultWidth = (int)(textWidth * 1.1);
			} else if (textWidth >= maxTextWidth) {
				resultWidth = maxTextWidth;
			} else if (textWidth <= iconWidth) {
				resultWidth = minTextWidth;
				// ��Ҫ�ÿո�������
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
	 * ����ļ�����Ӧ����ʾͼ��
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
