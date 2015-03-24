package org.mshare.server.ftp;

/**
 * ������Session��һЩ��Ϣ
 * @author HM
 *
 */
public class SessionInfo {

	private static final String TAG = SessionInfo.class.getSimpleName();
	private String username;
	private long connectTime; // ����ʱ��
	// ������ǰ��SessionInfo��������,̫�鷳�ˣ���ǰ��û���뷨
	private boolean isDeprecated = false;
	
	public SessionInfo() {
	}
	
	public SessionInfo(String username, long connectTime) {
		this.username = username;
		this.connectTime = connectTime;
	}
	
	/**
	 * @return the username�����ܷ���null
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
	
	// �������ʱ��
	public long getConnectTime() {
		return connectTime;
	}
	
}
