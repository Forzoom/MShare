package org.mshare.server.rtsp;

import android.util.Log;

import org.mshare.server.ftp.SessionThread;
import org.mshare.account.AccountFactory.Token;
import org.mshare.server.rtsp.cmd.CmdDESCRIBE;
import org.mshare.server.rtsp.cmd.CmdOPTIONS;
import org.mshare.server.rtsp.cmd.CmdPAUSE;
import org.mshare.server.rtsp.cmd.CmdPLAY;
import org.mshare.server.rtsp.cmd.CmdSETUP;
import org.mshare.server.rtsp.cmd.CmdTEARDOWN;

import java.lang.reflect.Constructor;
import java.util.Date;

public abstract class RtspCmd implements Runnable {
    private static final String TAG = RtspCmd.class.getSimpleName();

	// toString中所返回的内容
    protected String response="";
       
    protected int cseq = 0;
       
   	protected static int session_id = -1;       
   	protected boolean newSessionId = true;
	// TODO body不知道是什么呢？
	protected String body = "";

    protected SessionThread sessionThread;

    /**
     * CR = <US-ASCII CR, carriage return (13)>
     * LF = <US-ASCII LF, linefeed (10)>
     * CRLF = CR LF
     */
    public static final String CRLF  = "\r\n";
    public static final String CRLF2 = "\r\n\r\n";
    public static final String SEP   = " ";

    // 保存和纪录所有的RtspCmd
    private static RtspCmdMap cmdClasses[] = {
        new RtspCmdMap("OPTIONS", CmdOPTIONS.class),
        new RtspCmdMap("DESCRIBE", CmdDESCRIBE.class),
        new RtspCmdMap("PLAY", CmdPLAY.class),
        new RtspCmdMap("PAUSE", CmdPAUSE.class),
        new RtspCmdMap("TEARDOWN", CmdTEARDOWN.class),
        new RtspCmdMap("SETUP", CmdSETUP.class)
    };

    @Override
    public void run() {}

    public RtspCmd(SessionThread sessionThread, int cseq) {
        this.sessionThread = sessionThread;
        this.cseq = cseq;
    }
       
    protected String getHeader() {

    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("RTSP/1.0" + SEP + "200" + SEP + "OK" + CRLF);
    	sb.append(cseq() +CRLF);
    	
    	sb.append("Date: " + new Date().toGMTString() + CRLF);
    	sb.append("Server: " + getServer() + CRLF);

    	return sb.toString();
    	
    }
       
	protected String cseq() {
        return "CSeq:" + SEP + getCseq();
	}
   
    protected String getResponse() {
        return response;
    }

    protected void setResponse(String response) {
        this.response = response;
    }

    protected String getServer(){
        return RtspConstants.SERVER_NAME + "/" + RtspConstants.SERVER_VERSION;
    }
       
    protected int getCseq() {
        return cseq;
    }

    protected void setCseq(int cseq) {
        this.cseq = cseq;
    }

    protected String getBody() {
        return body;
    }

    protected void setBody(String cuerpo) {
        this.body = cuerpo;
    }

    protected void generate(){
    
    	// note that it is important to close the response
    	// message with 2 CRLFs
    	response += getHeader();
        response += getBody() + CRLF2;

    }
       
    protected abstract void generateBody();

	/**
	 * 重写的Rtsp.toString,生成使用了Body和Generate？可以是generateBody中仅仅是对于body进行了创建
	 * @return
	 */
    public String toString() {
            
    	generateBody();            
    	generate();
  
    	return response;
    
    }
       
    public void createSessionId(boolean bool){
        newSessionId = bool;
    }

    public static void dispatchCmd(SessionThread session, String inputString) {
        // 分割
        String[] strings = inputString.split(" ");

        String cmd = RtspParser.getCmd(strings);

        // 判断当前能否使用Cmd
        Token token = session.getToken();

        // 判断当前的Token是否可用
        if (token == null || !token.isValid()) {
            Log.e(TAG, "unauthorized! cannot use rtsp command");
            return;
        }

        RtspCmd cmdInstance = null;
        // 获得消息序号
        try {
            session.setCseq(RtspParser.getCseq(inputString));
        } catch (Exception e) {
            Log.e(TAG, "cannot get the rtsp cseq, stop handling rtsp command");
            e.printStackTrace();
            return;
        }

        // 获得ContentBase
        if (session.getContentBase().isEmpty()) {
            session.setContentBase(RtspParser.getContentBase(inputString));
        }

        // 生成对应的对象，这个时候已经不再需要判断了
        for (int i = 0; i < cmdClasses.length; i++) {

            if (cmdClasses[i].getName().equals(cmd)) {
                Constructor<? extends RtspCmd> constructor;
                try {
                    constructor = cmdClasses[i].getCommand().getConstructor(new Class[] { SessionThread.class, String.class, int.class });
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "RtspCmd subclass lacks expected " + "constructor ");
                    return;
                }
                try {
                    cmdInstance = constructor.newInstance(new Object[] {session, inputString, session.getCseq()});
                } catch (Exception e) {
                    Log.e(TAG, "Instance creation error on RtspCmd");
                    return;
                }
            }
        }

        cmdInstance.run();
    }

    // 判断当前的信息是否时RtspCmd
    public static boolean isRtspCmd(String inputString) {
        String[] strings = inputString.split(" ");
        // 不能识别
        if (strings == null || strings.length < 1) {
            Log.d(TAG, "No strings parsed");
            return false;
        }
        // 对应的Cmd的名称
        String verb = strings[0];
        if (verb.length() < 1) {
            Log.i(TAG, "Invalid command verb");
            return false;
        }
        // 可是没有办法对应
        for (int i = 0; i < cmdClasses.length; i++) {
            if (cmdClasses[i].getName().equals(verb)) {
                return true;
            }
        }
        return false;
    }

}

