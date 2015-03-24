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

/* The code that is common to LIST and NLST is implemented in the abstract
 * class CmdAbstractListing, which is inherited here.
 * CmdLIST and CmdNLST just override the
 * makeLsString() function in different ways to provide the different forms
 * of output.
 */

package org.mshare.server.ftp.cmd;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.mshare.file.share.SharedLink;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.SessionThread;

import android.util.Log;

public class CmdLIST extends CmdAbstractListing implements Runnable {
    static private final String TAG = CmdLIST.class.getSimpleName();

    // The approximate number of milliseconds in 6 months
    public final static long MS_IN_SIX_MONTHS = 6L * 30L * 24L * 60L * 60L * 1000L;
    private final String input;

    public CmdLIST(SessionThread sessionThread, String input) {
        super(sessionThread, input);
        this.input = input;
    }

    @Override
    public void run() {
        String errString = null;

        mainblock: {
            String param = getParameter(input);
            Log.d(TAG, "LIST parameter: " + param);
            // 所有的LIST参数都将被忽略
            while (param.startsWith("-")) {
                // Skip all dashed -args, if present
                Log.d(TAG, "LIST is skipping dashed arg " + param);
                param = getParameter(param);
            }
            
            // 筛除文件名不合格的文件
            // 获得所需要列出的文件
            SharedLink fileToList = null;
            if (param.equals("")) { // 没有参数
                fileToList = sessionThread.getToken().getSystem().getWorkingDir();
            } else {
                if (param.contains("*")) {
                    errString = "550 LIST does not support wildcards\r\n";
                    break mainblock;
                }
                fileToList = sessionThread.getToken().getSystem().getSharedLink(param);
            }
            // 列表的结果
            String listing;
            // 当前是一个共享的文件夹 
            // TODO 需要了解共享文件夹中是否可以有其他的文件
            if (fileToList.isDirectory() || fileToList.isFakeDirectory()) {
            	StringBuilder response = new StringBuilder();
                errString = listDirectory(response, fileToList);
                if (errString != null) {
                    break mainblock;
                }
                listing = response.toString();
            } else if (fileToList.isFile()) { // 当前是一个文件
                listing = makeLsString(fileToList);
                if (listing == null) {
                    errString = "450 Couldn't list that file\r\n";
                    break mainblock;
                }
            } else {
            	listing = ""; // 消除错误
            	errString = "500 internal server error\r\n";
            	break mainblock;
            }
            errString = sendListing(listing);
            if (errString != null) {
                break mainblock;
            }
        }

        if (errString != null) {
            sessionThread.writeString(errString);
            Log.d(TAG, "LIST failed with: " + errString);
        } else {
            Log.d(TAG, "LIST completed OK");
        }
        // The success or error response over the control connection will
        // have already been handled by sendListing, so we can just quit now.
    }

    // TODO 所有的操作都是SharedFile相关，该函数将来可以删除
    // 注意：只会添加文件的名字，和路径并没有关系
    // Generates a line of a directory listing in the traditional /bin/ls
    // format.
    @Override
    protected String makeLsString(SharedLink file) {
        StringBuilder response = new StringBuilder();

        if (!file.exists()) {
            Log.i(TAG, "makeLsString had nonexistent file :" + file.getRealFile());
            return null;
        }

        // See Daniel Bernstein's explanation of /bin/ls format at:
        // http://cr.yp.to/ftp/list/binls.html
        // This stuff is almost entirely based on his recommendations.

        String lastNamePart = file.getName();
        // Many clients can't handle files containing these symbols
        if (lastNamePart.contains("*") || lastNamePart.contains("/")) {
            Log.i(TAG, "Filename omitted due to disallowed character");
            return null;
        } else {
            // The following line generates many calls in large directories
            // staticLog.l(Log.DEBUG, "Filename: " + lastNamePart);
        }

        // 获得文件的权限
        // TODO: think about special files, symlinks, devices
        // TODO 需要了解1和group是什么
        // TODO 需要测试是否正确
        response.append(file.getLsPermission() + " 1 owner group");

        // The next field is a 13-byte right-justified space-padded file size
        long fileSize = file.length();
        String sizeString = new Long(fileSize).toString();
        int padSpaces = 13 - sizeString.length();
        while (padSpaces-- > 0) {
            response.append(' ');
        }
        response.append(sizeString);

        // The format of the timestamp varies depending on whether the mtime
        // is 6 months old
        long mTime = file.lastModified();
        SimpleDateFormat format;
        // Temporarily commented out.. trying to fix Win7 display bug
        if ((System.currentTimeMillis() - mTime) < MS_IN_SIX_MONTHS) {
            // The mtime is less than 6 months ago
            format = new SimpleDateFormat(" MMM dd HH:mm ", Locale.US);
        } else {
            // The mtime is more than 6 months ago
            format = new SimpleDateFormat(" MMM dd  yyyy ", Locale.US);
        }
        response.append(format.format(new Date(file.lastModified())));
        response.append(lastNamePart);
        response.append("\r\n");
        Log.d(TAG, "list result :" + response.toString());
        return response.toString();
    }

    
}
