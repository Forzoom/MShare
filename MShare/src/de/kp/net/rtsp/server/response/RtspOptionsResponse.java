package de.kp.net.rtsp.server.response;

/**
 * Ŀ���ǵõ��������ṩ�Ŀ��÷���:
 * OPTIONS rtsp://192.168.20.136:5000/xxx666 RTSP/1.0
 * CSeq: 1         //ÿ����Ϣ�����������ǣ���һ����ͨ����option������Ϣ
 * User-Agent: VLC media player (LIVE555 Streaming Media v2005.11.10)
 * 
 * ���ص�����
 * RTSP/1.0 200 OK
 * Server: UServer 0.9.7_rc1
 * Cseq: 1         //ÿ����Ӧ��Ϣ��cseq��ֵ��������Ϣ��cseq���Ӧ
 * Public: OPTIONS, DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE, SCALE, GET_PARAMETER //�������ṩ�Ŀ��õķ���
 */
public class RtspOptionsResponse extends RtspResponse {

    public RtspOptionsResponse(int cseq) {
        super(cseq);
    }
    
    protected void generateBody() {
        this.body = "Public:DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE"/*+SL*/;
    }

}
