package com.iwuvhugs.wallty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iwuvhugs.wallty.dialogs.NewUserDialog;
import com.iwuvhugs.wallty.dialogs.NoConnectionDialog;
import com.iwuvhugs.wallty.dialogs.RateAppDialog;
import com.iwuvhugs.wallty.scheduledtasks.WalltyAlarmReceiver;
import com.iwuvhugs.wallty.scheduledtasks.WalltyBootReceiver;
import com.iwuvhugs.wallty.tumblrauth.Constants;
import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;
import com.iwuvhugs.wallty.utils.WalltySettingsManager;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private PendingIntent pendingIntent;

    private String token;
    private String tokenSecret;

//    private LinearLayout ad_layout;
//    private AdView adView;
//    private InterstitialAd interstitial;

    private LinearLayout user_info;
    private LinearLayout wallty_settings;
    private LinearLayout wallty_new_pic_loader;

    private ImageView username_avatar;
    private ImageView last_loaded_pic;
    private String lastURL;

    private Button get_new_wallpaper_button;

    private TextView username_textView;
    private TextView following_count_textView;
    private TextView followers_count_textView;
    private TextView blogs_count_textView;
    private TextView likes_count_textView;

    private String avatar_URL;
    private String username;
    private int following_count = -1;
    private int followers_count = -1;
    private int blog_posts_count = -1;
    private int likes_count = -1;

    private Switch wallty_swicher;
    private boolean switched = false;

    private SeekBar wallty_seekBar;
    private TextView wallty_seekBar_textView;
    private int wallty_seekBar_position;
    private String[] wallty_seekBar_states;

    private Spinner wallty_spinner_selection;
    private int wallty_spinner_selection_position;

    private Spinner wallty_spinner_source;
    private int wallty_spinner_source_position;

    private ProgressBar progressBar;
    private ProgressBar progressBarCurrentWallpaper;

    private BroadcastReceiver br;

    private boolean already_helped = false;

    private AlertDialog noConnectionDialog;
    private AlertDialog newUserDialog;
    private AlertDialog rateAppDialog;

//    private ActionBar wallty_ActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get tracker.
        Tracker t = ((WalltyApplication) getApplication()).getTracker(WalltyApplication.TrackerName.APP_TRACKER);
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("Wallty Main Activity");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        setContentView(R.layout.activity_main);

//        initCustomActionBar();
        initViews();

        loadDataFromCache();
        fillViewsWithData();
        setListeners();

		/* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(MainActivity.this, WalltyAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (WalltyApplication.DEVELOPER_MODE)
                    Log.e(TAG, "Recieve message from service");

                SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                lastURL = sharedPrefs.getString(Constants.LAST_LOADED_PICTURE, "");
                if (lastURL != null) {
                    if (!lastURL.equals(""))
                        setLastLoadedPic(lastURL);
                }

            }

        };

        IntentFilter intFilt = new IntentFilter(Constants.MESSAGE);
        registerReceiver(br, intFilt);

        Intent intent = getIntent();
        if (intent != null) {
            token = intent.getStringExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN);
            tokenSecret = intent.getStringExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN_SECRET);
            // Log.e(TAG, "token              : " + token);
            // Log.e(TAG, "tokenSecret        : " + tokenSecret);
            // Log.e(TAG, "======================================");
            if (!(token.equals("")) && !(tokenSecret.equals("")))
                if (WalltyApplication.getInstance().isOnline()) {
                    requestTumblrInfo(token, tokenSecret);
                    // } else {
                    // showNoInternetDialog();
                }
        }

//        // Создание экземпляра adView.
//        adView = new AdView(this);
//        adView.setAdUnitId(WalltyApplication.BOTTOM_BANNER_ID);
//        adView.setAdSize(AdSize.SMART_BANNER);
//        adView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                if (ad_layout != null) {
//                    ad_layout.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//
//        // Добавление в разметку экземпляра adView.
//        if (ad_layout != null)
//            ad_layout.addView(adView);
//        // Инициирование общего запроса.
//        AdRequest adRequest = new AdRequest.Builder()
//                // .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("F61A4C8F24B6FB01ED9C94C3467A1817")
//                .build();
//        // Загрузка adView с объявлением.
//        adView.loadAd(adRequest);
//
//        // Создание межстраничного объявления.
//        interstitial = new InterstitialAd(this);
//        interstitial.setAdUnitId(WalltyApplication.INTERSTITIAL_BANNER_ID);
//        // Создание запроса объявления.
//        AdRequest adRequestInterstitial = new AdRequest.Builder()
//                // .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("F61A4C8F24B6FB01ED9C94C3467A1817")
//                .build();
//        // Запуск загрузки межстраничного объявления.
//        interstitial.loadAd(adRequestInterstitial);

        showRateDialog();

    }

    @Override
    protected void onResume() {
        if (!WalltyApplication.getInstance().isOnline()) {
            // new Dialogs().showNoInternetDialog(this);
            if (noConnectionDialog != null) {
                if (!noConnectionDialog.isShowing()) {
                    noConnectionDialog.show();
                }
            } else {
                noConnectionDialog = new NoConnectionDialog(this).getAlertDialog();
                noConnectionDialog.show();
            }
        } else {
            // requestTumblrInfo(token, tokenSecret);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    private void initViews() {

        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);

        wallty_seekBar_states = getResources().getStringArray(R.array.alarm_intervals);
//        ad_layout = (LinearLayout) findViewById(R.id.ad_layout);

        user_info = (LinearLayout) findViewById(R.id.user_info_layout);

        username_avatar = (ImageView) findViewById(R.id.username_avatar);
        last_loaded_pic = (ImageView) findViewById(R.id.current_wallpaper);
        lastURL = sharedPrefs.getString(Constants.LAST_LOADED_PICTURE, "");
        if (lastURL != null) {
            if (!lastURL.equals("")) {
                setLastLoadedPic(lastURL);
            }
        }

        username_textView = (TextView) findViewById(R.id.username_textview);
        following_count_textView = (TextView) findViewById(R.id.your_followings_textview);
        followers_count_textView = (TextView) findViewById(R.id.your_followers_textview);
        blogs_count_textView = (TextView) findViewById(R.id.your_blogs_textview);
        likes_count_textView = (TextView) findViewById(R.id.your_likes_textview);

        wallty_swicher = (Switch) findViewById(R.id.wallty_switcher);
        switched = sharedPrefs.getBoolean(Constants.SCHEDULER_CHECKED, false);
        wallty_swicher.setChecked(switched);

        wallty_settings = (LinearLayout) findViewById(R.id.wallty_settings);
        wallty_new_pic_loader = (LinearLayout) findViewById(R.id.wallty_new_pic_loader);
        if (switched) {
            wallty_settings.setVisibility(View.GONE);
            wallty_new_pic_loader.setVisibility(View.VISIBLE);
        } else {
            wallty_settings.setVisibility(View.VISIBLE);
            wallty_new_pic_loader.setVisibility(View.GONE);
        }

        wallty_seekBar = (SeekBar) findViewById(R.id.wallty_seekBar);
        wallty_seekBar_textView = (TextView) findViewById(R.id.wallty_seekBar_textView);
        wallty_seekBar_position = sharedPrefs.getInt(Constants.INTERVAL_SEEKBAR_POSITION, 1);
        wallty_seekBar.setProgress(wallty_seekBar_position);
        wallty_seekBar_textView.setText(wallty_seekBar_states[wallty_seekBar_position]);

        wallty_spinner_selection = (Spinner) findViewById(R.id.wallty_spinner_selection);
        ArrayAdapter<CharSequence> adapter_selection = ArrayAdapter.createFromResource(this, R.array.select_randomly_from, R.layout.wallty_spinner_item);
        adapter_selection.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wallty_spinner_selection.setAdapter(adapter_selection);
        wallty_spinner_selection_position = sharedPrefs.getInt(Constants.SELECTION_SPINNER_POSITION, 1);
        wallty_spinner_selection.setSelection(wallty_spinner_selection_position);

        wallty_spinner_source = (Spinner) findViewById(R.id.wallty_spinner_source);
        ArrayAdapter<CharSequence> adapter_source = ArrayAdapter.createFromResource(this, R.array.pictures_source, R.layout.wallty_spinner_item);
        adapter_source.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wallty_spinner_source.setAdapter(adapter_source);
        wallty_spinner_source_position = sharedPrefs.getInt(Constants.PICTURES_SOURCE, 1);
        wallty_spinner_source.setSelection(wallty_spinner_source_position);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarCurrentWallpaper = (ProgressBar) findViewById(R.id.progressBarCurrentWallpaper);

        get_new_wallpaper_button = (Button) findViewById(R.id.get_new_wallpaper);

    }

//    private void initCustomActionBar() {
//
//        wallty_ActionBar = getActionBar();
//        wallty_ActionBar.setDisplayShowTitleEnabled(false);
//
//        LayoutInflater mInflater = LayoutInflater.from(this);
//        View mCustomView = mInflater.inflate(R.layout.custom_acb_main, null);
//
//        wallty_ActionBar.setCustomView(mCustomView);
//        wallty_ActionBar.setDisplayShowCustomEnabled(true);
//
//    }

    private void loadDataFromCache() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        username = sharedPrefs.getString(Constants.PREF_KEY_USER_NAME, "");
        avatar_URL = sharedPrefs.getString(Constants.PREF_KEY_USER_PICTURE, "");
        blog_posts_count = sharedPrefs.getInt(Constants.PREF_KEY_USER_BLOGS, -1);
        following_count = sharedPrefs.getInt(Constants.PREF_KEY_USER_FOLLOWINGS, -1);
        followers_count = sharedPrefs.getInt(Constants.PREF_KEY_USER_FOLLOWERS, -1);
        likes_count = sharedPrefs.getInt(Constants.PREF_KEY_USER_LIKES, -1);

    }

    private void removeDataFromCache() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(Constants.PREF_KEY_USER_NAME);
        editor.remove(Constants.PREF_KEY_USER_PICTURE);
        editor.remove(Constants.PREF_KEY_USER_BLOGS);
        editor.remove(Constants.PREF_KEY_USER_FOLLOWINGS);
        editor.remove(Constants.PREF_KEY_USER_FOLLOWERS);
        editor.remove(Constants.PREF_KEY_USER_LIKES);
        editor.apply();
    }

    private void setDefaultWalltySettings() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(Constants.INTERVAL_SEEKBAR_POSITION, 1);
        editor.putInt(Constants.SELECTION_SPINNER_POSITION, 1);
        editor.putInt(Constants.PICTURES_SOURCE, 1);
        editor.apply();
    }

    private void removeLastLoadedPicture() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(Constants.LAST_LOADED_PICTURE);
        editor.apply();
    }

    private void fillViewsWithData() {
        if (!(username.equals(""))) {
            progressBar.setVisibility(View.GONE);
            wallty_swicher.setVisibility(View.VISIBLE);
            // wallty_settings.setVisibility(View.VISIBLE);
            username_textView.setText(getResources().getString(R.string.welcome) + " " + username + "!");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            wallty_swicher.setVisibility(View.INVISIBLE);
            // wallty_settings.setVisibility(View.INVISIBLE);
        }

        if (blog_posts_count != (-1) || following_count != (-1) || followers_count != (-1) || likes_count != (-1)) {

            blogs_count_textView.setText("" + blog_posts_count);
            following_count_textView.setText("" + following_count);
            followers_count_textView.setText("" + followers_count);
            likes_count_textView.setText("" + likes_count);

            user_info.setVisibility(View.VISIBLE);
        }

        if (!avatar_URL.equals("")) {
            loadUserPic(avatar_URL);
        }

    }

    private void helpNewbie() {

        if (!already_helped)
            if (blog_posts_count < 10 || following_count == 0 || likes_count < 10) {

                if (newUserDialog != null) {
                    if (!newUserDialog.isShowing()) {
                        newUserDialog.show();
                    }
                } else {
                    newUserDialog = new NewUserDialog(this);
                    newUserDialog.show();
                }
                // new Dialogs().showNewUserDialog(this);
                already_helped = true;
            }
    }

    private void setListeners() {
        wallty_swicher.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    start();
                    loadPicAndSetWallpaper();
                    wallty_settings.setVisibility(View.GONE);
                    wallty_new_pic_loader.setVisibility(View.VISIBLE);

                    if (!WalltyApplication.getInstance().isOnline()) {
                        if (noConnectionDialog != null) {
                            if (!noConnectionDialog.isShowing()) {
                                noConnectionDialog.show();
                            }
                        } else {
                            noConnectionDialog = new NoConnectionDialog(MainActivity.this).getAlertDialog();
                            noConnectionDialog.show();
                        }
                    }

                    helpNewbie();

                } else {
                    cancel();
                    wallty_settings.setVisibility(View.VISIBLE);
                    wallty_new_pic_loader.setVisibility(View.GONE);
//                    displayInterstitial();
                }
                switched = isChecked;
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean(Constants.SCHEDULER_CHECKED, isChecked);
                editor.apply();
            }

        });

        wallty_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wallty_seekBar_position = progress;
                wallty_seekBar_textView.setText(wallty_seekBar_states[progress]);
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(Constants.INTERVAL_SEEKBAR_POSITION, progress);
                editor.apply();
            }
        });

        wallty_spinner_selection.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                wallty_spinner_selection_position = pos;
                // wallty_spinner_selection.setSelection(wallty_spinner_selection_position);
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(Constants.SELECTION_SPINNER_POSITION, pos);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        wallty_spinner_source.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wallty_spinner_source_position = position;
                // wallty_spinner_selection.setSelection(wallty_spinner_selection_position);
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(Constants.PICTURES_SOURCE, position);
                editor.apply();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        get_new_wallpaper_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                helpNewbie();
//                displayInterstitial();
                if (progressBarCurrentWallpaper != null)
                    progressBarCurrentWallpaper.setVisibility(View.VISIBLE);
                if (WalltyApplication.getInstance().isOnline()) {
                    loadPicAndSetWallpaper();
                } else {

                    if (noConnectionDialog != null) {
                        if (!noConnectionDialog.isShowing()) {
                            noConnectionDialog.show();
                        }
                    } else {
                        noConnectionDialog = new NoConnectionDialog(MainActivity.this).getAlertDialog();
                        noConnectionDialog.show();
                    }

                    if (progressBarCurrentWallpaper != null)
                        progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void requestTumblrInfo(final String token, final String tokenSecret) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // get user informations
                    JumblrClient client = WalltyApplication.getClient(TumblrHelper.CONSUMER_KEY, TumblrHelper.CONSUMER_SECRET);
                    client.setToken(token, tokenSecret);

                    User user = client.user();

                    if (user != null) {

                        avatar_URL = user.getBlogs().get(0).avatar();
                        loadUserPic(avatar_URL);

                        username = user.getName();
                        blog_posts_count = user.getBlogs().get(0).getPostCount();
                        following_count = user.getFollowingCount();
                        // followers_count = 0;
                        // for (int i = 0; i < user.getBlogs().size(); i++) {
                        followers_count = user.getBlogs().get(0).getFollowersCount();
                        // }
                        likes_count = user.getLikeCount();

                        updateUI();

                        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString(Constants.PREF_KEY_USER_NAME, username);
                        editor.putString(Constants.PREF_KEY_USER_PICTURE, avatar_URL);
                        editor.putInt(Constants.PREF_KEY_USER_BLOGS, blog_posts_count);
                        editor.putInt(Constants.PREF_KEY_USER_FOLLOWINGS, following_count);
                        editor.putInt(Constants.PREF_KEY_USER_FOLLOWERS, followers_count);
                        editor.putInt(Constants.PREF_KEY_USER_LIKES, likes_count);
                        editor.apply();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void loadUserPic(final String URL) {
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                WalltyApplication.getInstance().getImageLoader().get(URL, new ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {

                        final Bitmap avatar = response.getBitmap();
                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (username_avatar != null)
                                    username_avatar.setImageBitmap(avatar);
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateUI() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fillViewsWithData();
            }
        });

    }

    public void start() {

        if (WalltyApplication.DEVELOPER_MODE)
            Log.e(TAG, "start");

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long interval;

        if (WalltyApplication.TEST_INTERVAL) {
            interval = 30000;
        } else {
            interval = WalltySettingsManager.getAlarmManagerInterval(wallty_seekBar_position);
        }

        if (WalltyApplication.DEVELOPER_MODE) {
            Log.e(TAG, "TEST START WITH INTERVAL " + interval);
        }

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, getResources().getString(R.string.wallty_set), Toast.LENGTH_SHORT).show();

        // Enable {@code WalltyBootReceiver} to automatically restart the alarm
        // when the device is rebooted.
        ComponentName receiver = new ComponentName(this, WalltyBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    public void cancel() {

        if (WalltyApplication.DEVELOPER_MODE)
            Log.e(TAG, "cancel");

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(this, getResources().getString(R.string.wallty_cancel), Toast.LENGTH_SHORT).show();

        // Disable {@code WalltyBootReceiver} so that it doesn't automatically
        // restart the alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(this, WalltyBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                if (switched) {
                    cancel();
                    SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean(Constants.SCHEDULER_CHECKED, false);
                    editor.apply();
                }
                TumblrHelper.logOutOfTumblr(this);
                removeDataFromCache();
                setDefaultWalltySettings();
                removeLastLoadedPicture();
                Intent intent = new Intent(this, LaunchActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.action_update:
                if (!(token.equals("")) && !(tokenSecret.equals("")))
                    requestTumblrInfo(token, tokenSecret);
                break;
            case R.id.action_rate_app:

                rateApp();

                break;
            case R.id.action_share:

                shareWithFriends();

                break;
        }
        return super.onOptionsItemSelected(item);
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

    private void shareWithFriends() {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, getResources().getText(R.string.share_title));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.share_title));
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_text) + "\n \n " + WalltyApplication.SHARED_LINK);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.action_share)));
    }

    private void loadPicAndSetWallpaper() {

        if (progressBarCurrentWallpaper != null)
            progressBarCurrentWallpaper.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (WalltyApplication.getInstance().isOnline()) {
                        if (!(TumblrHelper.getToken(getApplicationContext()).equals("")) && !(TumblrHelper.getTokenSecret(getApplicationContext()).equals(""))) {

                            JumblrClient client = WalltyApplication.getClient(TumblrHelper.CONSUMER_KEY, TumblrHelper.CONSUMER_SECRET);
                            client.setToken(token, tokenSecret);

                            User user = client.user();

                            if (user != null) {

                                boolean success = false;
                                int i = 0;

                                while (!success && i < 3) {
                                    if (WalltyApplication.DEVELOPER_MODE)
                                        Log.e(TAG, "user != null");

                                    Random rand = new Random();
                                    // nextInt is normally exclusive of the top
                                    // value, so add 1 to make it inclusive

                                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                                    int limit = sharedPrefs.getInt(Constants.SELECTION_SPINNER_POSITION, 1);

                                    if (WalltyApplication.DEVELOPER_MODE)
                                        Log.e(TAG, "TEST LIMIT " + WalltySettingsManager.getSelection(limit));

                                    Map<String, Integer> options = new HashMap<>();
                                    options.put("limit", WalltySettingsManager.getSelection(limit));
                                    options.put("offset", 0);

                                    int source = sharedPrefs.getInt(Constants.PICTURES_SOURCE, 1);

                                    if (WalltyApplication.DEVELOPER_MODE)
                                        Log.e(TAG, "TEST SOURCE " + source);

                                    List<Post> posts = new ArrayList<>();

                                    switch (source) {
                                        case 0:
                                            posts = user.getClient().userLikes(options);
                                            if (WalltyApplication.DEVELOPER_MODE)
                                                Log.e(TAG, "posts " + posts.size());
                                            break;
                                        case 1:
                                            posts = user.getClient().userDashboard(options);
                                            if (WalltyApplication.DEVELOPER_MODE)
                                                Log.e(TAG, "DASHBOARD");
                                            break;
                                        case 2:
                                            posts = user.getBlogs().get(0).posts(options);
                                            if (WalltyApplication.DEVELOPER_MODE)
                                                Log.e(TAG, "BLOGS  " + user.getBlogs().size());
                                            break;
                                    }

                                    int randomNum = rand.nextInt(posts.size());
                                    Post post = posts.get(randomNum);

                                    if (post.getType().equals("photo")) {
                                        if (WalltyApplication.DEVELOPER_MODE)
                                            Log.e(TAG, "photoPost         : " + ((PhotoPost) post).getPhotos().size());
                                        if (((PhotoPost) post).getPhotos().size() == 1) {
                                            List<Photo> photos = ((PhotoPost) post).getPhotos();
                                            for (final Photo photo : photos) {
                                                if (WalltyApplication.DEVELOPER_MODE)
                                                    Log.e(TAG, "photoSize      : " + photo.getOriginalSize().getUrl());
                                                success = true;
                                                loadImage(photo.getOriginalSize().getUrl());
                                            }
                                        } else {
                                            Random randSet = new Random();
                                            int randomNumSet = randSet.nextInt(((PhotoPost) post).getPhotos().size());
                                            if (WalltyApplication.DEVELOPER_MODE)
                                                Log.e(TAG, "photoPost size  " + ((PhotoPost) post).getPhotos().size() + " photoPost   " + ((PhotoPost) post).getPhotos().get(randomNumSet).getOriginalSize().getUrl());
                                            success = true;
                                            loadImage(((PhotoPost) post).getPhotos().get(randomNumSet).getOriginalSize().getUrl());
                                        }
                                    } else {
                                        if (WalltyApplication.DEVELOPER_MODE)
                                            Log.e(TAG, "post " + post.getType());
                                    }

                                    i++;
                                }
                            } else {
                                if (WalltyApplication.DEVELOPER_MODE)
                                    Log.e(TAG, "user == null");
                            }

                        } else {
                            if (WalltyApplication.DEVELOPER_MODE)
                                Log.e(TAG, "Ops. No Tokens");
                        }
                    } else {
                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(TAG, "No Internet. Sorry");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (WalltyApplication.DEVELOPER_MODE)
                        Log.e(TAG, "Unexpected exception. Sorry");
                }
            }

        }).start();
    }

    private void loadImage(final String url) {
        Handler mainHandler = new Handler(WalltyApplication.getInstance().getMainLooper());
        Runnable myRunnable = new Runnable() {

            @Override
            public void run() {

                WalltyApplication.getInstance().getImageLoader().get(url, new ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(TAG, "Image Load Error: " + error.getMessage());
                    }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                        if (response.getBitmap() != null) {

                            WallpaperManager mWallpaperManager = WallpaperManager.getInstance(getApplicationContext());

                            // try {

                            SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString(Constants.LAST_LOADED_PICTURE, url);
                            editor.apply();

                            if (progressBarCurrentWallpaper != null)
                                progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);

                            // Intent intent = new Intent(Constants.MESSAGE);
                            // // intent.putExtra(Constants.MESSAGE_PIC_URL,
                            // // url);
                            // sendBroadcast(intent);
                            // } catch (Exception e) {
                            // e.printStackTrace();
                            // }

                            try {
                                mWallpaperManager.setBitmap(response.getBitmap());

                                if (last_loaded_pic != null) {
                                    last_loaded_pic.setImageBitmap(response.getBitmap());
                                }

                            } catch (IOException e) {
                                if (WalltyApplication.DEVELOPER_MODE)
                                    Log.e(TAG, "failed to set wallpaper");
                                e.printStackTrace();
                            }

                        }
                    }
                });
            }
        };
        mainHandler.post(myRunnable);
    }

    private void setLastLoadedPic(final String url) {

        Handler mainHandler = new Handler(WalltyApplication.getInstance().getMainLooper());
        Runnable myRunnable = new Runnable() {

            @Override
            public void run() {

                WalltyApplication.getInstance().getImageLoader().get(url, new ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(TAG, "Image Load Error: " + error.getMessage());

                        if (progressBarCurrentWallpaper != null)
                            progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);

                    }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                        if (response.getBitmap() != null) {

                            if (progressBarCurrentWallpaper != null)
                                progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);

                            if (last_loaded_pic != null) {
                                last_loaded_pic.setImageBitmap(response.getBitmap());
                            }
                        }
                    }
                });
            }
        };
        mainHandler.post(myRunnable);

    }

//    // Вызовите displayInterstitial(), когда будете готовы показать
//    // межстраничное объявление.
//    public void displayInterstitial() {
//        if (WalltyApplication.getInstance().isAllow_interstitial()) {
//            if (interstitial.isLoaded()) {
//                interstitial.show();
//                WalltyApplication.getInstance().userSeenInterstitial();
//            }
//        }
//    }

    private void showRateDialog() {
        if (WalltyApplication.getInstance().showRateDialog()) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    if (rateAppDialog != null) {
                        if (!rateAppDialog.isShowing()) {
                            rateAppDialog.show();
                        }
                    } else {
                        rateAppDialog = new RateAppDialog(MainActivity.this);
                        rateAppDialog.show();
                    }
                    // new Dialogs().showRateAppDialog(MainActivity.this);
                }
            }, 1000);
        }
    }
}