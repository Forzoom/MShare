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
	 * ��������֧�����͵�ͼƬ����̬�������ṩ�����е�FileAdapterʵ��ʹ�ã���Ϊˢ��GridView��ʱ�������µ�FileAdapter
	 */
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	/**
	 * ���е�DRAWABLE�Ƿ��Ѿ����غ���
	 */
	private static boolean DRAWABLE_PREPARED = false;
	
	private ArrayList<View> convertViews;
	
	/**
	 * GridView�е�Item������������
	 * @author HM
	 *
	 */
	public class ItemContainer {
		public ImageView fileIcon = null;
		/**
		 * ��ӦGridView�е�TextView����
		 */
		public TextView fileName = null;
		/**
		 * ��TextView���Ӧ��file�ļ�
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
				// ����file
				item.file = files[position];
				item.fileName.setText(files[position].getName());
				item.fileIcon.setImageDrawable(getDrawable(item.file));
			}
		} else { // ��һ��ʹ�õ�convertView
			convertView = (View)LayoutInflater.from(context).inflate(R.layout.grid_item, null);
			
			// TODO ��ӵ�ArrayList���Ա��ͷţ�����и��õķ�����ʹ��LongClickListener�ͺ���
			convertViews.add(convertView);
			
			// TODO ΪconvertView���ó�����Ӧ��������Դû�а취�õ��ͷţ������������һ��Activityע��ģ���ô�ͻ������
			// ��Ϊû�а취�ֶ�����һ��ContextMenu
			((Activity)context).registerForContextMenu(convertView);
			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					
					return false;
				}
			});
			
			// create content
			ItemContainer item = new ItemContainer();
			
			// �����ļ����ݺͶ�Ӧ��ͼ��
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
	
	/**
	 * �ͷ����е�convertView��LongClick
	 * ��ȷ�ϸ�FileAdapter��ʹ��ǰ��Ҫ����
	 */
	public void release() {
		// ������
		Activity activity = (Activity)context;
		for (int i = 0, len = convertViews.size(); i < len; i++) {
			View view = convertViews.get(i);
			activity.unregisterForContextMenu(view);
		}
	}
}
