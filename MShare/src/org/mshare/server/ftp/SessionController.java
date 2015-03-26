package org.mshare.server.ftp;

import java.util.ArrayList;
import java.util.List;

import org.mshare.account.AccountFactory.Verifier;

import android.util.Log;

/**
 * FsService必须要掌控所有的Session
 * 
 * 原本在FsService中的sessionThreads中的Session需要在SessionController中存在并处理，所以Notifier也需要在SessionController中进行处理
 * 所有Session的控制器
 * 
 * 可以获得所有的SessionInfo
 * @author HM
 *
 */
public class SessionController {

	private static final String TAG = SessionController.class.getSimpleName();
	// 保存的所有Session
	private ArrayList<SessionThread> sessionThreads = new ArrayList<SessionThread>();
	// 用于验证用户身份信息，所有注册在SessionController中的SessionThread都将使用该验证器
	private Verifier verifier;
	
	// 最大可以拥有的Session数量,Session的数量不能超过该数值，当Session数量到达该数值的时候，将不再接受客户端的连接，或者接受客户端的连接后，告知
	// 客户端，当前的连接数量已经到达上线，然后再让线程退出
	// 但是连接后告知实在是消耗资源
	private int maxSessionCount = 8;
	// 对应的回调函数
	private SessionCallback sessionCallback;
	
	/**
     * 注册会话线程
     */
    public void registerSessionThread(SessionThread newSession) {
        // Before adding the new session thread, clean up any finished session
        // threads that are present in the list.

        // Since we're not allowed to modify the list while iterating over
        // it, we construct a list in toBeRemoved of threads to remove
        // later from the sessionThreads list.
        synchronized (this) {
        	// 获得所有"失效"的连接线程？调用join让其能够正确退出
            List<SessionThread> toBeRemoved = new ArrayList<SessionThread>();
            for (SessionThread sessionThread : sessionThreads) {
                if (!sessionThread.isAlive()) {
                    Log.d(TAG, "Cleaning up finished session...");
                    try {
                    	// 如果线程失效，那么终止线程，调用join
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
            
            // 所有失效线程都调用了join，所以现在可以将其移出
            for (SessionThread removeThread : toBeRemoved) {
                sessionThreads.remove(removeThread);
            }

            // Cleanup is complete. Now actually add the new thread to the list.
            // 可能verifier是null
            
            
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
     * 停止所有会话
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
     * 获得SessionInfo数组，其中包含所有的SessionInfo
     * 仅仅是获得所有的sessionInfo的引用
     * 每次调用都会循环获得
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
     * 获得特定Session的SessionInfo
     * @param index 对应的index
     * @return
     */
    public SessionInfo getSessionInfo(int index) {
    	return sessionThreads.get(index).sessionInfo;
    }
    
    /**
     * 临时用来获得SessionThread对象所使用的，用于Notifier来处理
     * @param index
     * @return
     */
    protected SessionThread getSessionThread(int index) {
    	return sessionThreads.get(index);
    }
    
    /**
     * 获得当前Session的个数
     * @return
     */
    public int getCount() {
    	return sessionThreads.size();
    }
    
    // 设置验证器,当没有验证器的时候该怎么办？
    public void setVerifier(Verifier verifier) {
    	this.verifier = verifier;
    }
    
    /**
     * 设置{@link SessionCallback}
     * @param callback 设置的Callback
     */
    public void setSessionCallback(SessionCallback callback) {
    	this.sessionCallback = callback;
    }
    
	/**
	 * 当Session的情况发生变化的时候的回调函数
	 * 不知道哪里会用到这个接口
	 * @author HM
	 *
	 */
	interface SessionCallback {
		// 当有新的Session被注册的时候
		public void onRegister(SessionInfo sessionInfo);
		// 当有Session退出的时候
		// 可能会出现Session大规模退出的情况
		// 好像没有onUnregister这个方法
//		public void onUnregister();
	}
}
