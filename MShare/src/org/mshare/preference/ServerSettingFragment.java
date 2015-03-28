package org.mshare.preference;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

import org.mshare.main.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServerSettingFragment extends PreferenceFragment {
	private static final String TAG = ServerSettingFragment.class.getSimpleName();

	public static final int REQUEST_IMAGE_GET = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.server_settings);
		PreferenceCategory category = (PreferenceCategory)getPreferenceScreen().getPreference(0);
		AvaterPreference avaterPreference = (AvaterPreference)category.getPreference(0);
		avaterPreference.setServerSettingFragment(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "the result code in avater : " + resultCode);
		if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			Log.e(TAG, "the uri : " + uri.getPath());
			ContentResolver cr = getActivity().getContentResolver();

			InputStream is = null;
			FileOutputStream fos = null;

			FileOutputStream extraFos = null;

			try {
				// 得到对应的头像内容
				is = cr.openInputStream(uri);
				// 保存头像内容
				// TODO 保存的类型要紧吗？.png?
				fos = getActivity().openFileOutput("avater", Context.MODE_PRIVATE);

				// 额外的保存图片，用于debug
				extraFos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "avater");

				// 将所有的内容都写入到
				// 4kb
				byte[] buf = new byte[4 * 1024];
				int readCount = -1;

				while ((readCount = is.read(buf)) != -1) {
					fos.write(buf);
					extraFos.write(buf);
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (is != null) {
						is.close();
					}
					if (fos != null) {
						fos.flush();
						fos.close();
					}
					if (extraFos != null) {
						extraFos.flush();
						extraFos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}