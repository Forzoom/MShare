package org.mshare.server.ftp;

import org.mshare.account.Account;
import org.mshare.account.AccountFactory.Token;

/**
 * 用于向多个Session发送消息，消息的内容已经包装好了
 * TODO 在客户端有小红点
 * 不应该让所有人都可以使用Notifier，因为Notifier将会向用户发送消息
 * TODO 发送消息通知其他线程:"有新的文件"，该如何发送这些消息呢？
 * TODO 对于发送这个提醒的Session，不应该被提醒
 * TODO SessionThread可能现在正在发送消息，需要用什么样的方式来发送消息提醒呢？使用消息队列？
 * TODO 将发送消息处理成一个函数
 *
 * 由FsService持有，交由
 * 对cmd，调用Token
 *
 * 应该保证传入的sessionThreads能够被正确的回收
 *
 * @author HM
 *
 */
public class SessionNotifier {
	/**
	 * Session控制器
	 */
	private SessionController sessionController;
	
	public SessionNotifier(SessionController sessionController) {
		this.sessionController = sessionController;
	}
	
	/**
	 * TODO 需要如何排除sender
	 * TODO 如果是管理员账户该怎么办？
	 * @param sender may be null
	 */
	public void notifyAddFile(Token token, SessionThread sender) {
		int sessionCount = sessionController.getCount();
		
		// 对于管理员账户来说
		if (token.isAdministrator()) {
			for (int index = 0; index < sessionCount; index++) {
    			SessionThread receiveSession = sessionController.getSessionThread(index);
				// 发送消息通知所有的Session
//    			receiveSession.
    		}
		} else {
			// 普通账户
			for (int index = 0; index < sessionCount; index++) {
    			// 在所有的session中寻找拥有相同的Accoount的SessionThread,但不包括sender
    			SessionThread receiveSession = sessionController.getSessionThread(index);
    			// session和sender不相等如何判断
    			if (receiveSession.getToken().equals(token) && receiveSession != sender) {
    				// 发送消息通知
//    				receiveSession.
    			}
    		}
		}
		
		
	}
	
	public void notifyDeleteFile(Account account, SessionThread sender) {
		
	}
	
	// 需要通知当前内容：新文件，正在使用，删除
}