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
 * ���Ժͷ������˿�������������Ϣ
 * ��Ҫ��������дdispatch system
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
		
		// ���View
		nfcHintView = (TextView)findViewById(R.id.nfc_hint);
		
		// ����ܷ�ʹ��NFC
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null) {
			isNfcEnabled = true;
		} else {
			// �Ƿ���Ҫ�˳�Activity?
			nfcHintView.setText("NFC�޷�ʹ��");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// ʹ��onResume��onPause����ȷ��foreground dispatch system������Activity���ڻ��focus��ʱ������
		// foreground dispatch system ����ֻ��intent������filter֮�󣬾ͻᱻforeground dispatch system�����أ�����foreground����ָ����Activity��
		
		if (isNfcEnabled) {

			// Ĭ�Ͻ�������Activity���Լ�?
			// FLAG_ACTIVITY_SINGLE_TOP������ָ����Activity�Ѿ���ջ����ʱ�򣬲���������һ��
			// ��Ϊ����������Activity�ˣ�����onNewIntent��õ�һ���µ�Intent
			PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			
			String[][] techLists = new String[][] {};
			
			// ����filter������������setDataType?
			// ֻ����NDEF�����ֵ�ʱ��Żᴦ��
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
	
	// �����ܵ�һ���µ�Intent��ʱ�򽫻ᱻ����
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// ��Server�˲�����ͬ���ǣ�����ʹ�õ���TAG
//		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		// TODO ����TAG����֪����ô����
		
		Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMessages.length > 0) {
			Log.d(TAG, "get raw message array, it has content");
			NdefMessage msg = (NdefMessage)rawMessages[0];
			// ��ý���е��ı�����
			String result = new String(msg.getRecords()[0].getPayload());
			Log.d(TAG, "content is");
		}
	}
}
