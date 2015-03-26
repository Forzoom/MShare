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

import java.io.File;

import org.mshare.file.share.SharedLink;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.SessionThread;

import android.util.Log;

public class CmdRNFR extends FtpCmd implements Runnable {
    private static final String TAG = CmdRNFR.class.getSimpleName();

    protected String input;

    public CmdRNFR(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "Executing RNFR");
        String param = getParameter(input);
        String errString = null;
        SharedLink file = null;
        mainblock: {
        	// 参数或者是文件名
            file = sessionThread.getToken().getSystem().getSharedLink(param);
            Log.d(TAG, "from file fake name " + file.getFakePath());
            if (!file.exists()) {
                errString = "450 Cannot rename nonexistent file\r\n";
            }
        }
        if (errString != null) {
            sessionThread.writeString(errString);
            Log.d(TAG, "RNFR failed: " + errString.trim());
            sessionThread.setRenameFrom(null);
        } else {
            sessionThread.writeString("350 Filename noted, now send RNTO\r\n");
            sessionThread.setRenameFrom(file);
        }
    }
}
