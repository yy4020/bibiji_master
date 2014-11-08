package com.bibizhaoji.bibiji;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.bibizhaoji.bibiji.utils.Pref;
import com.bibizhaoji.crash.CrashHandler;
import com.bibizhaoji.pocketsphinx.WorkerRemoteRecognizerService;

public class MyApp extends Application {
    public static final String TAG = MyApp.class.getSimpleName();
    private ScreenBroadcastReceiver mScreenReceiver;

    @Override
    public void onCreate() {
	super.onCreate();
	CrashHandler.getInstance().init(this);
	Pref.getSharePrefenrences(this);

	Log.d(G.LOG_TAG, "大开关--->" + Pref.isMainSwitcherOn());
	if (Pref.isMainSwitcherOn()) {
	    mScreenReceiver = new ScreenBroadcastReceiver();
	    startScreenBroadcastReceiver();
	    Intent intent = new Intent(this, ClientAccSensorService.class);
	    startService(intent);
	}

    }

    private void startScreenBroadcastReceiver() {
	IntentFilter filter = new IntentFilter();
	filter.addAction(Intent.ACTION_SCREEN_ON);
	filter.addAction(Intent.ACTION_SCREEN_OFF);
	registerReceiver(mScreenReceiver, filter);
    }

}
