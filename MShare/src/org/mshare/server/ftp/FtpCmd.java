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

package org.mshare.server.ftp;

import java.lang.reflect.Constructor;

import org.mshare.account.AccountFactory.Token;
import org.mshare.server.ftp.cmd.*;

import android.util.Log;

public abstract class FtpCmd implements Runnable {
    private static final String TAG = FtpCmd.class.getSimpleName();

    protected SessionThread sessionThread;

    protected static FtpCmdMap[] cmdClasses = { new FtpCmdMap("SYST", CmdSYST.class),
            new FtpCmdMap("USER", CmdUSER.class), new FtpCmdMap("PASS", CmdPASS.class),
            new FtpCmdMap("TYPE", CmdTYPE.class), new FtpCmdMap("CWD", CmdCWD.class),
            new FtpCmdMap("PWD", CmdPWD.class), new FtpCmdMap("LIST", CmdLIST.class),
            new FtpCmdMap("PASV", CmdPASV.class), new FtpCmdMap("RETR", CmdRETR.class),
            new FtpCmdMap("NLST", CmdNLST.class), new FtpCmdMap("NOOP", CmdNOOP.class),
            new FtpCmdMap("STOR", CmdSTOR.class), new FtpCmdMap("DELE", CmdDELE.class),
            new FtpCmdMap("RNFR", CmdRNFR.class), new FtpCmdMap("RNTO", CmdRNTO.class),
            new FtpCmdMap("RMD", CmdRMD.class), new FtpCmdMap("MKD", CmdMKD.class),
            new FtpCmdMap("OPTS", CmdOPTS.class), new FtpCmdMap("PORT", CmdPORT.class),
            new FtpCmdMap("QUIT", CmdQUIT.class), new FtpCmdMap("FEAT", CmdFEAT.class),
            new FtpCmdMap("SIZE", CmdSIZE.class), new FtpCmdMap("CDUP", CmdCDUP.class),
            new FtpCmdMap("APPE", CmdAPPE.class), new FtpCmdMap("XCUP", CmdCDUP.class), // synonym
            new FtpCmdMap("XPWD", CmdPWD.class), // synonym
            new FtpCmdMap("XMKD", CmdMKD.class), // synonym
            new FtpCmdMap("XRMD", CmdRMD.class), // synonym
            new FtpCmdMap("MDTM", CmdMDTM.class), //
            new FtpCmdMap("MFMT", CmdMFMT.class), //
            new FtpCmdMap("REST", CmdREST.class), //
            new FtpCmdMap("SITE", CmdSITE.class), //
            new FtpCmdMap("UUID", CmdUUID.class), //
    };

    // ���ж�Ȩ�����ܹ����͵�����
    private static Class<?>[] allowedCmdsWhileRead = { CmdUSER.class, CmdPASS.class, //
            CmdCWD.class, CmdLIST.class, CmdMDTM.class, CmdNLST.class, CmdPASV.class, //
            CmdPWD.class, CmdQUIT.class, CmdRETR.class, CmdSIZE.class, CmdTYPE.class, //
            CmdCDUP.class, CmdNOOP.class, CmdSYST.class, CmdPORT.class, CmdUUID.class
    };

    private static Class<?>[] allowedCmdsWhileNotLoggedIn = {
    	CmdUSER.class, CmdPASS.class, CmdQUIT.class, CmdUUID.class
    };
    
    public FtpCmd(SessionThread sessionThread) {
        this.sessionThread = sessionThread;
    }

    @Override
    abstract public void run();

    protected static void dispatchCommand(SessionThread session, String inputString) {
        // ʹ�ÿո����ָ�
        String[] strings = inputString.split(" ");
        // ���ڲ���ʶ���Cmd
        String unrecognizedCmdMsg = "502 Command not recognized\r\n";
        // ����ʶ��
        if (strings == null) {
            // There was some egregious sort of parsing error
            String errString = "502 Command parse error\r\n";
            Log.d(TAG, errString);
            session.writeString(errString);
            return;
        }
        // û��ʶ�������
        if (strings.length < 1) {
            Log.d(TAG, "No strings parsed");
            session.writeString(unrecognizedCmdMsg);
            return;
        }
        // ��Ӧ��Cmd������
        String verb = strings[0];
        if (verb.length() < 1) {
            Log.i(TAG, "Invalid command verb");
            session.writeString(unrecognizedCmdMsg);
            return;
        }

        // ��Ҫ�Ż�
        if (!isFtpCmd(inputString)) {
            Log.e(TAG, "something wrong happen? the command is not ftp command!");
            return;
        }

        FtpCmd cmdInstance = null;
        verb = verb.trim();
        verb = verb.toUpperCase();
        // ͨ��CmdMap���γɵ�һ��ӳ���ϵ
        for (int i = 0; i < cmdClasses.length; i++) {

            // ��verb�������ƶ�����
            if (cmdClasses[i].getName().equals(verb)) {
                Constructor<? extends FtpCmd> constructor;
                try {
                    constructor = cmdClasses[i].getCommand().getConstructor(new Class[] { SessionThread.class, String.class });
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "FtpCmd subclass lacks expected " + "constructor ");
                    return;
                }
                try {
                    cmdInstance = constructor.newInstance(new Object[] { session, inputString });
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

        // �ж��ܹ�ʹ����ЩCmd
        // ��Ҫ���жϵ�ǰ�Ƿ��ܹ�ʹ�ø�Cmd��Ȼ���ٴ���

        Token token = session.getToken();
        
        // �����Ѿ���¼���û�������������ִ�������͵�����
        if (token != null && token.isValid()) {
        	// TODO ���û���Ȩ���ϲ��ܹ��ܺõظ�֪�ͻ���,����ķ�ʽ���Ǻܺ�
        	if (token.accessWrite()) { // ���дȨ��
        		cmdInstance.run();
        	} else if (token.accessRead()) { // ����Ȩ��
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
        		// TODO �������޷�ִ��
        		Log.e(TAG, "û���κ�Ȩ��");
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
     * �ж������ܵ��������Ƿ���FTP������
     * @return
     */
    public static boolean isFtpCmd(String inputString) {
        String[] strings = inputString.split(" ");
        // ����ʶ��
        if (strings == null || strings.length < 1) {
            Log.d(TAG, "No strings parsed");
            return false;
        }
        // ��Ӧ��Cmd������
        String verb = strings[0];
        if (verb.length() < 1) {
            Log.i(TAG, "Invalid command verb");
            return false;
        }
        // ����û�а취��Ӧ
        for (int i = 0; i < cmdClasses.length; i++) {
            if (cmdClasses[i].getName().equals(verb)) {
                return true;
            }
        }
        return false;
    }

    /**
     * An FTP parameter is that part of the input string that occurs after the first
     * space, including any subsequent spaces. Also, we want to chop off the trailing
     * '\r\n', if present.
     *
     * Some parameters shouldn't be logged or output (e.g. passwords), so the caller can
     * use silent==true in that case.
     * @return ����û�����ݣ�Ҳ�᷵��""
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
        // ɾ�����еĽ�β���Ŀհ�
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
//                File chroot = ServerSettings.getRootDir();
//                return new File(chroot, param);
//            }
//        } catch (Exception e) {
//        }
//
//        // The STOR contained a relative path
//        return new File(existingPrefix, param);
//    }

}