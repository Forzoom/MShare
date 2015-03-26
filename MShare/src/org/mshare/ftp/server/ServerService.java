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
 * TODO ����Session�����������仯��ʱ������ܹ�֪ͨ�ͺ��ˣ��Ա�֤��ǰ��Session��������
 * TODO �ؼ����������֪ͨclient���ļ���Ҫ���£���FsService��ʹ��֪ͨ�Ƿ����
 * 
 * ά��AccountFactory��AccountFactory��Ӧ������֮���һֱ����
 * @author HM
 *
 */
public class ServerService extends Service implements Runnable {
    private static final String TAG = ServerService.class.getSimpleName();

    // Service will (global) broadcast when server start/stop
    // ��FTP��������������ֹͣ��ʱ�򣬻�㲥
    public static final String ACTION_STARTED = "org.mshare.ftp.server.FTPSERVER_STARTED";
    public static final String ACTION_STOPPED = "org.mshare.ftp.server.FTPSERVER_STOPPED";
    public static final String ACTION_FAILEDTOSTART = "org.mshare.ftp.server.FTPSERVER_FAILEDTOSTART";

    // RequestStartStopReceiver listens for these actions to start/stop this server
    // ��һ��Receiver�Ĵ������ڼ�����������ݣ�����������ֹͣ������
	public static final String ACTION_START_FTPSERVER = "org.mshare.ftp.server.ACTION_START_FTPSERVER";
    public static final String ACTION_STOP_FTPSERVER = "org.mshare.ftp.server.ACTION_STOP_FTPSERVER";
    
    // server thread
    protected static Thread serverThread = null;
    protected boolean shouldExit = false;
    // ���ڽ��տͻ��˵�socket
    protected ServerSocket listenSocket;

    /* RTSP������ */
    private RtspServer rtspServer;

    // The server thread will check this often to look for incoming
    // connections. We are forced to use non-blocking accept() and polling
    // because we cannot wait forever in accept() if we want to be able
    // to receive an exit signal and cleanly exit.
    public static final int WAKE_INTERVAL_MS = 1000; // milliseconds

    /**
     * TODO Ϊʲô���ֽ����?
     */
    private TcpListener wifiListener = null;
    
    // wifi��wake��
    private WakeLock wakeLock;
    private WifiLock wifiLock = null;
    
    // ����Session�Ŀ�����
    private SessionController sessionController;
    // ����֪ͨ�߳�����Ϣ
    private SessionNotifier sessionNotifier;
    
    /**
     * ��start�����õ�ʱ�򣬼���������һ���µķ������߳�
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldExit = false;
        int attempts = 10;
        // The previous server thread may still be cleaning up, wait for it to finish.
        // �ȴ���һ���������ر�
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

        // ��ʱContext
        Context context = MShareApp.getAppContext();
        
        // ����nickname
        SharedPreferences defaultSp = PreferenceManager.getDefaultSharedPreferences(context);
        String nickName = defaultSp.getString(FtpSettings.KEY_NICKNAME, FtpSettings.VALUE_NICKNAME_DEFAULT);
        if (nickName.equals("")) {// ��ǰnickName����Ĭ�ϵ�""
        	Editor editor = defaultSp.edit();
        	// �޸�Ϊ�豸����
        	editor.putString(FtpSettings.KEY_NICKNAME, Build.MODEL);
        	editor.commit();
        }
        
        // ����uuid
        if (FtpSettings.getUUID().equals(FtpSettings.VALUE_UUID_DEFAULT)) {
        	String uuid = UUID.randomUUID().toString();
        	FtpSettings.setUUID(uuid);
        }
        
        // ����SessionController������SessionNotifier
        sessionController = new SessionController();
        sessionNotifier = new SessionNotifier(sessionController);
        
        AccountFactory.getInstance().bindSessionNotifier(sessionNotifier);
        // ������֤��
        sessionController.setVerifier(AccountFactory.getInstance().getVerifier());
        
        // �����������߳�
        Log.d(TAG, "Creating server thread");
        serverThread = new Thread(this);
        serverThread.start();

        // ��Ƶ��������
        RtspConstants.VideoEncoder rtspVideoEncoder = (MediaConstants.H264_CODEC == true) ? RtspConstants.VideoEncoder.H264_ENCODER
                : RtspConstants.VideoEncoder.H263_ENCODER;


        // ����rtsp������
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
     * �ж�server�߳��Ƿ񻹻���
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

        // ֹͣrtsp������
        rtspServer.stop();
        Log.d(TAG, "FTPServerService.onDestroy() finished");
    }

    // This opens a listening socket on all interfaces.
    // ����һ���׽��֣�����������������
    void setupListener() throws IOException {
        listenSocket = new ServerSocket();
        // ��������TIME_WAIT״̬��Socket������һ������
        listenSocket.setReuseAddress(true);
        // ����󶨵��ض��Ķ˿ں���
        listenSocket.bind(new InetSocketAddress(FtpSettings.getPort()));
    }

    /**
     * �����������߳�
     */
    public void run() {
        Log.d(TAG, "Server thread running");

        // TODO ��⵱ǰ������״̬�����ݵ�ǰ������״̬��ȷ���Ƿ�Ӧ������������
        // ֻ�е�����״̬��WIFI�������Լ�������AP��ʱ��ſ�������������
        // ͬʱע�⵱3G������ʱ����Ҫ���ѿ��ܻ��������
        
        // ���������local network�������£����޷�����FTP������
        if (isConnectedToLocalNetwork() == false) {
            Log.w(TAG, "run: There is no local network, bailing out");
            stopSelf();
            sendBroadcast(new Intent(ACTION_FAILEDTOSTART));
            return;
        }

        // Initialization of wifi, set up the socket
        // ��ʼ��WIFI�����Ҵ����׽���
        try {
            setupListener();
        } catch (IOException e) {
            Log.w(TAG, "run: Unable to open port, bailing out.");
            stopSelf();
            sendBroadcast(new Intent(ACTION_FAILEDTOSTART));
            return;
        }

        // @TODO: when using ethernet, is it needed to take wifi lock?
        // ���Wifi��Wake��
        takeWifiLock();
        takeWakeLock();

        // A socket is open now, so the FTP server is started, notify rest of world
        Log.i(TAG, "Ftp Server up and running, broadcasting ACTION_STARTED");
        // ��֪��������
        sendBroadcast(new Intent(ACTION_STARTED));

        // shouldExit���˳���־
        while (!shouldExit) {
        	// ��һ��Thread
            if (wifiListener != null) {
                if (!wifiListener.isAlive()) {
                    Log.d(TAG, "Joining crashed wifiListener thread");
                    // �����ڻ��ǲ���Thread.join();���ǽ�һ��Thread�������ʵ���ϲ���
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
            	// �����������߳�
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
     * ���һ��WifiLock
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
     * ��ñ���IP��ַ
     * @return local ip adress or null if not found
     */
    public static InetAddress getLocalInetAddress() {
    	// ��Ҫ����Ƿ�ǰ���ӵ���WIFI���磬��Ҫ��֤�������������ͬһ��������
        if (isConnectedToLocalNetwork() == false) {
            Log.e(TAG, "getLocalInetAddress called and no connection");
            return null;
        }
        // TODO: next if block could probably be removed
        if (MShareUtil.isConnectedUsing(MShareUtil.WIFI) == true) {
            Context context = MShareApp.getAppContext();
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // ���WIFI�����µ�IP��ַ
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
     * �жϵ�ǰ�����绷���Ƿ���Կ���������
     * @return
     */
    public static boolean isNetworkAccess() {
    	// WIFI����WIFIP2P����AP�����������������
    	
    	return false;
    }
    
    /**
     * Checks to see if we are connected to a local network, for instance wifi or ethernet
     * ��⵱ǰ�Ƿ�����һ���������ڡ�
     * @return true if connected to a local network
     */
    public static boolean isConnectedToLocalNetwork() {
        boolean connected = false;

        Context context = MShareApp.getAppContext();
        
        // �����������ӵĹ�����
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        // ��� ���Ӵ��� && ������ && ������WIFI������̫��
        connected = ni != null && ni.isConnected() == true
                && (ni.getType() & (ConnectivityManager.TYPE_WIFI | ConnectivityManager.TYPE_ETHERNET)) != 0;
        
        if (connected == false) {
            Log.d(TAG, "Device not connected to a network, see if it is an AP");
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // isWifiApEnabled�Ǳ�hide annotationע�͵����ݣ���Ҫͨ��java��������ƹ�hide annotation
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
     * ��⵱ǰWifiAp�Ƿ����
     * TODO �Ƿ����ʹ��StatusController���ж�
     * @return
     */
    public static boolean isConnectedUsingWifiAp() {
    	boolean connect = false;
    	Context context = MShareApp.getAppContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // isWifiApEnabled�Ǳ����ص�API
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
     * ��֪���Ǹ�ʲô��
     * 
     * @param incoming
     * @param s
     */
    public static void writeMonitor(boolean incoming, String s) {
    }

    // �ж�ָ�����ļ��Ƿ��ǹ����ļ�
    public static boolean isFileShared(File file) {
    	return AccountFactory.getInstance().isFileShared(file);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * ����֧�ֵͰ汾��������ʹ�õĹ����У�����û��ʲô�ô���
     * Ӧ�ú�Service�����������йذ�
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
     * ��ù���Ա�˻���Ӧ��Token
     * @return
     */
    public static Token getAdminToken() {
    	return AccountFactory.getInstance().getAdminAccountToken();
    }

    /**
     * ��ʱ��ʹ�õ�����ע��session�ĺ�������֪���Ժ��費��Ҫ
     * @param newSession
     */
    public void registerSessionThread(SessionThread newSession) {
    	sessionController.registerSessionThread(newSession);
    }
    
}