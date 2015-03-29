package org.mshare.server.rtsp.cmd;

import org.mshare.server.rtsp.RtspCmd;
import org.mshare.server.rtsp.RtspThread;

public class RtspError extends RtspCmd {

    public RtspError(RtspThread rtspThread, String input, int cseq) {
        super(rtspThread, cseq);
    }

    protected void generateBody() {
    }

}
