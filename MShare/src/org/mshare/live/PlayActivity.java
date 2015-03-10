package org.mshare.live;

import java.io.File;
import java.io.FileInputStream;
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
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
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
public class PlayActivity extends Activity {
	private static final String TAG = PlayActivity.class.getSimpleName();
	
	private VideoView mVideoView;
	private TextView tvcache;
	// ��ӦԶ�̵�ַ��Զ�̵�ַ���Դ����ڸ�Activity��
	private File cacheFile;
	private File localFile;
	private ProgressDialog progressDialog = null;

	/**
	 * �������еĻ����ļ���·��
	 */
	public ArrayList<String> cachePaths = new ArrayList<String>();
	
	/**
	 * ��ǰ�����ļ����±�
	 */
	public int currentCacheIndex = 0;
	
	public int currentPlayIndex = 0;
	
	public int allCacheIndex = 0;
	
	/**
	 * ��Ӧ����1000KB
	 */
	private static final int READY_BUFF = 40 * 1000 * 1024;
	private static final int CACHE_BUFF = 500 * 1024;
	// 8mb
	private static final int CACHE_FILE_SIZE = 40 * 1024 * 1024;
	 
	private boolean isReady = false;
	// �жϵ�ǰ�Ƿ�����˲��Ŵ���
	private boolean isError = false;
	private int errorCnt = 0;
	private int curPosition = 0;
	// �ܹ����ļ���С
	private long mediaLength = 0;
	// ��ǰ�Ѿ���ȡ��size
	private long readSize = 0;
	
	private String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String cacheFileName = "cache";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ���õĲ����ļ�
		setContentView(R.layout.live_play);
		
		this.mVideoView = (VideoView) findViewById(R.id.video_view);
		this.tvcache = (TextView) findViewById(R.id.cache_info);
		Button button = (Button)findViewById(R.id.cache_clear);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (int i = 0, len = cachePaths.size(); i < len; i++) {
					File file = new File(cachePaths.get(i));
					if (file.exists()) {
						file.delete();
					}
				}
			}
		});

		// TODO �������жϲ���ã��Ͼ�Activity�Ѿ�������
		if (init()) {
			playVideo();
		}
	}
	
	/**
	 * ��ʼ����Ҫ���ŵ�����
	 * @return ���ؿ��Բ��ŵ�Uri
	 */
	private boolean init() {
		
		// TODO ����localFile,��ʱд��1.mp4
		localFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "1.mp4");
		if (!localFile.exists()) {
			Log.e(TAG, "local file is not exist");
			return false;
		}
		
		// �����ܹ�����ֵ
		mediaLength = localFile.length();
		// û�����ݣ�ֱ���˳�
		if (mediaLength == 0) {
			Log.e(TAG, "local file is empty");
			return false;
		}
		
		// ͳ����Ҫ�ֳɶ��ٸ�Ƭ��
		allCacheIndex = (int)(localFile.length() / CACHE_FILE_SIZE) + 1;
		Log.d(TAG, "will be split in " + allCacheIndex + " fragment");
		
		// �����ļ��Ĵ��������playVideo�У������Զ�������ɾ��
		for (int i = 0; i < allCacheIndex; i++) {
			cachePaths.add(externalStoragePath + File.separator + cacheFileName + i + ".mp4");
		}
		
		// ����controller
		mVideoView.setMediaController(new MediaController(this));
		
		// ����VideoView�Ļص�����
		// �����Զ��ж�prepared��
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
	 
			public void onPrepared(MediaPlayer mediaplayer) {
				dismissProgressDialog();
				mVideoView.seekTo(curPosition);
				mediaplayer.start();
			}
		});

		// �ж���Ƶ�����һ������ŵ�
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
	 
			public void onCompletion(MediaPlayer mediaplayer) {
				if (currentPlayIndex <= allCacheIndex) {
					// ��������Ƭ��
					currentPlayIndex++;
					mVideoView.setVideoPath(cachePaths.get(currentPlayIndex));
					mVideoView.start();
				} else {
					curPosition = 0;
					mVideoView.pause();
				}
			}
		});
		
		// ���ó�������µķ�Ӧ
		mVideoView.setOnErrorListener(new OnErrorListener() {
	 
			public boolean onError(MediaPlayer mediaplayer, int i, int j) {
				isError = true;
				errorCnt++;
				mVideoView.pause();
				showProgressDialog();
				// ���������Ѿ�����������true�Ų��ᵯ������������û�
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * ��ʾ�ȴ�����
	 */
	private void showProgressDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
//				if (progressDialog == null) {
//					progressDialog = ProgressDialog.show(PlayActivity.this,
//							"��Ƶ����", "����Ŭ�������� ...", true, false);
//				}
			}
		});
	}
	/**
	 * ����ʾ
	 */
	private void dismissProgressDialog() {
		mHandler.post(new Runnable() {
	 
			@Override
			public void run() {
//				if (progressDialog != null) {
//					progressDialog.dismiss();
//					progressDialog = null;
//				}
			}
		});
	}
	
	/**
	 * ���ڲ�����Ƶ
	 * TODO ��Ҫ�����ַ
	 */
	private void playVideo() {

		if (localFile == null) {
			Log.e(TAG, "localFile is null");
			return;
		}
		
		showProgressDialog();
		// �µ��߳����ڼ��ػ�������
		new CacheThread().start();
		// TODO ��Ҫ������ķ�ʽ
	}

	class CacheThread extends Thread {

		@Override
		public void run() {
			super.run();
			FileOutputStream out = null;
			InputStream is = null;
 
			try {
				
				// ��ǰ�����ļ��Ĵ�С
				readSize = getReadSize();
				Log.d(TAG, "current read size : " + readSize);

				// ��FTP�������ܹ�ʵ�ֶϵ���������������ͨ���ļ��У�ֻ����FileInputStream?����RandomAccessFile
				// TODO ��Ҫ����is,ʹ��FileInputStream���ᵼ��ÿ�δ�ͷ��ʼ����RandomAccess���ܱȽϺ�
				is = new FileInputStream(localFile);

				// ���û���Ϊ16k
				byte buf[] = new byte[4 * 1024];
				int size = 0;
				long lastReadSize = 0;
 
				mHandler.sendEmptyMessage(VIDEO_STATE_UPDATE);
				// ��HTTP��ȡ����
				while ((size = is.read(buf)) != -1) {

					// ����cacheFile
					if (cacheFile == null) {
						cacheFile = new File(cachePaths.get(currentCacheIndex));
						out = new FileOutputStream(cacheFile, true);
					}
					
					try {
						out.write(buf, 0, size);
						readSize += size;
					} catch (Exception e) {
						e.printStackTrace();
					}
 
					// �������жϲ�����
					// �л�����һ�������ļ�
					if (cacheFile.length() == CACHE_FILE_SIZE) {
						// ������һ��cacheFile
						cacheFile = new File(cachePaths.get(++currentCacheIndex));
						// �����ļ������ڱ��滺������
						if (!cacheFile.exists()) {
							cacheFile.createNewFile();
						}
						
						if (out != null) {
							out.flush();
							out.close();
							out = null;
						}
						out = new FileOutputStream(cacheFile, true);
					}
					
					// ���������Ҫ���������Ƭ�Σ���ô����Ϊʲô��isReady��?������isError��
					if (!isReady) {
						if ((readSize - lastReadSize) > READY_BUFF) {
							lastReadSize = readSize;
							mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
						}
					} else {
						// ���ﲻ֪����ʲô��˼
						// errorCnt��Ϊ�����������Ƭ��?
						if ((readSize - lastReadSize) > CACHE_BUFF
								* (errorCnt + 1)) {
							lastReadSize = readSize;
							mHandler.sendEmptyMessage(CACHE_VIDEO_UPDATE);
						}
					}

				}
 
				mHandler.sendEmptyMessage(CACHE_VIDEO_END);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						//
					}
				}
 
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						//
					}
				}
			}
 
		}
		
	}
	
	/**
	 * ���㵱ǰ�Ѿ�д�˶��ٵĻ�������
	 * @return
	 */
	private long getReadSize() {
		return (long)allCacheIndex + new File(cachePaths.get(currentCacheIndex)).length();
	}
	
	private final static int VIDEO_STATE_UPDATE = 0;
	private final static int CACHE_VIDEO_READY = 1;
	// ������ǰ����ʾ
	private final static int CACHE_VIDEO_UPDATE = 2;
	private final static int CACHE_VIDEO_END = 3;
	 
	private final Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VIDEO_STATE_UPDATE:
				double cachePercent = readSize * 100.00 / mediaLength * 1.0;
				String s = String.format("�ѻ���: [%.2f%%]", cachePercent);
	 
				if (mVideoView.isPlaying()) {
					curPosition = mVideoView.getCurrentPosition();
					int duration = mVideoView.getDuration();
					duration = duration == 0 ? 1 : duration;
	 
					double playPercent = curPosition * 100.00 / duration * 1.0;
	 
					int i = curPosition / 1000;
					int hour = i / (60 * 60);
					int minute = i / 60 % 60;
					int second = i % 60;
	 
					s += String.format(" ����: %02d:%02d:%02d [%.2f%%]", hour,
							minute, second, playPercent);
				}
	 
				tvcache.setText(s);
	 
				mHandler.sendEmptyMessageDelayed(VIDEO_STATE_UPDATE, 1000);
				break;
	 
			case CACHE_VIDEO_READY:
				isReady = true;
				// TODO Ϊʲô���������ã�
				if (cacheFile != null) {
					mVideoView.setVideoPath(cacheFile.getAbsolutePath());
					mVideoView.start();
				}
				break;
	 
			case CACHE_VIDEO_UPDATE:
				if (isError && cacheFile != null) {
					mVideoView.setVideoPath(cacheFile.getAbsolutePath());
					mVideoView.start();
					isError = false;
				}
				break;
	 
			case CACHE_VIDEO_END:
				if (isError && cacheFile != null) {
					mVideoView.setVideoPath(cacheFile.getAbsolutePath());
					mVideoView.start();
					isError = false;
				}
				break;
			}
	 
			super.handleMessage(msg);
		}
	};
}