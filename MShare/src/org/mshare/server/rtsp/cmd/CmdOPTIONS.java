package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspThread;

/**
 * 目的是得到服务器提供的可用方法:
 * OPTIONS rtsp://192.168.20.136:5000/xxx666 RTSP/1.0
 * CSeq: 1         //每个消息都有序号来标记，第一个包通常是option请求消息
 * User-Agent: VLC media player (LIVE555 Streaming Media v2005.11.10)
 * 
 * 返回的内容
 * RTSP/1.0 200 OK
 * Server: UServer 0.9.7_rc1
 * Cseq: 1         //每个回应消息的cseq数值和请求消息的cseq相对应
 * Public: OPTIONS, DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE, SCALE, GET_PARAMETER //服务器提供的可用的方法
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
