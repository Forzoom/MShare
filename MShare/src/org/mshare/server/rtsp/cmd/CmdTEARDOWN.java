package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspParser;
import org.mshare.server.rtsp.RtspThread;

import de.kp.net.rtp.RtpSender;

/**
 * �ͻ��˷���ر�����:
	TEARDOWN rtsp://192.168.20.136:5000/xxx666 RTSP/1.0
	CSeq: 5
	Session: 6310936469860791894
	User-Agent: VLC media player (LIVE555 Streaming Media v2005.11.10)
	
	��������Ӧ:
	RTSP/1.0 200 OK
	Server: UServer 0.9.7_rc1
	Cseq: 5
	Session: 6310936469860791894
	Connection: Close
	���Ϸ������ǽ�����������Ϊ���õ�,��������һЩ��Ҫ�ķ�����get/set_parameter,pause,redirect�ȵ�
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

		// TODO ����RtpSender�е����ݻ���Ҫ�˽�
		// ���Ǻ���û�н�rtpSocket���뵽RtpSender��
//		RtpSender.getInstance().removeReceiver(this.rtpSocket);

		// close the clienr socket for receiving incoming RTSP request
		// ���ǳ��Թر�cmdSocket������Ϊ��Ftp�������Ĵ��ڣ��Ͳ��ܹر�CmdSocket
//		this.clientSocket.close();

		// close the associated RTP socket for sending RTP packets
        rtspThread.getRtpSocket().close();

        rtspThread.getVideoPacketizer().stopStreaming();

		Log.d(TAG, "rtsp TEARDOWN finished");
    }
}
