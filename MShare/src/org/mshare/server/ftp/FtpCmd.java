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
            new FtpCmdMap("REST", CmdREST.class), ////
			new FtpCmdMap("SITE", CmdSITE.class), //
            new FtpCmdMap("SITE", CmdSITE.class), //
            new FtpCmdMap("UUID", CmdUUID.class), //
			new FtpCmdMap("NINA", CmdNINA.class),
			new FtpCmdMap("RTSP", CmdRTSP.class),
			new FtpCmdMap("CRTP", CmdCRTP.class)
    };

	// �Ὣ���е����ݶ�����һ�飬�����Ǻܺ�
	private static Class<?>[] allowCmdWhileWrite = {
			CmdSYST.class,
			CmdUSER.class, CmdPASS.class,
			CmdTYPE.class, CmdCWD.class,
			CmdPWD.class, CmdLIST.class,
			CmdPASV.class, CmdRETR.class,
			CmdNLST.class, CmdNOOP.class,
			CmdSTOR.class, CmdDELE.class,
			CmdRNFR.class, CmdRNTO.class,
			CmdRMD.class, CmdMKD.class,
			CmdOPTS.class, CmdPORT.class,
			CmdQUIT.class, CmdFEAT.class,
			CmdSIZE.class, CmdCDUP.class,
			CmdAPPE.class, CmdCDUP.class, //
			CmdPWD.class, //
			CmdMKD.class, //
			CmdRMD.class, //
			CmdMDTM.class, //
			CmdMFMT.class, //
			CmdREST.class, //
			CmdSITE.class, //
			CmdUUID.class, //
			CmdNINA.class,
			CmdRTSP.class,
			CmdCRTP.class
	};

    // ���ж�Ȩ�����ܹ����͵�����
    private static Class<?>[] allowedCmdsWhileRead = { CmdUSER.class, CmdPASS.class, //
            CmdCWD.class, CmdLIST.class, CmdMDTM.class, CmdNLST.class, CmdPASV.class, //
            CmdPWD.class, CmdQUIT.class, CmdRETR.class, CmdSIZE.class, CmdTYPE.class, //
            CmdCDUP.class, CmdNOOP.class, CmdSYST.class, CmdPORT.class, CmdUUID.class,
			CmdNINA.class, CmdRTSP.class, CmdCRTP.class // ������ֻ�ж�ȡȨ�޵��û���Ҳ����ʹ��RTSP
    };

    private static Class<?>[] allowedCmdsWhileNotLoggedIn = {
    	CmdUSER.class, CmdPASS.class, CmdQUIT.class, CmdUUID.class, CmdNINA.class
    };
    
    public FtpCmd(SessionThread sessionThread) {
        this.sessionThread = sessionThread;
    }

    @Override
    abstract public void run();

	// ����Ȩ�޽��зַ�����
    protected static void dispatchCommand(SessionThread session, String inputString) {
        // ʹ�ÿո����ָ�
        String[] strings = inputString.split(" ");
        // ��Ӧ��Cmd������
        String verb = strings[0];

        if (!isFtpCmd(inputString)) {
            Log.e(TAG, "something wrong happen? the command is not ftp command!");
			session.writeString(SessionThread.unrecognizedCmdMsg);
            return;
        }

        verb = verb.trim().toUpperCase();
        Token token = session.getToken();
		Class<?> verbClass = null;
		for (int i = 0; i < cmdClasses.length; i++) {
			if (cmdClasses[i].getName().equals(verb)) {
				verbClass = cmdClasses[i].getCommand();
			}
		}
        
        // �����Ѿ���¼���û�������������ִ�������͵�����
        if (token != null && token.isValid()) {
        	if (token.accessWrite()) { // ���дȨ��
				findAndExecuteCommand(session, verbClass, inputString, allowCmdWhileWrite);
        	} else if (token.accessRead()) { // ����Ȩ��
				findAndExecuteCommand(session, verbClass, inputString, allowedCmdsWhileRead);
                session.writeString("530 user is not allowed to use that command\r\n");
        	} else {
        		Log.e(TAG, "permission denied");
        		session.writeString("530 user is not allowed to use that command\r\n");
        	}
        } else {
			findAndExecuteCommand(session, verbClass, inputString, allowedCmdsWhileNotLoggedIn);
            session.writeString("530 Login first with USER and PASS, or QUIT\r\n");
        }
    }

	// �ַ��������cmd����֮�У�����Щ�����н���Ѱ��
	// �����ڲ��ַ���������û��Ĳ�ͬȨ��
	private static void findAndExecuteCommand(SessionThread session, Class<?> verb, String inputString, Class<?>[] commands) {
		for (int i = 0; i < commands.length; i++) {
			if (verb.equals(commands[i])) {
				// ��Ӧ�Ĺ��캯��
				Constructor<? extends FtpCmd> constructor = null;
				// ��Ӧ��Cmd����
				FtpCmd cmdInstance = null;

				// �������乹�캯��
				try {
					constructor = cmdClasses[i].getCommand().getConstructor(new Class[] { SessionThread.class, String.class });
				} catch (NoSuchMethodException e) {
					Log.e(TAG, "FtpCmd subclass lacks expected " + "constructor ");
					return;
				}
				// ����Cmd����
				try {
					cmdInstance = constructor.newInstance(new Object[] { session, inputString });
				} catch (Exception e) {
					Log.e(TAG, "Instance creation error on FtpCmd");
					return;
				}
				// ����cmd����ʧ�ܵ�ʱ��
				if (cmdInstance == null) {
					// If we couldn't find a matching command,
					Log.d(TAG, "Ignoring unrecognized FTP verb: " + verb);
					session.writeString(SessionThread.unrecognizedCmdMsg);
					return;
				}

				// ����cmd����ɹ�,����ֱ��ִ��
				cmdInstance.run();
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
		verb = verb.trim();
		verb = verb.toUpperCase();
        // ����û�а취��Ӧ
        for (int i = 0; i < cmdClasses.length; i++) {
            if (cmdClasses[i].getName().equals(verb)) {
                return true;
            }
        }
        return false;
    }

}
