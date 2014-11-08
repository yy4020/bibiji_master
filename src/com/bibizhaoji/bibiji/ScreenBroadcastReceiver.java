package com.bibizhaoji.bibiji;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bibizhaoji.bibiji.utils.Log;

public class ScreenBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Intent i = new Intent(context, ClientAccSensorService.class);
		Log.d(G.LOG_TAG, "工作时间--->" + MyApp.isWorkingTime());

		if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			// 锁屏
			Log.d(G.LOG_TAG, "SCREEN_OFF---");
			G.isScreenOff = true;
			// && G.isWorkTime
			if (MyApp.isWorkingTime()) {
				context.startService(i);
			}

		}
		if (Intent.ACTION_SCREEN_ON.equals(action)) {
			// 开屏
			Log.d(G.LOG_TAG, "SCREEN_ON--->END SERVICE");
			context.stopService(i);

		}
		if (Intent.ACTION_USER_PRESENT.equals(action)) {
			// 解锁
			Log.d(G.LOG_TAG, "解锁dfdfsdf--->END SERVICE");
			context.stopService(i);

		}

	}

}
