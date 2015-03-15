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
 * 修正为能够查看本地文件，以及缓存文件
 * 
 * TODO 据说能够加载头部信息，从而到达将视频文件分片的情况，那么视频的头部信息该在哪里加载呢?
 * TODO 所设置的Header是在MediaPlayer中使用，所以还是需要查看Guide->Playback中的内容和MediaPlayer中的源码？
 * 
 * TODO 考虑该如何分片段保存 
 * @author HM
 *
 */
public class PlayActivity extends Activity {
	private static final String TAG = PlayActivity.class.getSimpleName();
	
	private VideoView mVideoView;
	private TextView tvcache;
	// 对应远程地址，远程地址可以存在在该Activity中
	private File cacheFile;
	private File localFile;
	private ProgressDialog progressDialog = null;

	/**
	 * 保存所有的缓存文件的路径
	 */
	public ArrayList<String> cachePaths = new ArrayList<String>();
	
	/**
	 * 当前缓存文件的下标
	 */
	public int currentCacheIndex = 0;
	
	public int currentPlayIndex = 0;
	
	public int allCacheIndex = 0;
	
	/**
	 * 对应的是1000KB
	 */
	private static final int READY_BUFF = 40 * 1000 * 1024;
	private static final int CACHE_BUFF = 500 * 1024;
	// 8mb
	private static final int CACHE_FILE_SIZE = 40 * 1024 * 1024;
	 
	private boolean isReady = false;
	// 判断当前是否出现了播放错误
	private boolean isError = false;
	private int errorCnt = 0;
	private int curPosition = 0;
	// 总共的文件大小
	private long mediaLength = 0;
	// 当前已经读取的size
	private long readSize = 0;
	
	private String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String cacheFileName = "cache";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置的布局文件
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

		// TODO 在这里判断不大好，毕竟Activity已经启动了
		if (init()) {
			playVideo();
		}
	}
	
	/**
	 * 初始化需要播放的内容
	 * @return 返回可以播放的Uri
	 */
	private boolean init() {
		
		// TODO 处理localFile,暂时写死1.mp4
		localFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "1.mp4");
		if (!localFile.exists()) {
			Log.e(TAG, "local file is not exist");
			return false;
		}
		
		// 设置总共的数值
		mediaLength = localFile.length();
		// 没有内容，直接退出
		if (mediaLength == 0) {
			Log.e(TAG, "local file is empty");
			return false;
		}
		
		// 统计需要分成多少个片段
		allCacheIndex = (int)(localFile.length() / CACHE_FILE_SIZE) + 1;
		Log.d(TAG, "will be split in " + allCacheIndex + " fragment");
		
		// 缓存文件的处理放置在playVideo中，让其自动创建和删除
		for (int i = 0; i < allCacheIndex; i++) {
			cachePaths.add(externalStoragePath + File.separator + cacheFileName + i + ".mp4");
		}
		
		// 设置controller
		mVideoView.setMediaController(new MediaController(this));
		
		// 设置VideoView的回调内容
		// 可以自动判断prepared？
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
	 
			public void onPrepared(MediaPlayer mediaplayer) {
				dismissProgressDialog();
				mVideoView.seekTo(curPosition);
				mediaplayer.start();
			}
		});

		// 判断视频结束我还是相信的
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
	 
			public void onCompletion(MediaPlayer mediaplayer) {
				if (currentPlayIndex <= allCacheIndex) {
					// 更换播放片段
					currentPlayIndex++;
					mVideoView.setVideoPath(cachePaths.get(currentPlayIndex));
					mVideoView.start();
				} else {
					curPosition = 0;
					mVideoView.pause();
				}
			}
		});
		
		// 设置出错情况下的反应
		mVideoView.setOnErrorListener(new OnErrorListener() {
	 
			public boolean onError(MediaPlayer mediaplayer, int i, int j) {
				isError = true;
				errorCnt++;
				mVideoView.pause();
				showProgressDialog();
				// 表明错误已经被处理，返回true才不会弹出错误框提醒用户
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 显示等待加载
	 */
	private void showProgressDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
//				if (progressDialog == null) {
//					progressDialog = ProgressDialog.show(PlayActivity.this,
//							"视频缓存", "正在努力加载中 ...", true, false);
//				}
			}
		});
	}
	/**
	 * 不显示
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
	 * 用于播放视频
	 * TODO 需要缓存地址
	 */
	private void playVideo() {

		if (localFile == null) {
			Log.e(TAG, "localFile is null");
			return;
		}
		
		showProgressDialog();
		// 新的线程用于加载缓存内容
		new CacheThread().start();
		// TODO 需要更优秀的方式
	}

	class CacheThread extends Thread {

		@Override
		public void run() {
			super.run();
			FileOutputStream out = null;
			InputStream is = null;
 
			try {
				
				// 当前缓存文件的大小
				readSize = getReadSize();
				Log.d(TAG, "current read size : " + readSize);

				// 从FTP服务器能够实现断点续传，但是在普通的文件中，只能是FileInputStream?还是RandomAccessFile
				// TODO 需要设置is,使用FileInputStream将会导致每次从头开始，而RandomAccess可能比较好
				is = new FileInputStream(localFile);

				// 设置缓存为16k
				byte buf[] = new byte[4 * 1024];
				int size = 0;
				long lastReadSize = 0;
 
				mHandler.sendEmptyMessage(VIDEO_STATE_UPDATE);
				// 从HTTP读取内容
				while ((size = is.read(buf)) != -1) {

					// 创建cacheFile
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
 
					// 这样的判断并不好
					// 切换到下一个缓存文件
					if (cacheFile.length() == CACHE_FILE_SIZE) {
						// 换成下一个cacheFile
						cacheFile = new File(cachePaths.get(++currentCacheIndex));
						// 创建文件，用于保存缓存内容
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
					
					// 如果下面是要跳过出错的片段，那么这里为什么是isReady呢?而不是isError？
					if (!isReady) {
						if ((readSize - lastReadSize) > READY_BUFF) {
							lastReadSize = readSize;
							mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
						}
					} else {
						// 这里不知道是什么意思
						// errorCnt是为了跳过出错的片段?
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
	 * 计算当前已经写了多少的缓存内容
	 * @return
	 */
	private long getReadSize() {
		return (long)allCacheIndex + new File(cachePaths.get(currentCacheIndex)).length();
	}
	
	private final static int VIDEO_STATE_UPDATE = 0;
	private final static int CACHE_VIDEO_READY = 1;
	// 调整当前所显示
	private final static int CACHE_VIDEO_UPDATE = 2;
	private final static int CACHE_VIDEO_END = 3;
	 
	private final Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VIDEO_STATE_UPDATE:
				double cachePercent = readSize * 100.00 / mediaLength * 1.0;
				String s = String.format("已缓存: [%.2f%%]", cachePercent);
	 
				if (mVideoView.isPlaying()) {
					curPosition = mVideoView.getCurrentPosition();
					int duration = mVideoView.getDuration();
					duration = duration == 0 ? 1 : duration;
	 
					double playPercent = curPosition * 100.00 / duration * 1.0;
	 
					int i = curPosition / 1000;
					int hour = i / (60 * 60);
					int minute = i / 60 % 60;
					int second = i % 60;
	 
					s += String.format(" 播放: %02d:%02d:%02d [%.2f%%]", hour,
							minute, second, playPercent);
				}
	 
				tvcache.setText(s);
	 
				mHandler.sendEmptyMessageDelayed(VIDEO_STATE_UPDATE, 1000);
				break;
	 
			case CACHE_VIDEO_READY:
				isReady = true;
				// TODO 为什么在这里设置？
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