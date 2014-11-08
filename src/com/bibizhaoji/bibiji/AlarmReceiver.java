package com.bibizhaoji.bibiji;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String actionString = intent.getAction();
		if (intent.getAction().equals(Alarm.WAKEUP_ALARM_ACTION)) {
			// 早上七点
			G.isWorkTime = true;
			Log.d("AlarmReceiver", "isScreenOff-->" + G.isScreenOff);
			if (G.isScreenOff) {
				Intent intent1 = new Intent(context,
						ClientAccSensorService.class);
				context.startService(intent1);
				Log.d("AlarmReceiver",
						"AlarmReceiver wakeup-->start client service");
				Alarm.initAlarm(context);
			}
		} else {
			G.isWorkTime = false;
			Log.d("AlarmReceiver", "AlarmReceiver sleep");
			if (G.isScreenOff) {
				Intent intent1 = new Intent(context,
						ClientAccSensorService.class);
				context.stopService(intent1);
			}
		}
		Log.d(G.LOG_TAG, actionString + "闹钟时间到");
	}

}
