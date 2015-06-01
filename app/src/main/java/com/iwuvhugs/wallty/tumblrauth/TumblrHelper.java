package com.iwuvhugs.wallty.tumblrauth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wchgs on 07.05.15.
 */
public class TumblrHelper {


    // Taken from Tumblr app registration
    public static final String CONSUMER_KEY = "4G2TUW0i25rqawkELVHclrmziwLgyu9uvZq4JE5Fr97PxRpu5Q";
    public static final String CONSUMER_SECRET = "ir997lk7tNyOVDE0bUKFZafZDsdm1CTJvnjcy2isMAKQY4ozR6";

    // ====== TUMBLR HELPER METHODS ======

    public static boolean isConnected(Context ctx) {
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPrefs.getString(Constants.PREF_KEY_TOKEN, null) != null;
    }

    public static int loginMode(Context ctx) {
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPrefs.getInt(Constants.PREF_KEY_LOGIN_MODE, 0);
    }

    public static void logOutOfTumblr(Context ctx) {
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPrefs.edit();
        e.putString(Constants.PREF_KEY_TOKEN, null);
        e.putString(Constants.PREF_KEY_TOKEN_SECRET, null);
        e.putInt(Constants.PREF_KEY_LOGIN_MODE, 0);
        e.remove(Constants.PREF_KEY_ACCESS_TOKEN_INFOS);
        e.remove(Constants.PREF_KEY_USER);
        e.commit();
    }

    public static String getToken(Context ctx) {
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPrefs.getString(Constants.PREF_KEY_TOKEN, null);
    }

    public static String getTokenSecret(Context ctx) {
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPrefs.getString(Constants.PREF_KEY_TOKEN_SECRET, null);
    }

//	public static String getProfilPicture(Context ctx) {
//		SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//		return sharedPrefs.getString(Constants.PREF_KEY_PICTURE, null);
//	}
//
//	public static String getUsername(Context ctx) {
//		SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//		return sharedPrefs.getString(Constants.PREF_KEY_USER_NAME, null);
//	}
//
//	public static String getUserId(Context ctx) {
//		SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//		return sharedPrefs.getString(Constants.PREF_KEY_USER_ID, null);
//	}
//
//	public static void saveProfilePicture(Context ctx, String urlPicture) {
//		SharedPreferences sharedPrefs = ctx.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//		Editor e = sharedPrefs.edit();
//		e.putString(Constants.PREF_KEY_PICTURE, urlPicture);
//		e.commit();
//	}


}
