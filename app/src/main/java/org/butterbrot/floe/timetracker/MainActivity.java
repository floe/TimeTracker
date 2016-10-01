package org.butterbrot.floe.timetracker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    String[] init_values = { "foo", "bar", "baz", "qux", "qoo" };
    Integer[] imgid = {
        android.R.drawable.ic_media_pause,
        android.R.drawable.btn_star_big_off,
        android.R.drawable.ic_media_play,
        android.R.drawable.ic_media_previous,
        android.R.drawable.ic_btn_speak_now
    };

    //ArrayList<String> values = new ArrayList<String>(Arrays.asList(init_values));
    ItemViewAdapter iva;

    boolean is_tracking = false;
    int notificationId = 001;

    NotificationCompat.Builder notificationBuilder;
    NotificationManagerCompat notificationManager;

    PendingIntent startIntent;
    PendingIntent stopIntent;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: use local database, sync with log server
        iva = new ItemViewAdapter(this, init_values, imgid);

        // set content adapter for listview
        ListView lv = (ListView) findViewById(R.id.mainlist);
        lv.setAdapter(iva);

        // set actions for start/stop button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();*/
                if (is_tracking) {
                    stop_tracking();
                } else {
                    start_tracking();
                }
            }
        });

        notification_setup();

        broadcast_setup();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
    }

    public void broadcast_setup() {
        // listen for broadcast intents
        IntentFilter filter = new IntentFilter();
        filter.addAction("start");
        filter.addAction("stop");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("start")) start_tracking();
                if (intent.getAction().equals("stop"))  stop_tracking();
            }
        };

        registerReceiver(receiver,filter);
    }

    public void notification_setup() {
        // create notification

        //startIntent = PendingIntent.getBroadcast(this, 0, new Intent("start"), 0);
        //stopIntent  = PendingIntent.getBroadcast(this, 0, new Intent("stop"),  0);

        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("TimeTracker")
            .setContentText("foobaz")
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
            .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(new int[]{0,1,2,3}));

        for (int i = 0; i < init_values.length; i++) {
            PendingIntent current = PendingIntent.getBroadcast(this, 0, new Intent(init_values[i]), 0);
            notificationBuilder.addAction(imgid[i], init_values[i], current);
        }

        // Get an instance of the NotificationManager service
        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void start_tracking() {
        //aas.add("start");
        is_tracking = true;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_media_pause);
        //notificationBuilder.mActions.clear();
        //notificationBuilder.addAction(android.R.drawable.ic_media_pause,"Stop",stopIntent);
        notificationManager.notify(notificationId,notificationBuilder.build());
    }

    public void stop_tracking() {
        //aas.add("stop");
        is_tracking = false;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_media_play);
        //notificationBuilder.mActions.clear();
        //notificationBuilder.addAction(android.R.drawable.ic_media_play,"Start",startIntent);
        notificationManager.notify(notificationId,notificationBuilder.build());
    }
}
