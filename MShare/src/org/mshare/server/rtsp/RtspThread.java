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

	// �жϵ�ǰ�ķ������Ƿ���Ҫֹͣ
	private boolean stopped = false;
	
	private RtpSocket rtpSocket;
	// �������ӺͿ��Ƶ�TCP,����ͱ���ʼ��
	private final Socket clientSocket;
	// �����������ʼ��ʱ�򱻳�ʼ��
	private VideoEncoder encoder;
	// TODO ��Ӧ��SessionThread����Ҫʹ�ø��ӵײ�����������SessionThread������
	private FtpSessionThread sessionThread;
    // ��Ϣ���
    private int cseq = 0;
    // rtsp������������
    private String contentBase = "";
	// ��Ӧrtsp��������״̬
    private int rtspState;
    // �ͻ��˶˿� TODO ��Ҫ�����ڽ�Ϊ�ײ��������
    private int clientPort;
	// �Ӵ洢�϶�ȡ��inputStream
	private FileInputStream videoInputStream;
	// ��Ӧ�Ĵ����
	private AbstractPacketizer videoPacketizer;
	// ��Ӧ�����ݷ���ͨ��
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

    			// Ӧ���ܹ����ܹر�rtsp������
    			Log.d(TAG, "try receive request");
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
    				Log.e(TAG, "stop");
    				break;
    			}
    			
    		}
      
    	} catch(Throwable t) {
    		t.printStackTrace();
    		System.out.println("Caught " + t + " - closing thread");
    	}
    }
    
    // ����ֹͣRTSP������
	public void stopServer() {
		this.stopped = true;
		try {
			this.clientSocket.close();
		
		} catch (IOException e) {	
			// nothing todo
		}
	
	}
    
	// �����ı�����
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