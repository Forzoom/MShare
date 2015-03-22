package de.kp.net.rtsp.server.response;

/**
 * 客户端发送播放请求:
	PLAY rtsp://192.168.20.136:5000/xxx666 RTSP/1.0
	CSeq: 4
	Session: 6310936469860791894
	Range: npt=0.000-      //设置播放时间的范围
	User-Agent: VLC media player (LIVE555 Streaming Media v2005.11.10)
	
	服务器回应信息:
	RTSP/1.0 200 OK
	Server: UServer 0.9.7_rc1
	Cseq: 4
	Session: 6310936469860791894
	Range: npt=0.000000-
	RTP-Info: url=trackID=0;seq=17040;rtptime=1467265309     
	//seq和rtptime都是rtp包中的信息
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
