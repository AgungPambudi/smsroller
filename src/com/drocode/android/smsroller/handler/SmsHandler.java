package com.drocode.android.smsroller.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.drocode.android.smsroller.model.AppModel;

public class SmsHandler extends BroadcastReceiver {

	private SmsManager SMSsender = SmsManager.getDefault();

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		String message = prefs.getString("ResponseMessage",
				"I am busy right now, I will contact you later.");

		Bundle bundle = intent.getExtras();

		SmsMessage[] msgs = null;

		if (bundle != null) {
			// ---retrieve the SMS messages received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];

			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				// ---handle individual texts
				String sender = msgs[i].getOriginatingAddress();
				String body = msgs[i].getMessageBody();
				boolean email = false;

				if (msgs[i].isEmail()) {
					email = true;
					sender = msgs[i].getEmailFrom();
					body = msgs[i].getEmailBody();
				}

				// Coba ganti 7 dengan 3 apakah sms yg dikirim berjumlah 3 buah
				if (sender.length() >= 7
						&& prefs.getBoolean("RespondSMS", true)) {
					SharedPreferences sharedPrefs = context
							.getSharedPreferences("MobileResponderValues", 4);
					String SessionHistory = sharedPrefs.getString(
							"SessionHistory", "");
					String[] SessionHistoryNumbers = SessionHistory.split(" ");

					boolean respond = true;

					if (!prefs.getBoolean("MultipleResponse", false)) {
						for (int x = 0; x < SessionHistoryNumbers.length; x++) {
							if (SessionHistoryNumbers[x].matches(sender)) {
								respond = false;
							}
						}
					}

					SharedPreferences.Editor sharedPrefsEditor = sharedPrefs
							.edit();

					if (respond) {
						if (email) {
							String emessage = message;
							String esender = sender;
							String carrierName = ((TelephonyManager) context
									.getSystemService(Context.TELEPHONY_SERVICE))
									.getNetworkOperatorName();

							if (carrierName.matches("Sprint")) {
								emessage = esender + " " + emessage;
								esender = "6245";
							} else {
								if (Locale.getDefault().getCountry()
										.matches("US")) {
									sharedPrefsEditor
											.putString(
													"CARRIER",
													((TelephonyManager) context
															.getSystemService(Context.TELEPHONY_SERVICE))
															.getNetworkOperatorName());

								}
							}

							SMSsender.sendMultipartTextMessage(
									PhoneNumberUtils.formatNumber(esender),
									null, SMSsender.divideMessage(emessage),
									null, null);
						} else {
							SMSsender.sendMultipartTextMessage(
									PhoneNumberUtils.formatNumber(sender),
									null, SMSsender.divideMessage(message),
									null, null);
						}
					}

					if (prefs.getBoolean("HistoryEnabled", true)) {
						String HistoryChildren = sharedPrefs.getString(
								"HistoryChildren", " ; ");
						String[] HistoryChildrenSplit = HistoryChildren
								.split(";");

						String[] sms = HistoryChildrenSplit[1].split(" ");

						Date date = new Date();

						// Format tanggal yg akan ditampilkan, bisa diganti
						String DATE_FORMAT = "MM/dd/yyyy%20hh:mm:ss";
						SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
						String result = sdf.format(date) + "%20-%20" + sender;

						result += "\n" + body.replace(" ", "%20");

						for (int x = 0; x < sms.length; x++) {
							result += " " + sms[x];
						}

						result = HistoryChildrenSplit[0] + ";" + result;
						sharedPrefsEditor.putString("HistoryChildren", result);
					}

					SessionHistory = sender;

					for (int x = 0; x < SessionHistoryNumbers.length; x++) {
						SessionHistory = " " + SessionHistoryNumbers[x];
					}

					sharedPrefsEditor.putString("SessionHistory",
							SessionHistory);
					sharedPrefsEditor.commit();
				}

				/**if (prefs.getBoolean("PhoneFinderEnabled", true)) {
					if (body.matches(prefs.getString("PhoneFinderMessage", ""))) {
						AudioManager phoneAudio = (AudioManager) context
								.getSystemService(Context.AUDIO_SERVICE);
						phoneAudio
								.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

						for (int x = phoneAudio
								.getStreamVolume(AudioManager.STREAM_MUSIC); x < phoneAudio
								.getStreamMaxVolume(AudioManager.STREAM_MUSIC); x++) {
							phoneAudio.adjustStreamVolume(
									AudioManager.STREAM_MUSIC,
									AudioManager.ADJUST_RAISE,
									AudioManager.FLAG_PLAY_SOUND);

						}

						Ringtone ringtone = RingtoneManager
								.getRingtone(
										context,
										Uri.parse(prefs
												.getString(
														"PhoneFinderRingtone",
														"content://media/internal/audio/media/48")));
						ringtone.play();
						SMSsender.sendTextMessage(sender, null,
								"Phone Finder has been activated",
								PendingIntent.getActivity(context, 0,
										new Intent("SMS_SENT"), 0), null);

					}
				} */
			}
		}
		Intent broadcast = new Intent();
		broadcast.setAction(AppModel.FORCE_REFRESH);
		context.sendBroadcast(broadcast);
	}
}
