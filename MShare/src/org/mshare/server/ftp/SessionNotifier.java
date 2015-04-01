package org.mshare.server.ftp;

import org.mshare.account.Account;
import org.mshare.account.AccountFactory.Token;

/**
 * ��������Session������Ϣ����Ϣ�������Ѿ���װ����
 * TODO �ڿͻ�����С���
 * ��Ӧ���������˶�����ʹ��Notifier����ΪNotifier�������û�������Ϣ
 * TODO ������Ϣ֪ͨ�����߳�:"���µ��ļ�"������η�����Щ��Ϣ�أ�
 * TODO ���ڷ���������ѵ�Session����Ӧ�ñ�����
 * TODO SessionThread�����������ڷ�����Ϣ����Ҫ��ʲô���ķ�ʽ��������Ϣ�����أ�ʹ����Ϣ���У�
 * TODO ��������Ϣ�����һ������
 *
 * ��FsService���У�����
 * ��cmd������Token
 *
 * Ӧ�ñ�֤�����sessionThreads�ܹ�����ȷ�Ļ���
 *
 * @author HM
 *
 */
public class SessionNotifier {
	/**
	 * Session������
	 */
	private SessionController sessionController;
	
	public SessionNotifier(SessionController sessionController) {
		this.sessionController = sessionController;
	}
	
	/**
	 * TODO ��Ҫ����ų�sender
	 * TODO ����ǹ���Ա�˻�����ô�죿
	 * @param sender may be null
	 */
	public void notifyAddFile(Token token, FtpSessionThread sender) {
		int sessionCount = sessionController.getCount();
		
		// ���ڹ���Ա�˻���˵
		if (token.isAdministrator()) {
			for (int index = 0; index < sessionCount; index++) {
    			FtpSessionThread receiveSession = sessionController.getSessionThread(index);
				// ������Ϣ֪ͨ���е�Session
//    			receiveSession.
    		}
		} else {
			// ��ͨ�˻�
			for (int index = 0; index < sessionCount; index++) {
    			// �����е�session��Ѱ��ӵ����ͬ��Accoount��SessionThread,��������sender
    			FtpSessionThread receiveSession = sessionController.getSessionThread(index);
    			// session��sender���������ж�
    			if (receiveSession.getToken().equals(token) && receiveSession != sender) {
    				// ������Ϣ֪ͨ
//    				receiveSession.
    			}
    		}
		}
		
		
	}
	
	public void notifyDeleteFile(Account account, FtpSessionThread sender) {
		
	}
	
	// ��Ҫ֪ͨ��ǰ���ݣ����ļ�������ʹ�ã�ɾ��
}