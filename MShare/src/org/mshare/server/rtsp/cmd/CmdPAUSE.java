package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.FtpSessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspConstants;
import org.mshare.server.rtsp.RtspThread;

public class CmdPAUSE extends RtspCmd {
    private static final String TAG = CmdPAUSE.class.getSimpleName();

    public CmdPAUSE(RtspThread rtspThread, String input, int cseq) {
        super(rtspThread, cseq);
    }

    protected void generateBody() {
    }

    @Override
    public void run() {
        Log.d(TAG, "rtsp PAUSE executing!");
        rtspThread.writeString(toString());

		if (rtspThread.getRtspState() == RtspConstants.PLAYING) {
			// suspend RTP socket from sending video packets
			rtspThread.getRtpSocket().suspend(true);
		}

		Log.d(TAG, "rtsp PAUSE finished!");
    }
}
