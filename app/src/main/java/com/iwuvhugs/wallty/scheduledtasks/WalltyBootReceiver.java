package com.iwuvhugs.wallty.scheduledtasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.iwuvhugs.wallty.R;
import com.iwuvhugs.wallty.WalltyApplication;
import com.iwuvhugs.wallty.tumblrauth.Constants;
import com.iwuvhugs.wallty.utils.WalltySettingsManager;

public class WalltyBootReceiver extends BroadcastReceiver {

	private static final String TAG = WalltyBootReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			/* Setting the alarm here */

			Intent alarmIntent = new Intent(context, WalltyAlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

			AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
			int position = sharedPrefs.getInt(Constants.INTERVAL_SEEKBAR_POSITION, 1);

			long interval;
			if (WalltyApplication.TEST_INTERVAL) {
				interval = 30000;
			} else {
				interval = WalltySettingsManager.getAlarmManagerInterval(position);
			}

			if (WalltyApplication.DEVELOPER_MODE)
				Log.e(TAG, "TEST AFTER REBOOT START WITH INTERVAL " + interval);

			manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
			Toast.makeText(context, context.getResources().getString(R.string.wallty_set), Toast.LENGTH_SHORT).show();

			// Enable {@code SampleBootReceiver} to automatically restart the
			// alarm
			// when the
			// device is rebooted.
			ComponentName receiver = new ComponentName(context, WalltyBootReceiver.class);
			PackageManager pm = context.getPackageManager();

			pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		}

	}

}
