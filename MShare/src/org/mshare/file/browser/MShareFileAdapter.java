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
 * TODO ����и��õķ�����������FileAdapter�е����ݣ��Ѿ�ˢ��GridView�е����ݾ͸�����
 * @author HM
 */
public class MShareFileAdapter extends BaseAdapter {
	private static final String TAG = MShareFileAdapter.class.getSimpleName();
	
	private Context context;
	// ����ʾ���ļ�����
	private FileBrowserFile[] files = null;
	// ����ͼ��
	private static HashMap<String, Drawable> DRAWABLES = new HashMap<String, Drawable>();
	// ������ѡ������ͼ��
	private static HashMap<String, Drawable> DRAWABLES_SELECTED = new HashMap<String, Drawable>();
	// ����ͼ���Ƿ�������
	private static boolean DRAWABLE_PREPARED = false;
	// ���ڱ���ItemContainer
	private ItemContainer[] itemContainers;
	
	public MShareFileAdapter(Context context, FileBrowserFile[] files) {
		super();
		this.context = context;
		this.files = files;
		this.itemContainers = new ItemContainer[files.length];
		initDrawable();
	}
	
	/**
	 * ��ʼ������ͼ��
	 */
	private static void initDrawable() {
		if (DRAWABLE_PREPARED) {
			Log.d(TAG, "drawables have already prepared, do nothing");
			return;
		}
		
		// �����ļ�
		DRAWABLES.put(".mp3", getResourceDrawable(R.drawable.music));
		DRAWABLES.put(".wav", getResourceDrawable(R.drawable.music));
		DRAWABLES.put(".wma", getResourceDrawable(R.drawable.music));
		DRAWABLES.put(".aac", getResourceDrawable(R.drawable.music));
		
		// �����ļ�
//		DRAWABLES.put(".pdf", getResourceDrawable(R.drawable.pdf));
//		DRAWABLES.put(".doc", getResourceDrawable(R.drawable.doc));
//		DRAWABLES.put(".ppt", getResourceDrawable(R.drawable.ppt));
		
		// �ı��ļ�
//		DRAWABLES.put(".txt", getResourceDrawable(R.drawable.txt));
//		DRAWABLES.put(".xml", getResourceDrawable(R.drawable.xml));
		
		// Ĭ��,�����������ļ�
		DRAWABLES.put("file", getResourceDrawable(R.drawable.all));
		// �ļ���
		DRAWABLES.put("directory", getResourceDrawable(R.drawable.folder));
		
		// �����ļ�
		DRAWABLES_SELECTED.put(".mp3", getResourceDrawable(R.drawable.music_selected));
		DRAWABLES_SELECTED.put(".wav", getResourceDrawable(R.drawable.music_selected));
		DRAWABLES_SELECTED.put(".wma", getResourceDrawable(R.drawable.music_selected));
		DRAWABLES_SELECTED.put(".aac", getResourceDrawable(R.drawable.music_selected));
		
		// �����ļ�
//		DRAWABLES_SELECTED.put(".pdf", getResourceDrawable(R.drawable.pdf));
//		DRAWABLES_SELECTED.put(".doc", getResourceDrawable(R.drawable.doc));
//		DRAWABLES_SELECTED.put(".ppt", getResourceDrawable(R.drawable.ppt));
		
		// �ı��ļ�
//		DRAWABLES_SELECTED.put(".txt", getResourceDrawable(R.drawable.txt));
//		DRAWABLES_SELECTED.put(".xml", getResourceDrawable(R.drawable.xml));
		
		// Ĭ��,�����������ļ�
		DRAWABLES_SELECTED.put("file", getResourceDrawable(R.drawable.all_selected));
		// �ļ���
		DRAWABLES_SELECTED.put("directory", getResourceDrawable(R.drawable.folder_selected));
		
		Log.d(TAG, "drawables have already prepared");
		// set the flag to true
		DRAWABLE_PREPARED = true;
	}
	/**
	 * ���ڻ����Դ�ļ��е�Drawable
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
			// TODO ��Ҫ���ԣ��Ƿ����ʹ�ã�
			int lastPosition = (Integer)convertView.getTag();
			// ���ԭ����ItemContainer
			ItemContainer item = itemContainers[lastPosition];
			
			// ��������
			item.file = files[position];
			item.fileName.setText(files[position].getName());
			// TODO �������ˢ�����������
			item.fileIcon.setImageDrawable(getCommonDrawable(item.file));
			
			// �����µ�λ��
			itemContainers[lastPosition] = null;
			itemContainers[position] = item;
			
			// �����µ�position
			convertView.setTag(position);
		} else { // ��һ��ʹ�õ�convertView
			convertView = LayoutInflater.from(context).inflate(R.layout.file_browser_item, null);
			
			// ����ItemContainer
			ItemContainer item = new ItemContainer();
			
			// �����ļ����ݺͶ�Ӧ��ͼ��
			item.file = files[position];
			item.fileIcon = (ImageView)convertView.findViewById(R.id.item_file_image);
			item.fileName = (TextView)convertView.findViewById(R.id.item_file_name);
			item.fileName.setTextColor(Color.BLACK);
			item.fileName.setText(item.file.getName());
			item.fileIcon.setImageDrawable(getCommonDrawable(item.file));
			
			// ����ItemContainer
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
	 * ����ļ�����Ӧ����ʾͼ��
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
	 * ����ļ�����չ��
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

	// ���ItemContainer
	public ItemContainer getItemContainers(int position) {
		if (position < itemContainers.length) {
			return itemContainers[position];
		} else {
			return null;
		}
	}

	/**
	 * GridView�е�Item������������
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
		public FileBrowserFile file = null;
	}
	
}
