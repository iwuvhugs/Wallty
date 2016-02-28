package com.iwuvhugs.wallty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.iwuvhugs.wallty.dialogs.NoConnectionDialog;
import com.iwuvhugs.wallty.dialogs.RateAppDialog;
import com.iwuvhugs.wallty.scheduledtasks.WalltyAlarmReceiver;
import com.iwuvhugs.wallty.scheduledtasks.WalltyBootReceiver;
import com.iwuvhugs.wallty.tumblrauth.Constants;
import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;
import com.iwuvhugs.wallty.utils.WalltySettingsManager;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class GuestActivity extends AppCompatActivity {

    private static final String TAG = GuestActivity.class.getSimpleName();
    //    private TextView guest_textView;

    private PendingIntent pendingIntent;
    private BroadcastReceiver br;


//    private String blogAvatarList[];

    private ActionBar guestActionBar;


    /*Settings page*/
    private LinearLayout guest_settings_layout;
    private Spinner spinner;
    private TextView guest_seekBar_textView;
    private SeekBar guest_seekBar;


    /*Switch Layout*/
//    private RelativeLayout switchLayout;
    private SwitchCompat walltySwich;

    /*New Wallpaper page*/
    private Button getNewWallpaperButton;
    private ImageView lastLoadedPic;
    private ProgressBar progressBarCurrentWallpaper;
    private View currentWallpaperMask;


    /*Settings params*/
    private ArrayList<String> list;
    private String lastSelectedBlog;
    private int wallty_blog_selection;
    private int wallty_seekBar_position;
    private String[] wallty_seekBar_states;


    /*Switch mode*/
    private boolean switched = false;
    /*Last Wallpaper URL*/
    private String lastURL;


    /*Dialogs*/
    private AlertDialog noConnectionDialog;
    private AlertDialog rateAppDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get tracker.
        Tracker t = ((WalltyApplication) getApplication()).getTracker(WalltyApplication.TrackerName.APP_TRACKER);
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("Wallty Guest Activity");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        setContentView(R.layout.activity_guest);

        initCustomActionBar();
        initViews();

        tuneBlogSpinner();
        setListeners();

        /* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(GuestActivity.this, WalltyAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(GuestActivity.this, 0, alarmIntent, 0);

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


        showRateDialog();
    }

    @Override
    protected void onResume() {
        if (!WalltyApplication.getInstance().isOnline()) {
            if (noConnectionDialog != null) {
                if (!noConnectionDialog.isShowing()) {
                    noConnectionDialog.show();
                }
            } else {
                noConnectionDialog = new NoConnectionDialog(this).getAlertDialog();
                noConnectionDialog.show();
            }
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

    private void initCustomActionBar() {

        guestActionBar = getSupportActionBar();
        guestActionBar.setDisplayShowTitleEnabled(false);
        guestActionBar.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.actionbar_background, null));

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_acb_main, null);
        TextView actionbar_title = (TextView) mCustomView.findViewById(R.id.actionbar_title);
//        if (getApplicationContext().getResources().getConfiguration().locale.getLanguage().equals(Locale.ENGLISH)) {
        actionbar_title.setTypeface(WalltyApplication.Nabila);
//        } else {
//            actionbar_title.setTypeface(WalltyApplication.dinRoundProRegular);
//        }
        actionbar_title.setText(getResources().getString(R.string.title_activity_guest));

        guestActionBar.setCustomView(mCustomView);
        guestActionBar.setDisplayShowCustomEnabled(true);

    }

    private void initViews() {

        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);

        guest_settings_layout = (LinearLayout) findViewById(R.id.guest_settings_layout);
        spinner = (Spinner) findViewById(R.id.spinner);
        guest_seekBar_textView = (TextView) findViewById(R.id.wallty_seekBar_textView);
        guest_seekBar = (SeekBar) findViewById(R.id.wallty_seekBar);

        wallty_seekBar_states = getResources().getStringArray(R.array.alarm_intervals);
        wallty_seekBar_position = sharedPrefs.getInt(Constants.INTERVAL_SEEKBAR_POSITION, 1);
        guest_seekBar.setProgress(wallty_seekBar_position);
        guest_seekBar_textView.setText(wallty_seekBar_states[wallty_seekBar_position]);

           /*New Wallpaper page*/
        getNewWallpaperButton = (Button) findViewById(R.id.get_new_wallpaper);
        getNewWallpaperButton.setTypeface(WalltyApplication.dinRoundProRegular);
        lastLoadedPic = (ImageView) findViewById(R.id.current_wallpaper);
        progressBarCurrentWallpaper = (ProgressBar) findViewById(R.id.progressBarCurrentWallpaper);
        currentWallpaperMask = findViewById(R.id.current_wallpaper_mask);


        lastURL = sharedPrefs.getString(Constants.LAST_LOADED_PICTURE, "");
        if (lastURL != null) {
            if (!lastURL.equals("")) {
                setLastLoadedPic(lastURL);
            } else {
                lastLoadedPic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_image));
            }
        } else {
            lastLoadedPic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_image));
        }

        /*Switch Layout*/
//        switchLayout = (RelativeLayout) findViewById(R.id.switch_layout);
        walltySwich = (SwitchCompat) findViewById(R.id.wallty_switch);
        switched = sharedPrefs.getBoolean(Constants.SCHEDULER_CHECKED, false);
        walltySwich.setChecked(switched);

        switchGuestLayout();

    }

    private void switchGuestLayout() {
        if (switched) {
            getNewWallpaperButton.setVisibility(View.VISIBLE);
            guest_settings_layout.setVisibility(View.GONE);
        } else {
            getNewWallpaperButton.setVisibility(View.GONE);
            guest_settings_layout.setVisibility(View.VISIBLE);
        }
    }

    private void tuneBlogSpinner() {

        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        lastSelectedBlog = sharedPrefs.getString(Constants.BLOG_SELECTED, "");

        list = new ArrayList<>();

        if (!(lastSelectedBlog.equals(""))) {
            wallty_blog_selection = (-1);
            wallty_blog_selection = Arrays.asList(WalltyApplication.getInstance().blogList).indexOf(lastSelectedBlog);

            ArrayList<String> oldList = new ArrayList<String>(Arrays.asList(WalltyApplication.getInstance().blogList));
            if (wallty_blog_selection >= 0) {
                list.addAll(oldList);
            } else {
                wallty_blog_selection = 0;
                list.addAll(oldList);
                list.add(0, lastSelectedBlog);
            }

//            list = new ArrayList<String>(Arrays.asList(WalltyApplication.getInstance().blogList));
        } else {
            wallty_blog_selection = 0;
            list = new ArrayList<String>(Arrays.asList(WalltyApplication.getInstance().blogList));
        }


        // specify an adapter (see also next example)
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, R.layout.wallty_spinner_item, list/*WalltyApplication.getInstance().blogList*/);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.wallty_spinner_dropdown_item);
        // Apply the adapter to the spinner

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.e(TAG, "" + position);
                wallty_blog_selection = position;

//                SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPrefs.edit();
//                editor.putInt(Constants.BLOG_SELECTED, wallty_blog_selection);
//                editor.apply();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//        wallty_blog_selection = sharedPrefs.getInt(Constants.BLOG_SELECTED, 0);
        spinner.setSelection(wallty_blog_selection);
    }


    private void setListeners() {

        guest_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wallty_seekBar_position = progress;
                guest_seekBar_textView.setText(wallty_seekBar_states[progress]);
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(Constants.INTERVAL_SEEKBAR_POSITION, progress);
                editor.apply();
            }
        });


        walltySwich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    lastSelectedBlog = list.get(wallty_blog_selection);
                    SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(Constants.BLOG_SELECTED, lastSelectedBlog);
                    editor.apply();


                    start();

                    if (progressBarCurrentWallpaper != null) {
                        progressBarCurrentWallpaper.setVisibility(View.VISIBLE);
                        currentWallpaperMask.animate().setDuration(250).alpha(0.7f);
                    }

//                    loadPicAndSetWallpaper();

                    if (!WalltyApplication.getInstance().isOnline()) {
                        if (noConnectionDialog != null) {
                            if (!noConnectionDialog.isShowing()) {
                                noConnectionDialog.show();
                            }
                        } else {
                            noConnectionDialog = new NoConnectionDialog(GuestActivity.this).getAlertDialog();
                            noConnectionDialog.show();
                        }
                    }
                } else {
                    cancel();

//                    displayInterstitial();
                }
                switched = isChecked;
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean(Constants.SCHEDULER_CHECKED, isChecked);
                editor.apply();

                switchGuestLayout();
            }

        });

        getNewWallpaperButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                helpNewbie();
//                displayInterstitial();
                if (progressBarCurrentWallpaper != null) {
                    progressBarCurrentWallpaper.setVisibility(View.VISIBLE);
                    currentWallpaperMask.animate().setDuration(250).alpha(0.7f);
                }

                if (WalltyApplication.getInstance().isOnline()) {
                    loadPicAndSetWallpaper();
                } else {

                    if (noConnectionDialog != null) {
                        if (!noConnectionDialog.isShowing()) {
                            noConnectionDialog.show();
                        }
                    } else {
                        noConnectionDialog = new NoConnectionDialog(GuestActivity.this).getAlertDialog();
                        noConnectionDialog.show();
                    }

                    if (progressBarCurrentWallpaper != null) {
                        progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);
                        currentWallpaperMask.animate().setDuration(250).alpha(0f);
                    }
                }
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
        getMenuInflater().inflate(R.menu.menu_guest, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                setDefaultWalltySettings();
                removeLastLoadedPicture();
                Intent intent = new Intent(this, LaunchActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.action_rate_app:
                WalltyApplication.getInstance().rateApp();
                break;
            case R.id.action_share:
                WalltyApplication.getInstance().shareWithFriends();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestGuestTumblrInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // get user informations
                    JumblrClient client = WalltyApplication.getClient(getResources().getString(R.string.CONSUMER_KEY), getResources().getString(R.string.CONSUMER_SECRET));

                    Blog blog = client.blogInfo(WalltyApplication.getInstance().blogList[wallty_blog_selection]);
                    final String title = blog.getTitle();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void setLastLoadedPic(final String url) {

        Handler mainHandler = new Handler(WalltyApplication.getInstance().getMainLooper());
        Runnable myRunnable = new Runnable() {

            @Override
            public void run() {

                WalltyApplication.getInstance().getImageLoader().get(url, new ImageLoader.ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (WalltyApplication.DEVELOPER_MODE)
                            Log.e(TAG, "Image Load Error: " + error.getMessage());

                        if (progressBarCurrentWallpaper != null) {
                            progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);
                            currentWallpaperMask.animate().setDuration(250).alpha(0f);
                        }

                    }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                        if (response.getBitmap() != null) {

                            if (progressBarCurrentWallpaper != null) {
                                progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);
                                currentWallpaperMask.animate().setDuration(250).alpha(0f);
                            }

                            if (lastLoadedPic != null) {
                                lastLoadedPic.setImageBitmap(response.getBitmap());
                            }
                        }
                    }
                });
            }
        };
        mainHandler.post(myRunnable);

    }

    private void setDefaultWalltySettings() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(Constants.INTERVAL_SEEKBAR_POSITION, 1);
//        editor.putInt(Constants.BLOG_SELECTED, 0);
        editor.putString(Constants.BLOG_SELECTED, "");
//        editor.putInt(Constants.SELECTION_SPINNER_POSITION, 1);
//        editor.putInt(Constants.PICTURES_SOURCE, 1);
        editor.apply();
    }

    private void removeLastLoadedPicture() {
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(Constants.LAST_LOADED_PICTURE);
        editor.apply();
    }


    private void loadPicAndSetWallpaper() {

//        Log.e(TAG, "loadPicAndSetWallpaper");

        if (progressBarCurrentWallpaper != null) {
            progressBarCurrentWallpaper.setVisibility(View.VISIBLE);
            currentWallpaperMask.animate().setDuration(250).alpha(0.7f);
        }

//        if (!lastSelectedBlog.equals(""))
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (WalltyApplication.getInstance().isOnline()) {

                        JumblrClient client = WalltyApplication.getClient(getResources().getString(R.string.CONSUMER_KEY),
                                getResources().getString(R.string.CONSUMER_SECRET));
                        Blog blog = client.blogInfo(lastSelectedBlog);

//                        Log.e(TAG, " selected_blog MODE " + lastSelectedBlog);

                        if (blog != null) {

                            boolean success = false;
                            int i = 0;

                            while (!success && i < 3) {
                                if (WalltyApplication.DEVELOPER_MODE)
                                    Log.e(TAG, "user != null");

                                Random rand = new Random();
                                // nextInt is normally exclusive of the top
                                // value, so add 1 to make it inclusive


                                Map<String, Integer> options = new HashMap<>();
                                options.put("limit", WalltySettingsManager.getSelection(2));
                                options.put("offset", 0);


                                List<Post> posts;


                                posts = blog.posts(options);
                                if (WalltyApplication.DEVELOPER_MODE)
                                    Log.e(TAG, "posts  " + blog.getPostCount());

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
                                Log.e(TAG, "blog == null");
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

                WalltyApplication.getInstance().getImageLoader().get(url, new ImageLoader.ImageListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (WalltyApplication.DEVELOPER_MODE)
                                    Log.e(TAG, "Image Load Error: " + error.getMessage());
                            }

                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                                if (response.getBitmap() != null) {
                                    final Bitmap bitmap = response.getBitmap();

                                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString(Constants.LAST_LOADED_PICTURE, url);
                                    editor.apply();

                                    if (progressBarCurrentWallpaper != null) {
                                        progressBarCurrentWallpaper.setVisibility(View.INVISIBLE);
                                        currentWallpaperMask.animate().setDuration(250).alpha(0f);
                                    }

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            WallpaperManager mWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                                            try {
                                                mWallpaperManager.setBitmap(bitmap);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();


                                    if (lastLoadedPic != null) {
                                        lastLoadedPic.setImageBitmap(bitmap);
                                    }
                                }
                            }
                        }
                );
            }
        };
        mainHandler.post(myRunnable);
    }


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
                        rateAppDialog = new RateAppDialog(GuestActivity.this);
                        rateAppDialog.show();
                    }
                }
            }, 1000);
        }
    }

}
