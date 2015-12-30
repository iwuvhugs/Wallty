package com.iwuvhugs.wallty;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.iwuvhugs.wallty.tumblrauth.TumblrHelper;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;


public class GuestActivity extends AppCompatActivity {

    private static final String LOGTAG = GuestActivity.class.getSimpleName();
    private TextView guest_textView;
    private String blogList[] = {"iwuvhugs.tumblr.com", "onetouchonelie.tumblr.com", "theclassyissue.com", "lookwhatifound.ru"};

    private Spinner spinner;
    //    private RecyclerView mRecyclerView;
//    private RecyclerView.Adapter mAdapter;
//    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        guest_textView = (TextView) findViewById(R.id.guest_textview);
        spinner = (Spinner) findViewById(R.id.spinner);

//        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//
//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        mRecyclerView.setHasFixedSize(true);
//
//        // use a linear layout manager
//        mLayoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//


        // specify an adapter (see also next example)
//        mAdapter = new RecyclerViewAdapter(blogList);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, blogList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(LOGTAG, "" + position);
                Toast.makeText(GuestActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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
