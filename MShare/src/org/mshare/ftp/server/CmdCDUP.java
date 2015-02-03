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

import java.io.File;
import java.io.IOException;

import org.mshare.file.SharedLink;

import android.util.Log;
/**
 * 移动到父路径
 * @author HM
 *
 */
public class CmdCDUP extends FtpCmd implements Runnable {
    private static final String TAG = CmdCDUP.class.getSimpleName();
    protected String input;

    public CmdCDUP(SessionThread sessionThread, String input) {
        super(sessionThread);
    }

    @Override
    public void run() {
        Log.d(TAG, "CDUP executing");
        SharedLink newDir;
        String errString = null;
        mainBlock: {
            SharedLink workingDir = sessionThread.sharedLinkSystem.getWorkingDir();
            newDir = workingDir.getParent();
            if (newDir == null) {
                errString = "550 Current dir cannot find parent\r\n";
                break mainBlock;
            }

        	// TODO 确保没有..和.内容
            if (!newDir.isDirectory()) {
                errString = "550 Can't CWD to invalid directory\r\n";
                break mainBlock;
            } else if (newDir.canRead()) {
                sessionThread.sharedLinkSystem.setWorkingDir(newDir.getFakePath());
            } else {
                errString = "550 That path is inaccessible\r\n";
                break mainBlock;
            }
        }
        if (errString != null) {
            sessionThread.writeString(errString);
            Log.i(TAG, "CDUP error: " + errString);
        } else {
            sessionThread.writeString("200 CDUP successful\r\n");
            Log.d(TAG, "CDUP success");
        }
    }
}
