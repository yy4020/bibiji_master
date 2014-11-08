package com.bibizhaoji.bibiji;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.bibizhaoji.bibiji.utils.Pref;

public class MainActivity extends Activity implements OnClickListener {

	private Button mainSwticher;
	private Button nightModeSwitcher;
	private Button stopButton;

	private ImageView stateGif;
	private ImageView stateText;
	private AnimationDrawable gifAnim;
	private MediaPlayer mediaPlayer;
	private AudioManager audioManager;
	private Handler handler;
	private int originalVol;
	private int maximalVol;

	private static final int STATE_OFF = 0;
	private static final int STATE_LISTENING = 1;
	private static final int STATE_ACTIVE = 2;
	private static final int STATE_STOP = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mainSwticher = (Button) findViewById(R.id.main_switcher);
		nightModeSwitcher = (Button) findViewById(R.id.night_mode_switcher);
		stopButton = (Button) findViewById(R.id.stop_btn);

		stateGif = (ImageView) findViewById(R.id.gif_state);
		stateText = (ImageView) findViewById(R.id.text_state);

		mainSwticher.setOnClickListener(this);
		nightModeSwitcher.setOnClickListener(this);
		stopButton.setOnClickListener(this);

		gifAnim = (AnimationDrawable) stateGif.getBackground();
		gifAnim.start();
		// 初始化配置文件
		Pref.getSharePrefenrences(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 停掉监听服务
		Intent i = new Intent(this, ClientAccSensorService.class);
		stopService(i);
		setState(STATE_ACTIVE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		G.isMainActivityRunning = true;
		if (Pref.isMainSwitcherOn()) {
			Intent i = new Intent(this, ClientAccSensorService.class);
			startService(i);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		G.isMainActivityRunning = false;
		Intent i = new Intent(this, ClientAccSensorService.class);
		this.stopService(i);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Pref.isMainSwitcherOn()) {
			mainSwticher.setBackgroundResource(R.drawable.main_switcher_on);
			setState(STATE_LISTENING);
		} else {
			mainSwticher.setBackgroundResource(R.drawable.main_switcher_off);
			setState(STATE_OFF);
		}
		nightModeSwitcher.setBackgroundResource(Pref.isNightModeOn() ? R.drawable.night_mode_on : R.drawable.night_mode_off);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 主服务开关
		case R.id.main_switcher:
			if (Pref.isMainSwitcherOn()) {
				setState(STATE_OFF);
				Pref.setMainSwitcher(this, false);
				v.setBackgroundResource(R.drawable.main_switcher_off);
				Intent i = new Intent(this, ClientAccSensorService.class);
				this.stopService(i);
			} else {
				setState(STATE_LISTENING);
				Pref.setMainSwitcher(this, true);
				v.setBackgroundResource(R.drawable.main_switcher_on);
				Intent i = new Intent(this, ClientAccSensorService.class);
				this.startService(i);
			}
			break;
		// 夜间模式开关
		case R.id.night_mode_switcher:
			if (Pref.isNightModeOn()) {
				v.setBackgroundResource(R.drawable.night_mode_off);
				Pref.setNightMode(this, false);
			} else {
				v.setBackgroundResource(R.drawable.night_mode_on);
				Pref.setNightMode(this, true);
				Intent i = new Intent(this, NightModeNoticeActivity.class);
				this.startActivity(i);
			}
		case R.id.stop_btn:
			setState(STATE_STOP);
			break;
		}
	}

	private void setState(int state) {
		switch (state) {
		case STATE_OFF:
			stateText.setBackgroundResource(R.drawable.bg_main_off);
			stateGif.setBackgroundResource(R.drawable.state_off);
			break;
		case STATE_LISTENING:
			stateText.setBackgroundResource(R.drawable.bg_main_listening);
			stateGif.setBackgroundResource(R.drawable.state_listening);
			break;
		case STATE_ACTIVE:
			Log.d(G.LOG_TAG, "*********set state");
			// playSound(G.RINGTON, G.VOLUME);
			stateText.setBackgroundResource(R.drawable.bg_main_active);
			stateGif.setBackgroundResource(R.drawable.state_active);
			stopButton.setVisibility(View.VISIBLE);
			break;
		case STATE_STOP:
			// stopSound();
			stateText.setBackgroundResource(R.drawable.bg_main_stop);
			stateGif.setBackgroundResource(R.drawable.state_stop);
			stopButton.setVisibility(View.GONE);
			handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (Pref.isMainSwitcherOn()) {
						setState(STATE_LISTENING);
						Intent i = new Intent(MainActivity.this, ClientAccSensorService.class);
						startService(i);
					} else {
						setState(STATE_OFF);
					}
				}
			}, G.STOP_ANIM_DURATION);
			break;
		default:
			break;
		}
		gifAnim = (AnimationDrawable) stateGif.getBackground();
		gifAnim.start();
	}

	private void playSound(int soundResourceId, float volume) {
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		originalVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		maximalVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maximalVol, 0);
		mediaPlayer = MediaPlayer.create(this, soundResourceId);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}

	private void stopSound() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVol, 0);
		}
	}
}
