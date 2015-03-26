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

import org.mshare.file.share.SharedFile;
import org.mshare.file.share.SharedLink;
import org.mshare.file.share.SharedLinkSystem;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.SessionThread;

import android.util.Log;

public class CmdNLST extends CmdAbstractListing implements Runnable {
    private static final String TAG = CmdNLST.class.getSimpleName();

    // The approximate number of milliseconds in 6 months
    public final static long MS_IN_SIX_MONTHS = 6L * 30L * 24L * 60L * 60L * 1000L;
    private final String input;

    public CmdNLST(SessionThread sessionThread, String input) {
        super(sessionThread, input);
        this.input = input;
    }

    @Override
    public void run() {
        String errString = null;

        mainblock: {
            String param = getParameter(input);
            if (param.startsWith("-")) {
                // Ignore options to list, which start with a dash
                param = "";
            }
            SharedLink fileToList = null;
            if (param.equals("")) {
                fileToList = sessionThread.getToken().getSystem().getWorkingDir();
            } else {
                if (param.contains("*")) {
                    errString = "550 NLST does not support wildcards\r\n";
                    break mainblock;
                }
                // ���ܽ��ܵ�ֻ���ļ���, ����õ���working directory�е�����
                fileToList = sessionThread.getToken().getSystem().getSharedLink(param);
                if (fileToList.isFile()) {
                    // Bernstein suggests that NLST should fail when a
                    // parameter is given and the parameter names a regular
                    // file (not a directory).
                    errString = "550 NLST for regular files is unsupported\r\n";
                    break mainblock;
                }
            }
            String listing;
            if (fileToList.isDirectory()) {
                StringBuilder response = new StringBuilder();
                errString = listDirectory(response, fileToList);
                if (errString != null) {
                    break mainblock;
                }
                listing = response.toString();
            } else {
                listing = makeLsString(fileToList);
                if (listing == null) {
                    errString = "450 Couldn't list that file\r\n";
                    break mainblock;
                }
            }
            errString = sendListing(listing);
            if (errString != null) {
                break mainblock;
            }
        }

        if (errString != null) {
            sessionThread.writeString(errString);
            Log.d(TAG, "NLST failed with: " + errString);
        } else {
            Log.d(TAG, "NLST completed OK");
        }
        // The success or error response over the control connection will
        // have already been handled by sendListing, so we can just quit now.
    }

    // ֻ��Ҫ�г����־Ϳ�����
    @Override
    protected String makeLsString(SharedLink file) {
        if (!file.exists()) {
            Log.i(TAG, "makeLsString had nonexistent file");
            return null;
        }

        // See Daniel Bernstein's explanation of NLST format at:
        // http://cr.yp.to/ftp/list/binls.html
        // This stuff is almost entirely based on his recommendations.

        String lastNamePart = file.getName();
        // Many clients can't handle files containing these symbols
        if (lastNamePart.contains("*") || lastNamePart.contains("/")) {
            Log.i(TAG, "Filename omitted due to disallowed character");
            return null;
        } else {
            Log.d(TAG, "Filename: " + lastNamePart);
            return lastNamePart + "\r\n";
        }
    }
}
