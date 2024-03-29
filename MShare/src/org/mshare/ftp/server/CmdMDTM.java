/*
Copyright 2014 Pieter Pareit

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.mshare.file.SharedLink;

import android.util.Log;

/**
 * Implements File Modification Time
 */
public class CmdMDTM extends FtpCmd implements Runnable {
    private static final String TAG = CmdMDTM.class.getSimpleName();

    private String mInput;

    public CmdMDTM(SessionThread sessionThread, String input) {
        super(sessionThread);
        mInput = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: MDTM executing, input: " + mInput);
        String param = getParameter(mInput);
        SharedLink file = sessionThread.getToken().getSystem().getSharedLink(param);

        if (file.exists()) {
            long lastModified = file.lastModified();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
            String response = "213 " + df.format(new Date(lastModified)) + "\r\n";
            sessionThread.writeString(response);
        } else {
            Log.w(TAG, "run: file does not exist");
            sessionThread.writeString("550 file does not exist\r\n");
        }

        Log.d(TAG, "run: MDTM completed");
    }

}

