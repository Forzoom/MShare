package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspParser;

import java.util.Random;

import de.kp.net.rtp.RtpSocket;
import org.mshare.server.rtsp.RtspConstants;

/**
 * TODO 经过移植，需要测试
 */
public class CmdSETUP extends RtspCmd {
	private static final String TAG = CmdSETUP.class.getSimpleName();

	private int clientRtpPort, clientRtcpPort;
		
	private String clientIP = "";
		
	private int[] interleaved;		
	private String transportProtocol = "";
		
	private String sessionType = "";

	private String input;

	public CmdSETUP(SessionThread sessionThread, String input, int cseq) {
		super(sessionThread, cseq);
		this.input = input;
	}

	protected void generateBody() {

		createSessionId();

		body += "Session: " + session_id + CRLF + "Transport: " + transportProtocol + ";" + sessionType + ";";
		if (interleaved==null) {
			body += "source=" + RtspConstants.SERVER_IP + ";" + getPortPart();
		
		} else {
			body += getInterleavedPart();
		}

	}
		
	private String getPortPart(){
		
		String r= "client_port=" + clientRtpPort + "-" + clientRtcpPort + ";" + "server_port=" + RtspConstants.PORTS_RTSP_RTP[0] + "-" + RtspConstants.PORTS_RTSP_RTP[1];
		return r;

	}
		
	private String getInterleavedPart() {
		return "client_ip=" + clientIP + ";interleaved=" + interleaved[0] + "-" + interleaved[1];
	}

	private final void createSessionId() {
 
		Random r = new Random();
		int id = r.nextInt();
		
		if (id < 0) {
			id *= -1;
		}
		
		if (newSessionId) {
			session_id = id;
		}
	
	}

	@Override
	public void run() {
		Log.d(TAG, "rtsp SETUP executing!");
		sessionThread.writeString(toString());

		try {
			int clientPort = RtspParser.getClientPort(input);
			sessionThread.setClientPort(clientPort);
			// 客户端端口
			setClientPort(clientPort);
			// 传输协议
			setTransportProtocol(RtspParser.getTransportProtocol(input));
			// 会话类型
			setSessionType(RtspParser.getSessionType(input));
			// 客户端IP
			setClientIP(sessionThread.getClientAddress().getHostAddress());
			// TODO 设置什么

			// 在原有 的内容中还有setup ＝ true的内容，让RtspServer中的ServerThread跳过第一段循环

			int[] interleaved = RtspParser.getInterleavedSetup(input);
			if(interleaved != null){
				setInterleaved(interleaved);
			}

			// 接下来是一些配置？
			sessionThread.setRtspState(RtspConstants.READY);

			// TODO 准备dataSocket,需要了解是否正确
			sessionThread.setRtpSocket(new RtpSocket(sessionThread.getClientAddress(), clientPort));
			// 准备RtpSender，将RtpSocket注册到其中后，通过RtpSender来发送数据？
		} catch (Exception e) {
			Log.e(TAG, "something wrong happen in SETUP command!");
			e.printStackTrace();
		}


		Log.d(TAG, "rtsp SETUP finished!");
	}

	public void setClientPort(int port) {
		clientRtpPort = port;
		clientRtcpPort = port + 1;
	}

	public String getTransportProtocol() {
		return transportProtocol;
	}

   public void setTransportProtocol(String transportProtocol) {
		this.transportProtocol = transportProtocol;
	}

	public String getSessionType() {
		return sessionType;
	}

	public void setSessionType(String sessionType) {
		this.sessionType = sessionType;
	}

	 public int[] getInterleaved() {
			return interleaved;
	}

	public void setInterleaved(int[] interleaved) {
		this.interleaved = interleaved;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}
		
}
