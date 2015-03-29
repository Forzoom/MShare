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
	
	// 判断服务器是否启动了
	private boolean stopped = false;

	// reference to the video encoder (H263, H264) used over RTP
	// 视频编码器over rtp
	private VideoEncoder encoder;

	// a temporary cache to manage all threads initiated by the RTSP server
	// cache？对于所有由rtsp服务器启动的线程
	private Vector<Thread> serverThreads;
	

	
	public RtspServer(int port, VideoEncoder encoder) throws IOException {		
	
		this.serverThreads = new Vector<Thread>();
		
		this.encoder = encoder;
	    this.serverSocket = new ServerSocket(port);
	
	}

	// 不断地接受连接，并且启动一个ServerThread，将其加入到serverThreads中
	public void run() {
	    
	    /*
	     * 用于接受用户的连接
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

	// 客户线程
	private class ServerThread extends Thread {
		private String TAG = ServerThread.class.getSimpleName();

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
					// TODO 每次都要睡眠是什么意思
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
