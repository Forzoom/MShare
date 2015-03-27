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

import org.mshare.file.share.SharedLink;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.FtpParser;
import org.mshare.server.ftp.SessionThread;

import android.util.Log;

public class CmdCWD extends FtpCmd implements Runnable {
    private static final String TAG = CmdCWD.class.getSimpleName();

    protected String input;

    public CmdCWD(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "CWD executing");
        String param = FtpParser.getParameter(input);
        SharedLink newDir;
        String errString = null;
    	newDir = sessionThread.getToken().getSystem().getSharedLink(param);
    	// TODO 确保newDir不是null且exist，不知道当不存在的时候该返回什么内容
    	
    	
    	// TODO 确保传入的param中不存在..和.的内容
        Log.i(TAG, "New directory: " + newDir);
        // CWD允许切换到directory和fakeDirectory
        if (!(newDir.isDirectory() || newDir.isFakeDirectory())) {
            sessionThread.writeString("550 Can't CWD to invalid directory\r\n");
        } else if (newDir.canRead()) {
            sessionThread.getToken().getSystem().setWorkingDir(newDir.getFakePath());
            sessionThread.writeString("250 CWD successful\r\n");
        } else {
            sessionThread.writeString("550 That path is inaccessible\r\n");
        }
        Log.d(TAG, "CWD complete");
    }
}
