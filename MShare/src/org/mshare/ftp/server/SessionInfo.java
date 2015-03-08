package org.mshare.ftp.server;

public class SessionInfo {

	private static final String TAG = SessionInfo.class.getSimpleName();
	private String username;
	private long connectTime; // 连接时间
	// 表明当前的SessionInfo被废弃了
	private boolean isDeprecated = false;
	
	public SessionInfo(String username, long connectTime) {
		this.username = username;
		this.connectTime = connectTime;
	}
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	// 获得连接时间
	public long getConnectTime() {
		return connectTime;
	}
	
}
