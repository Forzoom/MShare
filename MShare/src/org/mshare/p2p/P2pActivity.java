package org.mshare.p2p;

import java.util.Collection;
import java.util.Iterator;

import org.mshare.main.ConnectInfo;
import org.mshare.main.R;
import org.mshare.nfc.NfcServerActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * ʹ��wifip2p��֪�Ƿ���Ҫ�û��ֶ�����wifip2p
 * 
 * ���������ϣ�wifip2p��wifi���ϲ���һ��wifip2pʹ��wifi�豸������Ҫʹ��wifip2p��Ҫ������wifi
 * 
 * ��Ҫ�����ڶ�ά��ɨ���У����Ӷ��ڵ�AP���ߵ�wifip2p peer
 * ʹ��wifip2p�Ѿ����������ˣ����޷�����
 * wifip2p����γ�������
 * 
 * Attention:
 * ��API�汾û�е�16(4.1)������£�stopDiscoverPeer�ǲ���ʹ�õģ�API14-15������£�ʹ��discover���ܻ���ɵ�������ģ���֪����API14-15������¸���ô�죿
 * ������ʱ���汾�޶���API16
 * 
 * TODO ��Ҫ����wifip2p�Ĵ����ٶ�
 * @author HM
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class P2pActivity extends Activity {
	private static final String TAG = P2pActivity.class.getSimpleName();
	
	private WifiP2pManager wpm;
	private Channel channel;
	private P2pReceiver pr;
	private IntentFilter filter;
	
	// ����Ǹ�ʲô�ģ�
	public static final String ACTION_ON_PEERS_AVAILABLE = "org.mshare.p2p.ON_PEERS_AVAILABLE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO ��ʾ��ǰ���豸������
		setContentView(R.layout.p2p);
		
		wpm = (WifiP2pManager)getSystemService(Service.WIFI_P2P_SERVICE);
		// TODO ��Ҫ���channel�Ƿ��ܹ�����ȷ�ػ�ã����ر�WiFi Direct��ʱ��channel�ܷ���ȷ�ػ��
		channel = wpm.initialize(this, getMainLooper(), null);

		Log.d(TAG, "get channel : " + channel);
		
		// ������ֹͣp2p����
		Button startButton = (Button)findViewById(R.id.start_discover);
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "discover start");
				wpm.discoverPeers(channel, new OnDiscoverPeersListener());
			}
		});
		
		// API�汾���������
		Button stopButton = (Button)findViewById(R.id.stop_discover);
		stopButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "discover stop");
				wpm.stopPeerDiscovery(channel, new OnStopDiscoverPeerListener());
			}
		});
		
		// �������ӺͶϿ����ٶ�
		// ��Ҫ��CONNECT_CHANGE�¼��������ж������Ƿ�ı�
		Button connectButton = (Button)findViewById(R.id.connect);
		connectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "try connect");
//				wpm.connect(channel, config, listener)
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		pr = new P2pReceiver(wpm, channel, this);
		
		filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		// TODO API�汾����
		filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		registerReceiver(pr, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(pr);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "NFC");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// ����NFC
			Intent startNfc = new Intent(P2pActivity.this, NfcServerActivity.class);
			// ��ʾ����������Ϊ��������Ϣ
			startNfc.putExtra(NfcServerActivity.EXTRA_MESSAGE_TYPE, NfcServerActivity.MESSAGE_SERVER_INFO);
			// TODO ��õ�ǰ��ConnectInfo
			ConnectInfo connectInfo = new ConnectInfo("192.168.0.1", "2121", "username", "password");
			startNfc.putExtra(NfcServerActivity.EXTRA_SERVER_INFO, connectInfo);
			
			startActivity(startNfc);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// ��֪��ʹ��TargetApi�Ƿ��
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void showPeers(WifiP2pDeviceList peers) {
		Collection<WifiP2pDevice> devices = peers.getDeviceList();
		Iterator<WifiP2pDevice> iterator = devices.iterator();
		int count = 0;
		
		while (iterator.hasNext()) {
			count++;
			WifiP2pDevice device = iterator.next();
			
			String address = device.deviceAddress;
			String name = device.deviceName;
			Log.v(TAG, "address " + address);
			Log.v(TAG, "name " + name);
		}

		Log.d(TAG, "will show " + count + " peers");
		wpm.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
			
			@Override
			public void onSuccess() {
				Log.d(TAG, "stop discover success");
			}
			
			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "stop discover fail");
			}
		});
	}
	
}
