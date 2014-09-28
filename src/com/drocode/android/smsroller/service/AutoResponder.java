package com.drocode.android.smsroller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.drocode.android.smsroller.R;
import com.drocode.android.smsroller.SMSRoller;
import com.drocode.android.smsroller.handler.CallHandler;
import com.drocode.android.smsroller.handler.SmsHandler;

public class AutoResponder extends Service {

	int mrStartMode; // indicates how to behave if the service is killed
	IBinder mrBinder; // interface for clients that bind
	boolean mrAllowRebind; // indicates whether onRebind should be used

	private CallHandler CallHandler = new CallHandler();
	private SmsHandler SmsHandler = new SmsHandler();
	private static final int N_ID = 668439;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		SharedPreferences sharedPrefs = getSharedPreferences(
				"MobileResponderValues", 4);
		SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
		sharedPrefsEditor.putBoolean("Running", true);
		sharedPrefsEditor.commit();

		this.registerReceiver(CallHandler, new IntentFilter(
				"android.intent.action.PHONE_STATE"));
		this.registerReceiver(SmsHandler, new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED"));

		AudioManager phoneAudio = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);

		if (prefs.getBoolean("SilentMode", false)) {
			phoneAudio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}

		if (prefs.getBoolean("ServiceNotification", false)) {
			Notification notification = new Notification(R.drawable.icon,
					"SMS Roller Started", 0);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			Intent notificationIntent = new Intent(this, SMSRoller.class);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(this, "SMS Roller Running",
					"Select to go to application", pendingIntent);
			startForeground(N_ID, notification);
		}

//		if (prefs.getBoolean("ClearHistory", false)) {
//			sharedPrefsEditor.putString("HistoryChildren", " ; ");
//			sharedPrefsEditor.commit();
//		}

		return mrStartMode;
	}

	@Override
	public void onDestroy() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		AudioManager phoneAudio = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		phoneAudio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

		if (prefs.getBoolean("ServiceNotification", false)) {
			stopForeground(true);
		}

		SharedPreferences sharedPrefs = getSharedPreferences(
				"MobileResponderValues", 4);
		SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
		sharedPrefsEditor.putBoolean("Running", false);
		sharedPrefsEditor.putString("SessionHistory", "");
		sharedPrefsEditor.commit();

		this.unregisterReceiver(CallHandler);
		this.unregisterReceiver(SmsHandler);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
