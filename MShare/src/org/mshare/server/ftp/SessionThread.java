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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.mshare.file.share.SharedLink;
import org.mshare.account.AccountFactory.Token;
import org.mshare.account.AccountFactory.Verifier;

import android.util.Log;

import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.cmd.RtspError;

import de.kp.net.rtp.RtpSocket;
import de.kp.net.rtp.packetizer.AbstractPacketizer;

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
    private Token token;
    // 保存SessionInfo,暂时设置为public
    public SessionInfo sessionInfo;
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

	private boolean isRtspEnabled = false;

	// rtsp传输数据所使用的
	private RtpSocket rtpSocket;

	// 从存储上读取
	private FileInputStream videoInputStream;

	private AbstractPacketizer videoPacketizer;

    /* 关于rtsp的内容 */
    // RTSP消息的序列号
    private int cseq = 0;
    private String contentBase = "";
	// 对应rtsp服务器的状态
    private int rtspState;
    // 客户端端口
    private int clientPort;
    // 远程客户端IP地址 TODO 可以将其纪录下来
    private InetAddress clientAddress;

    protected String encoding = Defaults.SESSION_ENCODING;
    protected long offset = -1; // where to start append when using REST

    // 当每次调用USER的时候重置
    public int authFails = 0;
    public static int MAX_AUTH_FAILS = 3;

    // 需要保证verifier能够被正常的初始化?
    public Verifier verifier;

	// ftp命令无法识别时的返回
	public static String unrecognizedCmdMsg = "502 Command not recognized\r\n";

	public SessionThread(Socket socket, LocalDataSocket dataSocket) {
        this.cmdSocket = socket;
        this.localDataSocket = dataSocket;
        this.sendWelcomeBanner = true;
        this.sessionInfo = new SessionInfo();

		// 纪录客户端的IP，所使用的command的IP
		this.clientAddress = this.cmdSocket.getInetAddress();
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
     * @param buf Where to place the input bytes
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
            writeString("220 FTP Server ready\r\n");
        }
        // Main loop: read an incoming line and process it
        try {
            // use 8k buffer
            BufferedReader in = new BufferedReader(new InputStreamReader(cmdSocket.getInputStream()), 8192);

            while (true) {
                String line;
                line = in.readLine(); // will accept \r\n or \n for terminator
                if (line != null) {
                    ServerService.writeMonitor(true, line);
                    Log.i(TAG, "Received line from client: " + line);
                    // 直接在这里调用就好了
					// 在这里需要判断当前rtsp是否开启了
					if (isRtspEnabled()) {
						// 应该能够接受关闭rtsp的命令


						// 接受rtsp命令
						if (RtspCmd.isRtspCmd(line)) {
							RtspCmd.dispatchCmd(this, line);
						}

						// 失败的时候，只能返回rtsp错误,rtsp的错误应该如何返回？,暂时先这样返回
						writeString(new RtspError(this, line, getCseq()).toString());
					} else {
						// 只能接受ftp命令
						if (FtpCmd.isFtpCmd(line)) {
							FtpCmd.dispatchCommand(this, line);
						} else {
							// 失败的时候，返回ftp错误
							writeString(unrecognizedCmdMsg);
						}
					}
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
        ServerService.writeMonitor(false, str);
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
     * 检测当前是否登录成功了
     * 当每次调用PASS命令的时候，都会调用该方法，调用verifier进行验证并获得Token
     * 同时记录失败的次数，当次数超过限制时，会话退出
     * @return 失败时返回null,成功时，和{@link #getToken}返回相同的{@link Token}
     */
    public Token authAttempt(String username, String password) {
    	// 释放之前的Token
    	Token currentToken = getToken();
    	if (currentToken != null) {
    		currentToken.release();
    	}
    	// 获得新的Token
    	Token newToken = null;
    	
    	if (verifier == null) {
    		Log.e(TAG, "verifier is null");
			return null;
    	}
        if ((newToken = verifier.auth(username, password, this)) != null) {
        	setToken(newToken);
            Log.i(TAG, "Authentication complete");
        } else {
        	authFails++;
            Log.i(TAG, "Auth failed: " + authFails + "/" + MAX_AUTH_FAILS);
            if (authFails > MAX_AUTH_FAILS) {
                Log.i(TAG, "Too many auth fails, quitting session");
                quit();
            }
        }
        return newToken;
    }

    // 在这里使用这个是不是不好
    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
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

    public Token getToken() {
    	return token;
    }
    
    public void setToken(Token token) {
    	this.token = token;
    }

    public int getCseq() {
        return cseq;
    }

    public void setCseq(int cseq) {
        this.cseq = cseq;
    }

    public String getContentBase() {
        return contentBase;
    }

    public void setContentBase(String contentBase) {
        this.contentBase = contentBase;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

	public int getRtspState() {
		return rtspState;
	}

	public void setRtspState(int rtspState) {
		this.rtspState = rtspState;
	}

	// 暂时放在这里来使用

	public RtpSocket getRtpSocket() {
		return rtpSocket;
	}

	public void setRtpSocket(RtpSocket rtpSocket) {
		this.rtpSocket = rtpSocket;
	}

	public FileInputStream getVideoInputStream() {
		return videoInputStream;
	}

	public void setVideoInputStream(FileInputStream videoInputStream) {
		this.videoInputStream = videoInputStream;
	}

	public AbstractPacketizer getVideoPacketizer() {
		return videoPacketizer;
	}

	public void setVideoPacketizer(AbstractPacketizer videoPacketizer) {
		this.videoPacketizer = videoPacketizer;
	}

	public void startUsingRtsp() {
		Log.d(TAG, "try start using Rtsp");
		isRtspEnabled = true;
	}

	public void stopRtsp() {
		Log.d(TAG, "try stop Rtsp");
		isRtspEnabled = false;
	}

	public boolean isRtspEnabled() {
		return isRtspEnabled;
	}
}
