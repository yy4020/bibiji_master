package com.bibizhaoji.bibiji;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.bibizhaoji.bibiji.aidl.IPPClient;
import com.bibizhaoji.bibiji.aidl.IWorkerService;
import com.bibizhaoji.bibiji.utils.Log;
import com.bibizhaoji.bibiji.utils.Pref;
import com.bibizhaoji.pocketsphinx.WorkerRemoteRecognizerService;

public class ClientAccSensorService extends Service implements
		SensorEventListener {
	private SensorManager sensorManager;
	private Sensor accSensor;

	IWorkerService mIWorkerService;
	private IPPClient mClient = new PPClient();

	private ServiceConnection mConnection;

	// 速度阈值，当摇晃速度达到这值后产生作用
	private int shakeThreshold = 25;
	// 两次检测的时间间隔
	static int UPDATE_INTERVAL = 5 * 10;

	private float x = 0f;
	private float y = 0f;
	private float z = 0f;

	private int idle_count;
	private int mark_count;
	private Handler mHandler;
	private static final int MSG_CHECK_STATUS = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(G.LOG_TAG, "client service onCreate");
		// initSensor();
		initConnection();
		initHandler();
		startForegroundCompat();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initConnection();
		Intent i = new Intent(WorkerRemoteRecognizerService.ACTION);
		bindService(i, mConnection, Service.BIND_AUTO_CREATE);
		Log.d(G.LOG_TAG_CONNECTION, "client service onStartCommand");
		return START_STICKY;
	}

	private void initHandler() {
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_CHECK_STATUS:
					// checkStatus();
					startRecognizer();
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}

		};
	}

	private void initConnection() {
		mConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(G.LOG_TAG_CONNECTION,
						"RemoteClient recognizer service disconnected..");
				mIWorkerService = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// 获取service aidl 对象
				Log.d(G.LOG_TAG_CONNECTION, "获取IWorkerService 对象");
				mIWorkerService = IWorkerService.Stub.asInterface(service);
				try {
					mIWorkerService.register(mClient);

				} catch (RemoteException e) {
					Log.d(G.LOG_TAG_CONNECTION, "获取IworkerService对象 error -->"
							+ e);
				}
				try {
					Log.d(G.LOG_TAG_CONNECTION, "开启 IWorkerService");

					mIWorkerService.start();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(MSG_CHECK_STATUS);
			}
		};
	}

	private void startRecognizer() {
		try {
			if (mClient.getResult() == WorkerRemoteRecognizerService.STATE_MARK) {
				mIWorkerService.stop();
				mIWorkerService.start();

				Log.d("client server", "mIWorkerService---> stop && start");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void initSensor() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// 加速度传感器
		accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// 注册监听器
		sensorManager.registerListener(this, accSensor,
				SensorManager.SENSOR_DELAY_GAME);
	}

	private void checkStatus() {
		int updateInterval = 500;
		if (G.isMainActivityRunning == false
				&& (Math.abs(x) + Math.abs(y) + Math.abs(z) > shakeThreshold)) {
			// 达到速度阀值，发出提示
			Log.d(G.LOG_TAG, "运动中......");
			try {
				mIWorkerService.stop();
				mHandler.sendEmptyMessageDelayed(MSG_CHECK_STATUS,
						updateInterval);

			} catch (RemoteException e) {
				e.printStackTrace();
			}

		} else {
			try {
				Log.d(G.LOG_TAG, "手机静止. +result-->" + mClient.getResult());

				if (mClient.getResult() == WorkerRemoteRecognizerService.STATE_NONE) {
					if (idle_count == 1) {
						mIWorkerService.stop();
						updateInterval = 200;
					} else if (idle_count == 0 || idle_count == 2) {
						mIWorkerService.start();
						idle_count = 0;
						updateInterval = 3000;
					}
					idle_count++;
					Log.d(G.LOG_TAG, "STATE_NONE count-->" + idle_count);
					mHandler.sendEmptyMessageDelayed(MSG_CHECK_STATUS,
							updateInterval);
				} else if (mClient.getResult() == WorkerRemoteRecognizerService.STATE_MATCH) {
					Log.d(G.LOG_TAG, "STATE_MATCH ");
					mIWorkerService.stop();
					stopSelf();
				} else if (mClient.getResult() == WorkerRemoteRecognizerService.STATE_MARK) {
					mark_count++;
					if (mark_count == 10) {
						mIWorkerService.stop();
						mark_count = 0;
						idle_count = 0;
					}
					updateInterval = 500;
					Log.d(G.LOG_TAG, "STATE_MARK -->mark_count " + mark_count);
					mHandler.sendEmptyMessageDelayed(MSG_CHECK_STATUS,
							updateInterval);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		try {
			if (mIWorkerService != null) {
				mIWorkerService.stop();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		unbindService(mConnection);
		// sensorManager.unregisterListener(this);
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		Log.d(G.LOG_TAG, "onDestroy()");

		if(Pref.isMainSwitcherOn()){
			initConnection();
			Log.d(G.LOG_TAG, "initConnection()--->restart server");
		}
		
		super.onDestroy();
	}

	private void startForegroundCompat() {
		try {
//			Notification status = new Notification();
//			status.flags |= Notification.FLAG_FOREGROUND_SERVICE;
//
//			startForeground(1, status);
			
			Notification notification = new Notification(R.drawable.ic_launcher, "逼逼机正在启动...",
			        System.currentTimeMillis());
			Intent notificationIntent = new Intent();
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(this, "逼逼机","前台服务", pendingIntent);
			startForeground(22, notification);

			if (Build.VERSION.SDK_INT < 18) {
				Log.d(G.LOG_TAG, "startForgroundCompat");
			}
		} catch (Exception e) {
			Log.e(G.LOG_TAG, e.toString());
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// 获得x,y,z坐标
		x = event.values[SensorManager.DATA_X];
		y = event.values[SensorManager.DATA_Y];
		z = event.values[SensorManager.DATA_Z];
	}

}