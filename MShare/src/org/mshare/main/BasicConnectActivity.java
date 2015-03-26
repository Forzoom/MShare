package org.mshare.main;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.mshare.server.ServerSettings;
import org.mshare.server.ftp.ServerService;

/**
 * Created by huangming on 15/3/26.
 */
public class BasicConnectActivity extends Activity {

	TextView hostView;
	TextView portView;
	TextView usernameView;
	TextView passwordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_connect);

		// 完成基本信息显示
		hostView = (TextView)findViewById(R.id.basic_connect_host);
		hostView.setText(ServerService.getLocalInetAddress().getHostAddress().toString().substring(1));
		portView = (TextView)findViewById(R.id.basic_connect_port);
		portView.setText(ServerSettings.getPort());
		usernameView = (TextView)findViewById(R.id.basic_connect_username);
		usernameView.setText(ServerSettings.getUsername());
		passwordView = (TextView)findViewById(R.id.basic_connect_password);
		passwordView.setText(ServerSettings.getPassword());
	}
}
