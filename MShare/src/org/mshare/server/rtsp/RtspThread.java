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
	 * ���ڷ������ݵ�UDP
	 */
	private RtpSocket rtpSocket;
	// �������ӺͿ��Ƶ�TCP,����ͱ���ʼ��
	private final Socket clientSocket;
	// �����������ʼ��ʱ�򱻳�ʼ��
	private VideoEncoder encoder;

	private SessionThread sessionThread;
	
	/* ����rtsp������ */
    // RTSP��Ϣ�����к�
    private int cseq = 0;
    private String contentBase = "";
	// ��Ӧrtsp��������״̬
    private int rtspState;
    // �ͻ��˶˿�
    private int clientPort;

	// �Ӵ洢�϶�ȡ
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
    			
    			// Ӧ���ܹ����ܹر�rtsp������
				String line = RtspParser.readRequest(rtspBr);
				Log.i(TAG, "Received line from client in rtsp mode: " + line);
				// ����rtsp����
				if (RtspCmd.isRtspCmd(line)) {
					RtspCmd.dispatchCmd(this, line);
				} else {
					// ʧ�ܵ�ʱ��ֻ�ܷ���rtsp����,rtsp�Ĵ���Ӧ����η��أ�,��ʱ����������
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

	// ��ʱ����������ʹ��

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
