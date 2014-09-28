package com.drocode.android.smsroller.adapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.drocode.android.smsroller.R;
import com.drocode.android.smsroller.model.AppModel;

public class ListviewActivity extends Activity {

	private ExpandableListView mrHistory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_screen);

		mrHistory = (ExpandableListView) findViewById(R.id.history);
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
	}

	public void refresh() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences sharedPrefs = getSharedPreferences(
				"MobileResponderValues", 4);

		if (prefs.getBoolean("HistoryEnabled", true)) {
			// Populate Lists
			String HistoryChildren = sharedPrefs.getString("HistoryChildren",
					" ; ");
			String[] HistoryChildrenSplit = HistoryChildren.split(";");
			String[] groups = { "    InCall", "    InBox" };
			String[][] children = { HistoryChildrenSplit[0].split(" "),
					HistoryChildrenSplit[1].split(" ") };

			for (int x = 0; x < children.length; x++) {
				for (int y = 0; y < children[x].length; y++) {
					children[x][y] = children[x][y].replace("%20", " ");

					if (!prefs.getBoolean("HistoryShowBody", true)) {
						children[x][y] = children[x][y].split("\n")[0];
					}
				}
			}
			mrHistory.setAdapter(new ListviewAdapter(mrHistory, groups,
					children));

			if (prefs.getBoolean("HistoryExpanded", true)) {
				mrHistory.expandGroup(0);
				mrHistory.expandGroup(1);
			}
		} else {
			SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
			sharedPrefsEditor.putString("HistoryChildren", " ; ");
			sharedPrefsEditor.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.history_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
		case R.id.clearHistory:

			SharedPreferences sharedPrefs = getSharedPreferences(
					"MobileResponderValues", 4);
			SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
			sharedPrefsEditor.putString("HistoryChildren", " ; ");
			sharedPrefsEditor.commit();

			refresh();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			refresh();
		}
	};
}