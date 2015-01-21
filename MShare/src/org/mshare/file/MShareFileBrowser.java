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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * �����
 * @author HM
 *
 */
public class MShareFileBrowser implements MShareCrumbController.OnItemClickListener {

	private static final String TAG = "MShareFileBrowser";
	
	private Context context = null;
	private ViewGroup container = null;
	/**
	 * ���м�����Ŀ�����
	 */
	private MShareCrumbController mShareCrumbs = null;
	/**
	 * GridView����Ӧ��������
	 */
	private FileAdapter adapter = null;
	/**
	 * ��Ҫ��ʾ��GridView
	 */
	private GridView gridView = null;
	/**
	 * ���˰�ť
	 */
	private Button backBtn = null;
	
	public MShareFileBrowser(Context context, ViewGroup container) {
		this.context = context;
		this.container = container;
	}
	
	public View getView() {
		// file browser view
		View view = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);

		// set back button
		backBtn = (Button)(view.findViewById(R.id.crumb_back_button));
		backBtn.setOnClickListener(new BackBtnListener(context));
		
		// ��Ŀ¼·��������չ�洢·��
		File root = Environment.getExternalStorageDirectory();
		
		LinearLayout crumbContainer = (LinearLayout)(view.findViewById(R.id.crumb_container));
		
		// ���м����������
		mShareCrumbs = new MShareCrumbController(context, root, crumbContainer);
		mShareCrumbs.setOnItemClickListener(this);
		
		// ��ø�Ŀ¼�µ��ļ��б�
		MShareFile[] files = mShareCrumbs.getFiles();
		// create grid view
		gridView = (GridView)(view.findViewById(R.id.grid_view));
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
		
		// register context menu
		((Activity)context).registerForContextMenu(gridView);
		
		// check external storage useful
		if (!isExternalStorageUseful()) {
			Toast.makeText(context, "��չ�洢������", Toast.LENGTH_SHORT).show();
			return null;
		} else {
			// set adapter
			adapter = new FileAdapter(context, files); 
			gridView.setAdapter(adapter);
			return view;
		}
	}
	
	/**
	 * �����չ�洢�Ƿ���Ч
	 * @return �ɹ�ʱ����true�����򷵻�false
	 */
	public boolean isExternalStorageUseful() {
		String state = Environment.getExternalStorageState();
		// ��������չ�洢�ɶ�д��ʱ�������Ч
		return state.equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * ˢ���õ���Ҫ����
	 */
	public void refresh() {
		if (isExternalStorageUseful()) {
			refreshGridView();
		} else {
			mShareCrumbs.clean();
			refreshGridView(new MShareFile[0]);
			Toast.makeText(context, "��չ�洢������", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * refresh gridview
	 */
	public void refreshGridView() {
		refreshGridView(mShareCrumbs.get());
	}
	/**
	 * refresh gridview
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
		// �µ�������������ˢ��GridView
		adapter = new FileAdapter(context, files);
		gridView.setAdapter(adapter);
		// ���õ������˰�ť����ʽ�����Ƿ���Ա�����
		if (!mShareCrumbs.canPop()) {
			backBtn.setClickable(false);
		} else {
			backBtn.setClickable(true);
		}
	}
	
	/**
	 * �����м����������µĵ�������
	 * @param file ��ӵ����м�����е�������
	 */
	public void pushCrumb(MShareFile file) {
		mShareCrumbs.push(file);
	}
	/**
	 * pop top crumb
	 * @return
	 */
	public void popCrumb() {
		mShareCrumbs.pop();
	}
	
	/**
	 * ������Ӧ�����м�����е����ݱ����ʱ���¼�
	 * @param selected
	 * @param name
	 */
	@Override
	public void onClick(int selected, String name) {
		// TODO Auto-generated method stub
//		this.selected = selected;
		refreshGridView();
	}
	
	
	/**
	 * ������ӦGridView�е�button����Ӧ�¼�
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
					if (file.getSubFiles() != null) {
						pushCrumb(file);
						refreshGridView(file.getSubFiles());
					} else {
						// cannot open the directory
						Toast.makeText(context, "�ļ����޷�����", Toast.LENGTH_LONG).show();
					}
				} else {
					// is file, do nothing
				}
			} else {
				// error
			}
		}
	}
	
	/**
	 * temp listener for crumb last button
	 * @author HM
	 *
	 */
	private class BackBtnListener implements View.OnClickListener {

		private Context context = null;
		
		public BackBtnListener(Context context) {
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
