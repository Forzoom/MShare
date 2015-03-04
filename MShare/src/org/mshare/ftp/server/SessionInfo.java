package org.mshare.ftp.server;

public class SessionInfo {

	private static final String TAG = SessionInfo.class.getSimpleName();
	private String username;
	private long connectTime; // ����ʱ��
	// ������ǰ��SessionInfo��������
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

	// �������ʱ��
	public long getConnectTime() {
		return connectTime;
	}
	
}
