package org.mshare.secure;

public interface Coder {
	// ����
	public byte[] encrypt(byte[] data);
	// ����
	public byte[] decrypt(byte[] data);
	
}
