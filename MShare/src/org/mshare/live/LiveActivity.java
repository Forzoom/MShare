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
 * 修正为能够查看本地文件，以及缓存文件
 * 
 * TODO 据说能够加载头部信息，从而到达将视频文件分片的情况，那么视频的头部信息该在哪里加载呢?
 * TODO 所设置的Header是在MediaPlayer中使用，所以还是需要查看Guide->Playback中的内容和MediaPlayer中的源码？
 * 
 * TODO 考虑该如何分片段保存 
 * @author HM
 *
 */
public class LiveActivity extends Activity {
	private static final String TAG = LiveActivity.class.getSimpleName();
	
	private VideoView mVideoView;
	// 对应远程地址，远程地址可以存在在该Activity中
	private File localFile;

	private boolean isReady = false;
	// 判断当前是否出现了播放错误
	private boolean isError = false;
	private int errorCnt = 0;
	private int curPosition = 0;
	
	private String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String cacheFileName = "cache";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置的布局文件
		setContentView(R.layout.live_play);
		
		this.mVideoView = (VideoView) findViewById(R.id.video_view);

		init();
		// TODO 在这里判断不大好，毕竟Activity已经启动了
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
	 * 初始化需要播放的内容
	 * @return 返回可以播放的Uri
	 */
	private boolean init() {
		
		// 设置controller
		mVideoView.setMediaController(new MediaController(this));
		
		// 设置VideoView的回调内容
		// 可以自动判断prepared？
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
	 
			public void onPrepared(MediaPlayer mediaplayer) {
//				dismissProgressDialog();
				mVideoView.seekTo(curPosition);
				mediaplayer.start();
			}
		});

		// 判断视频结束我还是相信的
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
	 
			public void onCompletion(MediaPlayer mediaplayer) {
				curPosition = 0;
				mVideoView.pause();
			}
		});
		
		// 设置出错情况下的反应
		mVideoView.setOnErrorListener(new OnErrorListener() {
	 
			public boolean onError(MediaPlayer mediaplayer, int i, int j) {
				isError = true;
				errorCnt++;
				mVideoView.pause();
//				showProgressDialog();
				// 表明错误已经被处理，返回true才不会弹出错误框提醒用户
				return true;
			}
		});
		
		return true;
	}

}