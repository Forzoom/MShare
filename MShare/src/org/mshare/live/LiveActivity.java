package org.mshare.live;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.mshare.main.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * ����Ϊ�ܹ��鿴�����ļ����Լ������ļ�
 * 
 * TODO ��˵�ܹ�����ͷ����Ϣ���Ӷ����ｫ��Ƶ�ļ���Ƭ���������ô��Ƶ��ͷ����Ϣ�������������?
 * TODO �����õ�Header����MediaPlayer��ʹ�ã����Ի�����Ҫ�鿴Guide->Playback�е����ݺ�MediaPlayer�е�Դ�룿
 * 
 * TODO ���Ǹ���η�Ƭ�α��� 
 * @author HM
 *
 */
public class LiveActivity extends Activity {
	private static final String TAG = LiveActivity.class.getSimpleName();
	
	private VideoView mVideoView;
	// ��ӦԶ�̵�ַ��Զ�̵�ַ���Դ����ڸ�Activity��
	private File localFile;

	private boolean isReady = false;
	// �жϵ�ǰ�Ƿ�����˲��Ŵ���
	private boolean isError = false;
	private int errorCnt = 0;
	private int curPosition = 0;
	
	private String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String cacheFileName = "cache";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ���õĲ����ļ�
		setContentView(R.layout.live_play);
		
		this.mVideoView = (VideoView) findViewById(R.id.video_view);

		init();
		// TODO �������жϲ���ã��Ͼ�Activity�Ѿ�������
		String playPath = externalStoragePath + File.separator + "1.mp4";
		File playFile = new File(playPath);
		if (!playFile.exists()) {
			Log.e(TAG, "play file is not exist");
		}
		
		FileOutputStream fos = null;
		
		try {
			fos = new FileOutputStream(playFile, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mVideoView.setVideoPath(playPath);
		mVideoView.start();
	}
	
	/**
	 * ��ʼ����Ҫ���ŵ�����
	 * @return ���ؿ��Բ��ŵ�Uri
	 */
	private boolean init() {
		
		// ����controller
		mVideoView.setMediaController(new MediaController(this));
		
		// ����VideoView�Ļص�����
		// �����Զ��ж�prepared��
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
	 
			public void onPrepared(MediaPlayer mediaplayer) {
//				dismissProgressDialog();
				mVideoView.seekTo(curPosition);
				mediaplayer.start();
			}
		});

		// �ж���Ƶ�����һ������ŵ�
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
	 
			public void onCompletion(MediaPlayer mediaplayer) {
				curPosition = 0;
				mVideoView.pause();
			}
		});
		
		// ���ó�������µķ�Ӧ
		mVideoView.setOnErrorListener(new OnErrorListener() {
	 
			public boolean onError(MediaPlayer mediaplayer, int i, int j) {
				isError = true;
				errorCnt++;
				mVideoView.pause();
//				showProgressDialog();
				// ���������Ѿ�����������true�Ų��ᵯ������������û�
				return true;
			}
		});
		
		return true;
	}

}