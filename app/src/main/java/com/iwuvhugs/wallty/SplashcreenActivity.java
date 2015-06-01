package com.iwuvhugs.wallty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;


public class SplashcreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashcreen);


        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    SplashcreenActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            switch (TumblrHelper.loginMode(SplashcreenActivity.this)) {
                                case 0:
                                    Intent intent = new Intent(SplashcreenActivity.this, LaunchActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                    SplashcreenActivity.this.finish();
                                    break;
                                case 1:
                                    if (TumblrHelper.isConnected(SplashcreenActivity.this)) {
                                        Intent tumblrLogedIntent = new Intent(SplashcreenActivity.this, MainActivity.class);
                                        tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN, TumblrHelper.getToken(SplashcreenActivity.this));
                                        tumblrLogedIntent.putExtra(TumblrLoginActivity.TUMBLR_EXTRA_TOKEN_SECRET, TumblrHelper.getTokenSecret(SplashcreenActivity.this));
                                        startActivity(tumblrLogedIntent);
                                        SplashcreenActivity.this.finish();
                                    }
                                    break;
                                case 2:
                                    Intent tumblrLoginIntent = new Intent(SplashcreenActivity.this, GuestActivity.class);
                                    startActivity(tumblrLoginIntent);
                                    SplashcreenActivity.this.finish();
                                    break;
                                default:
                                    break;

                            }


                        }
                    });

                }
            }
        }.start();
    }

}
