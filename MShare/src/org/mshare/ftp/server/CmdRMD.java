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

import org.mshare.file.share.SharedDirectory;
import org.mshare.file.share.SharedLink;

import android.util.Log;

public class CmdRMD extends FtpCmd implements Runnable {
    private static final String TAG = CmdRMD.class.getSimpleName();

    protected String input;

    public CmdRMD(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "RMD executing");
        String param = getParameter(input);
        SharedLink toRemove;
        String errString = null;
        mainblock: {
        	// 为什么这里要判定和处理参数？为了反之将根文件删除?
        	// TODO 需要加上"/"
            if (param.length() < 1) {
                errString = "550 Invalid argument\r\n";
                break mainblock;
            }
            toRemove = sessionThread.getToken().getSystem().getSharedLink(param);
            if (!(toRemove.isDirectory() || toRemove.isFakeDirectory())) {
                errString = "550 Can't RMD a non-directory\r\n";
                break mainblock;
            }
            if (toRemove.equals(new File("/"))) {
                errString = "550 Won't RMD the root directory\r\n";
                break mainblock;
            }
            if (!recursiveDelete(toRemove)) {
                errString = "550 Deletion error, possibly incomplete\r\n";
                break mainblock;
            }
        }
        if (errString != null) {
            sessionThread.writeString(errString);
            Log.i(TAG, "RMD failed: " + errString.trim());
        } else {
            sessionThread.writeString("250 Removed directory\r\n");
        }
        Log.d(TAG, "RMD finished");
    }

    /**
     * TODO 可能出现删除不完整的情况
     * Accepts a file or directory name, and recursively deletes the contents of that
     * directory and all subdirectories.
     * 
     * @param toDelete
     * @return Whether the operation completed successfully
     */
    protected boolean recursiveDelete(SharedLink toDelete) {
        if (!toDelete.exists()) {
            return false;
        }
        if (toDelete.isDirectory() || toDelete.isFakeDirectory()) { // 迭代删除其中内容
            // If any of the recursive operations fail, then we return false
            boolean success = true;
            SharedLink[] toDeleteList = toDelete.listFiles();
            for (SharedLink entry : toDeleteList) {
                success &= recursiveDelete(entry);
            }
            Log.d(TAG, "Recursively deleted: " + toDelete);
            return success && toDelete.delete();
        } else {
            Log.d(TAG, "RMD deleting file: " + toDelete);
            boolean success = toDelete.delete();
            // TODO 需要了解这里是为了什么
            MediaUpdater.notifyFileDeleted(toDelete.getFakePath());
            return success;
        }
    }
}
