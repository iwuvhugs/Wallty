package com.iwuvhugs.wallty;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;
import com.iwuvhugs.wallty.utils.Functions;


public class SplashcreenActivity extends Activity {

    private static final String LOGTAG = SplashcreenActivity.class.getSimpleName();

    private ImageView splashLogo;
    private RelativeLayout splashBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashcreen);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        splashLogo = (ImageView) findViewById(R.id.splash_logo);
        splashBackground = (RelativeLayout) findViewById(R.id.splash_background);

        splashBackground.setBackground(new BitmapDrawable(getResources(), Functions.decodeSampledBitmapFromResource(getResources(), R.drawable.splash_background, width, height)));
        splashLogo.setImageBitmap(Functions.decodeSampledBitmapFromResource(getResources(), R.drawable.splash_logo, (int) (width - (Functions.convertDpToPixel(128f))), (int) (width - (Functions.convertDpToPixel(128f)))));

        splashLogo.setScaleX(1.05f);
        splashLogo.setScaleY(1.05f);
        splashLogo.setAlpha(0f);

        splashLogo.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(900);

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
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
