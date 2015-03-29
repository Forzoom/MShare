package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspParser;
import org.mshare.server.rtsp.RtspThread;

import de.kp.net.rtp.RtpSender;

/**
 * 客户端发起关闭请求:
	TEARDOWN rtsp://192.168.20.136:5000/xxx666 RTSP/1.0
	CSeq: 5
	Session: 6310936469860791894
	User-Agent: VLC media player (LIVE555 Streaming Media v2005.11.10)
	
	服务器回应:
	RTSP/1.0 200 OK
	Server: UServer 0.9.7_rc1
	Cseq: 5
	Session: 6310936469860791894
	Connection: Close
	以上方法都是交互过程中最为常用的,其它还有一些重要的方法如get/set_parameter,pause,redirect等等
 * @author HM
 *
 */
public class CmdTEARDOWN extends RtspCmd {
    private static final String TAG = CmdTEARDOWN.class.getSimpleName();

	private String input;

    public CmdTEARDOWN(RtspThread rtspThread, String input, int cseq) {
        super(rtspThread, cseq);
		this.input = input;
    }

    protected void generar(){
        response += getHeader();
        response += getBody() + CRLF;
    }
    
    protected void generateBody() {
        body += "";
    }

    @Override
    public void run() {
        Log.d(TAG, "rtsp TEARDOWN executing!");
        rtspThread.writeString(toString());

		// TODO 关于RtpSender中的内容还需要了解
		// 我们好像并没有将rtpSocket将入到RtpSender中
//		RtpSender.getInstance().removeReceiver(this.rtpSocket);

		// close the clienr socket for receiving incoming RTSP request
		// 就是尝试关闭cmdSocket，但是为了Ftp服务器的存在，就不能关闭CmdSocket
//		this.clientSocket.close();

		// close the associated RTP socket for sending RTP packets
        rtspThread.getRtpSocket().close();

        rtspThread.getVideoPacketizer().stopStreaming();

		Log.d(TAG, "rtsp TEARDOWN finished");
    }
}
