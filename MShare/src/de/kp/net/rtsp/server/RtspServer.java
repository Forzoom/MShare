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
	
	// �жϷ������Ƿ�������
	private boolean stopped = false;

	// �жϷ��������е��߳��Ƿ񶼹ر���
	private boolean terminated = false;
	
	// reference to the video encoder (H263, H264) used over RTP
	// ��Ƶ������over rtp
	private VideoEncoder encoder;

	// a temporary cache to manage all threads initiated by the RTSP server
	// cache������������rtsp�������������߳�
	private Vector<Thread> serverThreads;
	
	AbstractPacketizer videoPacketizer;
	
	public RtspServer(int port, VideoEncoder encoder) throws IOException {		
	
		this.serverThreads = new Vector<Thread>();
		
		this.encoder = encoder;
	    this.serverSocket = new ServerSocket(port);	  
	
	}

	// ���ϵؽ������ӣ���������һ��ServerThread��������뵽serverThreads��
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

	// �жϵ�ǰ�����������߳��Ƿ񶼽�����
	public boolean isTerminated() {
		return this.terminated;
	}
	
	/**
	 * ֹͣRTSP������
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
	 * ֹͣ���е��߳�
	 */
	private void terminate() {
		
		for (Thread serverThread:serverThreads) {
			if (serverThread.isAlive()) serverThread.interrupt();
		}
		
		this.terminated = true;
		
	}
	
	// �ͻ��߳�
	private class ServerThread extends Thread {
		private String TAG = ServerThread.class.getSimpleName();

		// ���ڻظ�RTSP�ͻ��˵����ݣ�������HttpResponse?
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
		// RTSP��Ϣ�����к�
		private int cseq = 0;	
		// �ͻ��˶˿�
		private int clientPort;
		
		// remote (client) address
		// Զ�̿ͻ���IP��ַ
		private InetAddress clientAddress;
		
		/*
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
	    	
	    	// register IP address of requesting client
	    	this.clientAddress = this.clientSocket.getInetAddress();
	    	
	    	// �����߳�
	    	start();
	    
	    }
	    
	    public void run() {
	    	
	    	// prepare server response
	    	String response = "";
	    	
	    	try {

	    		// Set input and output stream filters
	    		// ��������������
	    		rtspBufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()) );
	    		rtspBufferedWriter = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()) );
	    		// �ж��Ƿ��Ѿ�����
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
	    				// ���еĶ�������������
	    				RtpSender.getInstance().addReceiver(this.rtpSocket);

	    			}
	    			
	    		}

	    		// this is an endless loop, that is terminated an
	    		// with interrupt sent to the respective thread
	    		// setup�Ѿ�Ϊtrue
	    		// ��������
	    		while (true) {

	    			// pares incoming request to decide how to proceed
	    			// ���Ӧ����PLAY���
	    			int requestType = getRequestType();

	    			// send response
	    			// toString ����д
	    			response = rtspResponse.toString();
	    			
	    			// 
	    			rtspBufferedWriter.write(response);
		    		rtspBufferedWriter.flush();
	    			
		    		FileInputStream fis = null;
		    		
		    		if ((requestType == RtspConstants.DESCRIBE)) {
		    			Log.i(TAG, "request : DESCRIBE");
		    			
		    			// ��ǰ�ļ����Ѿ�׼������
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
            	// TODO ��֪����ʲô�����������
            	requestLine = Parser.readRequest(rtspBufferedReader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // TODO �鿴requestLine������ʲô
            Log.i(TAG, "requestLine: " + requestLine);
            
            // determine request type from incoming RTSP request
            requestType = Parser.getRequestType(requestLine);

            if (contentBase.isEmpty()) {
                contentBase = Parser.getContentBase(requestLine);
            }

            // TODO ��֪��
            // �����Ϣ���
            if (!requestLine.isEmpty()) {
                cseq = Parser.getCseq(requestLine);
            }

            // ���rtspResponse
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
	     * ����DESCRIBE�Ļظ�����
	     * @param requestLine
	     * @throws Exception
	     */
	    private void buildDescribeResponse(String requestLine) throws Exception{
                
    	   rtspResponse = new RtspDescribeResponse(cseq);
           
    	   // set file name
    	   // ͨ������uri���н���
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
