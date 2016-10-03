package org.butterbrot.floe.timetracker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // FIXME: nasty hack to keep a reference to the activity for the receiver
    // (from http://stackoverflow.com/questions/21508858/android-calling-function-from-broadcastreceiver-unable-to-start-receiver-j)
    // FIXME: this should be solved by using a separate service
    public static MainActivity instance = null;

    String[] init_values = { "Pause", "Work", "Fun", "Sport", "Travel" };
    Long[] times = { 0l, 0l, 0l, 0l, 0l };
    Integer[] imgid = {
        R.drawable.ic_pause_black_24dp,
        R.drawable.ic_work_black_24dp,
        R.drawable.ic_mood_black_24dp,
        R.drawable.ic_directions_bike_black_24dp,
        R.drawable.ic_flight_black_24dp
    };

    ItemViewAdapter iva;

    int current_category = 0;
    Calendar start_time = Calendar.getInstance();
    int notificationId = 0xF10E;

    NotificationCompat.Builder notificationBuilder;
    NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: use sharedpreferences to store times
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        for (int i = 0; i< times.length; i++)
            times[i] = settings.getLong(Integer.toString(i),0l);

        // TODO: use local database, sync with log server
        iva = new ItemViewAdapter(this, init_values, imgid, times);

        // set content adapter for listview
        // TODO: should be RecyclerView? nah, probably fine
        ListView lv = (ListView) findViewById(R.id.mainlist);
        lv.setAdapter(iva);

        notification_setup();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i< times.length; i++)
            editor.putLong(Integer.toString(i),times[i]);
        editor.commit();
    }

    public void notification_setup() {
        // create notification

        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.ic_alarm_on_black_24dp)
            .setContentTitle("TimeTracker")
            .setContentText("Current: Pause")
            // FIXME: ongoing notifications are not shown on wearable
            // FIXME: either create full wear app or re-create notification on dismissal
            // see: http://stackoverflow.com/questions/24631932/android-wear-notification-is-not-displayed-if-flag-no-clear-is-used
            //.setOngoing(true)
            .setDeleteIntent(PendingIntent.getBroadcast(this, 0, new Intent("notify"), 0))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
            .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(new int[]{0,1,2,3}));

        for (int i = 0; i < init_values.length; i++) {
            Intent intent = new Intent("org.butterbrot.floe.timetracker.Start",Uri.parse("foobar:"+init_values[i]));
            PendingIntent current = PendingIntent.getBroadcast(this, 0, intent, 0);
            Log.d("TimeTracker","creating action "+init_values[i]);
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

    public void start_tracking(int category) {
        Toast.makeText(this,init_values[category],Toast.LENGTH_SHORT).show();
        if (current_category == category) return;
        Calendar now = Calendar.getInstance();
        long difference = now.getTimeInMillis() - start_time.getTimeInMillis();
        times[current_category] += Math.round(difference/1000.0);
        start_time = now;
        current_category = category;
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        notificationBuilder.setContentText("Current: "+init_values[category]+" (since "+df.format(now.getTime())+")");
        notificationManager.notify(notificationId,notificationBuilder.build());
        iva.notifyDataSetChanged();
    }

}
