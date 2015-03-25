package org.mshare.server.rtsp.cmd;

import org.mshare.server.ftp.SessionThread;
import org.mshare.server.rtsp.RtspCmd;

public class RtspError extends RtspCmd {

    public RtspError(SessionThread sessionThread, String input, int cseq) {
        super(sessionThread, cseq);
    }

    protected void generateBody() {
    }

}
