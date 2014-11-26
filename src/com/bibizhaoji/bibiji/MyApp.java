package com.bibizhaoji.bibiji;

import java.util.Calendar;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.bibizhaoji.bibiji.utils.Pref;
import com.bibizhaoji.crash.CrashHandler;

public class MyApp extends Application {
	public static final String TAG = MyApp.class.getSimpleName();
	private MyBroadcastReceiver mReceiver;
	private static int[] mStartTime = { 0, 0 };// 默认00:00
	private static int[] mEndTime = { 7, 0 };// 默认07:00


	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler.getInstance().init(this);
		Pref.getSharePrefenrences(this);

//		Alarm.initAlarm(this);

		Log.d(G.LOG_TAG, "大开关--->" + Pref.isMainSwitcherOn());
//		Log.d(G.LOG_TAG, "工作时间--->" + isWorkingTime());

		mReceiver = new MyBroadcastReceiver();
		startScreenBroadcastReceiver();
		if (Pref.isMainSwitcherOn()) {
			Intent intent = new Intent(this, ClientAccSensorService.class);
			startService(intent);
		}

	}

	private void startScreenBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filter);
	}

	/**
	 * 判断是否是当天工作时段
	 * 
	 * @param mStartTime2
	 *            起始时间数组
	 * @param mEndTime2
	 *            结束时间数组
	 * @return 是或否
	 */
	public static boolean isWorkingTime() {

		if (Pref.isNightModeOn()) {
			// 开启夜间免打扰模式，在夜间(00:00-07:00)server不允许运行
			Calendar cal = Calendar.getInstance();// 当前日期
			int hour = cal.get(Calendar.HOUR_OF_DAY);// 获取小时
			int minute = cal.get(Calendar.MINUTE);// 获取分钟
			int minuteOfDay = hour * 60 + minute;// 从0:00分开是到目前为止的分钟数
			final int start = mStartTime[0] * 60 + mStartTime[1];// 起始时间
			// 00:00的分钟数
			final int end = mEndTime[0] * 60 + mEndTime[1];// 结束时间 07:00的分钟数

			if (minuteOfDay >= start && minuteOfDay <= end) {
				System.out.println("在时间范围内");
				return false;
			} else {
				System.out.println("在时间范围外");
				return true;
			}
		} else {
			// 没开启免打扰模式，默认server可以运行
			return true;
		}
	}



}
