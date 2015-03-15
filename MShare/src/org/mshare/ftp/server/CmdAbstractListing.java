/*
Copyright 2013 Pareit Pieter
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

/*
 * Since the FTP verbs LIST and NLST do very similar things related to listing
 * directory contents, the common tasks that they share have been factored
 * out into this abstract class. Both CmdLIST and CmdNLST inherit from this
 * class.
 */

package org.mshare.ftp.server;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import org.mshare.file.share.SharedFile;
import org.mshare.file.share.SharedLink;
import org.mshare.ftp.server.*;

public abstract class CmdAbstractListing extends FtpCmd {
    // TODO: .class.getSimpleName() from abstract class?
    private static String TAG = "CmdAbstractListing";

    public CmdAbstractListing(SessionThread sessionThread, String input) {
        super(sessionThread);
    }

    abstract String makeLsString(SharedLink file);

    /**
     * 接受Directory和FakeDirectory
     * Creates a directory listing by finding the contents of the directory,
     * calling makeLsString on each file, and concatenating the results.
     * Returns an error string if failure, returns null on success. May be
     * called by CmdLIST or CmdNLST, since they each override makeLsString
     * in a different way.
     */
    public String listDirectory(StringBuilder response, SharedLink dir) {
    	String errString = null;
    	
        if (!dir.isDirectory() && !dir.isFakeDirectory()) {
            errString = "500 Internal error, listDirectory on non-directory\r\n";
        } else {
        	Log.d(TAG, "Listing directory: " + dir.toString());
        	// Get a listing of all files and directories in the path
        	SharedLink[] entries = dir.listFiles();
        	if (entries == null) {
        		errString = "500 Couldn't list directory. Check config and mount status.\r\n";
        	} else {
        		Log.d(TAG, "Dir len " + entries.length);
        	}

            try {
                Arrays.sort(entries, listingComparator);
            } catch (Exception e) {
                // once got a FC on this, seems it is possible to have a dir that
                // breaks the listing comparator (unable to reproduce)
                Log.e(TAG, "Unable to sort the listing: " + e.getMessage());
                // play for sure, and get back the entries
                entries = dir.listFiles();
            }
            for (SharedLink entry : entries) {
                String curLine = makeLsString(entry);
                if (curLine != null) {
                    response.append(curLine);
                }
            }
        }

        return errString;
    }

    // Send the directory listing over the data socket. Used by CmdLIST and CmdNLST.
    // Returns an error string on failure, or returns null if successful.
    /**
     * 将listing中的内容发送，如果出现错误，将返回错误信息
     * @param listing
     * @return
     */
    protected String sendListing(String listing) {
        if (sessionThread.startUsingDataSocket()) {
            Log.d(TAG, "LIST/NLST done making socket");
        } else {
            sessionThread.closeDataSocket();
            return "425 Error opening data socket\r\n";
        }
        String mode = sessionThread.isBinaryMode() ? "BINARY" : "ASCII";
        sessionThread.writeString("150 Opening " + mode
                + " mode data connection for file list\r\n");
        Log.d(TAG, "Sent code 150, sending listing string now");
        if (!sessionThread.sendViaDataSocket(listing)) {
            Log.d(TAG, "sendViaDataSocket failure");
            sessionThread.closeDataSocket();
            return "426 Data socket or network error\r\n";
        }
        sessionThread.closeDataSocket();
        Log.d(TAG, "Listing sendViaDataSocket success");
        sessionThread.writeString("226 Data transmission OK\r\n");
        return null;
    }

    /**
     * Comparator to sort file listings. Sorts directories before files, sorts
     * alphabetical ignoring case
     */
    static final Comparator<SharedLink> listingComparator = new Comparator<SharedLink> (){
        @Override
        public int compare(SharedLink lhs, SharedLink rhs) {
            if ((lhs.isDirectory() || lhs.isFakeDirectory()) && rhs.isFile()) {
                return -1;
            } else if (lhs.isFile() && (rhs.isDirectory() || rhs.isFakeDirectory())) {
                return 1;
            } else {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        }
    };
}
