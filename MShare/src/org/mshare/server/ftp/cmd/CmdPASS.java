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

package org.mshare.server.ftp.cmd;

import org.mshare.account.AccountFactory.Token;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.FtpParser;
import org.mshare.server.ftp.SessionThread;
import org.mshare.server.ftp.Util;

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
        String attemptPassword = FtpParser.getParameter(input); // silent
        
        if (sessionThread.sessionInfo.getUsername() == null) {
        	Log.e(TAG, "send USER first!");
        	sessionThread.writeString("503 Must send USER first\r\n");
        	return;
        }
        
        // 输入的内容可能会有错误
        if (attemptPassword != null && !attemptPassword.equals("")) {
        	if (sessionThread.authAttempt(sessionThread.sessionInfo.getUsername(), attemptPassword) != null) {
        		Log.i(TAG, "User " + sessionThread.sessionInfo.getUsername() + " verified");
        		
        		Token token = sessionThread.getToken();
        		if (token.isGuest()) {
            		sessionThread.writeString("230 Guest login ok, read only access.\r\n");
            	} else {
                	sessionThread.writeString("230 Access granted\r\n");
            	}
        	} else {
        		Log.i(TAG, "Failed authentication");
                Util.sleepIgnoreInterupt(1000); // sleep to foil brute force attack
                sessionThread.writeString("530 Login incorrect.\r\n");
        	}
        } else {
        	Log.e(TAG, "password is empty");
			// TODO 暂时在这里使用530表示没有密码，即密码不正确
			sessionThread.writeString("530 Login incorrect.\r\n");
        }
		Log.d(TAG, "finished PASS");
    }
}
