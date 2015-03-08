package org.mshare.main;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * ��װ�������ӵĲ�������Ҫ��װ�Ĳ�������host,port,username,password
 * ���ܻ���Ҫ��������Ϣ������ap��ssid��p2p������name�ȵ�
 * 
 * Serial��Parcel��̫���鷳��,���������ʹ��
 * @author HM
 *
 */
public class ConnectInfo implements Parcelable {
	private static final String TAG = ConnectInfo.class.getSimpleName();

	private String host;
	private String port;
	private String username;
	private String password;
	
	public static final int PARCELABLE_CONTENT_CONNECT_INFO = 1111;
	
	public ConnectInfo() {}
	
	public ConnectInfo(String host, String port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * ����info�����һ��ConnectInfo����һ�����ڿͻ��˽��ܵ��������ݺ����
	 * @param info
	 * @return ����ʧ�ܷ���null
	 */
	public static ConnectInfo parse(String info) {
		Log.d(TAG, "Parse string : " + info);
		String[] infos = info.split(" ");
		Log.d(TAG, "Split result count : " + infos.length);
		if (infos.length == 4) {
			return new ConnectInfo(infos[0], infos[1], infos[2], infos[3]);
		}
		return null;
	}
	
	/**
	 * ת�����ı����д���
	 * @return
	 */
	public static String stringify(ConnectInfo connectInfo) {
		String result = connectInfo.host + " " + connectInfo.port + " "
				+ connectInfo.username + " " + connectInfo.password;
		return result;
	}
	
	/**
	 * ͬ��ʹ��ConnectInfo.stringify
	 */
	@Override
	public String toString() {
		return ConnectInfo.stringify(this);
	}
	
	public static final Parcelable.Creator<ConnectInfo> CREATOR = new Creator<ConnectInfo>() {

		@Override
		public ConnectInfo createFromParcel(Parcel source) {
			ConnectInfo connectInfo = new ConnectInfo();
			connectInfo.host = source.readString();
			connectInfo.port = source.readString();
			connectInfo.username = source.readString();
			connectInfo.password = source.readString();
			return connectInfo;
		}

		@Override
		public ConnectInfo[] newArray(int size) {
			return new ConnectInfo[size];
		}
		
	};
	
	@Override
	public int describeContents() {
		return PARCELABLE_CONTENT_CONNECT_INFO;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(host);
		dest.writeString(port);
		dest.writeString(username);
		dest.writeString(password);
	}
}
