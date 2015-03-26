package org.mshare.server.ftp;

/**
 * 所包含Session的一些信息
 * @author HM
 *
 */
public class SessionInfo {

	private static final String TAG = SessionInfo.class.getSimpleName();
	private String username;
	private long connectTime; // 连接时间
	// 表明当前的SessionInfo被废弃了,太麻烦了，当前还没有想法
	private boolean isDeprecated = false;
	
	public SessionInfo() {
	}
	
	public SessionInfo(String username, long connectTime) {
		this.username = username;
		this.connectTime = connectTime;
	}
	
	/**
	 * @return the username，可能返回null
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	// 获得连接时间
	public long getConnectTime() {
		return connectTime;
	}
	
}
