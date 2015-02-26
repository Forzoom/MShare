package org.mshare.main;

import android.util.Log;

/**
 * ��װ�������ӵĲ�������Ҫ��װ�Ĳ�������host,port,username,password
 * ���ܻ���Ҫ��������Ϣ������ap��ssid��p2p������name�ȵ�
 * 
 * Serial��Parcel��̫���鷳��,���������ʹ��
 * @author HM
 *
 */
public class ConnectInfo {

	private static final String TAG = ConnectInfo.class.getSimpleName();
	
	// ��֪��hostҪʹ��ʲô���������洢
	private String host;
	private String port;
	private String username;
	private String password;
	
	public ConnectInfo(String host, String port, String username,
			String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * ����info�����һ��ConnectInfo����һ�����ڿͻ��˽��ܵ��������ݺ����
	 * @param info
	 * @return ����ʧ�ܷ���null
	 */
	public static ConnectInfo parse(String info) {
		Log.d(TAG, "Parse string : " + info);
		String[] infos = info.split(" ");
		Log.d(TAG, "Split result count : " + infos.length);
		if (infos.length == 4) {
			return new ConnectInfo(infos[0], infos[1], infos[2], infos[3]);
		}
		return null;
	}
	
	/**
	 * ת�����ı����д���
	 * @return
	 */
	public static String stringify(ConnectInfo connectInfo) {
		String result = connectInfo.host + " " + connectInfo.port + " "
				+ connectInfo.username + " " + connectInfo.password;
		return result;
	}
	
	/**
	 * ͬ��ʹ��ConnectInfo.stringify
	 */
	@Override
	public String toString() {
		return ConnectInfo.stringify(this);
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
