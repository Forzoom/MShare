package org.mshare.main;

import org.mshare.ftp.server.FsSettings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class ServerSettingActivity extends Activity {

	private EditText ftpUsername;
	private EditText ftpPassword;
	private EditText ftpPort;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ftp_server_setting);
		
		// 设置返回上一层级的Activity的按钮
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		ftpUsername = (EditText)findViewById(R.id.ftp_username);
		ftpPassword = (EditText)findViewById(R.id.ftp_password);
		ftpPort = (EditText)findViewById(R.id.ftp_port);
		
		ftpUsername.setText(FsSettings.getUsername());
		ftpPassword.setText(FsSettings.getPassword());
		ftpPort.setText(String.valueOf(FsSettings.getPort()));
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
		// 
		switch (item.getItemId()) {
			case R.id.ftp_server_setting_confirm:
				checkSetting();
				break;
		}
		return true;
	}
	
	/**
	 * 检测Setting，并修改
	 */
	private void checkSetting() {
		String username = ftpUsername.getText().toString();
		String password = ftpPassword.getText().toString();
		String port = ftpPort.getText().toString();
		
		FsSettings.setUsername(username);
		FsSettings.setPassword(password);
		// TODO:需要对port进行检测
		FsSettings.setPort(port);
	}
}
