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

import org.mshare.file.SharedLink;
import org.mshare.file.SharedLinkSystem;

import android.util.Log;

/**
 * �����ļ��У�ͨ���÷������������ļ��ж���SharedFakeDirectory
 * @author HM
 *
 */
public class CmdMKD extends FtpCmd implements Runnable {
    private static final String TAG = CmdMKD.class.getSimpleName();

    String input;

    public CmdMKD(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "MKD executing");
        String param = getParameter(input);
        SharedLink toCreate;
        String errString = null;
        mainblock: {
            // If the param is an absolute path, use it as is. If it's a
            // relative path, prepend the current working directory.
            if (param.length() < 1) {
                errString = "550 Invalid name\r\n";
                break mainblock;
            }
            // ���ݵĲ������������·����Ҳ�������ļ���
            // TODO Ҫ��Ҫ�����еĲ������������������������ʾ����
            // ����϶��ǲ����ڵģ�File��������ٵģ�����SharedLink��������ʵ�ģ�
            // �����SharedFakeDirectory��������֣��᲻��̫���鷳
            SharedLinkSystem sharedLinkSystem = sessionThread.sharedLinkSystem;
            
            toCreate = sharedLinkSystem.getSharedLink(param);
            if (toCreate.exists()) {
                errString = "550 Already exists\r\n";
                break mainblock;
            }
            
            boolean createSuccess = false;
            if (param.charAt(0) == SharedLinkSystem.SEPARATOR_CHAR) { // ��һ�����·��
            	createSuccess = sharedLinkSystem.addSharedPath(param, null);
            } else { // ��ǰworking directory��
            	SharedLink workingDir = sessionThread.sharedLinkSystem.getWorkingDir();
            	// TODO �Ļ���join
            	createSuccess = sharedLinkSystem.addSharedPath(workingDir + SharedLinkSystem.SEPARATOR + param, null);
            }
            
            if (!createSuccess) {
            	// TODO ������õķ���
            	errString = "500 making fail\r\n";
            	break mainblock;
            }
            
            if (!toCreate.mkdir()) {
                errString = "550 Error making directory (permissions?)\r\n";
                break mainblock;
            }
        }
        if (errString != null) {
            sessionThread.writeString(errString);
            Log.i(TAG, "MKD error: " + errString.trim());
        } else {
            sessionThread.writeString("250 Directory created\r\n");
        }
        Log.i(TAG, "MKD complete");
    }

}
