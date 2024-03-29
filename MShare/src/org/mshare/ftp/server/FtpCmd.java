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
import java.lang.reflect.Constructor;

import org.mshare.file.SharedLinkSystem.Permission;
import org.mshare.ftp.server.AccountFactory.Token;

import android.util.Log;

public abstract class FtpCmd implements Runnable {
    private static final String TAG = FtpCmd.class.getSimpleName();

    protected SessionThread sessionThread;

    protected static CmdMap[] cmdClasses = { new CmdMap("SYST", CmdSYST.class),
            new CmdMap("USER", CmdUSER.class), new CmdMap("PASS", CmdPASS.class),
            new CmdMap("TYPE", CmdTYPE.class), new CmdMap("CWD", CmdCWD.class),
            new CmdMap("PWD", CmdPWD.class), new CmdMap("LIST", CmdLIST.class),
            new CmdMap("PASV", CmdPASV.class), new CmdMap("RETR", CmdRETR.class),
            new CmdMap("NLST", CmdNLST.class), new CmdMap("NOOP", CmdNOOP.class),
            new CmdMap("STOR", CmdSTOR.class), new CmdMap("DELE", CmdDELE.class),
            new CmdMap("RNFR", CmdRNFR.class), new CmdMap("RNTO", CmdRNTO.class),
            new CmdMap("RMD", CmdRMD.class), new CmdMap("MKD", CmdMKD.class),
            new CmdMap("OPTS", CmdOPTS.class), new CmdMap("PORT", CmdPORT.class),
            new CmdMap("QUIT", CmdQUIT.class), new CmdMap("FEAT", CmdFEAT.class),
            new CmdMap("SIZE", CmdSIZE.class), new CmdMap("CDUP", CmdCDUP.class),
            new CmdMap("APPE", CmdAPPE.class), new CmdMap("XCUP", CmdCDUP.class), // synonym
            new CmdMap("XPWD", CmdPWD.class), // synonym
            new CmdMap("XMKD", CmdMKD.class), // synonym
            new CmdMap("XRMD", CmdRMD.class), // synonym
            new CmdMap("MDTM", CmdMDTM.class), //
            new CmdMap("MFMT", CmdMFMT.class), //
            new CmdMap("REST", CmdREST.class), //
            new CmdMap("SITE", CmdSITE.class), //
    };

    // 所有读权限所能够发送的命令
    private static Class<?>[] allowedCmdsWhileRead = { CmdUSER.class, CmdPASS.class, //
            CmdCWD.class, CmdLIST.class, CmdMDTM.class, CmdNLST.class, CmdPASV.class, //
            CmdPWD.class, CmdQUIT.class, CmdRETR.class, CmdSIZE.class, CmdTYPE.class, //
            CmdCDUP.class, CmdNOOP.class, CmdSYST.class, CmdPORT.class, //
    };

    private static Class<?>[] allowedCmdsWhileNotLoggedIn = {
    	CmdUSER.class, CmdPASS.class, CmdQUIT.class
    };
    
    public FtpCmd(SessionThread sessionThread) {
        this.sessionThread = sessionThread;
    }

    @Override
    abstract public void run();

    protected static void dispatchCommand(SessionThread session, String inputString) {
        String[] strings = inputString.split(" ");
        String unrecognizedCmdMsg = "502 Command not recognized\r\n";
        if (strings == null) {
            // There was some egregious sort of parsing error
            String errString = "502 Command parse error\r\n";
            Log.d(TAG, errString);
            session.writeString(errString);
            return;
        }
        if (strings.length < 1) {
            Log.d(TAG, "No strings parsed");
            session.writeString(unrecognizedCmdMsg);
            return;
        }
        String verb = strings[0];
        if (verb.length() < 1) {
            Log.i(TAG, "Invalid command verb");
            session.writeString(unrecognizedCmdMsg);
            return;
        }
        FtpCmd cmdInstance = null;
        verb = verb.trim();
        verb = verb.toUpperCase();
        for (int i = 0; i < cmdClasses.length; i++) {

            if (cmdClasses[i].getName().equals(verb)) {
                // We found the correct command. We retrieve the corresponding
                // Class object, get the Constructor object for that Class, and
                // and use that Constructor to instantiate the correct FtpCmd
                // subclass. Yes, I'm serious.
                Constructor<? extends FtpCmd> constructor;
                try {
                    constructor = cmdClasses[i].getCommand().getConstructor(
                            new Class[] { SessionThread.class, String.class });
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "FtpCmd subclass lacks expected " + "constructor ");
                    return;
                }
                try {
                    cmdInstance = constructor.newInstance(new Object[] { session,
                            inputString });
                } catch (Exception e) {
                    Log.e(TAG, "Instance creation error on FtpCmd");
                    return;
                }
            }
        }
        if (cmdInstance == null) {
            // If we couldn't find a matching command,
            Log.d(TAG, "Ignoring unrecognized FTP verb: " + verb);
            session.writeString(unrecognizedCmdMsg);
            return;
        }

        Token token = session.getToken();
        
        // 对于已经登录的用户，将无条件地执行所发送的命令
        if (token != null && token.isValid()) {
        	// TODO 在用户的权限上不能够很好地告知客户端,下面的方式不是很好
        	if (token.accessWrite()) { // 检测写权限
        		cmdInstance.run();
        	} else if (token.accessRead()) { // 检测读权限
        		boolean validCmd = false;
                for (Class<?> cl : allowedCmdsWhileRead) {
                    if (cmdInstance.getClass().equals(cl)) {
                        validCmd = true;
                        break;
                    }
                }
                if (validCmd == true) {
                    cmdInstance.run();
                } else {
                    session.writeString("530 user is not allowed to use that command\r\n");
                }
        	} else {
        		// TODO 将返回无法执行
        		Log.e(TAG, "没有任何权限");
        		session.writeString("530 user is not allowed to use that command\r\n");
        	}
        } else {
        	boolean validCmd = false;
            for (Class<?> cl : allowedCmdsWhileNotLoggedIn) {
                if (cmdInstance.getClass().equals(cl)) {
                    validCmd = true;
                    break;
                }
            }
            if (validCmd == true) {
                cmdInstance.run();
            } else {
                session.writeString("530 Login first with USER and PASS, or QUIT\r\n");
            }
        }
    }

    /**
     * An FTP parameter is that part of the input string that occurs after the first
     * space, including any subsequent spaces. Also, we want to chop off the trailing
     * '\r\n', if present.
     *
     * Some parameters shouldn't be logged or output (e.g. passwords), so the caller can
     * use silent==true in that case.
     * @return 即便没有内容，也会返回""
     */
    static public String getParameter(String input, boolean silent) {
        if (input == null) {
            return "";
        }
        int firstSpacePosition = input.indexOf(' ');
        if (firstSpacePosition == -1) {
            return "";
        }
        String retString = input.substring(firstSpacePosition + 1);

        // Remove trailing whitespace
        // todo: trailing whitespace may be significant, just remove \r\n
        retString = retString.replaceAll("\\s+$", "");

        if (!silent) {
            Log.d(TAG, "Parsed argument: " + retString);
        }
        return retString;
    }

    /**
     * A wrapper around getParameter, for when we don't want it to be silent.
     */
    static public String getParameter(String input) {
        return getParameter(input, false);
    }

//    public static File inputPathToChrootedFile(File existingPrefix, String param) {
//        try {
//            if (param.charAt(0) == '/') {
//                // The STOR contained an absolute path
//                File chroot = FsSettings.getRootDir();
//                return new File(chroot, param);
//            }
//        } catch (Exception e) {
//        }
//
//        // The STOR contained a relative path
//        return new File(existingPrefix, param);
//    }

}
