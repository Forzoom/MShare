/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mshare.ftp.server;

import android.util.Log;

public class CmdPASS extends FtpCmd implements Runnable {
    private static final String TAG = CmdPASS.class.getSimpleName();

    String input;

    public CmdPASS(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "Executing PASS");
        String attemptPassword = getParameter(input, true); // silent
        
        Account account = sessionThread.getAccount();
        if (account == null) {
        	// TODO ����ʹ��ACCT������Account
        	Log.e(TAG, "�����ȵ���USER");
        	sessionThread.writeString("503 Must send USER first\r\n");
        	return;
        }
        if (account.getUsername() == null || account.getPassword() == null) {
        	Log.e(TAG, "Account�����е�username��passwordΪnull");
        	sessionThread.writeString("500 Internal error during authentication");
        	return;
        }
        // ��������ݿ��ܻ��д���
        if (attemptPassword == null || attemptPassword.equals("")) {
        	account.setAttemptPassword(attemptPassword);
        }
        
        if (account.authAttempt()) { // ���Ե�½
        	Log.i(TAG, "User " + account.getUsername() + " password verified");
        	if (account.isAnonymous()) {
        		sessionThread.writeString("230 Guest login ok, read only access.\r\n");
        	} else {
            	sessionThread.writeString("230 Access granted\r\n");
        	}
        } else {
        	Log.i(TAG, "Failed authentication");
            Util.sleepIgnoreInterupt(1000); // sleep to foil brute force attack
            sessionThread.writeString("530 Login incorrect.\r\n");
            sessionThread.authAttempt(false);
        }
    }
}
