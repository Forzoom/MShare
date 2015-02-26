package org.mshare.ftp.server;

import org.mshare.file.SharedLinkSystem.Permission;

/**
 * {@link Account}
 * {@link Account#prepare()}
 * @author HM
 *
 */
public class AdminAccount extends Account {

	private int mPermission = AccountFactory.PERMISSION_ADMIN;
	
	/**
	 * {@link Account}
	 * {@link Account#prepare()}
	 * @param username
	 * @param password
	 */
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
