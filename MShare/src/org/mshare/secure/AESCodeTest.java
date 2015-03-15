package org.mshare.secure;

import java.io.InputStream;
import java.security.Key;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;

/**
 * 经过了调整，需要重新进行验证
 * TODO 既然key是相同的，那么所生成的密文也应该是相同的?
 * @author HM
 *
 */
public class AESCodeTest extends Activity {
	private static final String TAG = AESCodeTest.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try {
		
			String inputStr = "AES";
			byte[] inputData = inputStr.getBytes();
			Log.d(TAG, "inputStr : " + inputStr);
			
			Coder aesCoder = new AESCoder("hahaha");
			// 加密
			inputData = aesCoder.encrypt(inputData);
			Log.d(TAG, "encrypt : " + Base64.encodeToString(inputData, Base64.DEFAULT));
			
			// 解密
			byte[] outputData = aesCoder.decrypt(inputData);
			String outputStr = new String(outputData);
			
			Log.d(TAG, "decrypt :" + outputStr);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		super.onCreate(savedInstanceState);
	}
}
