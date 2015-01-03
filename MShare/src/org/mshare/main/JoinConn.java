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
            map.put("ItemImage", R.drawable.folder);// 添加图像资源的ID  
            map.put("ItemText", "192.168.2."+i);// 按序号做ItemText  
            listImageItem.add(map);  
        }  
      
	    simpleAdapter = new SimpleAdapter(  
	            this, listImageItem,  
	            R.layout.labelicon, new String[] {  
	                    "ItemImage", "ItemText" }, new int[] { R.id.imageview,  
	                    R.id.textview });  
	    //添加图片绑定  
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
		//装载/res/layout/login.xml界面布局
		TableLayout loginForm = (TableLayout)getLayoutInflater()
			.inflate( R.layout.login, null);		
		new AlertDialog.Builder(this)
			// 设置对话框的图标
			.setIcon(R.drawable.app_default_icon)
			// 设置对话框的标题
			.setTitle("新建FTP服务器")
			// 设置对话框显示的View对象
			.setView(loginForm)
			// 为对话框设置一个“确定”按钮
			.setPositiveButton("确定" , new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog,
						int which)
				{
					// 此处可执行登录处理
				}
			})
			// 为对话框设置一个“取消”按钮
			.setNegativeButton("取消", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog,
						int which)
				{
					// 取消登录，不做任何事情。
				}
			})
			// 创建、并显示对话框
			.create()
			.show();
	}
}
