package com.bibizhaoji.bibiji;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

import com.bibizhaoji.bibiji.utils.Log;
import com.bibizhaoji.bibiji.utils.Pref;
import com.bibizhaoji.bibiji.utils.ToastUtils;

public class MyBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Intent i = new Intent(context, ClientAccSensorService.class);
		Log.d(G.LOG_TAG, "工作时间--->" + MyApp.isWorkingTime());

		if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			// 锁屏
			Log.d(G.LOG_TAG, "SCREEN_OFF---");
			Log.d(G.LOG_TAG, "手机空闲状态————>" + G.isCallPhone);

			if (Pref.isMainSwitcherOn() && G.isCallPhone) {
				G.isScreenOff = true;
				context.startService(i);
				Log.d(G.LOG_TAG, "start service---");
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

		if ("android.intent.action.PHONE_STATE".equals(action)
				&& Pref.isMainSwitcherOn()) {
			String str = null;
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_IDLE:
				Log.d(G.LOG_TAG, "手机空闲");
//				context.startService(i);
				G.isCallPhone = true;
				str = "逼逼机恢复正常监听";
				break;
				
			case TelephonyManager.CALL_STATE_RINGING:
				Log.d(G.LOG_TAG, "手机响铃，来电");
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.d(G.LOG_TAG, "正在通话");
				context.stopService(i);
				G.isCallPhone = false;
				str = "逼逼机暂时关闭";
				break;
			default:
				break;
			}

//			ToastUtils.show(context, str);
			Log.d(G.LOG_TAG, "PHONE_STATE--->" + str);
		}

	}

}
