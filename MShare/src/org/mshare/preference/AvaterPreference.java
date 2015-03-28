package org.mshare.preference;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.AttributeSet;

public class AvaterPreference extends Preference {
	private static final String TAG = AvaterPreference.class.getSimpleName();

	private Context context;
	private PreferenceFragment serverSettingFragment;

	public AvaterPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
	}

	public AvaterPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public AvaterPreference(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onClick() {
		// 当用户点击对应的内容的时候
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");

		// 启动图片选择应用
		if (serverSettingFragment != null) {
			serverSettingFragment.startActivityForResult(intent, ServerSettingFragment.REQUEST_IMAGE_GET);
		}

		super.onClick();
	}

	public void setServerSettingFragment(ServerSettingFragment serverSettingFragment) {
		this.serverSettingFragment = serverSettingFragment;
	}

}
