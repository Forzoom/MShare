package org.mshare.server.rtsp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.mshare.preference.ServerSettings;
import org.mshare.server.ServerService;

/**
 * This class provides a parser for incoming RTSP
 * messages and splits them into appropriate parts.
 *
 * �������rtsp���ݷָ����ȷ������
 *
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */
public class RtspParser {
	private static final String TAG = RtspParser.class.getSimpleName();
        
    /**
     * ����ʱ��ȡ��/r/n
     * @param
     * @return
     * @throws IOException
     */

    public static String readRequest(BufferedReader br) throws IOException {
    	StringBuilder builder = new StringBuilder();
    	boolean endFound = false;
    	boolean hasAppend = false;
    	int i;
    	String str;
    	char c;

//    	while ((i = br.read()) != -1) {
//    		c = (char)i;
//    		builder.append(c);
//    		hasAppend = true;
//    		if (c == '\n') {
//    			Log.d(TAG, "find \\n");
//    			if (endFound) {
//    				break;
//    			} else {
//    				endFound = true;
//    			}
//    		} else if (c == '\r') {
//    			Log.d(TAG, "find \\r");
//    			endFound = true;
//    		} else {
//    			endFound = false;
//    		}
//    	}
    	
//    	while ((i = br.read()) != -1) {
//    		c = (char)i;
//    		builder.append(c);
//    		Log.d(TAG, "c : " + c);
//    		hasAppend = true;
//    		if (c == '\n') {
//    			if (endFound) {
//    				break;
//    			} else {
//    				endFound = true;
//    			}
//    		} else {
//    			if (c != '\r') {
//    				endFound = false;
//    			}
//    		}
//    	}

    	str = br.readLine();
    	while (str != null && !str.isEmpty()) {
    		str += "\r\n";
    		builder.append(str);
    		hasAppend = true;
    		str = br.readLine();
    	}
    	
    	if (hasAppend) {
        	return builder.toString();
    	} else {
    		return null;
    	}
    }

    // ���ڻ�õ�ǰ��cmd������ÿ�ζ���Ҫʹ��split���ָ��������˷�����
    public static String getCmd(String[] strings) {
        return strings[0];
    }


    /**
     * �����ڻ���������ز���
     * @param request
     * @return
     */
    public static String getContentBase(String request) {

    	StringTokenizer tokens = new StringTokenizer(request);
        String contentBase = "";
        
        if (tokens.hasMoreTokens()) {
        	contentBase = tokens.nextToken();
            contentBase = tokens.nextToken();
        }

        return contentBase;
    
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    public static int getCseq(String request) throws Exception {
        
    	String ineInput = getLineInput(request, "\r\n", "CSeq");
        String cseq = ineInput.substring(6);
            
        return Integer.parseInt(cseq);
    
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    public static int[] getInterleavedSetup(String request) throws Exception {

    	int[] interleaved = null;
            
    	String lineInput = getLineInput(request, "\r\n", "Transport:");
        String[] parts = lineInput.split("interleaved=");

        int t = parts.length;
        if (t > 1) {
        	
            parts = parts[1].split("-");
            interleaved = new int[2];
            
            interleaved[0] = Integer.parseInt(parts[0]);
            interleaved[1] = Integer.parseInt(parts[1]);
        }

        Log.d(TAG, "interleaved : " + interleaved);
        return interleaved;

    }

    /**
     * @param request
     * @return
     */
    public static String getFileName(String request) {
                
    	String lineInput = getLineInput(request, " ", "rtsp");

    	String ip = ServerService.getLocalInetAddress().toString();
    	ip = ip.charAt(0) == '/' ? ip.substring(1) : ip;
    	
    	// ��IP���Ѿ�������PORT
    	String splitRegex = "rtsp://" + ip + ":" + ServerSettings.getRtspPort();
    	String[] parts = lineInput.split(splitRegex);
    	Log.d(TAG, "split regex : " + splitRegex);
    	// ����һ�����·��
    	Log.d(TAG, "split result : " + parts[0]);
    	
        String fileName = parts[1];
    	Log.d(TAG, "getFileName : " + fileName);
        return fileName;
    }

    /**
     * This method retrieves a certain input from an
     * incoming RTSP request, described by a separator
     * and a specific prefix.
     * 
     * @param request
     * @param separator
     * @param prefix
     * @return
     */
    public static String getLineInput(String request, String separator, String prefix) {
		// �ڴ����е�һЩ�ط�������split������ʹ�ã�Ϊʲô�еĵط�ʹ����split���������еĵط�����StringToken
		String[] strings = request.split(separator);
		String ret = null;
        boolean match = false;

		for (int i = 0; i < strings.length; i++) {
			if (strings[i].startsWith(prefix)) {
				match = true;
				ret = strings[i];
				break;
			}
		}

		Log.d(TAG, "simple log getLineInput : " + ret + " prefix : " + prefix);

        return (match == true) ? ret : null;
    }

    /**
     * This method retrieves the client port
     * from an incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static int getClientPort(String request) {

    	String lineInput = getLineInput(request, "\r\n", "Transport:");
		if (lineInput == null) {
			return 0;
		}

    	String[] parts = lineInput.split(";");
        parts[2] = parts[2].substring(12);
            
        String[] ports = parts[2].split("-");
        int port = Integer.parseInt(ports[0]);
        Log.d(TAG, "client port : " + port);
        
        return port;
    
    }
 
    /**
     * This method retrieves the transport protocol
     * from an incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static String getTransportProtocol(String request) throws Exception {
    	
        String lineInput = getLineInput(request, "\r\n", "Transport:");
    	if (lineInput == null) throw new Exception();
        
        String[] parts = lineInput.split(";");
        parts[0] = parts[0].substring(11);
        
        String tp = parts[0];
        Log.d(TAG, "tp : " + tp);
        return tp;
    }

    /**
     * This method retrieves the range from an
     * incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static String getRangePlay(String request) {

    	String lineInput = getLineInput(request, "\r\n", "Range:");
    	if (lineInput == null) {
    		
    		/* 
    		 * Android's video view does not provide
    		 * range information with a PLAY request
    		 */
    		
    		return null;
    	}
        
    	String[] parts = lineInput.split("=");
        return parts[1];
    
    }

    /**
     * 
     * This method determines the session type from an
     * incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static String getSessionType(String request) throws Exception {
        
    	String lineInput = getLineInput(request, "\r\n", "Transport:");
    	if (lineInput == null) throw new Exception();
        
    	String[] parts = lineInput.split(";");
    	
    	String sessionType = parts[1].trim();
    	
        return sessionType;
    
    }
        
    /**
     * This method retrieves the user agent from an
     * incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public String getUserAgent(String request) throws Exception{
        
    	String lineInput = getLineInput(request, "\r\n", "User-Agent:");
    	if (lineInput == null) throw new Exception();
        
    	String[] parts = lineInput.split(":");
        return parts[1];
    
    }

}
