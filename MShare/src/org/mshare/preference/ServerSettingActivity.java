package org.mshare.preference;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.mshare.main.R;

public class ServerSettingActivity extends Activity {

	// ���Կ�����ӵ����������趨
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new ServerSettingFragment()).commit();
	}
	
}
