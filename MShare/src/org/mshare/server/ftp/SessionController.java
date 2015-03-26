package org.mshare.server.ftp;

import java.util.ArrayList;
import java.util.List;

import org.mshare.account.AccountFactory.Verifier;

import android.util.Log;

/**
 * FsService����Ҫ�ƿ����е�Session
 * 
 * ԭ����FsService�е�sessionThreads�е�Session��Ҫ��SessionController�д��ڲ���������NotifierҲ��Ҫ��SessionController�н��д���
 * ����Session�Ŀ�����
 * 
 * ���Ի�����е�SessionInfo
 * @author HM
 *
 */
public class SessionController {

	private static final String TAG = SessionController.class.getSimpleName();
	// ���������Session
	private ArrayList<SessionThread> sessionThreads = new ArrayList<SessionThread>();
	// ������֤�û������Ϣ������ע����SessionController�е�SessionThread����ʹ�ø���֤��
	private Verifier verifier;
	
	// ������ӵ�е�Session����,Session���������ܳ�������ֵ����Session�����������ֵ��ʱ�򣬽����ٽ��ܿͻ��˵����ӣ����߽��ܿͻ��˵����Ӻ󣬸�֪
	// �ͻ��ˣ���ǰ�����������Ѿ��������ߣ�Ȼ�������߳��˳�
	// �������Ӻ��֪ʵ����������Դ
	private int maxSessionCount = 8;
	// ��Ӧ�Ļص�����
	private SessionCallback sessionCallback;
	
	/**
     * ע��Ự�߳�
     */
    public void registerSessionThread(SessionThread newSession) {
        // Before adding the new session thread, clean up any finished session
        // threads that are present in the list.

        // Since we're not allowed to modify the list while iterating over
        // it, we construct a list in toBeRemoved of threads to remove
        // later from the sessionThreads list.
        synchronized (this) {
        	// �������"ʧЧ"�������̣߳�����join�����ܹ���ȷ�˳�
            List<SessionThread> toBeRemoved = new ArrayList<SessionThread>();
            for (SessionThread sessionThread : sessionThreads) {
                if (!sessionThread.isAlive()) {
                    Log.d(TAG, "Cleaning up finished session...");
                    try {
                    	// ����߳�ʧЧ����ô��ֹ�̣߳�����join
                        sessionThread.join();
                        Log.d(TAG, "Thread joined");
                        toBeRemoved.add(sessionThread);
                        sessionThread.closeSocket(); // make sure socket closed
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Interrupted while joining");
                        // We will try again in the next loop iteration
                    }
                }
            }
            
            // ����ʧЧ�̶߳�������join���������ڿ��Խ����Ƴ�
            for (SessionThread removeThread : toBeRemoved) {
                sessionThreads.remove(removeThread);
            }

            // Cleanup is complete. Now actually add the new thread to the list.
            // ����verifier��null
            
            
            newSession.verifier = verifier;
            Log.d(TAG, "bind Verifier");
            sessionThreads.add(newSession);
            if (sessionCallback != null) {
            	sessionCallback.onRegister(newSession.sessionInfo);
            }
        }
        Log.d(TAG, "Registered session thread");
    }
    
    /**
     * ֹͣ���лỰ
     */
    public void terminateAllSessions() {
        Log.i(TAG, "Terminating " + sessionThreads.size() + " session thread(s)");
        synchronized (this) {
            for (SessionThread sessionThread : sessionThreads) {
                if (sessionThread != null) {
                    sessionThread.closeDataSocket();
                    sessionThread.closeSocket();
                }
            }
        }
    }
    
    /**
     * ���SessionInfo���飬���а������е�SessionInfo
     * �����ǻ�����е�sessionInfo������
     * ÿ�ε��ö���ѭ�����
     * @return
     */
    public SessionInfo[] getAllSessionInfo() {
    	SessionInfo[] result = new SessionInfo[getCount()];
    	for (int i = 0, len = getCount(); i < len; i++) {
    		result[i] = sessionThreads.get(i).sessionInfo;
    	}
    	return result;
    }
    
    /**
     * ����ض�Session��SessionInfo
     * @param index ��Ӧ��index
     * @return
     */
    public SessionInfo getSessionInfo(int index) {
    	return sessionThreads.get(index).sessionInfo;
    }
    
    /**
     * ��ʱ�������SessionThread������ʹ�õģ�����Notifier������
     * @param index
     * @return
     */
    protected SessionThread getSessionThread(int index) {
    	return sessionThreads.get(index);
    }
    
    /**
     * ��õ�ǰSession�ĸ���
     * @return
     */
    public int getCount() {
    	return sessionThreads.size();
    }
    
    // ������֤��,��û����֤����ʱ�����ô�죿
    public void setVerifier(Verifier verifier) {
    	this.verifier = verifier;
    }
    
    /**
     * ����{@link SessionCallback}
     * @param callback ���õ�Callback
     */
    public void setSessionCallback(SessionCallback callback) {
    	this.sessionCallback = callback;
    }
    
	/**
	 * ��Session����������仯��ʱ��Ļص�����
	 * ��֪��������õ�����ӿ�
	 * @author HM
	 *
	 */
	interface SessionCallback {
		// �����µ�Session��ע���ʱ��
		public void onRegister(SessionInfo sessionInfo);
		// ����Session�˳���ʱ��
		// ���ܻ����Session���ģ�˳������
		// ����û��onUnregister�������
//		public void onUnregister();
	}
}
