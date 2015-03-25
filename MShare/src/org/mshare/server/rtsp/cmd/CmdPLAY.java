package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspConstants;
import org.mshare.server.rtsp.RtspParser;

import java.net.SocketException;

import de.kp.net.rtp.packetizer.H263Packetizer;
import de.kp.net.rtp.packetizer.H264Packetizer;
import de.kp.rtspcamera.MediaConstants;

/**
 * �ͻ��˷��Ͳ�������:
	PLAY rtsp://192.168.20.136:5000/xxx666 RTSP/1.0
	CSeq: 4
	Session: 6310936469860791894
	Range: npt=0.000-      //���ò���ʱ��ķ�Χ
	User-Agent: VLC media player (LIVE555 Streaming Media v2005.11.10)
	
	��������Ӧ��Ϣ:
	RTSP/1.0 200 OK
	Server: UServer 0.9.7_rc1
	Cseq: 4
	Session: 6310936469860791894
	Range: npt=0.000000-
	RTP-Info: url=trackID=0;seq=17040;rtptime=1467265309     
	//seq��rtptime����rtp���е���Ϣ
 * @author HM
 *
 */
public class CmdPLAY extends RtspCmd {
    private static final String TAG = CmdPLAY.class.getSimpleName();

    protected String range = "";

	private String input;

    public CmdPLAY(SessionThread sessionThread, String input, int cseq) {
        super(sessionThread, cseq);
		this.input = input;
    }

    protected void generateBody() {	
    	this.body += "Session: " + session_id + CRLF + "Range: npt=" + range;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    @Override
    public void run() {
        Log.d(TAG, "rtsp PLAY executing ");
        sessionThread.writeString(toString());

		try {
			String range = RtspParser.getRangePlay(input);
			if (range != null) {
				setRange(range);
			}

		} catch (Exception e) {
			Log.e(TAG, "something wrong happen in PLAY!");
			e.printStackTrace();
		}

		if (sessionThread.getRtspState() == RtspConstants.READY) {

			// make sure that the respective client socket is ready to send RTP packets
			sessionThread.getRtpSocket().suspend(false);

			sessionThread.setRtspState(RtspConstants.PLAYING);

			try {

				// ������������
				if (MediaConstants.H264_CODEC == true) {
					sessionThread.setVideoPacketizer(new H264Packetizer(sessionThread.getVideoInputStream()));
				} else {
					sessionThread.setVideoPacketizer(new H263Packetizer(sessionThread.getVideoInputStream()));
				}

			} catch (SocketException e) {
				Log.e(TAG, "the socket exception? so the play is stop!");
				e.printStackTrace();
			}
			sessionThread.getVideoPacketizer().startStreaming();
		}

		Log.d(TAG, "rtsp PLAY finished");
    }
}
