package org.mshare.file;

import java.io.File;
import java.util.Map;

import org.mshare.main.*;
import org.mshare.file.MShareFileAdapter.ItemContainer;
import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.mshare.main.MShareUtil;
/**
 * �ļ������
 * ����GridView��ͼ��ĵ���¼� {@link GridViewItemClickListener}
 * @author HM
 *
 */
public class MShareFileBrowser extends BroadcastReceiver implements MShareCrumbController.OnCrumbClickListener {

	private static final String TAG = MShareFileBrowser.class.getSimpleName();
	// ����ContextMenu�е�groupId
	// TODO ��ʱ���������ʹ��������MainActivity��
	public static final int CONTEXT_MENU_ITEM_ID_SHARE = 4;
	public static final int CONTEXT_MENU_ITEM_ID_UNSHARE = 5;
	
	private Context context = null;
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
	private Button backBtn;
	/**
	 * ��Ŀ¼·��������չ�洢·��
	 */
	private MShareFile rootFile;
	
	/**
	 * ������ʾ������
	 */
	private MShareFile[] files;
	
	private boolean isLongClicked = true;
	
	private boolean enable;
	/**
	 * ��ǰ������ѡ�������
	 * ��Ҫ��֤�õ���ʱ�ĸ���
	 * TODO �ں�ʱɾ������
	 */
	private MShareFile selectFile = null;
	
	public MShareFileBrowser(Context context, ViewGroup container, String rootPath) {
		this.context = context;
		this.container = container;
		this.rootFile = new MShareFile(rootPath);
	}
	
	public View getView() {
		// �ļ����������
		View fileBrowserLayout = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);
		
		fileBrowserLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "file browser is clicked");
			}
		});
		
		// ���ú��˰�ť
		backBtn = (Button)(fileBrowserLayout.findViewById(R.id.crumb_back_button));
		backBtn.setOnClickListener(new BackBtnListener(context));
		
		// TODO ʹ��include��ǩ
		LinearLayout crumbContainer = (LinearLayout)(fileBrowserLayout.findViewById(R.id.crumb_container));
		
		// ���м����������
		crumbController = new MShareCrumbController(context, rootFile, crumbContainer);
		crumbController.setOnCrumbClickListener(this);
		
		// ��ø�Ŀ¼�µ��ļ��б�
		MShareFile[] files = crumbController.getFiles();
		// create grid view
		gridView = (GridView)(fileBrowserLayout.findViewById(R.id.grid_view));
		gridView.setOnItemLongClickListener(new LongListener());
		gridView.setOnItemClickListener(new GridViewItemClickListener(context));
		
		
		// ��setOnContextMenuCreateListener
		
		// TODO ���ܲ����Ǻܺõ�ע��ContextMenu�ķ�������Ϊ��Ҫ��context��ΪActivity��ʹ��
		((Activity)context).registerForContextMenu(gridView);
		
		// �����չ�洢�Ƿ����
		setEnabled(StateController.getExternalStorageState() == StateController.STATE_EXTERNAL_STORAGE_ENABLE);
		if (!isEnabled()) {
			Toast.makeText(context, R.string.external_storage_removed, Toast.LENGTH_SHORT).show();
			return null;
		} else {
			// set adapter
			adapter = new MShareFileAdapter(context, this, files); 
			gridView.setAdapter(adapter);
			return fileBrowserLayout;
		}
	}
	
	/**
	 * ���õ�ǰ��ѡ�е��ļ�����
	 * @param file
	 */
	public void setSelectFile(MShareFile file) {
		selectFile = file;
	}
	
	/**
	 * ��õ�ǰ���ڹ�����ļ�����������ContextMenu�У���������²���֤��Ϣ�ļ�ʱ��
	 * @return �ڷ�ContextMenu������µ��ÿ�����null
	 */
	public MShareFile getSelectFile() {
		return selectFile;
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
	 * ˢ���õ���Ҫ����
	 */
	public void refresh() {
		if (isEnabled()) {
			refreshGridView();
		} else {
			
			crumbController.clean();
			// ��GridView�е���������Ϊ��
			refreshGridView(new MShareFile[0]);
			Toast.makeText(context, "��չ�洢������", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * ˢ��GridView��Ĭ��ʹ�����м�����е�����
	 */
	public void refreshGridView() {
		refreshGridView(crumbController.getSelectedFile());
	}
	/**
	 * ˢ��GridView
	 * @param file
	 */
	public void refreshGridView(MShareFile file) {
		refreshGridView(file.getFiles());
	}
	
	/**
	 * ˢ��GridView������������
	 * ˢ�µ���Ҫ���� 
	 * @param files
	 */
	public void refreshGridView(MShareFile[] files) {
		// ���õ�ǰ�����ļ�������е�����
		this.files = files;
		
		// ���õ�ǰ����ˢ�µ��Ƿ��ǹ�����ļ�
		for (int i = 0; i < files.length; i++) {
			MShareFile file = files[i];
//			FsService.isFile
		}
		
		// �µ�������������ˢ��GridView
		adapter = new MShareFileAdapter(context, this, files);
		gridView.setAdapter(adapter);
		
		// ���õ������˰�ť����ʽ�����Ƿ���Ա�����
		if (!crumbController.canPop()) {
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
	
	/**
	 * ��һ���ļ�����Ϊ�����
	 * @param file
	 */
	public void setFileShared(MShareFile file, boolean shared) {
		String filePath = file.getAbsolutePath();
		// TODO ������SharedPreference����¼
		// ��ǰ��¼�û������ö���
		SharedPreferences sp = context.getSharedPreferences("username", Context.MODE_PRIVATE);
		// �жϵ�ǰ�ļ�·���Ƿ��ǹ����
		// ���Ի�����е�����
		Map<String, Boolean> map = (Map<String, Boolean>)sp.getAll();
		
		boolean isShared = sp.getBoolean(filePath, false);
		
		if (isShared != shared) {
			Editor editor = sp.edit();
			editor.putBoolean(filePath, shared);
			editor.commit();
		}
	}
	
	/**
	 * ������Ӧ�����м�����е����ݱ����ʱ���¼�
	 * @param selected
	 * @param name
	 */
	@Override
	public void onCrumbClick(int selected, String name) {
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
			if (isLongClicked) {
				Log.d(TAG, "is LongClick!");
				isLongClicked = false;
				return;
			}
			Log.d(TAG, "item click!");
			Object tag = view.getTag();
			
			if (tag != null) {
				ItemContainer item = (ItemContainer)tag; 
				MShareFile file = item.file;
				if (file.isDirectory()) { // whether is a directory
					
					if (file != null && file.canRead()) { // �ļ��п��Դ�
						pushCrumb(file);
						refreshGridView(file);
					} else {
						// TODO �ļ����޷��򿪣�������Ȩ������
						Toast.makeText(context, "�ļ����޷�����", Toast.LENGTH_SHORT).show();
					}
				} else {
					Log.d(TAG, "���������һ���ļ�");
					// ���ļ�
				}
			} else {
				Log.e(TAG, "tag��null");
				// error
			}
		}
	}
	
	private class LongListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {

			// ������
			MShareFile file = files[position];
			setSelectFile(file);
			Log.d(TAG, "set select file : " + file.getAbsolutePath());
			
			// ����longclick����click��������
			isLongClicked = true;
			
			// TODO ���ó�false��������?
			return false;
		}
		
	}
	
	/**
	 * ���˰�ť�ļ�����
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

	/**
	 * <p>������չ�洢��״̬</p>
	 * TODO ������ʹ��{@link ExternalStorageStateReceiver}������
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
