package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.FtpSessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspConstants;
import org.mshare.server.rtsp.RtspParser;
import org.mshare.server.rtsp.RtspThread;

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

    public CmdPLAY(RtspThread rtspThread, String input, int cseq) {
        super(rtspThread, cseq);
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
		String errString = new RtspError(rtspThread, input, cseq).toString();

		mainblock : {

			String range = RtspParser.getRangePlay(input);
			if (range != null) {
				setRange(range);
			}

			if (rtspThread.getRtspState() == RtspConstants.READY) {

				// make sure that the respective client socket is ready to send RTP packets
				rtspThread.getRtpSocket().suspend(false);

				rtspThread.setRtspState(RtspConstants.PLAYING);

				try {

					// ������������
					if (MediaConstants.H264_CODEC == true) {
						rtspThread.setVideoPacketizer(new H264Packetizer(rtspThread.getVideoInputStream(), rtspThread.getRtpSocket()));
					} else {
						rtspThread.setVideoPacketizer(new H263Packetizer(rtspThread.getVideoInputStream(), rtspThread.getRtpSocket()));
					}

				} catch (SocketException e) {
					Log.e(TAG, "the socket exception? so the play is stop!");
					e.printStackTrace();
					rtspThread.writeString(errString);
					break mainblock;
				}

				// TODO ��ʱ��Ҫ���͵�cmdд������
				rtspThread.writeString(toString());
				rtspThread.getVideoPacketizer().startStreaming();
			}
		}

		Log.d(TAG, "rtsp PLAY finished");
    }
}
