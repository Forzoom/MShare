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
 * TODO 当在该Activity使用后退按钮的时候，可以返回到上一个Activity吗，使用parentActivity可以吗
 * 
 * TODO 需要设置FTP服务器超时关闭
 * TODO 考虑直接使用Beam来发送文件，而不是创建一个FTP服务器
 * 用来测试所有的和NFC相关的内容
 * 使用NFC将手机变成一个NFC的TAG，或者直接等待Android Beam的连接
 * 创建NFC的TAG也有一定的作用，因为这是最基本的，可以跨平台的，如果将来需要兼容iOS的情况下，就需要TAG
 * permission:NFC
 * TODO 扫描和创建一个TAG,所创建的TAG中包含的内容是plain text,需要Android4.4以上才能够模拟一个NFC卡
 * TODO 创建AAR，AAR中的NdefRecord中包含应用的包名，如果扫描到AAR，就打开一个应用，如果没有该应用，就会从Google Play上下载该应用
 * AAR的存在阻止了其他的应用处理我们所部署的内容。对于AAR，dispatch system 1.使用intent filter发送intent,如果Activity的intent filter符合，并且包名符合
 * 将启动Activity 2.否则启动Application 3.到Google Play 下载该应用
 * TODO 当手机并不支持NFC的时候该怎么办？
 * 
 * AAR仅仅支持Android 4.0 or later后的手机，对于其他的系统的手机并不支持
 * 
 * TODO 需要统一的登录内容,可以加密
 * 
 * 可以使用foreground dispatch system 替代默认的dispatch system
 * @author HM
 *
 * Android的NFCAndroid Beam支持直接通过Beam发送文件，也可以通过NDEF来发送一些小的数据
 *
 */
public class NfcServerActivity extends Activity {

	// 对应nfc的适配器
	private NfcAdapter mNfcAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_server);
		
		// 只有在nfc启动的情况下
		// TODO 不知道能不能代码启动NFC
		if (isNfcEnable()) {
			// 创建一个NdefMessage
			// TODO 不知道为什么要设置Locale
			// 使用该方法，因为该方法支持到API9
			NdefMessage message = new NdefMessage(new NdefRecord[] {createTextRecord("content", Locale.CHINA, true)});
			
			// setNdefPushMessage();当两台设备足够靠近的时候自动发送Message
			// 该函数需要指定一些Activity，只有Push Message 的 Activity还在前台运行(resume)的时候，才能够push Message，该函数不阻塞线程，相当于异步操作
			// 建议在onCreate中调用
			// 只有当两台设备足够靠近的时候才会发送Message
			// 所设置的Activity是将可能被Beam所启动的Activity，但是怎么能获得这些Activity的对象呢？
			// 当所有需要推送的Message都一样的情况下，一般使用
			mNfcAdapter.setNdefPushMessage(message, this);
			
			// 在每次两台设备足够靠近的时候，将调用callback中的createNdefMessage来创建一个Message进行发送
			// callback会在距离足够发射(beam)数据的时候，就发送Message,由于每次只能push一个Message,所以一般是callback所创建的Message更早地被发送
			// 需要根据不同的情况，来发送不同的Message
			NdefCallback callback = new NdefCallback();
			mNfcAdapter.setNdefPushMessageCallback(callback, this);
			
			// 在传送数据的时候，可以使用com.android.npp协议或者NFC Forum的SNEP协议
			
			// 关于dispatching system中还有不明白的
		} else {
			// 当NFC失败的时候，暂时提示用户的那个钱NFC不可用，以后如果用户的NFC不可用情况下，不应该有NFC相关的内容显示出来
			Toast.makeText(this, "NFC无法使用", Toast.LENGTH_SHORT).show();
			TextView hint = (TextView)findViewById(R.id.nfc_hint);
			hint.setText("NFC无法使用");
		}
	}
	
	class NdefCallback implements NfcAdapter.CreateNdefMessageCallback {

		@Override
		public NdefMessage createNdefMessage(NfcEvent event) {
			// TODO 不知道NfcEvent是干什么的
			// 考虑到用户名和密码可能会有所不同，所以考虑将使用callback的方式
			NdefMessage message = new NdefMessage(new NdefRecord[] {createTextRecord("content", Locale.CHINA, true)});
			return message;
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 因为在前面注册callback时，所加入的是this，所以Android Beam可能会打开该Activity
		String action = getIntent().getAction();
		// NDEF被正常的检测到
		if (action != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			processIntent(getIntent());
		}
	}
	
	/**
	 * 处理解析NFC内容
	 */
	public void processIntent(Intent intent) {
		// 获得Message中的内容，结果是一个个的NdefMessage包装的结果
		Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		NdefMessage msg = (NdefMessage)rawMessages[0];
		// 获得结果中的文本数据
		String result = new String(msg.getRecords()[0].getPayload());
	}
	
	/**
	 * 尝试创建Record
	 * NdefMessage中包含了多个NdefRecord
	 */
	public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
		// 语言文字信息
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
