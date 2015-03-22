package de.kp.net.rtsp.server.response;

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
public class RtspPlayResponse extends RtspResponse {

    protected String range = "";
   
    public RtspPlayResponse(int cseq) {
        super(cseq);
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
      
}
