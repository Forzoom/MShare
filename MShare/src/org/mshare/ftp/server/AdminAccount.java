package org.mshare.ftp.server;

import org.mshare.file.SharedLinkSystem.Permission;

public class AdminAccount extends Account {

	private int mPermission = AccountFactory.PERMISSION_ADMIN;
	
	public AdminAccount(String username, String password) {
		super(username, password);
	}

	@Override
	public int getPermission() {
		return mPermission;
	}

	@Override
	public boolean isGuest() {
		return false;
	}

	@Override
	public boolean isAdministrator() {
		return true;
	}

	@Override
	public boolean isUser() {
		return false;
	}
}
