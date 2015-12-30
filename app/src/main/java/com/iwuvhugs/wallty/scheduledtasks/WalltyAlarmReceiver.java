package com.iwuvhugs.wallty.scheduledtasks;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.iwuvhugs.wallty.WalltyApplication;

public class WalltyAlarmReceiver extends WakefulBroadcastReceiver {

	private static final String LOGTAG = WalltyAlarmReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		// For our recurring task, we'll just display a message

		if (WalltyApplication.DEVELOPER_MODE) {
			Log.e(LOGTAG, "I'm running");
			Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
		}

		/*
		 * In this example, we simply create a new intent to deliver to the
		 * service. This intent holds an extra identifying the wake lock.
		 */
		Intent service = new Intent(context, WalltySchedulingService.class);

		// Start the service, keeping the device awake while it is launching.
		startWakefulService(context, service);
	}

}
