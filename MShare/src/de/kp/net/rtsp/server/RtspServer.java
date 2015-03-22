package de.kp.net.rtsp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import android.os.Environment;
import android.util.Log;

import de.kp.net.rtp.RtpSender;
import de.kp.net.rtp.RtpSocket;
import de.kp.net.rtp.packetizer.AbstractPacketizer;
import de.kp.net.rtp.packetizer.H263Packetizer;
import de.kp.net.rtp.packetizer.H264Packetizer;
import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.RtspConstants.VideoEncoder;
import de.kp.net.rtsp.server.response.Parser;
import de.kp.net.rtsp.server.response.RtspDescribeResponse;
import de.kp.net.rtsp.server.response.RtspError;
import de.kp.net.rtsp.server.response.RtspOptionsResponse;
import de.kp.net.rtsp.server.response.RtspPauseResponse;
import de.kp.net.rtsp.server.response.RtspPlayResponse;
import de.kp.net.rtsp.server.response.RtspResponse;
import de.kp.net.rtsp.server.response.RtspResponseTeardown;
import de.kp.net.rtsp.server.response.RtspSetupResponse;
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

	// serverSocket
	private ServerSocket serverSocket;
	
	// 判断服务器是否启动了
	private boolean stopped = false;

	// 判断服务器所有的线程是否都关闭了
	private boolean terminated = false;
	
	// reference to the video encoder (H263, H264) used over RTP
	// 视频编码器over rtp
	private VideoEncoder encoder;

	// a temporary cache to manage all threads initiated by the RTSP server
	// cache？对于所有由rtsp服务器启动的线程
	private Vector<Thread> serverThreads;
	
	AbstractPacketizer videoPacketizer;
	
	public RtspServer(int port, VideoEncoder encoder) throws IOException {		
	
		this.serverThreads = new Vector<Thread>();
		
		this.encoder = encoder;
	    this.serverSocket = new ServerSocket(port);	  
	
	}

	// 不断地接受连接，并且启动一个ServerThread，将其加入到serverThreads中
	public void run() {
	    
	    /*
	     * In order to communicate with different clients,
	     * we construct a thread for each client that is
	     * connected.
	     */
	    while (this.stopped == false) {
	    	
			try {
				Socket  clientSocket = this.serverSocket.accept();
		    	serverThreads.add(new ServerThread(clientSocket, this.encoder));

			} catch (IOException e) {
				e.printStackTrace();
			}
	    
	    }
		
	}

	// 判断当前服务器所有线程是否都结束了
	public boolean isTerminated() {
		return this.terminated;
	}
	
	/**
	 * 停止RTSP服务器
	 */
	public void stop() {
		
		this.stopped = true;
		terminate();

		try {
			this.serverSocket.close();
		} catch (IOException e) {	
			// nothing todo
		}
	}
	
	/**
	 * 停止所有的线程
	 */
	private void terminate() {
		
		for (Thread serverThread:serverThreads) {
			if (serverThread.isAlive()) serverThread.interrupt();
		}
		
		this.terminated = true;
		
	}
	
	// 客户线程
	private class ServerThread extends Thread {
		private String TAG = ServerThread.class.getSimpleName();

		// 用于回复RTSP客户端的内容，类似于HttpResponse?
		private RtspResponse rtspResponse;
		
		private String contentBase = "";
		
		/*
		 * input and output stream buffer for TCP connection; 
		 * UDP response are sent through DatagramSocket
		 */
		private BufferedReader rtspBufferedReader;
		private BufferedWriter rtspBufferedWriter;

		private int rtspState;
		
		// Sequence number of RTSP messages within the session
		// RTSP消息的序列号
		private int cseq = 0;	
		// 客户端端口
		private int clientPort;
		
		// remote (client) address
		// 远程客户端IP地址
		private InetAddress clientAddress;
		
		/*
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
	    	
	    	// register IP address of requesting client
	    	this.clientAddress = this.clientSocket.getInetAddress();
	    	
	    	// 启动线程
	    	start();
	    
	    }
	    
	    public void run() {
	    	
	    	// prepare server response
	    	String response = "";
	    	
	    	try {

	    		// Set input and output stream filters
	    		// 创建输入和输出流
	    		rtspBufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()) );
	    		rtspBufferedWriter = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()) );
	    		// 判断是否已经启动
	    		boolean setup = false;
	    		
	    		while (setup == false) {
	    			
	    			// determine request type and also provide
	    			// server response
	    			int requestType = getRequestType();

	    			// send response
	    			response = rtspResponse.toString();

	    			rtspBufferedWriter.write(response);
		    		rtspBufferedWriter.flush();

	    			if (requestType == RtspConstants.SETUP) {
	    			    
	    				setup = true;

	    			    // update RTSP state
	    			    rtspState = RtspConstants.READY;
	    				
	    				// in case of a setup request, we create a new RtpSocket 
	    				// instance used to send RtpPacket
	    				this.rtpSocket = new RtpSocket(this.clientAddress, this.clientPort);
	    				
	    				// this RTP socket is registered as RTP receiver to also
	    				// receive the streaming video of this device
	    				// 所有的都被保存在其中
	    				RtpSender.getInstance().addReceiver(this.rtpSocket);

	    			}
	    			
	    		}

	    		// this is an endless loop, that is terminated an
	    		// with interrupt sent to the respective thread
	    		// setup已经为true
	    		// 发送数据
	    		while (true) {

	    			// pares incoming request to decide how to proceed
	    			// 这个应该是PLAY命令？
	    			int requestType = getRequestType();

	    			// send response
	    			// toString 被复写
	    			response = rtspResponse.toString();
	    			
	    			// 
	    			rtspBufferedWriter.write(response);
		    		rtspBufferedWriter.flush();
	    			
		    		FileInputStream fis = null;
		    		
		    		if ((requestType == RtspConstants.DESCRIBE)) {
		    			Log.i(TAG, "request : DESCRIBE");
		    			
		    			// 当前文件名已经准备好了
		    			String fileName = ((RtspDescribeResponse)rtspResponse).getFileName();
		    			Log.d(TAG, "try to get the file : " + fileName);
		    			
		    			String root = Environment.getExternalStorageDirectory().getAbsolutePath();
		    			String filePath = root + File.separator + fileName;
		    			
		    			fis = new FileInputStream(filePath);
		    			// 
		    		} else if ((requestType == RtspConstants.PLAY) && (rtspState == RtspConstants.READY)) {	    				
		    			Log.i(TAG, "request: PLAY");

	    				// make sure that the respective client socket is 
	    				// ready to send RTP packets
	    				this.rtpSocket.suspend(false);
	    				
	    				this.rtspState = RtspConstants.PLAYING;
	    				
	    				if (MediaConstants.H264_CODEC == true) {
	    					videoPacketizer = new H264Packetizer(fis);
	    				} else {
	    					videoPacketizer = new H263Packetizer(fis);
	    				}
	    				videoPacketizer.startStreaming();
	    				
	    			} else if ((requestType == RtspConstants.PAUSE) && (rtspState == RtspConstants.PLAYING)) {
		    			Log.i(TAG, "request: PAUSE");
	    				
	    				// suspend RTP socket from sending video packets
	    				this.rtpSocket.suspend(true);
	    				
	    			} else if (requestType == RtspConstants.TEARDOWN) {
		    			Log.i(TAG, "request: TEARDOWN");

	    				// this RTP socket is removed from the RTP Sender
	    				RtpSender.getInstance().removeReceiver(this.rtpSocket);
	    				
	    				// close the clienr socket for receiving incoming RTSP request
	    				this.clientSocket.close();
	    				
	    				// close the associated RTP socket for sending RTP packets
	    				this.rtpSocket.close();
	    				
	    				videoPacketizer.stopStreaming();
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
	    
	    private int getRequestType() throws Exception {
	  	
	    	int requestType = -1;

	    	// retrieve the request in a string representation 
	    	// for later evaluation
	    	String requestLine = "";
            try {
            	// TODO 不知道干什么，请求的数据
            	requestLine = Parser.readRequest(rtspBufferedReader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // TODO 查看requestLine究竟是什么
            Log.i(TAG, "requestLine: " + requestLine);
            
            // determine request type from incoming RTSP request
            requestType = Parser.getRequestType(requestLine);

            if (contentBase.isEmpty()) {
                contentBase = Parser.getContentBase(requestLine);
            }

            // TODO 不知道
            // 获得消息序号
            if (!requestLine.isEmpty()) {
                cseq = Parser.getCseq(requestLine);
            }

            // 获得rtspResponse
            if (requestType == RtspConstants.OPTIONS) {
        		rtspResponse = new RtspOptionsResponse(cseq);

            } else if (requestType == RtspConstants.DESCRIBE) {
                buildDescribeResponse(requestLine);

            } else if (requestType == RtspConstants.SETUP) {
                buildSetupResponse(requestLine);
		                
            } else if (requestType == RtspConstants.PAUSE) {
                rtspResponse = new RtspPauseResponse(cseq);
		
            } else if (requestType == RtspConstants.TEARDOWN) {
                rtspResponse = new RtspResponseTeardown(cseq);
 
            } else if (requestType == RtspConstants.PLAY) {
                rtspResponse = new RtspPlayResponse(cseq);	       
                
                String range = Parser.getRangePlay(requestLine);
                if (range != null) ((RtspPlayResponse) rtspResponse).setRange(range);

            } else {
	        	if( requestLine.isEmpty()){
	        		rtspResponse = new RtspError(cseq);
            
	        	} else {
                    rtspResponse = new RtspError(cseq);
	        	}
         
            }

            return requestType;
	    }

	    /**
	     * Create an RTSP response for an incoming SETUP request.
	     * 
	     * @param requestLine
	     * @throws Exception
	     */
	    private void buildSetupResponse(String requestLine) throws Exception {
	        
	    	rtspResponse = new RtspSetupResponse(cseq);
	        
	    	// client port
	    	clientPort = Parser.getClientPort(requestLine);	            
	    	((RtspSetupResponse) rtspResponse).setClientPort(clientPort);
	    	
	    	// transport protocol
            ((RtspSetupResponse) rtspResponse).setTransportProtocol(Parser.getTransportProtocol(requestLine));
            
            // session type
            ((RtspSetupResponse) rtspResponse).setSessionType(Parser.getSessionType(requestLine));

            ((RtspSetupResponse) rtspResponse).setClientIP(this.clientAddress.getHostAddress());
	            
            int[] interleaved = Parser.getInterleavedSetup(requestLine);
            if(interleaved != null){
                ((RtspSetupResponse) rtspResponse).setInterleaved(interleaved);
            }

	    }

	    /**
	     * Create an RTSP response for an incoming DESCRIBE request.
	     * 创建DESCRIBE的回复内容
	     * @param requestLine
	     * @throws Exception
	     */
	    private void buildDescribeResponse(String requestLine) throws Exception{
                
    	   rtspResponse = new RtspDescribeResponse(cseq);
           
    	   // set file name
    	   // 通过对于uri进行解析
    	   String fileName = Parser.getFileName(requestLine);
    	   Log.d(TAG, "request file name : " + fileName);
    	   ((RtspDescribeResponse) rtspResponse).setFileName(fileName);
        	   
    	   // set video encoding
    	   ((RtspDescribeResponse) rtspResponse).setVideoEncoder(encoder);

    	   // finally set content base
    	   ((RtspDescribeResponse)rtspResponse).setContentBase(contentBase);
        
       }

	}

}
