package org.mshare.preference;

import android.app.Service;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * 继承了Switch但是不知道怎么用
 * Created by huangming on 15/3/28.
 */
public class ApPreference extends SwitchPreference {
	private static final String TAG = ApPreference.class.getSimpleName();

	private Context context;

	public ApPreference(Context context) {
		super(context);
		this.context = context;
	}

	public ApPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;

	}

	public ApPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

	}

	@Override
	protected void onClick() {
		super.onClick();

		// 判断当前的AP是否启动

	}

	public void setApEnabled(boolean enabled) {
		// 当被点击的是否启动AP
		/* 通过反射机制调用AP开启功能 */
		WifiManager wm = (WifiManager)context.getSystemService(Service.WIFI_SERVICE);

		try {
			// 用于获得WifiConfiguration
			Method getWifiApConfigurationMethod = wm.getClass().getDeclaredMethod("getWifiApConfiguration");
			WifiConfiguration config = (WifiConfiguration)getWifiApConfigurationMethod.invoke(wm);

			Method setWifiApEnabledMethod = wm.getClass().getDeclaredMethod("setWifiApEnabled");
			setWifiApEnabledMethod.invoke(wm, config, enabled);

		} catch (Exception e) {
			Toast.makeText(context, "AP无法启动", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
}
