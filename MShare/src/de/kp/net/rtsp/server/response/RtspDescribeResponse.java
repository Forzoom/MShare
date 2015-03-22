package de.kp.net.rtsp.server.response;

import java.net.UnknownHostException;

import de.kp.net.rtsp.RtspConstants.VideoEncoder;

/**
 * sdp�ĸ�ʽ
	v=<version>
	o=<username> <session id> <version> <network type> <address type> <address>
	s=<session name>
	i=<session description>
	u=<URI>
	e=<email address>
	p=<phone number>
	c=<network type> <address type> <connection address>
	b=<modifier>:<bandwidth-value>
	t=<start time> <stop time>
	r=<repeat interval> <active duration> <list of offsets from start-time>
	z=<adjustment time> <offset> <adjustment time> <offset> ....
	k=<method>
	k=<method>:<encryption key>
	a=<attribute>
	a=<attribute>:<value>
	m=<media> <port> <transport> <fmt list>
	v = ��Э��汾��
	o = ��������/�����ߺͻỰ��ʶ����
	s = ���Ự���ƣ�
	i = * ���Ự��Ϣ��
	u = * ��URI ������
	e = * ��Email ��ַ��
	p = * ���绰���룩
	c = * ��������Ϣ��
	b = * ��������Ϣ��
	z = * ��ʱ�����������
	k = * ��������Կ��
	a = * ��0 �������Ự�����У�
	ʱ��������
	t = ���Ự�ʱ�䣩
	r = * ��0�����ظ�������
	ý��������
	m = ��ý�����ƺʹ����ַ��
	i = * ��ý����⣩
	c = * ��������Ϣ �� ��������ڻỰ������ֶο�ѡ��
	b = * ��������Ϣ��
	k = * ��������Կ��
	a = * ��0 ������ý�������У�
	
	
 * 
 * C��S����DESCRIBE����,Ϊ�˵õ��Ự������Ϣ(SDP):
 * DESCRIBE rtsp://192.168.20.136:5000/xxx666 RTSP/1.0
 * CSeq: 2
 * token:
 * Accept: application/sdp
 * User-Agent: VLC media player (LIVE555 Streaming Media v2005.11.10)
 * 
 * ��������ӦһЩ�Դ˻Ự��������Ϣ(sdp):
 * RTSP/1.0 200 OK
 * Server: UServer 0.9.7_rc1
 * Cseq: 2
 * x-prev-url: rtsp://192.168.20.136:5000
 * x-next-url: rtsp://192.168.20.136:5000
 * x-Accept-Retransmit: our-retransmit
 * x-Accept-Dynamic-Rate: 1
 * Cache-Control: must-revalidate
 * Last-Modified: Fri, 10 Nov 2006 12:34:38 GMT
 * Date: Fri, 10 Nov 2006 12:34:38 GMT
 * Expires: Fri, 10 Nov 2006 12:34:38 GMT
 * Content-Base: rtsp://192.168.20.136:5000/xxx666/
 * Content-Length: 344
 * Content-Type: application/sdp
	
	v=0        //���¶���sdp��Ϣ
	o=OnewaveUServerNG 1451516402 1025358037 IN IP4 192.168.20.136
	s=/xxx666
	u=http:///
	e=admin@
	c=IN IP4 0.0.0.0
	t=0 0
	a=isma-compliance:1,1.0,1
	a=range:npt=0-
	m=video 0 RTP/AVP 96    //m��ʾý�������������ǶԻỰ����Ƶͨ����ý������
	a=rtpmap:96 MP4V-ES/90000
	a=fmtp:96 profile-level-id=245;config=000001B0F5000001B509000001000000012000C888B0E0E0FA62D089028307
	a=control:trackID=0//trackID��0��ʾ��Ƶ���õ���ͨ��0
 * @author HM
 *
 */

public class RtspDescribeResponse extends RtspResponse {

    protected String rtpSession  = "";
    protected String contentBase = "";

    private String fileName;

	private VideoEncoder encoder;
    
    public RtspDescribeResponse(int cseq) {
        super(cseq);
    }

    protected void generateBody() {
            
    	SDP sdp = new SDP(fileName, encoder);

        String sdpContent = "";
        try {
            sdpContent = CRLF2 + sdp.getSdp();
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
        body += "Content-base: "+contentBase + CRLF
        + "Content-Type: application/sdp"+ CRLF 
        + "Content-Length: "+ sdpContent.length() + sdpContent;

    }

    public String getContentBase() {
        return contentBase;
    }

    public void setContentBase(String contentBase) {
        this.contentBase = contentBase;
    }
    
    public void setFileName(String fileName) {
    	this.fileName = fileName;
    }

    public String getFileName() {
		return fileName;
	}
    
    public void setVideoEncoder(VideoEncoder encoder) {
    	this.encoder = encoder;
    }
}
