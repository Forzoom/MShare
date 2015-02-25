package org.mshare.ftp.server;

import org.mshare.file.SharedLinkSystem.Permission;

public class GuestAccount extends Account {

	private int mPermission = AccountFactory.PERMISSION_GUEST;
	
	public GuestAccount(String username, String password) {
		super(username, password);
	}

	@Override
	public int getPermission() {
		return mPermission;
	}

	@Override
	public boolean isGuest() {
		return true;
	}

	@Override
	public boolean isAdministrator() {
		return false;
	}

	@Override
	public boolean isUser() {
		return false;
	}
}
