/*
Copyright 2011-2013 Pieter Pareit
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

package org.mshare.ftp.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;

import org.mshare.main.MShareApp;
import org.mshare.main.MShareUtil;

public class FsService extends Service implements Runnable {
    private static final String TAG = FsService.class.getSimpleName();

    // Service will (global) broadcast when server start/stop
    // 当FTP服务器启动或者停止的时候，会广播
    public static final String ACTION_STARTED = "org.mshare.ftp.server.FTPSERVER_STARTED";
    public static final String ACTION_STOPPED = "org.mshare.ftp.server.FTPSERVER_STOPPED";
    public static final String ACTION_FAILEDTOSTART = "org.mshare.ftp.server.FTPSERVER_FAILEDTOSTART";

    // RequestStartStopReceiver listens for these actions to start/stop this server
    // 有一个Receiver的存在用于监听下面的内容，用于启动和停止服务器
	public static final String ACTION_START_FTPSERVER = "org.mshare.ftp.server.ACTION_START_FTPSERVER";
    public static final String ACTION_STOP_FTPSERVER = "org.mshare.ftp.server.ACTION_STOP_FTPSERVER";
    
    // TODO 使用反射来获得WifiAp状态改变的广播
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    
    // server thread
    protected static Thread serverThread = null;
    protected boolean shouldExit = false;
    // 用于接收客户的socket
    protected ServerSocket listenSocket;

    // The server thread will check this often to look for incoming
    // connections. We are forced to use non-blocking accept() and polling
    // because we cannot wait forever in accept() if we want to be able
    // to receive an exit signal and cleanly exit.
    public static final int WAKE_INTERVAL_MS = 1000; // milliseconds

    private TcpListener wifiListener = null;
    // 所有客户连接
    private final List<SessionThread> sessionThreads = new ArrayList<SessionThread>();
    
    // wifi和唤醒锁
    private WakeLock wakeLock;
    private WifiLock wifiLock = null;
    
    /**
     * 当start被调用的时候，即尝试启动一个新的服务器线程
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldExit = false;
        int attempts = 10;
        // The previous server thread may still be cleaning up, wait for it to finish.
        // 用于等待上一个服务器关闭
        while (serverThread != null) {
            Log.w(TAG, "Won't start, server thread exists");
            if (attempts > 0) {
                attempts--;
                Util.sleepIgnoreInterupt(1000);
            } else {
                Log.w(TAG, "Server thread already exists");
                return START_STICKY;
            }
        }
        Log.d(TAG, "Creating server thread");
        serverThread = new Thread(this);
        serverThread.start();
        return START_STICKY;
    }

    /**
     * 判断server线程是否还活着
     * @return
     */
    public static boolean isRunning() {
        // return true if and only if a server Thread is running
        if (serverThread == null) {
            Log.d(TAG, "Server is not running (null serverThread)");
            return false;
        }
        if (!serverThread.isAlive()) {
            Log.d(TAG, "serverThread non-null but !isAlive()");
        } else {
            Log.d(TAG, "Server is alive");
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy() Stopping server");
        shouldExit = true;
        if (serverThread == null) {
            Log.w(TAG, "Stopping with null serverThread");
            return;
        }
        serverThread.interrupt();
        try {
            serverThread.join(10000); // wait 10 sec for server thread to finish
        } catch (InterruptedException e) {
        }
        if (serverThread.isAlive()) {
            Log.w(TAG, "Server thread failed to exit");
            // it may still exit eventually if we just leave the shouldExit flag set
        } else {
            Log.d(TAG, "serverThread join()ed ok");
            serverThread = null;
        }
        try {
            if (listenSocket != null) {
                Log.i(TAG, "Closing listenSocket");
                listenSocket.close();
            }
        } catch (IOException e) {
        }

        if (wifiLock != null) {
            Log.d(TAG, "onDestroy: Releasing wifi lock");
            wifiLock.release();
            wifiLock = null;
        }
        if (wakeLock != null) {
            Log.d(TAG, "onDestroy: Releasing wake lock");
            wakeLock.release();
            wakeLock = null;
        }
        Log.d(TAG, "FTPServerService.onDestroy() finished");
    }

    // This opens a listening socket on all interfaces.
    // 创建一个套接字，即创建服务器进程
    void setupListener() throws IOException {
        listenSocket = new ServerSocket();
        // 允许出于TIME_WAIT状态的Socket连接下一个内容
        listenSocket.setReuseAddress(true);
        // 将其绑定到特定的端口号上
        listenSocket.bind(new InetSocketAddress(FsSettings.getPort()));
    }

    /**
     * 启动服务器线程
     */
    public void run() {
        Log.d(TAG, "Server thread running");

        // 如果不是在local network的情形下，将无法启动FTP服务器
        if (isConnectedToLocalNetwork() == false) {
            Log.w(TAG, "run: There is no local network, bailing out");
            stopSelf();
            sendBroadcast(new Intent(ACTION_FAILEDTOSTART));
            return;
        }

        // Initialization of wifi, set up the socket
        // 初始化WIFI，并且创建套接字
        try {
            setupListener();
        } catch (IOException e) {
            Log.w(TAG, "run: Unable to open port, bailing out.");
            stopSelf();
            sendBroadcast(new Intent(ACTION_FAILEDTOSTART));
            return;
        }

        // @TODO: when using ethernet, is it needed to take wifi lock?
        // 获得Wifi和Wake锁
        takeWifiLock();
        takeWakeLock();

        // A socket is open now, so the FTP server is started, notify rest of world
        Log.i(TAG, "Ftp Server up and running, broadcasting ACTION_STARTED");
        // 告知服务启动
        sendBroadcast(new Intent(ACTION_STARTED));

        // shouldExit是退出标志
        while (!shouldExit) {
        	// 是一个Thread
            if (wifiListener != null) {
                if (!wifiListener.isAlive()) {
                    Log.d(TAG, "Joining crashed wifiListener thread");
                    // 到现在还是不大懂Thread.join();，是将一个Thread类的所有实例合并？
                    try {
                        wifiListener.join();
                    } catch (InterruptedException e) {
                    }
                    wifiListener = null;
                }
            }
            if (wifiListener == null) {
                // Either our wifi listener hasn't been created yet, or has crashed,
                // so spawn it
            	// 创建并启动线程
                wifiListener = new TcpListener(listenSocket, this);
                wifiListener.start();
            }
            try {
                // TODO: think about using ServerSocket, and just closing
                // the main socket to send an exit signal
                Thread.sleep(WAKE_INTERVAL_MS);
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread interrupted");
            }
        }

        terminateAllSessions();

        if (wifiListener != null) {
            wifiListener.quit();
            wifiListener = null;
        }
        shouldExit = false; // we handled the exit flag, so reset it to acknowledge
        Log.d(TAG, "Exiting cleanly, returning from run()");

        stopSelf();
        sendBroadcast(new Intent(ACTION_STOPPED));
    }
    /**
     * 停止所有会话
     */
    private void terminateAllSessions() {
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
     * Takes the wake lock
     * 
     * Many devices seem to not properly honor a PARTIAL_WAKE_LOCK, which should prevent
     * CPU throttling. For these devices, we have a option to force the phone into a full
     * wake lock.
     */
    private void takeWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (FsSettings.shouldTakeFullWakeLock()) {
                Log.d(TAG, "takeWakeLock: Taking full wake lock");
                wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
            } else {
                Log.d(TAG, "maybeTakeWakeLock: Taking parial wake lock");
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            }
            wakeLock.setReferenceCounted(false);
        }
        wakeLock.acquire();
    }

    /**
     * 获得一个WifiLock
     */
    private void takeWifiLock() {
        Log.d(TAG, "takeWifiLock: Taking wifi lock");
        if (wifiLock == null) {
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiLock = manager.createWifiLock(TAG);
            wifiLock.setReferenceCounted(false);
        }
        wifiLock.acquire();
    }

    /**
     * Gets the local ip address
     * 获得本地IP地址
     * @return local ip adress or null if not found
     */
    public static InetAddress getLocalInetAddress() {
    	// 需要检测是否当前连接的是WIFI网络，需要保证接入的内容是在同一个网络中
        if (isConnectedToLocalNetwork() == false) {
            Log.e(TAG, "getLocalInetAddress called and no connection");
            return null;
        }
        // TODO: next if block could probably be removed
        if (MShareUtil.isConnectedUsingWifi() == true) {
            Context context = MShareApp.getAppContext();
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // 获得WIFI环境下的IP地址
            int ipAddress = wm.getConnectionInfo().getIpAddress();
            if (ipAddress == 0)
                return null;
            return Util.intToInet(ipAddress);
        }
        // This next part should be able to get the local ip address, but in some case
        // I'm receiving the routable address
        try {
            Enumeration<NetworkInterface> netinterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (netinterfaces.hasMoreElements()) {
                NetworkInterface netinterface = netinterfaces.nextElement();
                Enumeration<InetAddress> adresses = netinterface.getInetAddresses();
                while (adresses.hasMoreElements()) {
                    InetAddress address = adresses.nextElement();
                    // this is the condition that sometimes gives problems
                    if (address.isLoopbackAddress() == false
                            && address.isLinkLocalAddress() == false)
                        return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks to see if we are connected to a local network, for instance wifi or ethernet
     * 检测当前是否是在一个局域网内。
     * @return true if connected to a local network
     */
    public static boolean isConnectedToLocalNetwork() {
        boolean connected = false;

        Context context = MShareApp.getAppContext();
        
        // 操作网络连接的管理器
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        // 检测 连接存在 && 连接上 && 类型是WIFI或者以太网
        connected = ni != null && ni.isConnected() == true
                && (ni.getType() & (ConnectivityManager.TYPE_WIFI | ConnectivityManager.TYPE_ETHERNET)) != 0;
        
        if (connected == false) {
            Log.d(TAG, "Device not connected to a network, see if it is an AP");
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // isWifiApEnabled是被hide annotation注释的内容，需要通过java反射机制绕过hide annotation
            try {
                Method method = wm.getClass().getDeclaredMethod("isWifiApEnabled");
                connected = (Boolean) method.invoke(wm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    /**
     * 检测当前WifiAp是否可用
     * @return
     */
    public static boolean isConnectedUsingWifiAp() {
    	boolean connect = false;
    	Context context = MShareApp.getAppContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // isWifiApEnabled是被隐藏的API
        try {
            Method method = wm.getClass().getDeclaredMethod("isWifiApEnabled");
            connect = (Boolean) method.invoke(wm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connect;
    }
    
    /**
     * All messages server<->client are also send to this call
     * 
     * @param incoming
     * @param s
     */
    public static void writeMonitor(boolean incoming, String s) {
    }

    /**
     * The FTPServerService must know about all running session threads so they can be
     * terminated on exit. Called when a new session is created.
     * 注册会话线程？
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
            sessionThreads.add(newSession);
        }
        Log.d(TAG, "Registered session thread");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 应该和Service的生命周期有关吧
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "user has removed my activity, we got killed! restarting...");
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 2000, restartServicePI);
    }

}
