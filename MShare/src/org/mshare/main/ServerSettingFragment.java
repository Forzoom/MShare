package org.mshare.main;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class ServerSettingFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.server_settings);
	}
	
}
