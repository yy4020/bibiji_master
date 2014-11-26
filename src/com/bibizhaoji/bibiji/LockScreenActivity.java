package com.bibizhaoji.bibiji;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

/**
 * 接收到语音指令后，弹出于锁屏之上的界面
 * 
 * @author jinzhenzu
 * 
 */
public class LockScreenActivity extends Activity implements OnClickListener {

	private AnimationDrawable gifAnim;
	private MediaPlayer mediaPlayer;
	private AudioManager audioManager;
	private Button stopBtn;
	private ImageView gif;
	private ImageView text;
	private Handler handler;
	private int originalVol;
	private int maximalVol;
	private Context mContext;

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 设为锁屏全屏弹窗
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 如果系统版本在JellyBean之上，隐藏虚拟按键和状态栏
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int uiOptions;
			View decorView = getWindow().getDecorView();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			} else {
				uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
			}
			decorView.setSystemUiVisibility(uiOptions);
		}
		// 渲染界面
		setContentView(R.layout.activity_lock_screen);

		gif = (ImageView) findViewById(R.id.gif);
		text = (ImageView) findViewById(R.id.text);
		stopBtn = (Button) findViewById(R.id.stop_btn_lockscreen);

		stopBtn.setOnClickListener(this);
		mContext = this;

	}

	@Override
	protected void onStart() {
		super.onStart();
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		originalVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		maximalVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		playSound(G.RINGTON, G.VOLUME);
		gifAnim = (AnimationDrawable) gif.getBackground();
		gifAnim.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopSound();
		// 恢复铃声
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVol, 0);
		// this.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 播放铃声(以最大音量)
	 * 
	 * @param soundResourceId
	 *            声音资源ID
	 * @param volume
	 *            音量(0.0-1.0)
	 */
	private void playSound(int soundResourceId, float volume) {
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maximalVol, 0);

		mediaPlayer = MediaPlayer.create(this, soundResourceId);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}

	/**
	 * 播放铃声(以正常音量，测试时用)
	 * 
	 * @param soundResourceId
	 *            声音资源ID
	 * @param volume
	 *            音量(0.0-1.0)
	 */
	// private void playSound(int soundResourceId, float volume) {
	// mediaPlayer = MediaPlayer.create(this, soundResourceId);
	// mediaPlayer.setVolume(volume, volume);
	// mediaPlayer.setLooping(true);
	// mediaPlayer.start();
	// }

	/**
	 * 停止播放铃声
	 */
	private void stopSound() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.stop_btn_lockscreen:
			stopSound();
//			stopBtn.setVisibility(View.GONE);
//			gif.setBackgroundResource(R.drawable.state_stop);
//			text.setBackgroundResource(R.drawable.bg_main_stop);
//			gifAnim = (AnimationDrawable) gif.getBackground();
//			handler = new Handler();
//			gifAnim.start();
//			handler.postDelayed(new Runnable() {
//
//				@Override
//				public void run() {
//					finish();
//				}
//			}, G.STOP_ANIM_DURATION);
//			Intent i = new Intent(mContext, ClientAccSensorService.class);
//			startService(i);
			finish();
			break;
		}
	}
}
