package org.mshare.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.mshare.main.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;


public class NewConn extends Activity {
	Button ftpswitch;
	TextView ftpaddr;
	TextView connhint;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newconn);
		ftpswitch = (Button) findViewById(R.id.ftpswitch);
		ftpaddr = (TextView) findViewById(R.id.ftpaddr);
		connhint = (TextView) findViewById(R.id.connhint);
		ftpaddr.setVisibility(View.GONE);
		ftpswitch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(ftpaddr.getVisibility()==View.GONE) //�鿴�����������
				{
					ftpaddr.setVisibility(View.VISIBLE);
					ftpswitch.setText("�ر�");
					connhint.setText("�������豸�����룺");
				}
				else
				{
					ftpaddr.setVisibility(View.GONE);
					ftpswitch.setText("��");
					connhint.setText("�򿪺�����������豸�Ϲ����豸���ļ�");
				}      
			}
		});
	}
}
