package de.kp.net.rtsp.server.response;

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
