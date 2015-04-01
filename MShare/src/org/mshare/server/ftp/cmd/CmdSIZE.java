package org.mshare.server.ftp.cmd;

import java.io.File;

import org.mshare.file.share.SharedLink;
import org.mshare.server.ftp.FtpCmd;
import org.mshare.server.ftp.FtpParser;
import org.mshare.server.ftp.FtpSessionThread;

import android.util.Log;

/**
 * 获得文件的大小 传递的参数只能是文件名，对应当前的working directory
 * SIZE好像并不是ftp协议中的内容...总之不再RFC959中
 * @author HM
 *
 */
public class CmdSIZE extends FtpCmd {
    private static final String TAG = CmdSIZE.class.getSimpleName();

    protected String input;

    public CmdSIZE(FtpSessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Log.d(TAG, "SIZE executing");
        String errString = null;
        String param = FtpParser.getParameter(input);
        long size = 0;
        mainblock: {
            SharedLink currentDir = sessionThread.getToken().getSystem().getWorkingDir();
            if (param.contains(File.separator)) {
                errString = "550 No directory traversal allowed in SIZE param\r\n";
                break mainblock;
            }
            SharedLink target = sessionThread.getToken().getSystem().getSharedLink(currentDir, param);
            // We should have caught any invalid location access before now, but
            // here we check again, just to be explicitly sure.
            
            if (!target.exists()) {
                errString = "550 Cannot get the SIZE of nonexistent object\r\n";
                Log.i(TAG, "Failed getting size of: " + target.getFakePath() + target.getRealPath());
                break mainblock;
            }
            if (!target.isFile()) {
                errString = "550 Cannot get the size of a non-file\r\n";
                break mainblock;
            }
            size = target.length();
        }
        if (errString != null) {
            sessionThread.writeString(errString);
        } else {
            sessionThread.writeString("213 " + size + "\r\n");
        }
        Log.d(TAG, "SIZE complete");
    }

}
