package org.mshare.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.mshare.main.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.SimpleAdapter.ViewBinder;


public class JoinConn extends Activity {
	
	private GridView gridview;
	private Button btftp;
	private ArrayList<HashMap<String, Object>> listImageItem;  
    private SimpleAdapter simpleAdapter;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slidelistview);
		gridview = (GridView) findViewById(R.id.gridview);
	    btftp = (Button) findViewById(R.id.btnewftp);
	    listImageItem = new ArrayList<HashMap<String, Object>>();  
        for (int i = 0; i < 4; i++) {  
            HashMap<String, Object> map = new HashMap<String, Object>();  
            map.put("ItemImage", R.drawable.folder);// ���ͼ����Դ��ID  
            map.put("ItemText", "192.168.2."+i);// �������ItemText  
            listImageItem.add(map);  
        }  
      
	    simpleAdapter = new SimpleAdapter(  
	            this, listImageItem,  
	            R.layout.labelicon, new String[] {  
	                    "ItemImage", "ItemText" }, new int[] { R.id.imageview,  
	                    R.id.textview });  
	    //���ͼƬ��  
	    simpleAdapter.setViewBinder(new ViewBinder() {  
	        public boolean setViewValue(View view, Object data,  
	                String textRepresentation) {  
	            if (view instanceof ImageView && data instanceof Drawable) {  
	                ImageView iv = (ImageView) view;  
	                iv.setImageDrawable((Drawable) data);  
	                return true;  
	            } else  
	                return false;  
	        }  
	    });  
	    gridview.setAdapter(simpleAdapter); 
	}
	
	public void customView(View source)
	{
		//װ��/res/layout/login.xml���沼��
		TableLayout loginForm = (TableLayout)getLayoutInflater()
			.inflate( R.layout.login, null);		
		new AlertDialog.Builder(this)
			// ���öԻ����ͼ��
			.setIcon(R.drawable.app_default_icon)
			// ���öԻ���ı���
			.setTitle("�½�FTP������")
			// ���öԻ�����ʾ��View����
			.setView(loginForm)
			// Ϊ�Ի�������һ����ȷ������ť
			.setPositiveButton("ȷ��" , new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog,
						int which)
				{
					// �˴���ִ�е�¼����
				}
			})
			// Ϊ�Ի�������һ����ȡ������ť
			.setNegativeButton("ȡ��", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog,
						int which)
				{
					// ȡ����¼�������κ����顣
				}
			})
			// ����������ʾ�Ի���
			.create()
			.show();
	}
}
