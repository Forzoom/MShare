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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.mshare.file.SharedLink;
import org.mshare.file.SharedLinkSystem;

import android.util.Log;

/**
 * 代表的应该是与Client的Thread
 * @author HM
 * TODO 主要是如果出现..的情况下的文件路径，可能会出现问题
 */
public class SessionThread extends Thread {
    private static final String TAG = SessionThread.class.getSimpleName();

    protected boolean shouldExit = false;
    protected Socket cmdSocket;
    protected ByteBuffer buffer = ByteBuffer.allocate(Defaults.getInputBufferSize());
    protected boolean pasvMode = false;
    protected boolean binaryMode = false;
    private Account account = null;
    // TODO 从数据存储中将原本的文件数据取出
    /**
     * 数据传送所使用的Socket
     */
    protected Socket dataSocket = null;
    protected SharedLink renameFrom = null;
    protected LocalDataSocket localDataSocket;
    /**
     * 从dataSocket中获得的OutputStream，只有当使用dataSocket发送的时候，才不是null
     */
    OutputStream dataOutputStream = null;
    /**
     * 指示是否在开始的是否发送HELLO
     */
    private boolean sendWelcomeBanner;
    /**
     * 所有通过writeString的内容都将使用
     */
    protected String encoding = Defaults.SESSION_ENCODING;
    protected long offset = -1; // where to start append when using REST

    public static int MAX_AUTH_FAILS = 3;

    public SessionThread(Socket socket, LocalDataSocket dataSocket) {
        this.cmdSocket = socket;
        this.localDataSocket = dataSocket;
        this.sendWelcomeBanner = true;
    }

    /**
     * Sends a string over the already-established data socket
     * 发送一个字符串，通过已经建立的数据套接字(通过对字符串进行相应编码的转换，转换成2进制再发送)
     * @param string
     * @return Whether the send completed successfully
     */
    public boolean sendViaDataSocket(String string) {
        try {
            byte[] bytes = string.getBytes(encoding);
            Log.d(TAG, "Using data connection encoding: " + encoding);
            return sendViaDataSocket(bytes, bytes.length);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding for data socket send");
            return false;
        }
    }

    /**
     * see {@link #sendViaDataSocket(String)}
     * @param bytes
     * @param len
     * @return
     */
    public boolean sendViaDataSocket(byte[] bytes, int len) {
        return sendViaDataSocket(bytes, 0, len);
    }

    /**
     * Sends a byte array over the already-established data socket
     *
     * @param bytes
     * @param len
     * @return
     */
    public boolean sendViaDataSocket(byte[] bytes, int start, int len) {

        if (dataOutputStream == null) {
            Log.i(TAG, "Can't send via null dataOutputStream");
            return false;
        }
        if (len == 0) {
            return true; // this isn't an "error"
        }
        try {
            dataOutputStream.write(bytes, start, len);
        } catch (IOException e) {
            Log.i(TAG, "Couldn't write output stream for data socket");
            Log.i(TAG, e.toString());
            return false;
        }
        localDataSocket.reportTraffic(len);
        return true;
    }

    /**
     * Received some bytes from the data socket, which is assumed to already be connected.
     * The bytes are placed in the given array, and the number of bytes successfully read
     * is returned.
     *
     * 从dataSocket接收数据
     *
     * @param bytes
     *            Where to place the input bytes
     * @return >0 if successful which is the number of bytes read, -1 if no bytes remain
     *         to be read, -2 if the data socket was not connected, 0 if there was a read
     *         error
     */
    public int receiveFromDataSocket(byte[] buf) {
        int bytesRead;

        if (dataSocket == null) {
            Log.i(TAG, "Can't receive from null dataSocket");
            return -2;
        }
        if (!dataSocket.isConnected()) {
            Log.i(TAG, "Can't receive from unconnected socket");
            return -2;
        }
        InputStream in;
        try {
            in = dataSocket.getInputStream();
            // If the read returns 0 bytes, the stream is not yet
            // closed, but we just want to read again.
            while ((bytesRead = in.read(buf, 0, buf.length)) == 0) {
            }
            if (bytesRead == -1) {
                // If InputStream.read returns -1, there are no bytes
                // remaining, so we return 0.
                return -1;
            }
        } catch (IOException e) {
            Log.i(TAG, "Error reading data socket");
            return 0;
        }
        localDataSocket.reportTraffic(bytesRead);
        return bytesRead;
    }

    /**
     * Called when we receive a PASV command.
     *
     * @return Whether the necessary initialization was successful.
     */
    public int onPasv() {
        return localDataSocket.onPasv();
    }

    /**
     * Called when we receive a PORT command.
     *
     * @return Whether the necessary initialization was successful.
     */
    public boolean onPort(InetAddress dest, int port) {
        return localDataSocket.onPort(dest, port);
    }

    public InetAddress getDataSocketPasvIp() {
        // When the client sends PASV, our reply will contain the address and port
        // of the data connection that the client should connect to. For this purpose
        // we always use the same IP address that the command socket is using.
        return cmdSocket.getLocalAddress();
    }

    /**
     * Will be called by (e.g.) CmdSTOR, CmdRETR, CmdLIST, etc. when they are about to
     * start actually doing IO over the data socket.
     *
     * @return
     */
    public boolean startUsingDataSocket() {
        try {
            dataSocket = localDataSocket.onTransfer();
            if (dataSocket == null) {
                Log.i(TAG, "dataSocketFactory.onTransfer() returned null");
                return false;
            }
            dataOutputStream = dataSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            Log.i(TAG, "IOException getting OutputStream for data socket");
            dataSocket = null;
            return false;
        }
    }

    public void quit() {
        Log.d(TAG, "SessionThread told to quit");
        closeSocket();
    }

    public void closeDataSocket() {
        Log.d(TAG, "Closing data socket");
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
            }
            dataOutputStream = null;
        }
        if (dataSocket != null) {
            try {
                dataSocket.close();
            } catch (IOException e) {
            }
        }
        dataSocket = null;
    }

    protected InetAddress getLocalAddress() {
        return cmdSocket.getLocalAddress();
    }

    @Override
    public void run() {
        Log.i(TAG, "SessionThread started");

        if (sendWelcomeBanner) {
            writeString("220 SwiFTP ready\r\n");
        }
        // Main loop: read an incoming line and process it
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    cmdSocket.getInputStream()), 8192); // use 8k buffer
            while (true) {
                String line;
                line = in.readLine(); // will accept \r\n or \n for terminator
                if (line != null) {
                    FsService.writeMonitor(true, line);
                    Log.d(TAG, "Received line from client: " + line);
                    FtpCmd.dispatchCommand(this, line);
                } else {
                    Log.i(TAG, "readLine gave null, quitting");
                    break;
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "Connection was dropped");
        }
        closeSocket();
    }

    /**
     * A static method to check the equality of two byte arrays, but only up to a given
     * length.
     */
    public static boolean compareLen(byte[] array1, byte[] array2, int len) {
        for (int i = 0; i < len; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    public void closeSocket() {
        if (cmdSocket == null) {
            return;
        }
        try {
            cmdSocket.close();
        } catch (IOException e) {
        }
    }

    public void writeBytes(byte[] bytes) {
        try {
            // TODO: do we really want to do all of this on each write? Why?
            BufferedOutputStream out = new BufferedOutputStream(
                    cmdSocket.getOutputStream(), Defaults.dataChunkSize);
            out.write(bytes);
            out.flush();
            localDataSocket.reportTraffic(bytes.length);
        } catch (IOException e) {
            Log.i(TAG, "Exception writing socket");
            closeSocket();
            return;
        }
    }

    /**
     * 
     * @param str
     */
    public void writeString(String str) {
        FsService.writeMonitor(false, str);
        byte[] strBytes;
        try {
            strBytes = str.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding: " + encoding);
            strBytes = str.getBytes();
        }
        writeBytes(strBytes);
    }

    protected Socket getSocket() {
        return cmdSocket;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isPasvMode() {
        return pasvMode;
    }

    static public ByteBuffer stringToBB(String s) {
        return ByteBuffer.wrap(s.getBytes());
    }

    public boolean isBinaryMode() {
        return binaryMode;
    }

    public void setBinaryMode(boolean binaryMode) {
        this.binaryMode = binaryMode;
    }

    /**
     * @return true if we should allow FTP opperations
     */
    public boolean isAuthenticated() {
    	Account account = getAccount();
    	if (account != null) {
    		return account.isLoggedIn();
    	} else {
    		return false;
    	}
    }

    /**
     * @return true only when we are anonymously logged in
     */
    public boolean isAnonymouslyLoggedIn() {
    	Account account = getAccount();
    	if (account != null) {
    		return account.isLoggedIn() && account.isAnonymous();
    	} else {
    		return false;
    	}
    }

    /**
     * 通过身份认证并且不是通过匿名登录的
     * @return true if a valid user has logged in
     */
    public boolean isUserLoggedIn() {
    	Account account = getAccount();
    	if (account != null) {
    		return account.isLoggedIn() && !account.isAnonymous();
    	} else {
    		return false;
    	}
    }

    /**
     * 检测当前是否是登录成功了
     * 当每次登录PASS经过调用后就会被调用
     * 检测登录失败的次数，当次数太多的时候，线程退出
     */
    public void authCheck() {
    	Account account = getAccount();
    	
        if (!account.isLoggedIn()) {
            Log.i(TAG, "Authentication complete");
            // TODO 关键是文件的存储和创建不是由SharedLinkSystem来控制，而是跨越来太多的层次
            // LinkSystem是否需要形成一个自己的圈子呢
        } else {
            Log.i(TAG, "Auth failed: " + account.authFails + "/" + MAX_AUTH_FAILS);
            if (account.authFails > MAX_AUTH_FAILS) {
                Log.i(TAG, "Too many auth fails, quitting session");
                quit();
            }
        }
    }

    public Socket getDataSocket() {
        return dataSocket;
    }

    public void setDataSocket(Socket dataSocket) {
        this.dataSocket = dataSocket;
    }

    public SharedLink getRenameFrom() {
        return renameFrom;
    }
    // TODO 需要修改
    public void setRenameFrom(SharedLink renameFrom) {
        this.renameFrom = renameFrom;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
