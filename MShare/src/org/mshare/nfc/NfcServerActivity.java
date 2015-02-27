package org.mshare.nfc;

import java.nio.charset.Charset;
import java.util.Locale;

import org.mshare.main.R;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

/**
 * TODO ���ڸ�Activityʹ�ú��˰�ť��ʱ�򣬿��Է��ص���һ��Activity��ʹ��parentActivity������
 * 
 * TODO ��Ҫ����FTP��������ʱ�ر�
 * TODO ����ֱ��ʹ��Beam�������ļ��������Ǵ���һ��FTP������
 * �����������еĺ�NFC��ص�����
 * ʹ��NFC���ֻ����һ��NFC��TAG������ֱ�ӵȴ�Android Beam������
 * ����NFC��TAGҲ��һ�������ã���Ϊ����������ģ����Կ�ƽ̨�ģ����������Ҫ����iOS������£�����ҪTAG
 * permission:NFC
 * TODO ɨ��ʹ���һ��TAG,��������TAG�а�����������plain text,��ҪAndroid4.4���ϲ��ܹ�ģ��һ��NFC��
 * TODO ����AAR��AAR�е�NdefRecord�а���Ӧ�õİ��������ɨ�赽AAR���ʹ�һ��Ӧ�ã����û�и�Ӧ�ã��ͻ��Google Play�����ظ�Ӧ��
 * AAR�Ĵ�����ֹ��������Ӧ�ô�����������������ݡ�����AAR��dispatch system 1.ʹ��intent filter����intent,���Activity��intent filter���ϣ����Ұ�������
 * ������Activity 2.��������Application 3.��Google Play ���ظ�Ӧ��
 * TODO ���ֻ�����֧��NFC��ʱ�����ô�죿
 * 
 * AAR����֧��Android 4.0 or later����ֻ�������������ϵͳ���ֻ�����֧��
 * 
 * TODO ��Ҫͳһ�ĵ�¼����,���Լ���
 * 
 * ����ʹ��foreground dispatch system ���Ĭ�ϵ�dispatch system
 * @author HM
 *
 * Android��NFCAndroid Beam֧��ֱ��ͨ��Beam�����ļ���Ҳ����ͨ��NDEF������һЩС������
 *
 */
public class NfcServerActivity extends Activity {

	// ��Ӧnfc��������
	private NfcAdapter mNfcAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_server);
		
		// ֻ����nfc�����������
		// TODO ��֪���ܲ��ܴ�������NFC
		if (isNfcEnable()) {
			// ����һ��NdefMessage
			// TODO ��֪��ΪʲôҪ����Locale
			// ʹ�ø÷�������Ϊ�÷���֧�ֵ�API9
			NdefMessage message = new NdefMessage(new NdefRecord[] {createTextRecord("content", Locale.CHINA, true)});
			
			// setNdefPushMessage();����̨�豸�㹻������ʱ���Զ�����Message
			// �ú�����Ҫָ��һЩActivity��ֻ��Push Message �� Activity����ǰ̨����(resume)��ʱ�򣬲��ܹ�push Message���ú����������̣߳��൱���첽����
			// ������onCreate�е���
			// ֻ�е���̨�豸�㹻������ʱ��Żᷢ��Message
			// �����õ�Activity�ǽ����ܱ�Beam��������Activity��������ô�ܻ����ЩActivity�Ķ����أ�
			// ��������Ҫ���͵�Message��һ��������£�һ��ʹ��
			mNfcAdapter.setNdefPushMessage(message, this);
			
			// ��ÿ����̨�豸�㹻������ʱ�򣬽�����callback�е�createNdefMessage������һ��Message���з���
			// callback���ھ����㹻����(beam)���ݵ�ʱ�򣬾ͷ���Message,����ÿ��ֻ��pushһ��Message,����һ����callback��������Message����ر�����
			// ��Ҫ���ݲ�ͬ������������Ͳ�ͬ��Message
			NdefCallback callback = new NdefCallback();
			mNfcAdapter.setNdefPushMessageCallback(callback, this);
			
			// �ڴ������ݵ�ʱ�򣬿���ʹ��com.android.nppЭ�����NFC Forum��SNEPЭ��
			
			// ����dispatching system�л��в����׵�
		} else {
			// ��NFCʧ�ܵ�ʱ����ʱ��ʾ�û����Ǹ�ǮNFC�����ã��Ժ�����û���NFC����������£���Ӧ����NFC��ص�������ʾ����
			Toast.makeText(this, "NFC�޷�ʹ��", Toast.LENGTH_SHORT).show();
			TextView hint = (TextView)findViewById(R.id.nfc_hint);
			hint.setText("NFC�޷�ʹ��");
		}
	}
	
	class NdefCallback implements NfcAdapter.CreateNdefMessageCallback {

		@Override
		public NdefMessage createNdefMessage(NfcEvent event) {
			// TODO ��֪��NfcEvent�Ǹ�ʲô��
			// ���ǵ��û�����������ܻ�������ͬ�����Կ��ǽ�ʹ��callback�ķ�ʽ
			NdefMessage message = new NdefMessage(new NdefRecord[] {createTextRecord("content", Locale.CHINA, true)});
			return message;
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// ��Ϊ��ǰ��ע��callbackʱ�����������this������Android Beam���ܻ�򿪸�Activity
		String action = getIntent().getAction();
		// NDEF�������ļ�⵽
		if (action != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			processIntent(getIntent());
		}
	}
	
	/**
	 * �������NFC����
	 */
	public void processIntent(Intent intent) {
		// ���Message�е����ݣ������һ������NdefMessage��װ�Ľ��
		Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		NdefMessage msg = (NdefMessage)rawMessages[0];
		// ��ý���е��ı�����
		String result = new String(msg.getRecords()[0].getPayload());
	}
	
	/**
	 * ���Դ���Record
	 * NdefMessage�а����˶��NdefRecord
	 */
	public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
		// ����������Ϣ
		byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
		Charset uftEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
		byte[] textBytes = payload.getBytes(uftEncoding);
		int utfBit = encodeInUtf8 ? 0 : (1 << 7);
	    char status = (char) (utfBit + langBytes.length);
	    byte[] data = new byte[1 + langBytes.length + textBytes.length];
	    data[0] = (byte) status;
	    System.arraycopy(langBytes, 0, data, 1, langBytes.length);
	    System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
		return record;
	}
	
	public boolean isNfcEnable() {
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			return false;
		} else {
			return true;
		}
	}
}
