package org.mshare.server.rtsp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.mshare.server.ftp.Defaults;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.ServerService;
import org.mshare.server.ftp.SessionThread;
import org.mshare.server.ftp.cmd.CmdCRTP;
import org.mshare.server.rtsp.RtspConstants.VideoEncoder;
import org.mshare.server.rtsp.cmd.RtspError;

import android.util.Log;

import de.kp.net.rtp.RtpSocket;
import de.kp.net.rtp.packetizer.AbstractPacketizer;

public class RtspThread extends Thread {
	private String TAG = RtspThread.class.getSimpleName();

	/*
	 * input and output stream buffer for TCP connection;
	 * UDP response are sent through DatagramSocket

	 * This datagram socket is used to send UDP
	 * packets to the clientIPAddress
	 * 用于发送数据的UDP
	 */
	private RtpSocket rtpSocket;
	// 用于连接和控制的TCP,在最开就被初始化
	private final Socket clientSocket;
	// 编码器，在最开始的时候被初始化
	private VideoEncoder encoder;

	private SessionThread sessionThread;
	
	/* 关于rtsp的内容 */
    // RTSP消息的序列号
    private int cseq = 0;
    private String contentBase = "";
	// 对应rtsp服务器的状态
    private int rtspState;
    // 客户端端口
    private int clientPort;

	// 从存储上读取
	private FileInputStream videoInputStream;

	private AbstractPacketizer videoPacketizer;

    
    public RtspThread(Socket socket, VideoEncoder encoder, SessionThread sessionThread) {
    	
    	this.clientSocket = socket;
    	this.encoder = encoder;
    	this.sessionThread = sessionThread;
    }
    
    public void run() {
    	
    	// prepare server response
    	String response = "";
    	
    	try {
    		boolean setup = false;

			// this RTP socket is registered as RTP receiver to also
			// receive the streaming video of this device
    		

    		// this is an endless loop, that is terminated an
    		// with interrupt sent to the respective thread
    		while (true) {

    			BufferedReader rtspBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    			
    			// 应该能够接受关闭rtsp的命令
				String line = RtspParser.readRequest(rtspBr);
				Log.i(TAG, "Received line from client in rtsp mode: " + line);
				// 接受rtsp命令
				if (RtspCmd.isRtspCmd(line)) {
					RtspCmd.dispatchCmd(this, line);
				} else {
					// 失败的时候，只能返回rtsp错误,rtsp的错误应该如何返回？,暂时先这样返回
					writeString(new RtspError(this, line, getCseq()).toString());
				}
    			
    			// the pattern below enables an interrupt
    			// which allows to close this thread
    			try {
    				sleep(20);
    			} catch (InterruptedException e) {
    				break;
    			}
    			
    		}
      
    	} catch(Throwable t) {
    		t.printStackTrace();
    		System.out.println("Caught " + t + " - closing thread");
    	}
    }
    
    public void writeString(String str) {
        try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			bw.write(str);
		} catch (IOException e) {	
			e.printStackTrace();
		}
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

	public int getRtspState() {
		return rtspState;
	}

	public void setRtspState(int rtspState) {
		this.rtspState = rtspState;
	}

	public SessionThread getSessionThread() {
		return this.sessionThread;
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

}
