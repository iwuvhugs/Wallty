package com.iwuvhugs.wallty;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.iwuvhugs.wallty.adapters.RecyclerViewAdapter;
import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;


public class GuestActivity extends AppCompatActivity {

    private TextView guest_textView;
    private String blogList[] = {"iwuvhugs.tumblr.com", "onetouchonelie.tumblr.com", "theclassyissue.com", "whiskey-please.tumblr.com", "lookwhatifound.ru"};


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        guest_textView = (TextView) findViewById(R.id.guest_textview);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerViewAdapter(blogList);
        mRecyclerView.setAdapter(mAdapter);



        requestGuestTumblrInfo();

    }

    private void requestGuestTumblrInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // get user informations
                    JumblrClient client = WalltyApplication.getClient(TumblrHelper.CONSUMER_KEY, TumblrHelper.CONSUMER_SECRET);

                    Blog blog = client.blogInfo("iwuvhugs.tumblr.com");
                    String title = blog.getTitle();
                    guest_textView.setText(title);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
}
