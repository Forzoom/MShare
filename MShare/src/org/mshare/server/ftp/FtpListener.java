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

import org.mshare.server.ServerService;

import android.util.Log;

public class FtpListener extends Thread {
    private static final String TAG = FtpListener.class.getSimpleName();

    ServerSocket listenSocket;
    
    private SessionController sessionController;
    
    public FtpListener(ServerSocket listenSocket, SessionController sessionController) {
        this.listenSocket = listenSocket;
        this.sessionController= sessionController; 
    }

    public void quit() {
    	Log.d(TAG, "try to quit FtpListener");
        try {
            listenSocket.close(); // if the FtpListener thread is blocked on accept,
                                  // closing the socket will raise an exception
            Log.v(TAG, "quit FtpListener succeed!");
        } catch (Exception e) {
            Log.d(TAG, "Exception closing FtpListener listenSocket");
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
            	Log.i(TAG, "New connection, spawned thread");
            	FtpSessionThread newSession = new FtpSessionThread(clientSocket, new LocalDataSocket());
                newSession.start();
                // register应该放在哪里呢？
                sessionController.registerSessionThread(newSession);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception in FtpListener");
        }
    }
}
