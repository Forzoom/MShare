package org.mshare.main;

import org.mshare.ftp.server.FsService;
import org.mshare.ftp.server.FsSettings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class ServerSettingActivity extends Activity {

	private EditText ftpUsername;
	private EditText ftpPassword;
	private EditText ftpPort;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new ServerSettingFragment());
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
