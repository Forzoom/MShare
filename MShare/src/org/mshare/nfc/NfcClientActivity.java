package org.mshare.nfc;

import org.mshare.main.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

/**
 * 尝试和服务器端靠近后获得连接信息
 * 需要在这里重写dispatch system
 * @author HM
 *
 */
public class NfcClientActivity extends Activity {
	private static final String TAG = NfcClientActivity.class.getSimpleName();
	
	private NfcAdapter mNfcAdapter;
	private boolean isNfcEnabled = false;
	private TextView nfcHintView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_server);
		
		// 获得View
		nfcHintView = (TextView)findViewById(R.id.nfc_hint);
		
		// 检测能否使用NFC
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null) {
			isNfcEnabled = true;
		} else {
			// 是否需要退出Activity?
			nfcHintView.setText("NFC无法使用");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 使用onResume和onPause可以确保foreground dispatch system可以在Activity正在获得focus的时候运行
		// foreground dispatch system 表明只有intent满足了filter之后，就会被foreground dispatch system所拦截，并打开foreground中所指定的Activity？
		
		if (isNfcEnabled) {

			// 默认将启动的Activity是自己?
			// FLAG_ACTIVITY_SINGLE_TOP，当所指定的Activity已经在栈顶的时候，不会再启动一个
			// 因为不会再启动Activity了，所以onNewIntent会得到一个新的Intent
			PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			
			String[][] techLists = new String[][] {};
			
			// 对于filter，还可以设置setDataType?
			// 只有在NDEF被发现的时候才会处理
			IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
			IntentFilter[] filters = new IntentFilter[] {filter};
			mNfcAdapter.enableForegroundDispatch(this, intent, filters, techLists);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (isNfcEnabled) {
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}
	
	// 当接受到一个新的Intent的时候将会被调用
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 和Server端并不相同的是，这里使用的是TAG
//		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		// TODO 对于TAG，不知道怎么处理
		
		Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMessages.length > 0) {
			Log.d(TAG, "get raw message array, it has content");
			NdefMessage msg = (NdefMessage)rawMessages[0];
			// 获得结果中的文本数据
			String result = new String(msg.getRecords()[0].getPayload());
			Log.d(TAG, "content is");
		}
	}
}
