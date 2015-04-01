package org.mshare.server.rtsp;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import android.util.Log;

import de.kp.net.rtp.RtpSocket;
import de.kp.net.rtp.packetizer.AbstractPacketizer;
import de.kp.net.rtp.packetizer.H263Packetizer;
import de.kp.net.rtp.packetizer.H264Packetizer;

import org.mshare.server.ftp.SessionController;
import org.mshare.server.ftp.FtpSessionThread;
import org.mshare.server.rtsp.RtspConstants.VideoEncoder;

import de.kp.rtspcamera.MediaConstants;

/**
 * This class describes a RTSP streaming
 * server for Android platforms. RTSP is
 * used to control video streaming from
 * a remote user agent.
 * 
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */

public class RtspListener extends Thread {
    private static final String TAG = RtspListener.class.getSimpleName();

	// listenSocket
	private ServerSocket listenSocket;
	
	// �жϷ������Ƿ�������
	private boolean stopped = false;

	// reference to the video encoder (H263, H264) used over RTP
	// ��Ƶ������over rtp
	private VideoEncoder encoder;

	private SessionController sessionController;
	
	public RtspListener(int port, VideoEncoder encoder, SessionController sessionController) throws IOException {		

		this.encoder = encoder;
	    this.listenSocket = new ServerSocket(port);
	    this.sessionController = sessionController;
	}

	// �������ӣ���������һ��ServerThread��������뵽serverThreads��
	public void run() {
	    
	    // ��ǰ��ֹͣ,�����û�������
	    while (this.stopped == false) {

            boolean findTargetSession = false;
            Socket clientSocket = null;
            
			try {
                Log.d(TAG, "receive new client");
				clientSocket = this.listenSocket.accept();

				// ��ǰ��Session����
                int sessionCount = sessionController.getCount();
                for (int i = 0; i < sessionCount; i++) {
                	
                	FtpSessionThread sessionThread = sessionController.getSessionThread(i);
                	// client��Ӧ��IP��ַ
                	String clientIp = sessionThread.getClientAddress().toString();
                	// ����Ҫ��IP��ַ
                	String targetIp = clientSocket.getInetAddress().toString();
                	Log.d(TAG, "targetIp : " + targetIp + " clientIp : " + clientIp);
                	// �ҵ���Ҫ��Ӧ��FtpSession
                	if (clientIp.equals(targetIp) && sessionThread.isRtspEnabled()) {
                    	Log.d(TAG, "find target session�� join the rtsp thread into it!");
                    	findTargetSession = true;
                    	
                    	RtspThread rtspThread = new RtspThread(clientSocket, this.encoder, sessionThread);
                    	rtspThread.start();
                    }
                }
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (!findTargetSession && clientSocket != null) {
					// �رն�Ӧ��socket
					try {
						clientSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	    }
	}

	// �����˳�RtspListener
	public void quit() {
		Log.d(TAG, "try quit RtspListener");
		try {
            listenSocket.close();
            Log.v(TAG, "quit RtspListener succeed!");
        } catch (Exception e) {
            Log.d(TAG, "Exception closing RtspListener listenSocket");
        }
	}
	
}
