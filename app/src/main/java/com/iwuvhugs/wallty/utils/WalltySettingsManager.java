package com.iwuvhugs.wallty.utils;

import android.app.AlarmManager;

public class WalltySettingsManager {

    public static long getAlarmManagerInterval(int seekBar_position) {
        long interval = 0;

        switch (seekBar_position) {
            case 0:
                interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                break;
            case 1:
                interval = AlarmManager.INTERVAL_HALF_HOUR;
                break;
            case 2:
                interval = AlarmManager.INTERVAL_HALF_HOUR;
                break;
            case 3:
                interval = AlarmManager.INTERVAL_HALF_DAY;
                break;
            case 4:
                interval = AlarmManager.INTERVAL_DAY;
                break;
        }

        return interval;
    }

    public static int getSelection(int spinner_position) {
        int amount = 0;

        switch (spinner_position) {
            case 0:
                amount = 15;
                break;
            case 1:
                amount = 30;
                break;
            case 2:
                amount = 50;
                break;
        }
        return amount;
    }
}
