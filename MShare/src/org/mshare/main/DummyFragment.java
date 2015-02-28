package org.mshare.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.mshare.file.MShareFileAdapter;
import org.mshare.file.MShareCrumbController;
import org.mshare.file.MShareFileBrowser;
import org.mshare.ftp.server.FsSettings;
import org.mshare.main.R;

import android.app.backup.FileBackupHelper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.view.View.OnClickListener; 
/**
 * @author 
 * @version 1.0
 */
public class DummyFragment extends Fragment {
	private static final String TAG = DummyFragment.class.getSimpleName();
	public static final String ARG_SECTION_NUMBER = "section_number";
	
//	private Button btftp;
	private RelativeLayout relative;
	private ArrayList<HashMap<String, Object>> listImageItem;
    private SimpleAdapter simpleAdapter;
    
    private MShareFileBrowser mFileBrowser = null;
    
	/**
	 * �÷����ķ���ֵ���Ǹ�Fragment��ʾ��View���
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Bundle args = getArguments();
		int num = args.getInt(ARG_SECTION_NUMBER);
		
		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		// ���ļ��������ʾ��չ�洢�е�����
		mFileBrowser = new MShareFileBrowser(this.getActivity(), container, rootPath);
		View fileBrowser = mFileBrowser.getView();
		
		View view2 = inflater.inflate(R.layout.ftpservice, container, false);
		// initial the view
//		InitView(fileBrowser);
		Button joinconn = (Button) view2.findViewById(R.id.joinconn);
		Button newconn = (Button) view2.findViewById(R.id.newconn);
		joinconn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), JoinConn.class));
			}        
		}); 
		newconn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), NewConn.class));
			}        
		});
		if(num == 2)
			return fileBrowser;
		else
			return view2; 
	}  

	@Override  
    public void onStart() {
		
		mFileBrowser.refresh();  
        super.onStart();  
    }
	
	/**
	 * Ϊ��MainActivity�ܹ����FileBrowser
	 * TODO ��DummyFragment�ƶ���MainActivity����Ϊ�ڲ��ࣿ
	 * @return
	 */
	public MShareFileBrowser getFileBrowser() {
		return mFileBrowser;
	}
}