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

import android.util.Log;

import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.FtpParser;
import org.mshare.server.ftp.SessionThread;

public class CmdUSER extends FtpCmd implements Runnable {
    private static final String TAG = CmdUSER.class.getSimpleName();

    protected String input;

    public CmdUSER(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;

    }

    @Override
    public void run() {
        Log.d(TAG, "USER executing");
        String username = FtpParser.getParameter(input);
        // 只允许这样的用户名
        if (!username.matches("[A-Za-z0-9]+")) {
        	Log.e(TAG, "Invalid username " + username);
            sessionThread.writeString("530 Invalid username\r\n");
            return;
        }
        
        // 在SessionThread中处理
        sessionThread.getSessionInfo().setUsername(username);
        sessionThread.authFails = 0;
        // TODO 没有办法判定username不存在，那么不是没用了吗
//        if (username == null) {
//        	Log.e(TAG, "username " + username + " is not exist");
//        	sessionThread.writeString("530 username is not exist\r\n");
//            return;
//        }

        sessionThread.writeString("331 Send password\r\n");
        Log.d(TAG, "USER finished");
    }

}
