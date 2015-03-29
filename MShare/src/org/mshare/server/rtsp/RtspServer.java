package org.mshare.server.rtsp;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import android.util.Log;

import de.kp.net.rtp.RtpSender;
import de.kp.net.rtp.RtpSocket;
import de.kp.net.rtp.packetizer.AbstractPacketizer;
import de.kp.net.rtp.packetizer.H263Packetizer;
import de.kp.net.rtp.packetizer.H264Packetizer;

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

public class RtspServer implements Runnable {
    private static final String TAG = RtspServer.class.getSimpleName();

	// serverSocket
	private ServerSocket serverSocket;
	
	// �жϷ������Ƿ�������
	private boolean stopped = false;

	// reference to the video encoder (H263, H264) used over RTP
	// ��Ƶ������over rtp
	private VideoEncoder encoder;

	// a temporary cache to manage all threads initiated by the RTSP server
	// cache������������rtsp�������������߳�
	private Vector<Thread> serverThreads;
	

	
	public RtspServer(int port, VideoEncoder encoder) throws IOException {		
	
		this.serverThreads = new Vector<Thread>();
		
		this.encoder = encoder;
	    this.serverSocket = new ServerSocket(port);
	
	}

	// ���ϵؽ������ӣ���������һ��ServerThread��������뵽serverThreads��
	public void run() {
	    
	    /*
	     * ���ڽ����û�������
	     */
	    while (this.stopped == false) {
	    	
			try {
                Log.d(TAG, "");
				Socket clientSocket = this.serverSocket.accept();
		    	serverThreads.add(new ServerThread(clientSocket, this.encoder));

			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}

	// �ͻ��߳�
	private class ServerThread extends Thread {
		private String TAG = ServerThread.class.getSimpleName();

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

	    public ServerThread(Socket socket, VideoEncoder encoder) {
	    	
	    	this.clientSocket = socket;
	    	this.encoder = encoder;
	    }
	    
	    public void run() {
	    	
	    	// prepare server response
	    	String response = "";
	    	
	    	try {
	    		boolean setup = false;

				// this RTP socket is registered as RTP receiver to also
				// receive the streaming video of this device
//				RtpSender.getInstance().addReceiver(this.rtpSocket);

	    		// this is an endless loop, that is terminated an
	    		// with interrupt sent to the respective thread
	    		while (true) {

	    			// the pattern below enables an interrupt
	    			// which allows to close this thread
					// TODO ÿ�ζ�Ҫ˯����ʲô��˼
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

	}

}
