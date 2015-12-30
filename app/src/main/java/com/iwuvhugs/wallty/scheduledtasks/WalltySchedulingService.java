package com.iwuvhugs.wallty.scheduledtasks;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.iwuvhugs.wallty.WalltyApplication;
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

public class WalltySchedulingService extends IntentService {

	private static final String LOGTAG = WalltySchedulingService.class.getSimpleName();

	public WalltySchedulingService() {
		super("WalltySchedulingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		try {
			if (WalltyApplication.getInstance().isOnline()) {
				if (!(TumblrHelper.getToken(getApplicationContext()).equals("")) && !(TumblrHelper.getTokenSecret(getApplicationContext()).equals(""))) {

					JumblrClient client = WalltyApplication.getClient(TumblrHelper.CONSUMER_KEY, TumblrHelper.CONSUMER_SECRET);
					client.setToken(TumblrHelper.getToken(getApplicationContext()), TumblrHelper.getTokenSecret(getApplicationContext()));

					User user = client.user();

					if (user != null) {

						boolean success = false;
						int i = 0;

						while (!success && i < 3) {
							if (WalltyApplication.DEVELOPER_MODE)
								Log.e(LOGTAG, "user != null");

							// int likes_count = user.getLikeCount();
							// Log.e(LOGTAG, "likes_count " + likes_count);

							Random rand = new Random();
							// nextInt is normally exclusive of the top
							// value,
							// so add 1 to make it inclusive

							SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
							int limit = sharedPrefs.getInt(Constants.SELECTION_SPINNER_POSITION, 1);

							if (WalltyApplication.DEVELOPER_MODE)
								Log.e(LOGTAG, "TEST LIMIT " + WalltySettingsManager.getSelection(limit));

							Map<String, Integer> options = new HashMap<String, Integer>();
							options.put("limit", WalltySettingsManager.getSelection(limit));
							options.put("offset", 0);

							int source = sharedPrefs.getInt(Constants.PICTURES_SOURCE, 0);

							if (WalltyApplication.DEVELOPER_MODE)
								Log.e(LOGTAG, "TEST SOURCE " + source);

							List<Post> posts = new ArrayList<Post>();

							switch (source) {
							case 0:
								/* List<Post> */
								posts = user.getClient().userLikes(options);
								if (WalltyApplication.DEVELOPER_MODE)
									Log.e(LOGTAG, "posts " + posts.size());
								break;
							case 1:
								posts = user.getClient().userDashboard(options);
								if (WalltyApplication.DEVELOPER_MODE)
									Log.e(LOGTAG, "DASHBOARD");
								break;
							case 2:
								posts = user.getBlogs().get(0).posts(options);
								if (WalltyApplication.DEVELOPER_MODE)
									Log.e(LOGTAG, "BLOGS  " + user.getBlogs().size());
								break;
							}

							int randomNum = rand.nextInt(posts.size());
							Post post = posts.get(randomNum);

							if (post.getType().equals("photo")) {
								if (WalltyApplication.DEVELOPER_MODE)
									Log.e(LOGTAG, "photoPost         : " + ((PhotoPost) post).getPhotos().size());
								if (((PhotoPost) post).getPhotos().size() == 1) {
									List<Photo> photos = ((PhotoPost) post).getPhotos();
									for (final Photo photo : photos) {
										if (WalltyApplication.DEVELOPER_MODE)
											Log.e(LOGTAG, "photoSize      : " + photo.getOriginalSize().getUrl());
										success = true;
										loadImage(photo.getOriginalSize().getUrl()/*
																				 * ,
																				 * bundle
																				 */);
									}
								} else {
									Random randSet = new Random();
									int randomNumSet = randSet.nextInt(((PhotoPost) post).getPhotos().size());
									if (WalltyApplication.DEVELOPER_MODE)
										Log.e(LOGTAG, "photoPost size  " + ((PhotoPost) post).getPhotos().size() + " photoPost   " + ((PhotoPost) post).getPhotos().get(randomNumSet).getOriginalSize().getUrl());
									success = true;
									loadImage(((PhotoPost) post).getPhotos().get(randomNumSet).getOriginalSize().getUrl()/* */);
								}
							} else {
								if (WalltyApplication.DEVELOPER_MODE)
									Log.e(LOGTAG, "post " + post.getType());
							}

							i++;
						}
					} else {
						if (WalltyApplication.DEVELOPER_MODE)
							Log.e(LOGTAG, "user == null");
					}

				} else {
					if (WalltyApplication.DEVELOPER_MODE)
						Log.e(LOGTAG, "Ops. No Tokens");
				}
			} else {
				if (WalltyApplication.DEVELOPER_MODE)
					Log.e(LOGTAG, "No Internet. Sorry");
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (WalltyApplication.DEVELOPER_MODE)
				Log.e(LOGTAG, "Unexpected exception. Sorry");
		}
		// Release the wake lock provided by the BroadcastReceiver.
		WalltyAlarmReceiver.completeWakefulIntent(intent);

	}

	private void loadImage(final String url/* , final Bundle bundle */) {

		Handler mainHandler = new Handler(WalltyApplication.getInstance().getMainLooper());
		Runnable myRunnable = new Runnable() {

			@Override
			public void run() {

				WalltyApplication.getInstance().getImageLoader().get(url, new ImageListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if (WalltyApplication.DEVELOPER_MODE)
							Log.e(LOGTAG, "Image Load Error: " + error.getMessage());
					}

					@Override
					public void onResponse(ImageContainer response, boolean arg1) {
						if (response.getBitmap() != null) {

							WallpaperManager mWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
							try {
								mWallpaperManager.setBitmap(response.getBitmap());

								try {

									SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
									sharedPrefs = getApplicationContext().getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = sharedPrefs.edit();
									editor.putString(Constants.LAST_LOADED_PICTURE, url);
									editor.commit();

									Intent intent = new Intent(Constants.MESSAGE);
									// intent.putExtra(Constants.MESSAGE_PIC_URL,
									// url);
									sendBroadcast(intent);
								} catch (Exception e) {
									e.printStackTrace();
								}

							} catch (IOException e) {
								if (WalltyApplication.DEVELOPER_MODE)
									Log.e(LOGTAG, "failed to set wallpaper");
								e.printStackTrace();
							}
						}
					}
				});
			}
		};
		mainHandler.post(myRunnable);
	}
}
