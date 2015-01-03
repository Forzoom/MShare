package org.mshare.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.mshare.file.FileAdapter;
import org.mshare.file.MShareCrumbs;
import org.mshare.file.MShareFileBrowser;
import org.mshare.main.R;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
public class DummyFragment extends Fragment 
{
	public static final String ARG_SECTION_NUMBER = "section_number";
	final String TAG = "--MShare--";
//	private Button btftp;
	private RelativeLayout relative;
	private ArrayList<HashMap<String, Object>> listImageItem;  
    private SimpleAdapter simpleAdapter;
    
    private MShareFileBrowser mFileBrowser = null;
    
	/**
	 * �÷����ķ���ֵ���Ǹ�Fragment��ʾ��View���(non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		Bundle args = getArguments();
		int num = args.getInt(ARG_SECTION_NUMBER);
		
		mFileBrowser = new MShareFileBrowser(this.getActivity(), container);
		
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

//	private void InitView(View view) {  
//	    gridView = (GridView) view.findViewById(R.id.grid_view);
//	    btftp = (Button) view.findViewById(R.id.btnewftp);
//	}  
	
	@Override  
    public void onStart() {
		
		mFileBrowser.refresh();
		
        // ���ɶ�̬���飬����ת������<span style="color:#ff6666;"> #��onStart()�����м������ݣ�����Fragment�п��Զ�̬ˢ������#</span>  
//        listImageItem = new ArrayList<HashMap<String, Object>>();  
//            for (int i = 0; i < 4; i++) {  
//                HashMap<String, Object> map = new HashMap<String, Object>();  
//                map.put("ItemImage", R.drawable.folder);// ���ͼ����Դ��ID  
//                map.put("ItemText", "�ļ���"+i);// �������ItemText  
//                listImageItem.add(map);  
//            }  
//          
//        simpleAdapter = new SimpleAdapter(  
//                getActivity().getApplicationContext(), listImageItem,  
//                R.layout.labelicon, new String[] {  
//                        "ItemImage", "ItemText" }, new int[] { R.id.imageview,  
//                        R.id.textview });  
//        //���ͼƬ��  
//        simpleAdapter.setViewBinder(new ViewBinder() {  
//            public boolean setViewValue(View view, Object data,  
//                    String textRepresentation) {  
//                if (view instanceof ImageView && data instanceof Drawable) {  
//                    ImageView iv = (ImageView) view;  
//                    iv.setImageDrawable((Drawable) data);  
//                    return true;  
//                } else  
//                    return false;  
//            }  
//        });  
//        gridView.setAdapter(simpleAdapter);  
        super.onStart();  
    }  
}