package org.mshare.server.rtsp.cmd;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspConstants;

public class CmdPAUSE extends RtspCmd {
    private static final String TAG = CmdPAUSE.class.getSimpleName();

    public CmdPAUSE(SessionThread sessionThread, String input, int cseq) {
        super(sessionThread, cseq);
    }

    protected void generateBody() {
    }

    @Override
    public void run() {
        Log.d(TAG, "rtsp PAUSE executing!");
		sessionThread.writeString(toString());

		if (sessionThread.getRtspState() == RtspConstants.PLAYING) {
			// suspend RTP socket from sending video packets
			sessionThread.getRtpSocket().suspend(true);
		}

		Log.d(TAG, "rtsp PAUSE finished!");
    }
}
