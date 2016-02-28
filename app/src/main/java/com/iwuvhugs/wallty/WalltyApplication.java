package com.iwuvhugs.wallty;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.iwuvhugs.wallty.tumblrauth.Constants;
import com.iwuvhugs.wallty.utils.LruBitmapCache;
import com.tumblr.jumblr.JumblrClient;

import java.util.HashMap;


public class WalltyApplication extends Application {

    public static final boolean DEVELOPER_MODE = false;
    public static final boolean TEST_INTERVAL = false;

    public static final CharSequence SHARED_LINK = "https://play.google.com/store/apps/details?id=com.iwuvhugs.wallty";

    private static final String TAG = WalltyApplication.class.getSimpleName();

    //    public static final String BOTTOM_BANNER_ID = "ca-app-pub-5848359003337785/1347242553";
//    public static final String INTERSTITIAL_BANNER_ID = "ca-app-pub-5848359003337785/2823975752";
    private static final String APP_OPENED_INTERSTITIAL = "app_opened_interstitial";
    private static final String USER_SEEN_INTERSTITIAL = "user_seen_interstitial";
    private static final String APP_OPENED_RATE_DIALOG = "app_opened_rate_dialog";
    private static final String NEVER_SHOW_RATE_DIALOG = "never_show_rate_dialog";
    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-60315028-4";
    public static int GENERAL_TRACKER = 0;
    public static Typeface dinRoundProRegular;
    public static Typeface Nabila;
    private static WalltyApplication instance;
    private static JumblrClient client = null;
    public String blogList[] = {"onetouchonelie.tumblr.com", "lookwhatifound.ru", "theclassyissue.com", "axarina.tumblr.com","iwuvhugs.tumblr.com"};
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    //    private boolean allow_interstitial = false;
    private boolean allow_rate_dialog = false;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public static synchronized WalltyApplication getInstance() {
        return instance;
    }


    public static JumblrClient getClient(String consumerKey, String consumerSecret) {
        if (client == null)
            client = new JumblrClient(consumerKey, consumerSecret);
        return client;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initFonts();
//        interstitialAppLaunchCounter();
        showRateDialogAppLaunchCounter();

    }

    private void initFonts() {

        dinRoundProRegular = Typeface.createFromAsset(getAssets(), "DINRoundPro-Regular.otf");
        Nabila = Typeface.createFromAsset(getAssets(), "NABILA.TTF");
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID) : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker) : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
//        Log.e(TAG, "" + mRequestQueue.getSequenceNumber());

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
//        Log.e(TAG, "getImageLoader");
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

//    public void interstitialAppLaunchCounter() {
//
//        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPrefs.edit();
//        int app_opened = sharedPrefs.getInt(APP_OPENED_INTERSTITIAL, 0);
//        boolean user_seen = sharedPrefs.getBoolean(USER_SEEN_INTERSTITIAL, false);
//
//        if (DEVELOPER_MODE)
//            Log.e(TAG, "app opened (interstitial) " + app_opened + " " + user_seen);
//
//        if (app_opened >= 10) {
//            if (user_seen) {
//                allow_interstitial = false;
//                app_opened = 0;
//                editor.putBoolean(USER_SEEN_INTERSTITIAL, false);
//                editor.putInt(APP_OPENED_INTERSTITIAL, (app_opened + 1));
//                editor.commit();
//            } else {
//                allow_interstitial = true;
//            }
//        } else {
//            editor.putBoolean(USER_SEEN_INTERSTITIAL, false);
//            editor.putInt(APP_OPENED_INTERSTITIAL, (app_opened + 1));
//            editor.commit();
//        }
//
//    }

//    public boolean isAllow_interstitial() {
//        boolean show = false;
//        if (allow_interstitial) {
//            show = true;
//            allow_interstitial = false;
//            if (DEVELOPER_MODE)
//                Log.e(TAG, "SHOW INTERSTITIAL");
//        } else {
//            if (DEVELOPER_MODE)
//                Log.e(TAG, "DO NOT SHOW INTERSTITIAL");
//        }
//        return show;
//    }

//    public void userSeenInterstitial() {
//        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPrefs.edit();
//        editor.putBoolean(USER_SEEN_INTERSTITIAL, true);
//        editor.commit();
//    }

    private void showRateDialogAppLaunchCounter() {
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int app_opened = sharedPrefs.getInt(APP_OPENED_RATE_DIALOG, 0);

        if (DEVELOPER_MODE)
            Log.e(TAG, "app opened (rate dialog)" + app_opened);

        if (app_opened >= 15) {
            allow_rate_dialog = true;
            app_opened = 0;
        }

        editor.putInt(APP_OPENED_RATE_DIALOG, (app_opened + 1));
        editor.commit();
    }

    public boolean showRateDialog() {
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean show = sharedPrefs.getBoolean(NEVER_SHOW_RATE_DIALOG, false);
        if (show) return false;
        else return allow_rate_dialog;
    }

    public void setNeverShowRateDialog() {
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(NEVER_SHOW_RATE_DIALOG, true);
        editor.commit();
    }

    public void rateApp() {

        Uri uri = Uri.parse("market://details?id=com.iwuvhugs.wallty");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.iwuvhugs.wallty")));
        }

    }

    public void shareWithFriends() {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, getResources().getText(R.string.share_title));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.share_title));
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_text) + "\n \n " + WalltyApplication.SHARED_LINK);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.action_share)));
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * <p>
     * A single tracker is usually enough for most purposes. In case you do need
     * multiple trackers, storing them all in Application object helps ensure
     * that they are created only once per application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
        // roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
        // company.

    }

}
