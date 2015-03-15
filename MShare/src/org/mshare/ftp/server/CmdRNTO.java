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

import org.mshare.file.share.SharedLink;
import org.mshare.file.share.SharedLinkSystem;
import org.mshare.main.MShareUtil;

import android.test.RenamingDelegatingContext;
import android.util.Log;

public class CmdRNTO extends FtpCmd implements Runnable {
    private static final String TAG = CmdRNTO.class.getSimpleName();

    protected String input;

    public CmdRNTO(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "RNTO executing");
        String param = getParameter(input);
        String errString = null;
        // 这需要文件的写权限
        // 这里的文件重命名操作确实有点麻烦，不知道为什么要这么做
        SharedLink toFile = null;
        mainblock: {
            Log.i(TAG, "param: " + param);
            // TODO 需要修改至三种文件类型都接受
            // 需要能够响应相对路径和文件名两种情况
            
            // 写权限检测
            if (!sessionThread.getToken().canWrite(sessionThread.getRenameFrom().getPermission())) {
            	errString = "550 permission denied\r\n";
            	break mainblock;
            }
            
            toFile = sessionThread.getToken().getSystem().getSharedLink(param);
            // TODO 可能存在真实文件被删除的情况
            if (toFile != null && toFile.exists()) {
            	errString = "550 already exist\r\n";
            	break mainblock;
            }
            
            String fakePath = null, realPath = null;
            SharedLink fromFile = sessionThread.getRenameFrom();
            if (fromFile == null) {
                errString = "550 Rename error, maybe RNFR not sent\r\n";
                break mainblock;
            }
            Log.i(TAG, "RNTO from file: " + fromFile.getFakePath());
            String fromFileParentPath = fromFile.getParent();
            
            // TODO 在这里创建SharedLink的对象不是很好
            if (fromFile.isFile()) {
            	if (fromFileParentPath.equals(SharedLinkSystem.SEPARATOR)) {
            		fakePath = fromFileParentPath + param;
            	} else {
            		fakePath = fromFileParentPath + SharedLinkSystem.SEPARATOR + param;
            	}
            	realPath = fromFile.getRealFile().getParent() + File.separator + MShareUtil.guessName(param);
            	toFile = SharedLink.newFile(sessionThread.getToken().getSystem(), fakePath, realPath, fromFile.getPermission());
            } else if (fromFile.isFakeDirectory()) {
            	if (fromFileParentPath.equals(SharedLinkSystem.SEPARATOR)) {
            		fakePath = fromFileParentPath + param;
            	} else {
            		fakePath = fromFileParentPath + SharedLinkSystem.SEPARATOR + param;
            	}
            	toFile = SharedLink.newFakeDirectory(sessionThread.getToken().getSystem(), fakePath, fromFile.getPermission());
            } else if (fromFile.isDirectory()) {
            	// 暂时没有
            	toFile = null;
            }
            Log.i(TAG, "RNTO to file: " + toFile.getFakePath());
            
            // TODO: this code is working around a bug that java6 and before cannot
            // reliable move a file, once java7 is supported by Dalvik, this code can
            // be replaced with Files.move()
            
            if (!fromFile.renameTo(toFile)) {
            	errString = "550 Error during rename operation\r\n";
            	break mainblock;
            }
            
            /*
            
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("temp_" + fromFile.getName(), null,
                        sessionThread.getWorkingDirStr());
                if (fromFile.isDirectory()) {
                    String tmpFilePath = tmpFile.getPath();
                    tmpFile.delete();
                    tmpFile = new File(tmpFilePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                errString = "550 Error during rename operation\r\n";
                break mainblock;
            }
            if (!fromFile.renameTo(tmpFile)) {
                errString = "550 Error during rename operation\r\n";
                break mainblock;
            }
            fromFile.delete();
            if (!tmpFile.renameTo(toFile)) {
                errString = "550 Error during rename operation\r\n";
                break mainblock;
            }
            */
        }
        if (errString != null) {
            sessionThread.writeString(errString);
            Log.i(TAG, "RNFR failed: " + errString.trim());
        } else {
            sessionThread.writeString("250 rename successful\r\n");
        }
        sessionThread.setRenameFrom(null);
        Log.d(TAG, "RNTO finished");
    }
}
