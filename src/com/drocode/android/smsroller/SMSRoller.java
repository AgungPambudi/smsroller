package com.drocode.android.smsroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.drocode.android.smsroller.adapter.ListviewActivity;
import com.drocode.android.smsroller.model.AppModel;
import com.drocode.android.smsroller.prefs.Preferences;

public class SMSRoller extends Activity implements View.OnClickListener,
		CompoundButton.OnCheckedChangeListener {

	public static final String TAG = "Main";

	private ToggleButton mrOnOffSwitch;
	private TextView mrResponse;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		// Set content
		setContentView(R.layout.main_screen);

		// Set the titlebar layout
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_bar);

		// Obtain handles to UI objects
		((Button) findViewById(R.id.setReplyMessage)).setOnClickListener(this);
		mrOnOffSwitch = (ToggleButton) findViewById(R.id.onOffSwitch);

		// Register handler for UI elements
		mrOnOffSwitch.setOnCheckedChangeListener(this);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (prefs.getBoolean("AutoStart", false)) {
			mrOnOffSwitch.setChecked(true);
		}

		SharedPreferences sharedPrefs = getSharedPreferences(
				"MobileResponderValues", 4);

		if (!sharedPrefs.getString("CARRIER", "").matches("")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Your carrier is not yet supported to respond to emails, we apologizes for the inconvenience. Please send an email to the developer via Menu -> More -> Feedback with the message body as \""
							+ sharedPrefs.getString("CARRIER", "")
							+ "\". Thank you.")
					.setTitle("WARNING")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {// ganti int which menjadi
													// int id
									dialog.cancel();
								}
							});

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	public void btnHeaderClick(View target) {
		switch (target.getId()) {
		case R.id.header_left_btn:
			startActivity(new Intent(this, ListviewActivity.class));
			break;

		case R.id.header_right_btn:
			startActivity(new Intent(this, Preferences.class));
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AppModel.FORCE_REFRESH);
		registerReceiver(refreshReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(refreshReceiver);

		// SharedPreferences prefs = PreferenceManager
		// .getDefaultSharedPreferences(this);

		// hilangkan kode ini
		// if (prefs.getBoolean("PauseDestroyUI", false)) {
		// finish();
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:

			return true;

		case R.id.about:

			return true;

		case R.id.exit:

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			if (prefs.getBoolean("ExitOption", false)) {
				mrOnOffSwitch = (ToggleButton) findViewById(R.id.onOffSwitch);
				mrOnOffSwitch.setChecked(false);
			}
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void refresh() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences sharedPrefs = getSharedPreferences(
				"MobileResponderValues", 4);

		mrResponse = (TextView) findViewById(R.id.response);
		mrOnOffSwitch = (ToggleButton) findViewById(R.id.onOffSwitch);

		if (!sharedPrefs.getString("VERSION_NAME", "").matches(
				AppModel.VERSION_NAME)) {
			SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
			sharedPrefsEditor.putString("VERSION_NAME", AppModel.VERSION_NAME);
			sharedPrefsEditor.putBoolean("FIRST_TIME", true);
			startActivity(new Intent(this, Preferences.class));
			sharedPrefsEditor.commit();
		}

		if (sharedPrefs.getBoolean("Running", false)) {
			mrOnOffSwitch.setChecked(true);
		} else {
			mrOnOffSwitch.setChecked(false);
		}

		mrResponse.setText(prefs.getString("ResponseMessage",
				"I am busy right now, I will contact you later."));
	}

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			refresh();
		}
	};

	@Override
	public void onClick(View v) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(SMSRoller.this);
		String message = prefs.getString("ResponseMessage",
				"I am busy right now, I will contact you later.");

		AlertDialog.Builder alert = new AlertDialog.Builder(SMSRoller.this);

		alert.setTitle("Message");

		final EditText input = new EditText(SMSRoller.this);
		alert.setView(input);
		input.setText(message);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(SMSRoller.this);
				SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();

				String value = input.getText().toString();
				sharedPrefsEditor.putString("ResponseMessage", value);
				sharedPrefsEditor.commit();

				refresh();

			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		alert.show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
		// Ganti CompoundButton butonView dengan CompoundButton view jika ada
		// error

		if (checked) {
			// Toggled ON
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(AppModel.RESPONDER);
			startService(serviceIntent);

			SharedPreferences sharedPrefs = getSharedPreferences(
					"MobileResponderValues", 4);
			SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
			sharedPrefsEditor.putBoolean("Running", true);
			sharedPrefsEditor.commit();

			refresh();
		} else {
			// Toggled OFF
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(AppModel.RESPONDER);
			stopService(serviceIntent);

			SharedPreferences sharedPrefs = getSharedPreferences(
					"MobileResponderValues", 4);
			SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
			sharedPrefsEditor.putBoolean("Running", false);
			sharedPrefsEditor.commit();

			refresh();
		}
	}
}