package com.iwuvhugs.wallty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iwuvhugs.wallty.dialogs.AuthProblemDialog;
import com.iwuvhugs.wallty.dialogs.NoConnectionDialog;
import com.iwuvhugs.wallty.tumblrauth.Constants;
import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;


public class LaunchActivity extends Activity implements View.OnClickListener {

    private static final String TAG = LaunchActivity.class.getSimpleName();

    private static final int REQUEST_CODE_TUMBLR_LOGIN = 5593;

    private AlertDialog noConnectionDialog;
    private AlertDialog authProblemDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get tracker.
        Tracker t = ((WalltyApplication) getApplication()).getTracker(WalltyApplication.TrackerName.APP_TRACKER);
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("Launch Activity");
        // // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

//        if (TumblrHelper.isConnected(this)) {
//
//            Intent tumblrLogedIntent = new Intent(this, MainActivity.class);
//            tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN, TumblrHelper.getToken(this));
//            tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN_SECRET, TumblrHelper.getTokenSecret(this));
//            startActivity(tumblrLogedIntent);
//            finish();
//        }
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch);


        findViewById(R.id.sign_in).setOnClickListener(this);
        findViewById(R.id.unsigned_user).setOnClickListener(this);
        findViewById(R.id.discover).setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!WalltyApplication.getInstance().isOnline()) {
            noConnectionDialogAction();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get an Analytics tracker to report app starts and uncaught exceptions
        // etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in:
                tumblrLogin();
                break;
            case R.id.unsigned_user:
                guestLogin();
                break;
            case R.id.discover:
                openTumblrPlayStore();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (WalltyApplication.DEVELOPER_MODE)
            Log.e(TAG, "onActivityResult() called, requestCode :" + requestCode + ", resultCode : " + resultCode + ", data : " + data);
        if (requestCode == REQUEST_CODE_TUMBLR_LOGIN) {
            if (resultCode == TumblrLoginActivity.TUMBLR_LOGIN_RESULT_CODE_SUCCESS) {

                String token = data.getStringExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN);
                String tokenSecret = data.getStringExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN_SECRET);

                // Log.e(TAG, "token       : " + token);
                // Log.e(TAG, "tokenSecret : " + tokenSecret);

                Intent tumblrLogedIntent = new Intent(this, MainActivity.class);
                tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN, token);
                tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN_SECRET, tokenSecret);
                startActivity(tumblrLogedIntent);
                finish();

            } else if (resultCode == TumblrLoginActivity.TUMBLR_LOGIN_RESULT_CODE_FAILURE) {
                if (WalltyApplication.DEVELOPER_MODE) {
                    Toast.makeText(this, "Tumblr login fail", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Tumblr LOGIN FAIL");
                }
                if (authProblemDialog != null) {
                    if (!authProblemDialog.isShowing()) {
                        authProblemDialog.show();
                    }
                } else {
                    authProblemDialog = new AuthProblemDialog(this).getAlertDialog();
                    authProblemDialog.show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openTumblrPlayStore() {
        final String appPackageName = "com.tumblr";
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void tumblrLogin() {
        if (WalltyApplication.getInstance().isOnline()) {
            if (TumblrHelper.isConnected(this)) {
                TumblrHelper.logOutOfTumblr(this);
            }
            Intent tumblrLoginIntent = new Intent(this, TumblrLoginActivity.class);
            tumblrLoginIntent.putExtra(TumblrLoginActivity.TUMBLR_CONSUMER_KEY, TumblrHelper.CONSUMER_KEY);
            tumblrLoginIntent.putExtra(TumblrLoginActivity.TUMBLR_CONSUMER_SECRET, TumblrHelper.CONSUMER_SECRET);
            startActivityForResult(tumblrLoginIntent, REQUEST_CODE_TUMBLR_LOGIN);
        } else {
            noConnectionDialogAction();
        }
    }

    private void guestLogin() {
        if (WalltyApplication.getInstance().isOnline()) {
            SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.PREF_KEY_LOGIN_MODE, 2);
            editor.apply();

            Intent tumblrLoginIntent = new Intent(this, GuestActivity.class);
            startActivity(tumblrLoginIntent);
            finish();
        } else {
            noConnectionDialogAction();
        }
    }

    private void noConnectionDialogAction() {
        if (noConnectionDialog != null) {
            if (!noConnectionDialog.isShowing()) {
                noConnectionDialog.show();
            }
        } else {
            noConnectionDialog = new NoConnectionDialog(this).getAlertDialog();
            noConnectionDialog.show();
        }
    }
}