package org.mshare.main;

/**
 * ��װ�������ӵĲ�������Ҫ��װ�Ĳ�������host,port,username,password
 * @author HM
 *
 */
public class ConnectInfo {

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
	 * ͨ������info�����һ��ConnectInfo����һ�����ڿͻ��˽��ܵ��������ݺ����
	 * @param info
	 * @return
	 */
	public static ConnectInfo parse(String info) {
		return null;
	}
	
}
