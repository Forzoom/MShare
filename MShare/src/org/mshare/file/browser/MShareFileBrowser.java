package org.mshare.file.browser;

import java.util.Map;

import org.mshare.main.*;
import org.mshare.file.browser.MShareFileAdapter.ItemContainer;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * �ļ������
 * ����GridView��ͼ��ĵ���¼� {@link GridViewItemClickListener}
 * ��������м�������¼���������ļ��е�ʱ���Զ������ļ���
 * @author HM
 *
 */
public class MShareFileBrowser extends BroadcastReceiver implements MShareCrumbController.OnCrumbClickListener {
	private static final String TAG = MShareFileBrowser.class.getSimpleName();
	
	private Context context = null;
	// ���ڰ����ļ��������container
	private ViewGroup container = null;
	/**
	 * ���м�����Ŀ�����
	 */
	private MShareCrumbController crumbController;
	/**
	 * GridView����Ӧ��������
	 */
	private MShareFileAdapter adapter;
	/**
	 * ��Ҫ��ʾ��GridView
	 */
	private GridView gridView;
	/**
	 * ���˰�ť
	 */
	private ImageView backButton;
	/**
	 * ��Ŀ¼·��
	 */
	private FileBrowserFile rootFile;
	/**
	 * ��ǰ��ʾ������
	 */
	private FileBrowserFile[] currentFiles;
	
	// �ص�����
	private FileBrowserCallback callback;

	private View fileBrowserLayout;
	
	private boolean enable;
	
	public MShareFileBrowser(Context context, ViewGroup container, FileBrowserFile rootFile) {
		this.context = context;
		this.container = container;
		this.rootFile = rootFile;
		setEnabled(true);
		init();
	}
	
	private void init() {
		// �ļ����������
		fileBrowserLayout = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);
		
		// ���ú��˰�ť
		backButton = (ImageView)(fileBrowserLayout.findViewById(R.id.crumb_back_button));
		backButton.setOnClickListener(new BackButtonListener());
		
		// TODO ʹ��include��ǩ
		LinearLayout crumbContainer = (LinearLayout)(fileBrowserLayout.findViewById(R.id.crumb_container));
		
		// ���м����������
		crumbController = new MShareCrumbController(crumbContainer, rootFile);
		crumbController.setOnCrumbClickListener(this);
		
		// create grid view
		gridView = (GridView)(fileBrowserLayout.findViewById(R.id.grid_view));
		gridView.setOnItemLongClickListener(new GridViewItemLongClickListener());
		gridView.setOnItemClickListener(new GridViewItemClickListener());
		refresh();
	}
	
	/**
	 * ������SD���Ƿ���ã���SD�������õ�ʱ�򣬽����null
	 * @return
	 */
	public View getView() {
		return fileBrowserLayout;
	}
	
	/**
	 * ���õ�ǰ���ļ�������Ƿ����
	 * һ������չ�洢�����õ�ʱ�򣬽��ļ����������Ϊ������
	 * @return
	 */
	public boolean isEnabled() {
		return this.enable;
	}
	/**
	 * ������չ�洢�Ƿ����
	 * @param enable
	 */
	public void setEnabled(boolean enable) {
		this.enable = enable;
	}
	
	/**
	 * ˢ���õ���Ҫ���������������м���������������GridView�е�����
	 */
	public void refresh() {
		if (isEnabled()) {
			refreshGridView(crumbController.getSelectedFile().listFiles());
		} else {
			crumbController.clean();
			// ��GridView�е������ÿ�
			refreshGridView(new FileBrowserFile[0]);
			Toast.makeText(context, "��չ�洢������", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ˢ��GridView������������
	 * @param currentFiles
	 */
	private void refreshGridView(FileBrowserFile[] files) {
		// ���õ�ǰ�����ļ�������е�����
		this.currentFiles = files;
		
		// �µ�������������ˢ��GridView
		adapter = new MShareFileAdapter(context, files);
		gridView.setAdapter(adapter);
		
		// ���õ������˰�ť����ʽ�����Ƿ���Ա�����
		if (!crumbController.canPop()) {
			backButton.setClickable(false);
		} else {
			backButton.setClickable(true);
		}
	}
	
	/**
	 * �����м����������µĵ�������
	 * @param file ��ӵ����м�����е�������
	 */
	public void pushCrumb(FileBrowserFile file) {
		int index = crumbController.push(file);
		crumbController.unselect();
		crumbController.select(index);
	}
	/**
	 * ������ǰѡ������м����
	 * @return
	 */
	public void popCrumb() {
		int index = crumbController.pop();
		crumbController.select(index - 1);
	}
	
	public GridView getGridView() {
		return gridView;
	}

	public FileBrowserFile[] getCurrentFiles() {
		return currentFiles;
	}
	
	// ����callback
	public void setCallback(FileBrowserCallback callback) {
		this.callback = callback;
	}
	
	/**
	 * ������Ӧ�����м�����е����ݱ����ʱ���¼�
	 * @param selected
	 * @param name
	 */
	@Override
	public void onCrumbClick(int selected, String name) {
		// ��Ϊ���м�����ݣ������м�е�Button�Ѿ�����ά���ˣ����ԣ�����ֻ��Ҫˢ��һ�¾ͺ���
		refresh();
	}

	/**
	 * ������ӦGridView��Item����Ӧ�¼�
	 */
	private class GridViewItemClickListener implements AdapterView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemClick invoke!");
			Object tag = view.getTag();
			
			if (tag == null) {
				Log.e(TAG, "tag��null");
				return;
			}

			ItemContainer item = (ItemContainer)tag; 
			FileBrowserFile file = item.file;
			if (file.isDirectory()) { // whether is a directory
				
				if (file != null && file.canRead()) { // �ļ��п��Դ�
					pushCrumb(file);
					refresh();
				} else {
					Log.e(TAG, "�ļ����޷�����");
				}
			} else {
				Log.d(TAG, "���������һ���ļ�");
			}
			
			// ���Ը�֪File������ˣ��������ļ������ļ���
			if (callback != null) {
				callback.onItemClick(file);
			}
		}
	}
	
	private class GridViewItemLongClickListener implements AdapterView.OnItemLongClickListener {

		// ���õ�ǰ��ѡ����ļ�
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemLongClick invoke!");
			FileBrowserFile file = currentFiles[position];
			Log.d(TAG, "set select file : " + file.getAbsolutePath());

			if (callback != null) {
				callback.onItemLongClick(file);
			}
			
			return false;
		}
		
	}
	
	/**
	 * ���˰�ť�ļ�����
	 * @author HM
	 *
	 */
	private class BackButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Log.v(TAG, "crumb item is clicked");
			popCrumb();
			refresh();
		}
		
	}

	/**
	 * <p>������չ�洢��״̬</p>
	 * TODO ������ʹ��{@link ExternalStorageStatusReceiver}������
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // ��չ�����γ�
			setEnabled(false);
			refresh();
		} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // ��չ������ʹ��
			setEnabled(true);
			refresh();
		}
	}	
}
