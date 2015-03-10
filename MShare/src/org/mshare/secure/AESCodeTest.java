package org.mshare.secure;

import java.io.InputStream;
import java.security.Key;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;

/**
 * �����˵�������Ҫ���½�����֤
 * TODO ��Ȼkey����ͬ�ģ���ô�����ɵ�����ҲӦ������ͬ��?
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
			// ����
			inputData = aesCoder.encrypt(inputData);
			Log.d(TAG, "encrypt : " + Base64.encodeToString(inputData, Base64.DEFAULT));
			
			// ����
			byte[] outputData = aesCoder.decrypt(inputData);
			String outputStr = new String(outputData);
			
			Log.d(TAG, "decrypt :" + outputStr);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		super.onCreate(savedInstanceState);
	}
}
