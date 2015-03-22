package de.kp.net.rtsp.server.response;

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
public class RtspResponseTeardown extends RtspResponse {

    public RtspResponseTeardown(int cseq) {
        super(cseq);
    }

    protected void generar(){
        response += getHeader();
        response += getBody() + CRLF;
    }
    
    protected void generateBody() {
        body += "";
    }

}
