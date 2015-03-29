package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspThread;

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
public class CmdOPTIONS extends RtspCmd {
    private static final String TAG = CmdOPTIONS.class.getSimpleName();

    public CmdOPTIONS(RtspThread rtspThread, int cseq) {
        super(rtspThread, cseq);
    }
    
    protected void generateBody() {
        this.body = "Public:DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE"/*+SL*/;
    }

    @Override
    public void run() {
        Log.d(TAG, "rtsp OPTIONS executing");
        rtspThread.writeString(toString());
        Log.d(TAG, "rtsp OPTIONS finished");
    }
}
