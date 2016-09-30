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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> values;
    ArrayAdapter<String> aas;

    boolean is_tracking = false;
    int notificationId = 001;

    NotificationCompat.Builder notificationBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: use local database, sync with log server
        values =  new ArrayList<String>();
        aas = new ArrayAdapter<String>(this, R.layout.textview, values);

        // set content adapter for listview
        ListView lv = (ListView) findViewById(R.id.mainlist);
        lv.setAdapter(aas);

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

    public void broadcast_setup() {
        // listen for broadcast intents
        IntentFilter filter = new IntentFilter();
        filter.addAction("start");
        filter.addAction("stop");

        BroadcastReceiver receiver = new BroadcastReceiver() {
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
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        Intent startIntent = new Intent("start");
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, 0, startIntent, 0);

        Intent stopIntent = new Intent("stop");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        notificationBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("foobar")
            .setContentText("foobaz")
            .setOngoing(true)
            .setContentIntent(viewPendingIntent)
            .addAction(android.R.drawable.ic_media_play,"Start",startPendingIntent)
            .addAction(android.R.drawable.ic_media_pause,"Stop",stopPendingIntent);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
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
        aas.add("start");
        is_tracking = true;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_media_pause);
    }

    public void stop_tracking() {
        aas.add("stop");
        is_tracking = false;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_media_play);
    }
}
