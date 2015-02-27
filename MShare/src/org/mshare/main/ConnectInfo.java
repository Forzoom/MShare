package org.mshare.main;

import android.util.Log;

/**
 * 包装用于连接的参数，需要包装的参数包括host,port,username,password
 * 可能还需要其他的信息，例如ap的ssid，p2p的连接name等等
 * 
 * Serial和Parcel都太过麻烦了,这里决定不使用
 * @author HM
 *
 */
public class ConnectInfo {

	private static final String TAG = ConnectInfo.class.getSimpleName();
	
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
	 * 解析info来获得一个ConnectInfo对象，一般用于客户端接受到连接数据后解析
	 * @param info
	 * @return 解析失败返回null
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
	 * 转化成文本进行传输
	 * @return
	 */
	public static String stringify(ConnectInfo connectInfo) {
		String result = connectInfo.host + " " + connectInfo.port + " "
				+ connectInfo.username + " " + connectInfo.password;
		return result;
	}
	
	/**
	 * 同样使用ConnectInfo.stringify
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
