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

import org.mshare.file.share.SharedLink;

import android.util.Log;

/**
 * TODO 对于客户端，应该可以发送是否删除磁盘上的文件,应该提示用户是取消共享还是删除文件
 * @author HM
 *
 */
public class CmdDELE extends FtpCmd implements Runnable {
    private static final String TAG = CmdDELE.class.getSimpleName();

    protected String input;

    public CmdDELE(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "DELE executing");
        String param = getParameter(input);
        SharedLink storeFile = sessionThread.getToken().getSystem().getSharedLink(param);
        
        String errString = null;
        if (storeFile == null) {
        	errString = "550 file is not exist\r\n";
        } else if (storeFile.isDirectory()) {
            errString = "550 Can't DELE a directory\r\n";
        } else if (!storeFile.delete()) {
            errString = "450 Error deleting file\r\n";
        }

        if (errString != null) {
            sessionThread.writeString(errString);
            Log.i(TAG, "DELE failed: " + errString.trim());
        } else {
            sessionThread.writeString("250 File successfully deleted\r\n");
            // TODO 这里必须了解MediaUpdater是干什么用的
//            MediaUpdater.notifyFileDeleted(storeFile.getPath());
        }
        Log.d(TAG, "DELE finished");
    }

}
