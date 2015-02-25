package org.mshare.ftp.server;

public class UserAccount extends Account {

	private int mPermission = AccountFactory.PERMISSION_USER;
	
	public UserAccount(String username, String password) {
		super(username, password);
	}

	@Override
	public int getPermission() {
		return mPermission;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public boolean isAdministrator() {
		return false;
	}

	@Override
	public boolean isUser() {
		return true;
	}
}
