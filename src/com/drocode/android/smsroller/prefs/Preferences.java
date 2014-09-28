package com.drocode.android.smsroller.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.drocode.android.smsroller.R;
import com.drocode.android.smsroller.model.AppModel;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences_screen);

		SharedPreferences sharedPrefs = getSharedPreferences(
				"MobileResponderValues", 4);

		if (sharedPrefs.getBoolean("FIRST_TIME", false)) {
			SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();

			sharedPrefsEditor.putBoolean("FIRST_TIME", false);
			sharedPrefsEditor.commit();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preferences_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
		case R.id.restoreDefaults:
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor prefsEditor = prefs.edit();
			prefsEditor.clear();
			prefsEditor.commit();

			SharedPreferences sharedPrefs = getSharedPreferences(
					"MobileResponderValues", 4);
			SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
			sharedPrefsEditor.clear();
			sharedPrefsEditor.putString("VERSION_NAME", AppModel.VERSION_NAME);
			sharedPrefsEditor.commit();
			onCreate(null);

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}