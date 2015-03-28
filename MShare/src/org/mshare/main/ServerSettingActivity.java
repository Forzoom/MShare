package org.mshare.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ServerSettingActivity extends Activity {

	// 可以考虑添加电池相关内容设定
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new ServerSettingFragment()).commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 添加确定按钮
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ftp_server_setting, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

}
