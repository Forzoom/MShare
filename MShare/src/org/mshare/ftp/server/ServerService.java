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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.UUID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import org.mshare.account.AccountFactory;
import org.mshare.account.AccountFactory.Token;
import org.mshare.main.MShareApp;
import org.mshare.main.MShareUtil;

import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.server.RtspServer;
import de.kp.rtspcamera.MediaConstants;

/**
 * TODO 当有Session的数量发生变化的时候，如果能够通知就好了，以保证当前的Session数量合适
 * TODO 关键是现在如何通知client有文件需要更新，在FsService中使用通知是否合适
 * 
 * 维护AccountFactory，AccountFactory在应用启动之后就一直存在
 * @author HM
 *
 */
public class ServerService extends Service implements Runnable {
    private static final String TAG = ServerService.class.getSimpleName();

    // Service will (global) broadcast when server start/stop
    // 当FTP服务器启动或者停止的时候，会广播
    public static final String ACTION_STARTED = "org.mshare.ftp.server.FTPSERVER_STARTED";
    public static final String ACTION_STOPPED = "org.mshare.ftp.server.FTPSERVER_STOPPED";
    public static final String ACTION_FAILEDTOSTART = "org.mshare.ftp.server.FTPSERVER_FAILEDTOSTART";

    // RequestStartStopReceiver listens for these actions to start/stop this server
    // 有一个Receiver的存在用于监听下面的内容，用于启动和停止服务器
	public static final String ACTION_START_FTPSERVER = "org.mshare.ftp.server.ACTION_START_FTPSERVER";
    public static final String ACTION_STOP_FTPSERVER = "org.mshare.ftp.server.ACTION_STOP_FTPSERVER";
    
    // server thread
    protected static Thread serverThread = null;
    protected boolean shouldExit = false;
    // 用于接收客户端的socket
    protected ServerSocket listenSocket;

    /* RTSP服务器 */
    private RtspServer rtspServer;

    // The server thread will check this often to look for incoming
    // connections. We are forced to use non-blocking accept() and polling
    // because we cannot wait forever in accept() if we want to be able
    // to receive an exit signal and cleanly exit.
    public static final int WAKE_INTERVAL_MS = 1000; // milliseconds

    /**
     * TODO 为什么名字叫这个?
     */
    private TcpListener wifiListener = null;
    
    // wifi和wake锁
    private WakeLock wakeLock;
    private WifiLock wifiLock = null;
    
    // 管理Session的控制器
    private SessionController sessionController;
    // 用于通知线程新消息
    private SessionNotifier sessionNotifier;
    
    /**
     * 当start被调用的时候，即尝试启动一个新的服务器线程
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldExit = false;
        int attempts = 10;
        // The previous server thread may still be cleaning up, wait for it to finish.
        // 等待上一个服务器关闭
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

        // 临时Context
        Context context = MShareApp.getAppContext();
        
        // 创建nickname
        SharedPreferences defaultSp = PreferenceManager.getDefaultSharedPreferences(context);
        String nickName = defaultSp.getString(FtpSettings.KEY_NICKNAME, FtpSettings.VALUE_NICKNAME_DEFAULT);
        if (nickName.equals("")) {// 当前nickName仍是默认的""
        	Editor editor = defaultSp.edit();
        	// 修改为设备名称
        	editor.putString(FtpSettings.KEY_NICKNAME, Build.MODEL);
        	editor.commit();
        }
        
        // 创建uuid
        if (FtpSettings.getUUID().equals(FtpSettings.VALUE_UUID_DEFAULT)) {
        	String uuid = UUID.randomUUID().toString();
        	FtpSettings.setUUID(uuid);
        }
        
        // 创建SessionController，并绑定SessionNotifier
        sessionController = new SessionController();
        sessionNotifier = new SessionNotifier(sessionController);
        
        AccountFactory.getInstance().bindSessionNotifier(sessionNotifier);
        // 设置验证器
        sessionController.setVerifier(AccountFactory.getInstance().getVerifier());
        
        // 启动服务器线程
        Log.d(TAG, "Creating server thread");
        serverThread = new Thread(this);
        serverThread.start();

        // 视频编码器？
        RtspConstants.VideoEncoder rtspVideoEncoder = (MediaConstants.H264_CODEC == true) ? RtspConstants.VideoEncoder.H264_ENCODER
                : RtspConstants.VideoEncoder.H263_ENCODER;


        // 启动rtsp服务器
        try {
            if (rtspServer == null) {
                rtspServer = new RtspServer(5544, rtspVideoEncoder);
            }
            new Thread(rtspServer).start();
        } catch (IOException e) {
            Log.e(TAG, "something wrong happen! start rtsp server failed");
            e.printStackTrace();
        }

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
        
        AccountFactory.getInstance().releaseSessionNotifier();

        // 停止rtsp服务器
        rtspServer.stop();
        Log.d(TAG, "FTPServerService.onDestroy() finished");
    }

    // This opens a listening socket on all interfaces.
    // 创建一个套接字，即创建服务器进程
    void setupListener() throws IOException {
        listenSocket = new ServerSocket();
        // 允许出于TIME_WAIT状态的Socket连接下一个内容
        listenSocket.setReuseAddress(true);
        // 将其绑定到特定的端口号上
        listenSocket.bind(new InetSocketAddress(FtpSettings.getPort()));
    }

    /**
     * 启动服务器线程
     */
    public void run() {
        Log.d(TAG, "Server thread running");

        // TODO 检测当前的网络状态，根据当前的网络状态来确定是否应当开启服务器
        // 只有当网络状态是WIFI或者是自己启动的AP的时候才可以启动服务器
        // 同时注意当3G启动的时候需要提醒可能会产生流量
        
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

        sessionController.terminateAllSessions();

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
     * Takes the wake lock
     * 
     * Many devices seem to not properly honor a PARTIAL_WAKE_LOCK, which should prevent
     * CPU throttling. For these devices, we have a option to force the phone into a full
     * wake lock.
     */
    private void takeWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (FtpSettings.shouldTakeFullWakeLock()) {
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
        if (MShareUtil.isConnectedUsing(MShareUtil.WIFI) == true) {
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
     * 判断当前的网络环境是否可以开启服务器
     * @return
     */
    public static boolean isNetworkAccess() {
    	// WIFI或者WIFIP2P或者AP情况下允许开启网络
    	
    	return false;
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
     * TODO 是否可以使用StatusController来判断
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
     * 不知道是干什么的
     * 
     * @param incoming
     * @param s
     */
    public static void writeMonitor(boolean incoming, String s) {
    }

    // 判断指定的文件是否是共享文件
    public static boolean isFileShared(File file) {
    	return AccountFactory.getInstance().isFileShared(file);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 并不支持低版本，但是在使用的过程中，好像并没有什么用处，
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

    /**
     * 获得管理员账户对应的Token
     * @return
     */
    public static Token getAdminToken() {
    	return AccountFactory.getInstance().getAdminAccountToken();
    }

    /**
     * 临时所使用的用于注册session的函数，不知道以后还需不需要
     * @param newSession
     */
    public void registerSessionThread(SessionThread newSession) {
    	sessionController.registerSessionThread(newSession);
    }
    
}
