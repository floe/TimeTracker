package org.butterbrot.floe.timetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // FIXME: nasty hack to keep a reference to the activity for the receiver
    // (from http://stackoverflow.com/questions/21508858/android-calling-function-from-broadcastreceiver-unable-to-start-receiver-j)
    // FIXME: this should be solved by using a separate service
    public static MainActivity instance = null;

    // TODO: should rather use a helper class instead of 3 arrays
    String[] init_values = { "Pause", "Work", "Fun", "Sport", "Travel" };
    Long[] times = { 0L, 0L, 0L, 0L, 0L };
    Integer[] imgid = {
        R.drawable.ic_pause_white_24dp,
        R.drawable.ic_work_white_24dp,
        R.drawable.ic_mood_white_24dp,
        R.drawable.ic_bike_white_24dp,
        R.drawable.ic_flight_white_24dp
    };

    // TODO: how about user-configurable categories? (with floating action button or so)
    // TODO: better logging, maybe also with separate logfile for category changes

    ItemViewAdapter iva;

    int current_category = 0;
    Calendar start_time = Calendar.getInstance();
    int notificationId = 0xF10E;
    String channelId = "floe";

    NotificationCompat.Builder notificationBuilder;
    NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        Log.d("TimeTracker","onCreate");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        load_settings();

        // TODO: use helper class instead of 3 separate arrays
        iva = new ItemViewAdapter(this, init_values, imgid, times);

        // set content adapter for listview
        ListView lv = (ListView) findViewById(R.id.mainlist);
        lv.setAdapter(iva);

        notification_setup();
        issue_notification();
    }

    // TODO: check if lifecycle handling is complete for load/save
    private void load_settings() {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        for (int i = 0; i< times.length; i++)
            times[i] = settings.getLong(Integer.toString(i), 0L);
        current_category = settings.getInt("current_category",0);
        start_time.setTimeInMillis( settings.getLong("start_time",start_time.getTimeInMillis()) );
    }

    private void save_settings() {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i< times.length; i++)
            editor.putLong(Integer.toString(i),times[i]);
        editor.putInt("current_category",current_category);
        editor.putLong("start_time",start_time.getTimeInMillis());
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("TimeTracker","onStop - saving data");
        save_settings();
    }

    public void notification_setup() {

        // create notification channel (on Oreo or newer)
        notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(channelId, "TimeTracker Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(nc);
        }

        // create notification itself
        notificationBuilder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alarm_on_white_24dp)
            .setContentTitle("TimeTracker")
            .setContentText("Current: Pause")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
            .setStyle(new androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle().setShowActionsInCompactView(0,1,2));

        // set intent for dismissing notification
        Intent intent = new Intent("org.butterbrot.floe.timetracker.Notify",Uri.parse("foobar:nope"),this,Receiver.class);
        PendingIntent current = PendingIntent.getBroadcast(this, 0, intent, 0);
        notificationBuilder.setDeleteIntent(current);

        // set individual category actions
        for (int i = 0; i < init_values.length; i++) {
            intent = new Intent("org.butterbrot.floe.timetracker.Start",Uri.parse("foobar:"+init_values[i]),this,Receiver.class);
            current = PendingIntent.getBroadcast(this, 0, intent, 0);
            Log.d("TimeTracker","creating action "+init_values[i]);
            notificationBuilder.addAction(imgid[i], init_values[i], current);
        }
    }

    public void issue_notification() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        notificationBuilder.setContentText("Current: "+init_values[current_category]+" (since "+df.format(start_time.getTime())+")");
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
        switch (item.getItemId()) {
            case R.id.action_reset:
                // TODO: add dialog before wiping data
                start_tracking(0);
                Arrays.fill(times,0L);
                iva.notifyDataSetChanged();
                return true;
            case R.id.action_export:
                // TODO: actually export something :-)
                return true;
            // TODO: add an "about" action
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void start_tracking(int category) {
        Toast.makeText(this,"Now tracking: "+init_values[category],Toast.LENGTH_SHORT).show();
        if (current_category == category) return;
        Calendar now = Calendar.getInstance();
        long difference = now.getTimeInMillis() - start_time.getTimeInMillis();
        times[current_category] += Math.round(difference/1000.0);
        start_time = now;
        current_category = category;
        issue_notification();
        iva.notifyDataSetChanged();
    }
}
