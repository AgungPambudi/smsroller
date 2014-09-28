package com.drocode.android.smsroller.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.drocode.android.smsroller.model.AppModel;

public class CallHandler extends BroadcastReceiver {

	private SmsManager SMSsender = SmsManager.getDefault();

	@Override
	public void onReceive(final Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (prefs.getBoolean("RespondCall", false)) {
			PhoneStateListener phoneListener = new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String caller) {
					switch (state) {
					case TelephonyManager.CALL_STATE_RINGING:
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(context);
						SharedPreferences sharedPrefs = context
								.getSharedPreferences("MobileResponderValues",
										4);

						String message = prefs
								.getString("ResponseMessage",
										"I am busy right now, I will contact you later.");

						String SessionHistory = sharedPrefs.getString(
								"SessionHistory", "");
						String[] SessionHistoryNumbers = SessionHistory
								.split(" ");
						boolean respond = true;

						if (!prefs.getBoolean("MultipleResponse", false)) {
							for (int x = 0; x < SessionHistoryNumbers.length; x++) {
								if (SessionHistoryNumbers[x].matches(caller)) {
									respond = false;
								}
							}
						}

						if (respond) {
							SMSsender.sendMultipartTextMessage(
									PhoneNumberUtils.formatNumber(caller),
									null, SMSsender.divideMessage(message),
									null, null);
						}

						SharedPreferences.Editor sharedPrefsEditor = sharedPrefs
								.edit();

						if (prefs.getBoolean("HistoryEnabled", true)) {
							String HistoryChildren = sharedPrefs.getString(
									"HistoryChildren", " ; ");
							String[] HistoryChildrenSplit = HistoryChildren
									.split(";");
							String[] call = HistoryChildrenSplit[0].split(" ");

							Date date = new Date();
							String DATE_FORMAT = "MM/dd/yyyy%20hh:mm:ss";
							SimpleDateFormat sdf = new SimpleDateFormat(
									DATE_FORMAT);
							String result = sdf.format(date) + "%20-%20"
									+ caller;

							for (int x = 0; x < call.length; x++) {
								result += " " + call[x];
							}
							result += ";" + HistoryChildrenSplit[1];
							sharedPrefsEditor.putString("HistoryChildren",
									result);

						}

						SessionHistory = caller;
						for (int x = 0; x < SessionHistoryNumbers.length; x++) {
							SessionHistory = " " + SessionHistoryNumbers[x];
						}

						sharedPrefsEditor.putString("SessionHistory",
								SessionHistory);
						sharedPrefsEditor.commit();

						return;

					case TelephonyManager.CALL_STATE_IDLE:
						return;
					}
				}
			};

			TelephonyManager telephony = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			telephony.listen(phoneListener,
					PhoneStateListener.LISTEN_CALL_STATE);

			Intent broadcast = new Intent();
			broadcast.setAction(AppModel.FORCE_REFRESH);
			context.sendBroadcast(broadcast);
		}
	}
}
