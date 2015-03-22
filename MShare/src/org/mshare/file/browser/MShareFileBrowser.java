package org.mshare.file.browser;

import java.util.ArrayList;
import java.util.Map;

import org.mshare.main.*;
import org.mshare.file.browser.MShareFileAdapter.ItemContainer;
import org.mshare.main.R;

import android.widget.AdapterView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * �ļ������
 * ����GridView��ͼ��ĵ���¼� {@link GridViewItemClickListener}
 * ��������м�������¼���������ļ��е�ʱ���Զ������ļ���
 * 
 * contextmenu������single_select��ģʽ�¿���֧��
 * 
 * ������˳�multi_select_mode��ʱ�����е�fileIcon����ԭ�أ�
 * 
 * Ŀǰ���ڵ�ѡ����˵��û�к��ʵķ���������ֻ��Ҫ�����ص������Ϳ�����
 * 
 * @author HM
 *
 */
public class MShareFileBrowser extends LinearLayout {
	private static final String TAG = MShareFileBrowser.class.getSimpleName();

	private Context context;
	
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
	private ImageButton backButton;
	
	private ImageButton refreshButton;
	
	private Animation refreshAnimation;
	/**
	 * ��ǰ��ʾ������
	 */
	private FileBrowserFile[] currentFiles;
	
	// �ص�����
	private FileBrowserCallback callback;

	private View fileBrowserLayout;
	
	private RelativeLayout.LayoutParams coverLayoutParam;
	private LinearLayout cover;
	private RelativeLayout gridViewContainer;
	
	private boolean enable;
	private boolean isWaitForRefresh = false;
	
	private boolean isMultiSelectEnabled = false;

	public static final int MODE_SINGLE_SELECT = 1;
	public static final int MODE_MULTI_SELECT = 2;
	
	private int mode = MODE_SINGLE_SELECT;
	
	// ֻ����LongClick��ʱ��Żᱻ����
	private int selectPosition;
	// ��ʱ��ʹ��boolean
	private boolean[] multiSelectPosition;
	
	public MShareFileBrowser(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		prepare();
	}

	public MShareFileBrowser(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		prepare();
	}

	public MShareFileBrowser(Context context) {
		super(context);
		this.context = context;
		prepare();
	}
	
	// ��Ҫ�ֶ�����
	public void prepare() {
		// ��Ҫ����
		setEnabled(true);
		
		// �ļ����������
		fileBrowserLayout = LayoutInflater.from(context).inflate(R.layout.file_browser, container, false);
		
		// ���ú��˰�ť
		backButton = (ImageButton)(fileBrowserLayout.findViewById(R.id.crumb_back_button));
		backButton.setOnClickListener(new BackButtonListener());
		backButton.setClickable(false);
		
		// ˢ�°�ť
		refreshButton = (ImageButton)(fileBrowserLayout.findViewById(R.id.file_browser_refresh_button));
		refreshButton.setOnClickListener(new RefreshButtonListener());
		
		// ˢ�¶���
		refreshAnimation = AnimationUtils.loadAnimation(context, R.anim.file_browser_refresh);
		
		// ���м��������
		LinearLayout crumbContainer = (LinearLayout)(fileBrowserLayout.findViewById(R.id.crumb_container));

		// TODO ���ڴ���smoothScroll��λ��
		HorizontalScrollView scrollView = (HorizontalScrollView)fileBrowserLayout.findViewById(R.id.crumb_scroller);
		
		// ���м����������
		crumbController = new MShareCrumbController(scrollView, crumbContainer, this);
		if (callback != null) {
			crumbController.setCallback(callback);
		}
		
		// ����GridView
		gridView = (GridView)(fileBrowserLayout.findViewById(R.id.file_browser_grid_view));
		gridView.setOnItemLongClickListener(new GridViewItemLongClickListener());
		gridView.setOnItemClickListener(new GridViewItemClickListener());
		
		// GridView������
		gridViewContainer = (RelativeLayout)fileBrowserLayout.findViewById(R.id.file_browser_grid_view_container);
		
		// ����cover
		cover = new LinearLayout(context);
		cover.setClickable(true);
		cover.setLongClickable(true);
		coverLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

		// fileBrowserLayout�����е�view��������������
		addView(fileBrowserLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
	}
	
	/**
	 * ���ú��ȴ�ˢ��
	 * @param file
	 */
	public void setRootFile(FileBrowserFile file) {
		setRootFile(file, file.getName());
	}
	
	// �����Զ�������
	public void setRootFile(FileBrowserFile file, String fileRootName) {
		crumbController.push(file, fileRootName);
		crumbController.selectCrumb(0);
		waitForRefresh();
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
	 * TODO ��Ҫһ������������Activity������ʹ�ã�����չ�洢û�е�ʱ�����ô�죿
	 * @param enable
	 */
	public void setEnabled(boolean enable) {
		this.enable = enable;
	}

	// ����ˢ�¶���
	public void waitForRefresh() {
		if (isWaitForRefresh) {
			Log.e(TAG, "is already waiting!");
			return;
		}
		// ����ˢ�¶���
		refreshButton.startAnimation(refreshAnimation);
		gridViewContainer.addView(cover, coverLayoutParam);
		
		Log.d(TAG, "waiting for refresh data");
		isWaitForRefresh = true;
	}
	
	/**
	 * ˢ��GridView������������
	 * @param currentFiles
	 */
	public void refreshGridView(FileBrowserFile[] files) {
		
		if (!isEnabled()) {
			crumbController.clean();
			// ��GridView�е������ÿ�
			files = new FileBrowserFile[0];
		} else {
			// ��ȥcover
			if (isWaitForRefresh) {
				refreshButton.clearAnimation();
				gridViewContainer.removeView(cover);
				isWaitForRefresh = false;
			}
		}
		
		// ���õ�ǰ�����ļ�������е�����
		this.currentFiles = files;
		
		// ����multiSelect������
		multiSelectPosition = new boolean[files.length];
		// ����mode
		setMode(MODE_SINGLE_SELECT);
		
		// �µ�������������ˢ��GridView
		// ʹ�õĿ����ǲ����ʵ�Context
		adapter = new MShareFileAdapter(MShareApp.getAppContext(), this);
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
		crumbController.unselectCrumb();
		crumbController.selectCrumb(index);
	}
	/**
	 * ������ǰѡ������м����
	 * @return
	 */
	public void popCrumb() {
		int index = crumbController.pop();
		crumbController.selectCrumb(index - 1);
	}
	
	public FileBrowserFile[] getCurrentFiles() {
		return currentFiles;
	}
	
	// ����callback
	public void setCallback(FileBrowserCallback callback) {
		this.callback = callback;
		// ��ʱ��������Ϊ�˺�init��������˭�����õ�����£�������crumbController������callback
		if (crumbController != null) {
			crumbController.setCallback(callback);
		}
	}
	// �Ƿ�������multiSelect
	public boolean isMultiSelectEnabled() {
		return isMultiSelectEnabled;
	}
	// �����Ƿ�����
	public void setMultiSelectEnabled(boolean isMultiSelectEnabled) {
		
		// ��ǰ���ڶ�ѡģʽ,���˳���ѡģʽ
		if (isMultiSelectEnabled == false && mode == MODE_MULTI_SELECT) {
			for (int i = 0, len = multiSelectPosition.length; i < len; i++) {
				multiSelectPosition[i] = false;
			}
		}
		
		this.isMultiSelectEnabled = isMultiSelectEnabled;
	}
	
//	pub
	
	// TODO ��Ҫ��֤����ȷ��ģʽ�»����ȷ������
	// Ӧ��ֻ����SINGLE_MODE�µ���
	public int getSelectPosition() {
		return selectPosition;
	}

	public FileBrowserFile getSelectFile() {
		return currentFiles[selectPosition];
	}
	
	public FileBrowserFile[] getMultiSelectedFiles() {
		ArrayList<FileBrowserFile> arrayList = new ArrayList<FileBrowserFile>();
		for (int i = 0; i < multiSelectPosition.length; i++) {
			// ��֪��û�г�ʼ����������ǲ���false
			if (multiSelectPosition[i]) {
				arrayList.add(currentFiles[i]);
			}
		}
		FileBrowserFile[] ret = new FileBrowserFile[arrayList.size()];
		arrayList.toArray(ret);
		
		return ret;
	}
	
	public Integer[] getMultiSelectedPosition() {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int i = 0; i < multiSelectPosition.length; i++) {
			// ��֪��û�г�ʼ����������ǲ���false
			if (multiSelectPosition[i]) {
				arrayList.add(i);
			}
		}
		Integer[] ret = new Integer[arrayList.size()];
		arrayList.toArray(ret);
		return ret;
	}
	/**
	 * �ж��ļ��Ƿ�ѡ���ˣ�ʹ��SINGLE��MULTIģʽ
	 * @param position
	 * @return
	 */
	public boolean isFileSelected(int position) {
		if (mode == MODE_MULTI_SELECT) {
			
			if (position < multiSelectPosition.length) {
				return multiSelectPosition[position];
			} else {
				Log.e(TAG, "something wrong may happen! the target position is not exists");
				return false;
			}
			
		} else if (mode == MODE_SINGLE_SELECT) {
			// TODO ���ڵ�ѡȷʵ��֪������ô��ź�,����˵����ѡ�͵�ѡ�ϲ�
			return position == selectPosition;
		} else {
			Log.e(TAG, "unknown mode");
			return false;
		}
	}
	
	public void quitMultiSelectMode() {
		// ��ǰ�Ƕ�ѡģʽ
		if (this.mode == MODE_MULTI_SELECT) {
			if (adapter == null) {
				Log.e(TAG, "something must be wrong, the adapter is null!");
				return;
			}
			
			// ����ǰ�Ѿ�ѡ�����������ΪCommon
			for (int position = 0; position < multiSelectPosition.length; position++) {
				if (multiSelectPosition[position]) {
					ItemContainer item = adapter.getItemContainers(position);
					item.fileIcon.setImageDrawable(MShareFileAdapter.getCommonDrawable(currentFiles[position]));
				}
			}
			
			// ����mode
			setMode(MODE_SINGLE_SELECT);
		}
	}

	// ��õ�ǰ�ļ��������ʲôģʽ��
	public int getMode() {
		return mode;
	}
	
	private void setMode(int mode) {
		int currentMode = this.mode;
		if (mode == currentMode) {
			Log.w(TAG, "the same mode, and do nothing!");
			return;
		}
		
		if (mode == MODE_MULTI_SELECT) {
			if (isMultiSelectEnabled()) {
				// ���ѡ������
				selectPosition = -1;
				
				// ����Ϊ��ѡ
				this.mode = mode;
			} else {
				Log.e(TAG, "multiSelect mode is disabled");
				return;
			}
		} else if (mode == MODE_SINGLE_SELECT) {
			// ���ѡ������
			// ��ʱ���������
			for (int i = 0; i < multiSelectPosition.length; i++) {
				multiSelectPosition[i] = false;
			}
			
			// ����Ϊ��ѡ
			this.mode = mode;
		}
	}
	
	// ��״̬����Ϊѡ�У�����֧���ڶ�ѡģʽ��
	public boolean selectFile(int position) {
		if (getMode() == MODE_SINGLE_SELECT) {
			Log.w(TAG, "cannot invoke select in single select mode");
			return false;
		}
		if (multiSelectPosition[position]) {
			Log.w(TAG, "the file is already select! do nothing");
			return false;
		}
		
		// TODO �ж�ʹ����ȷ��tag/��Ҫ�ж�adapter�Ƿ���null?
		ItemContainer item = adapter.getItemContainers(position);
		FileBrowserFile file = currentFiles[position];
		ImageView fileIcon = item.fileIcon;
		
		fileIcon.setImageDrawable(MShareFileAdapter.getSelectedDrawable(file));
		multiSelectPosition[position] = true;
		return true;
	}
	
	// ��״̬����Ϊδѡ�У�����֧���ڶ�ѡģʽ��
	public boolean unselectFile(int position) {
		if (getMode() == MODE_SINGLE_SELECT) {
			Log.w(TAG, "cannot invoke unselect in single select mode");
			return false;
		}
		if (!multiSelectPosition[position]) {
			Log.w(TAG, "the file is already unselect! do nothing");
			return false;
		}
		
		// TODO �ж�ʹ����ȷ��tag/��Ҫ�ж�adapter�Ƿ���null?
		ItemContainer item = adapter.getItemContainers(position);
		FileBrowserFile file = currentFiles[position];
		ImageView fileIcon = item.fileIcon;
		
		fileIcon.setImageDrawable(MShareFileAdapter.getUnselectedDrawable(file));
		multiSelectPosition[position] = false;
		return true;
	}
		
	/**
	 * ������ӦGridView��Item����Ӧ�¼�
	 */
	private class GridViewItemClickListener implements AdapterView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemClick invoke!");
			
			FileBrowserFile file = currentFiles[position];
			
			if (isMultiSelectEnabled() && mode == MODE_MULTI_SELECT) {
				
				Log.d(TAG, "operate multi select mode");
				if (multiSelectPosition[position]) {
					// �ļ��Ѿ���ѡ�У����Խ�������Ϊδѡ�У�����ArrayList���Ƴ�
					unselectFile(position);
				} else {
					// �ļ�δ��ѡ�У����Խ�������Ϊѡ�У�����ArrayList
					selectFile(position);
				}

			} else {
				if (file.isDirectory()) { // whether is a directory
					
					if (file != null && file.canRead()) { // �ļ��п��Դ�
						pushCrumb(file);
						waitForRefresh();
						if (callback != null) {
							callback.onItemClick(file);
						}
					} else {
						Log.e(TAG, "�ļ����޷�����");
					}
				} else {
					Log.d(TAG, "���������һ���ļ�");
					if (callback != null) {
						callback.onItemClick(file);
					}
				}
			}
			
		}
	}
	
	private class GridViewItemLongClickListener implements AdapterView.OnItemLongClickListener {

		// ���õ�ǰ��ѡ����ļ�
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemLongClick invoke!");
			FileBrowserFile longClickFile = currentFiles[position];
			Log.d(TAG, "set select file : " + longClickFile.getAbsolutePath());

			// �ж��Ƿ�Ҫ����multiSelect
			// ��multiSelected��������ʱ��OnItemClick�¼�����������
			if (isMultiSelectEnabled()) {
				
				if (mode == MODE_SINGLE_SELECT) {
					setMode(MODE_MULTI_SELECT);
					// �޸�ͼƬ
					ItemContainer item = adapter.getItemContainers(position);
					item.fileIcon.setImageDrawable(MShareFileAdapter.getSelectedDrawable(longClickFile));
					// ��¼��ѡ�����
					multiSelectPosition[position] = true;
					
					// �ص�����
					if (callback != null) {
						callback.onItemLongClick(currentFiles[position]);
					}
				} else if (mode == MODE_MULTI_SELECT) {
					
					if (multiSelectPosition[position]) {
						unselectFile(position);
					} else {
						selectFile(position);
					}
				}
				
			} else {// ������MultiSelected
				
				// TODO �������õ�selectPosition����û����,OnItemClick�������ҲҪ�޸�selectPosition
				selectPosition = position;
				if (callback != null) {
					callback.onItemLongClick(longClickFile);
				}
			}
			
			return true;
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
			waitForRefresh();
			if (callback != null) {
				callback.onBackButtonClick(crumbController.getSelectedFile());
			}
		}
	}

	// ˢ�°�ť�ĵ���¼�
	private class RefreshButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "refresh button is clicked");
			
			// ����ˢ��
			refreshButton.startAnimation(refreshAnimation);
			waitForRefresh();
			
			if (callback != null) {
				// ����ֻ�ǰѵ�ǰӦ����ʾ���ļ����ļ���������
				callback.onRefreshButtonClick(crumbController.getSelectedFile());
			}
		}
	}
	
	// ָ��selected == null
	private class GridViewClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// ������ǰ��״̬
			if (mode == MODE_SINGLE_SELECT) {
				selectPosition = -1;
			}
			
			if (callback != null) {
				callback.onGridViewClick();
			}
		}
	}
	
	// TODO ��Ҫ������
	/**
	 * <p>������չ�洢��״̬</p>
	 * TODO ������ʹ��{@link ExternalStorageStatusReceiver}������
	 */
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_MEDIA_REMOVED)) { // ��չ�����γ�
			setEnabled(false);
			refreshGridView(new FileBrowserFile[0]);
		} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { // ��չ������ʹ��
			setEnabled(true);
			refreshGridView(FileBrowserActivity.listFiles(crumbController.getSelectedFile()));
		}
	}	
}
