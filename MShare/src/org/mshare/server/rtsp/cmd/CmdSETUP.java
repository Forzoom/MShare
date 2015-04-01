package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.preference.ServerSettings;
import org.mshare.server.ServerService;
import org.mshare.server.ftp.FtpSessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspParser;
import org.mshare.server.rtsp.RtspThread;

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

	public CmdSETUP(RtspThread rtspThread, String input, int cseq) {
		super(rtspThread, cseq);
		this.input = input;
	}

	protected void generateBody() {

		createSessionId();

		body += "Session: " + session_id + CRLF + "Transport: " + transportProtocol + ";" + sessionType + ";";
		if (interleaved==null) {
			body += "source=" + ServerService.getLocalInetAddress().getHostAddress().toString() + ":" + "8080" + ";" + getPortPart();
		
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
		String errString = new RtspError(rtspThread, input, cseq).toString();

		mainblock : {
			try {
				int clientPort = RtspParser.getClientPort(input);
				rtspThread.setClientPort(clientPort);
				// 客户端端口
				setClientPort(clientPort);
				// 传输协议
				setTransportProtocol(RtspParser.getTransportProtocol(input));
				// 会话类型
				setSessionType(RtspParser.getSessionType(input));
				// 客户端IP
				setClientIP(rtspThread.getSessionThread().getClientAddress().getHostAddress());
				// TODO 设置什么
				// 在原有 的内容中还有setup ＝ true的内容，让RtspServer中的ServerThread跳过第一段循环
				int[] interleaved = RtspParser.getInterleavedSetup(input);
				if (interleaved != null) {
					setInterleaved(interleaved);
				}

				// 接下来是一些配置？
				rtspThread.setRtspState(RtspConstants.READY);

				// TODO 准备dataSocket,需要了解是否正确
				rtspThread.setRtpSocket(new RtpSocket(rtspThread.getSessionThread().getClientAddress(), clientPort));
				// 准备RtpSender，将RtpSocket注册到其中后，通过RtpSender来发送数据？

				// 发送正确的数据
				rtspThread.writeString(toString());
			} catch (Exception e) {
				Log.e(TAG, "something wrong happen in SETUP command!");
				e.printStackTrace();
				rtspThread.writeString(errString);
			}
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
