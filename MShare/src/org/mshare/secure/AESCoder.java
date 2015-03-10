package org.mshare.secure;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder implements Coder {
	private static final String TAG = AESCoder.class.getSimpleName();
	
	public static final String KEY_ALGORITHM = "AES";
	
	public static final String CIPHPER_ALGORITHM = "AES/ECB/PKCS5Padding";
	
	private Key key;
	
	public AESCoder(String seed) {
		key = toKey(initKey(seed));
	}
	
	public static Key toKey(byte[] key) {
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		return secretKey;
	}
	
	public static byte[] initKey(String seed) {
		try {
			KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
			kg.init(256, new SecureRandom(seed.getBytes()));
			SecretKey secretKey = kg.generateKey();
			return secretKey.getEncoded();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] encrypt(byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHPER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] decrypt(byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHPER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
