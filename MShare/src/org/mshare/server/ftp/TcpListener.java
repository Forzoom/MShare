/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mshare.server.ftp;

import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class TcpListener extends Thread {
    private static final String TAG = TcpListener.class.getSimpleName();

    ServerSocket listenSocket;
    ServerService ftpServerService;
    
    private SessionController sessionController;
    
    public TcpListener(ServerSocket listenSocket, ServerService ftpServerService, SessionController sessionController) {
        this.listenSocket = listenSocket;
        this.ftpServerService = ftpServerService;
        this.sessionController= sessionController; 
    }

    public void quit() {
        try {
            listenSocket.close(); // if the TcpListener thread is blocked on accept,
                                  // closing the socket will raise an exception
        } catch (Exception e) {
            Log.d(TAG, "Exception closing TcpListener listenSocket");
        }
    }

    /**
     * 不退出循环，监听并接收连接，创建的是SessionThread，并在ServerThread中注册
     */
    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = listenSocket.accept();
                
                boolean findTargetSession = false;
                
                int sessionCount = sessionController.getCount();
                for (int i = 0; i < sessionCount; i++) {
                	
                	SessionThread sessionThread = sessionController.getSessionThread(i);
                	String currentIp = sessionThread.getClientAddress().toString();
                	String targetIp = clientSocket.getInetAddress().toString();
                	Log.d(TAG, "targetIp : " + targetIp + " currentIp : " + currentIp);
                	if (currentIp.equals(targetIp) && sessionThread.isRtspEnabled()) {
                    	Log.d(TAG, "find target session");
                    	findTargetSession = true;
                    	sessionThread.setRtspSocket(clientSocket);
                    }
                }
                
                if (!findTargetSession) {
                	Log.i(TAG, "New connection, spawned thread");
                	SessionThread newSession = new SessionThread(clientSocket,
                            new LocalDataSocket());
                    newSession.start();
                    // register应该放在哪里呢？
                    ftpServerService.registerSessionThread(newSession);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception in TcpListener");
        }
    }
}
