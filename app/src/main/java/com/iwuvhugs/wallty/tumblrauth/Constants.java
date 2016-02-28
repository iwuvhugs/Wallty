package com.iwuvhugs.wallty.tumblrauth;

/**
 * Created by wchgs on 07.05.15.
 */
public class Constants {

    public static final String TAG = "TumblrLogin";

    // TUMBLR OAUTH====================================================
    public static final String PREFERENCE_NAME = "tumblr_oauth";
    public static final String PREF_KEY_TOKEN_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TOKEN = "oauth_token";
    public static final String PREF_KEY_SCREEN_NAME = "oauth_screen_name";
    public static final String PREF_KEY_ACCESS_TOKEN_INFOS = "oauth_access_token";
    public static final String PREF_KEY_USER = "tumblr_user";
    public static final String PREF_KEY_LOGIN_MODE = "login_mode";
    // public static final String PREF_KEY_PICTURE = "tumblr_user_picture";
    // public static final String PREF_KEY_USER_NAME = "tumblr_user_name";
    // public static final String PREF_KEY_USER_ID = "tumblr_user_id";

    public static final String TUMBLR_CALLBACK_URL = "x-oauthflow-tumblr://tumblrlogin";
    public static final String REQUEST_TOKEN_URL = "http://www.tumblr.com/oauth/request_token";
    public static final String ACCESS_TOKEN_URL = "http://www.tumblr.com/oauth/access_token";
    public static final String AUTH_URL = "http://www.tumblr.com/oauth/authorize";

    public static final String IEXTRA_AUTH_URL = "auth_url";
    public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
    public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";
    // =================================================================
    // public static final String PREF_KEY_PICTURE = "tumblr_user_picture";
    public static final String PREF_KEY_USER_NAME = "tumblr_user_name";
    public static final String PREF_KEY_USER_PICTURE = "tumblr_user_picture";
    // public static final String PREF_KEY_USER_DASHBOARD_IS_EMPTY =
    // "tumblr_user_dashboard_is_empty";
    public static final String PREF_KEY_USER_LIKES = "tumblr_user_likes";
    public static final String PREF_KEY_USER_BLOGS = "tumblr_user_blogs";
    public static final String PREF_KEY_USER_FOLLOWINGS = "tumblr_user_followings";
    public static final String PREF_KEY_USER_FOLLOWERS = "tumblr_user_followers";
    // public static final String PREF_KEY_USER_DASHBOARD =
    // "tumblr_user_dashboard";
    // public static final String PREF_KEY_USER_ID = "tumblr_user_id";
    // =================================================================

    // BACKGROUND TASKS
    public static final String SCHEDULER_CHECKED = "scheduler_checked";
    //

    // WALLTY SETTINGS
    public static final String INTERVAL_SEEKBAR_POSITION = "interval_seekbar_position";
    public static final String SELECTION_SPINNER_POSITION = "selection_spinner_position";
    public static final String PICTURES_SOURCE = "pictures_source";
    public static final String BLOG_SELECTED = "blog_selected";
    //

    // MESSENGER PROVIDES CONNECTION BETWEEN SERVICE AND MAON ACTIVITY
    // public static final String MESSAGE_PIC_URL = "tumblr message pic URL";
    public static final String MESSAGE = "tumblr message";
    public static final String LAST_LOADED_PICTURE = "last loaded picture";
    //

}
