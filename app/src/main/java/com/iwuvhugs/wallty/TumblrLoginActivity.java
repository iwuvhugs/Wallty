package com.iwuvhugs.wallty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iwuvhugs.wallty.tumblrauth.Constants;
import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

/**
 * Created by wchgs on 13.05.15.
 */
public class TumblrLoginActivity extends Activity {

    public static final String TAG = TumblrLoginActivity.class.getSimpleName();

    public static final int TUMBLR_LOGIN_RESULT_CODE_SUCCESS = 233;
    public static final int TUMBLR_LOGIN_RESULT_CODE_FAILURE = 234;

    public static final String TUMBLR_EXTRA_TOKEN = "extra_access_token_tumblr";
    public static final String TUMBLR_EXTRA_TOKEN_SECRET = "extra_access_token_secret_tumblr";

    public static final String TUMBLR_CONSUMER_KEY = "tumblr_consumer_key";
    public static final String TUMBLR_CONSUMER_SECRET = "tumblr_consumer_secret";
    public String tumblrConsumerKey;
    public String tumblrConsumerSecret;
    public String tempAccessToken;
    public String tempAccessTokenSecret;
    CommonsHttpOAuthConsumer consumer;// = new
    // CommonsHttpOAuthConsumer(CONSUMER_KEY,
    // CONSUMER_SECRET);
    CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(Constants.REQUEST_TOKEN_URL, Constants.ACCESS_TOKEN_URL, Constants.AUTH_URL);
    private WebView tumblrLoginWebView;
    private ProgressDialog mProgressDialog;
    private String authURL;
    private WebViewClient tumblrWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (WalltyApplication.DEVELOPER_MODE)
                Log.e(TAG, "shouldOverrideUrlLoading called, url : " + url);

            if (url.contains(Constants.TUMBLR_CALLBACK_URL)) {
                // Log.e(TAG, "tumblr callback url : " + url);
                Uri uri = Uri.parse(url);
                // Log.e(TAG, "tumblr uri : " + uri.toString());
                TumblrLoginActivity.this.saveAccessTokenAndFinish(uri);
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mProgressDialog != null)
                mProgressDialog.show();
        }
    };

    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Get tracker.
        Tracker t = ((WalltyApplication) getApplication()).getTracker(WalltyApplication.TrackerName.APP_TRACKER);
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("Tumblr Login Activity");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

//        // Hide actionBar
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_launch);

        setContentView(R.layout.activity_tumblr_login);

        // Get WebView
        tumblrLoginWebView = (WebView) findViewById(R.id.webview_tumblr_login);
        tumblrLoginWebView.getSettings().setJavaScriptEnabled(true);

        if (WalltyApplication.DEVELOPER_MODE)
            Log.e(TAG, "has Cookies " + CookieManager.getInstance().hasCookies());

        if (CookieManager.getInstance().hasCookies()) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion == android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Do something for LOLLIPOP versions
                CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                    @Override
                    public void onReceiveValue(Boolean value) {
                    }
                });
            } else {
                // do something for phones running an SDK before LOLLIPOP
                CookieManager.getInstance().removeAllCookie();
            }
        }

        // Get consumer key and consumer secret from extras
        Intent intent = getIntent();
        if (intent != null) {
            tumblrConsumerKey = intent.getStringExtra(TUMBLR_CONSUMER_KEY);
            tumblrConsumerSecret = intent.getStringExtra(TUMBLR_CONSUMER_SECRET);
            if (tumblrConsumerKey == null || tumblrConsumerSecret == null) {
                if (WalltyApplication.DEVELOPER_MODE)
                    Log.e(Constants.TAG, "ERROR: Consumer Key and Consumer Secret required!");
                TumblrLoginActivity.this.setResult(TUMBLR_LOGIN_RESULT_CODE_FAILURE);
                TumblrLoginActivity.this.finish();
            } else {
                // if (WalltyApplication.DEVELOPER_MODE)
                // Log.e(TAG, "consumer params " + tumblrConsumerKey + " " +
                // tumblrConsumerSecret);

                consumer = new CommonsHttpOAuthConsumer(tumblrConsumerKey, tumblrConsumerSecret);


                tumblrLoginWebView.setWebViewClient(tumblrWebViewClient);

                // if (WalltyApplication.DEVELOPER_MODE)
                // Log.e(Constants.TAG, "ASK OAUTH");
                askOAuth();
            }
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    @Override
    protected void onDestroy() {
        // Log.e(TAG, "onDestroy called ");
        super.onDestroy();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        mProgressDialog = null;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void saveAccessTokenAndFinish(final Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // retrieve token and tokenSecret saved in
                    // retrieveRequestToken() call

                    consumer.setTokenWithSecret(tempAccessToken, tempAccessTokenSecret);

                    if (WalltyApplication.DEVELOPER_MODE) {
                        Log.e(TAG, "consumer.token       : " + consumer.getToken());
                        Log.e(TAG, "consumer.tokenSecret : " + consumer.getTokenSecret());
                    }

                    String oauthToken = uri.getQueryParameter(Constants.IEXTRA_OAUTH_TOKEN);// "oauth_token");
                    String oauthVerifier = uri.getQueryParameter(Constants.IEXTRA_OAUTH_VERIFIER);// "oauth_verifier");

                    if (WalltyApplication.DEVELOPER_MODE) {
                        Log.e(TAG, "Token:" + oauthToken);
                        Log.e(TAG, "Verifier:" + oauthVerifier);
                    }

                    provider.retrieveAccessToken(consumer, oauthVerifier);

                    if (WalltyApplication.DEVELOPER_MODE) {
                        Log.e(TAG, "accessToken       retrieveAccessToken    : " + consumer.getToken());
                        Log.e(TAG, "accessTokenSecret retrieveAccessToken    : " + consumer.getTokenSecret());
                    }

                    // save the new access token and tokenSecret retrieved by
                    // provider
                    // Вот это те самые токены, по которым можно обращаться
                    SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Constants.PREF_KEY_TOKEN, consumer.getToken());
                    editor.putString(Constants.PREF_KEY_TOKEN_SECRET, consumer.getTokenSecret());
                    editor.putInt(Constants.PREF_KEY_LOGIN_MODE,1);
//
//                    // send result back to the onActivityResult()
//                    Intent data = new Intent();
//                    data.putExtra(TUMBLR_EXTRA_TOKEN, consumer.getToken());
//                    data.putExtra(TUMBLR_EXTRA_TOKEN_SECRET, consumer.getTokenSecret());
//                    TumblrLoginActivity.this.setResult(TUMBLR_LOGIN_RESULT_CODE_SUCCESS, data);

                    editor.commit();

                    Intent tumblrLogedIntent = new Intent(TumblrLoginActivity.this, MainActivity.class);
                    tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN, consumer.getToken());
                    tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN_SECRET, consumer.getTokenSecret());
                    startActivity(tumblrLogedIntent);
                    TumblrLoginActivity.this.finish();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(Constants.TAG, e.getMessage());

                    } else {
                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(Constants.TAG, "ERROR: Tumblr callback failed");

                    }

                    TumblrLoginActivity.this.setResult(TUMBLR_LOGIN_RESULT_CODE_FAILURE);
                    TumblrLoginActivity.this.finish();

                }
            }
        }).start();
    }

    /**
     * send RequestToken request to get the authURL
     */
    private void askOAuth() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //

                    // Log.e(Constants.TAG, "" +
                    // provider.getResponseParameters().toString());

                    authURL = provider.retrieveRequestToken(consumer, Constants.TUMBLR_CALLBACK_URL, OAuth.OAUTH_TIMESTAMP);

                    // Log.e(Constants.TAG, "" +
                    // provider.getResponseParameters().toString());

                    if (WalltyApplication.DEVELOPER_MODE) {
                        Log.e(TAG, "Auth url:" + authURL);
                        Log.e(TAG, "accessToken       : " + consumer.getToken());
                        Log.e(TAG, "accessTokenSecret : " + consumer.getTokenSecret());
                    }

                    tempAccessToken = consumer.getToken();
                    tempAccessTokenSecret = consumer.getTokenSecret();

                    // save tokens in preferences
                    // Скорей всего это не те токены.
                } catch (Exception e) {
                    final String errorString = e.toString();
                    TumblrLoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.cancel();

                            if (WalltyApplication.DEVELOPER_MODE)
                                Log.e(TAG, "" + errorString);

                            TumblrLoginActivity.this.setResult(TUMBLR_LOGIN_RESULT_CODE_FAILURE);
                            TumblrLoginActivity.this.finish();
                        }
                    });
                    e.printStackTrace();
                    return;
                }

                TumblrLoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(Constants.TAG, "LOADING AUTH URL : " + authURL);

                        tumblrLoginWebView.loadUrl(authURL);

                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(TAG, "COOKIE " + CookieManager.getInstance().getCookie(authURL));

                    }
                });
            }
        }).start();
    }

    // public static JumblrClient getClient(String consumerKey, String
    // consumerSecret) {
    // if (client == null)
    // client = new JumblrClient(consumerKey, consumerSecret);
    // return client;
    // }
    //
    // public static void setClient(JumblrClient client) {
    // TumblrLoginActivity.client = client;
    // }

    @Override
    public void onBackPressed() {
        // handle the case when the user clic return to cancel the login
        // operation
        if (WalltyApplication.DEVELOPER_MODE)
            Log.e(TAG, "onBackPressed() TumblrLoginActivity");

        TumblrHelper.logOutOfTumblr(this);
        super.onBackPressed();
    }

}