package org.mshare.server.rtsp.cmd;

import android.os.Environment;
import android.util.Log;

import org.mshare.file.share.SharedLink;
import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspParser;
import org.mshare.account.AccountFactory.Token;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import org.mshare.server.rtsp.RtspConstants.VideoEncoder;
import org.mshare.server.rtsp.SDP;

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

public class CmdDESCRIBE extends RtspCmd {
    private static final String TAG = CmdDESCRIBE.class.getSimpleName();

    protected String rtpSession  = "";
    protected String contentBase = "";

    private String fileName;

	private VideoEncoder encoder;

	private String input;

    public CmdDESCRIBE(SessionThread sessionThread, String input, int cseq) {
        super(sessionThread, cseq);
		this.input = input;
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

    public void setVideoEncoder(VideoEncoder encoder) {
    	this.encoder = encoder;
    }

    @Override
    public void run() {
        Log.d(TAG, "rtsp DESCRIBE executing");

		// ����һ���հ����ݶ����ʾ����
		String errString = new RtspError(sessionThread, input, cseq).toString();

		mainblock : {
			// �����ļ���
			fileName = RtspParser.getFileName(input);

			// set video encoding
			// TODO ���������û�б���ȷ����
			setVideoEncoder(encoder);

			// finally set content base
			// TODO û�б���ȷ����
			setContentBase(contentBase);

			// ��ǰ�ļ����Ѿ�׼������
			Log.d(TAG, "try to get the file : " + fileName);
			// TODO ��Ҫ��֤����Ӧ�������·�����Է�б�ܿ�ͷ��

//			Token token = sessionThread.getToken();
//			if (token == null || !token.isValid()) {
//				Log.e(TAG, "maybe has not authorized!");
//				sessionThread.writeString(errString);
//				break mainblock;
//			}
//
//			// ��ö�Ӧ�ļ�����
//			SharedLink sharedLink = token.getSystem().getSharedLink(fileName);
//			if (sharedLink == null) {
//				Log.e(TAG, "something wrong is happen, the file is null!");
//				sessionThread.writeString(errString);
//				break mainblock;
//			} else if (sharedLink.isDirectory() || sharedLink.isFakeDirectory()) {
//				Log.e(TAG, "the file is not a file!");
//				sessionThread.writeString(errString);
//				break mainblock;
//			}
//
//			// ����ļ���
//			File file = sharedLink.getRealFile();
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dog.mp4");
			try {
				sessionThread.setVideoInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				Log.e(TAG, "the file is not found");
				e.printStackTrace();
				sessionThread.writeString(errString);
				break mainblock;
			}

			// TODO ��ʱ������д������
			sessionThread.writeString(toString());
		}

        Log.d(TAG, "rtsp DESCRIBE finished");
    }
}
