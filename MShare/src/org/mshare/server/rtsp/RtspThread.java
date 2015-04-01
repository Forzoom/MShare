package org.mshare.server.rtsp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.mshare.server.ftp.FtpSessionThread;
import org.mshare.server.ftp.cmd.CmdCRTP;
import org.mshare.server.rtsp.RtspConstants.VideoEncoder;
import org.mshare.server.rtsp.cmd.RtspError;

import android.util.Log;

import de.kp.net.rtp.RtpSocket;
import de.kp.net.rtp.packetizer.AbstractPacketizer;

public class RtspThread extends Thread {
	private String TAG = RtspThread.class.getSimpleName();

	// 判断当前的服务器是否需要停止
	private boolean stopped = false;
	
	private RtpSocket rtpSocket;
	// 用于连接和控制的TCP,在最开就被初始化
	private final Socket clientSocket;
	// 编码器，在最开始的时候被初始化
	private VideoEncoder encoder;
	// TODO 对应的SessionThread，需要使用更加底层的内容来替代SessionThread的作用
	private FtpSessionThread sessionThread;
    // 消息序号
    private int cseq = 0;
    // rtsp服务器的内容
    private String contentBase = "";
	// 对应rtsp服务器的状态
    private int rtspState;
    // 客户端端口 TODO 需要放置在较为底层的内容中
    private int clientPort;
	// 从存储上读取的inputStream
	private FileInputStream videoInputStream;
	// 对应的打包器
	private AbstractPacketizer videoPacketizer;
	// 对应的数据发送通道
	private BufferedWriter bw;
    
    public RtspThread(Socket socket, VideoEncoder encoder, FtpSessionThread sessionThread) {
    	this.clientSocket = socket;
    	this.encoder = encoder;
    	this.sessionThread = sessionThread;
    }
    
    public void run() {
    	
    	// prepare server response
    	String response = "";
    	
    	try {
    		boolean setup = false;

			BufferedReader rtspBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

    		// this is an endless loop, that is terminated an
    		// with interrupt sent to the respective thread
    		while (true) {

    			// 应该能够接受关闭rtsp的命令
    			Log.d(TAG, "try receive request");
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
    				Log.e(TAG, "stop");
    				break;
    			}
    			
    		}
      
    	} catch(Throwable t) {
    		t.printStackTrace();
    		System.out.println("Caught " + t + " - closing thread");
    	}
    }
    
    // 用于停止RTSP服务器
	public void stopServer() {
		this.stopped = true;
		try {
			this.clientSocket.close();
		
		} catch (IOException e) {	
			// nothing todo
		}
	
	}
    
	// 发送文本数据
    public void writeString(String str) {
    	if (bw == null) {
    		Log.e(TAG, "bw is null");
    		return;
    	}
    	
        try {
        	Log.d(TAG, "write string : " + str);
			bw.write(str);
			bw.flush();
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

	public FtpSessionThread getSessionThread() {
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
