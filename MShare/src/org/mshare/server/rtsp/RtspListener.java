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
import org.mshare.server.ftp.SessionThread;
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

	// serverSocket
	private ServerSocket serverSocket;
	
	// 判断服务器是否启动了
	private boolean stopped = false;

	// reference to the video encoder (H263, H264) used over RTP
	// 视频编码器over rtp
	private VideoEncoder encoder;

	private SessionController sessionController;
	
	public RtspListener(int port, VideoEncoder encoder, SessionController sessionController) throws IOException {		
	
//		this.serverThreads = new Vector<Thread>();
		this.encoder = encoder;
	    this.serverSocket = new ServerSocket(port);
	    this.sessionController = sessionController;
	}

	// 接受连接，并且启动一个ServerThread，将其加入到serverThreads中
	public void run() {
	    
	    // 当前不停止,接受用户的连接
	    while (this.stopped == false) {

            boolean findTargetSession = false;
            Socket clientSocket = null;
            
			try {
                Log.d(TAG, "receive new client");
				clientSocket = this.serverSocket.accept();

                int sessionCount = sessionController.getCount();
                for (int i = 0; i < sessionCount; i++) {
                	
                	SessionThread sessionThread = sessionController.getSessionThread(i);
                	String currentIp = sessionThread.getClientAddress().toString();
                	String targetIp = clientSocket.getInetAddress().toString();
                	Log.d(TAG, "targetIp : " + targetIp + " currentIp : " + currentIp);
                	if (currentIp.equals(targetIp) && sessionThread.isRtspEnabled()) {
                    	Log.d(TAG, "find target session");
                    	findTargetSession = true;
                    	
                    	RtspThread rtspThread = new RtspThread(clientSocket, this.encoder, sessionThread);
                    	rtspThread.start();
                    }
                }
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (!findTargetSession && clientSocket != null) {
					// 关闭对应的socket
					try {
						clientSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	    }
	}

}
