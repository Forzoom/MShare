package org.mshare.main;

/**
 * 包装用于连接的参数，需要包装的参数包括host,port,username,password
 * @author HM
 *
 */
public class ConnectInfo {

	// 不知道host要使用什么样类型来存储
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
	 * 通过解析info来获得一个ConnectInfo对象，一般用于客户端接受到连接数据后解析
	 * @param info
	 * @return
	 */
	public static ConnectInfo parse(String info) {
		return null;
	}
	
}
