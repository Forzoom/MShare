package org.mshare.live;

import java.net.Socket;

import org.mshare.main.R;

import android.app.Activity;
import android.os.Bundle;

public class LiveActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live);
		
		Socket socket = new Socket();
	}
}
