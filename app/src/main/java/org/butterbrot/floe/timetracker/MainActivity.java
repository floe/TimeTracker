package org.butterbrot.floe.timetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // FIXME: nasty hack to keep a reference to the activity for the receiver
    // (from http://stackoverflow.com/questions/21508858/android-calling-function-from-broadcastreceiver-unable-to-start-receiver-j)
    // FIXME: this should be solved by using a separate service
    public static MainActivity instance = null;

    public static class Category {
        public Category(String s, Long l, Integer i) { name = s; duration = l; imgid = i; }
        public String name;
        public Long duration;
        public Integer imgid;
    }

    Category[] categories = {
        new Category("Pause", 0L, R.drawable.ic_pause_white_24dp ),
        new Category("Work",  0L, R.drawable.ic_work_white_24dp  ),
        new Category("Fun",   0L, R.drawable.ic_mood_white_24dp  ),
        new Category("Sport" ,0L, R.drawable.ic_bike_white_24dp  ),
        new Category("Travel",0L, R.drawable.ic_flight_white_24dp)
    };

    // TODO: how about user-configurable categories? (with floating action button or so)
    // TODO: for editing/sorting categories, RecyclerView is needed (see https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.u7416aupw)
    // TODO: better logging, maybe also with separate logfile for category changes

    ItemViewAdapter iva;

    int current_category = 0;
    Calendar start_time = Calendar.getInstance();
    final int notificationId = 0xF10E;
    final String channelId = "floe";

    NotificationCompat.Builder notificationBuilder;
    NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        Log.d("TimeTracker","onCreate");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        load_settings();

        iva = new ItemViewAdapter(categories);

        // set content adapter for listview
        RecyclerView lv = findViewById(R.id.mainlist);
        lv.setAdapter(iva);

        notification_setup();
        issue_notification();

        ItemTouchHelper mIth = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,0) {
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    final int fromPos = viewHolder.getAdapterPosition();
                    final int toPos = target.getAdapterPosition();
                    // move item in `fromPos` to `toPos` in adapter.
                    iva.onItemMove(fromPos,toPos);
                    return true;// true if moved, false otherwise
                }
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    // remove from adapter
                }
            }
        );
        mIth.attachToRecyclerView(lv);
    }

    // TODO: check if lifecycle handling is complete for load/save
    private void load_settings() {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        for (int i = 0; i< categories.length; i++)
            categories[i].duration = settings.getLong(Integer.toString(i), 0L);
        current_category = settings.getInt("current_category",0);
        start_time.setTimeInMillis( settings.getLong("start_time",start_time.getTimeInMillis()) );
    }

    private void save_settings() {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i< categories.length; i++)
            editor.putLong(Integer.toString(i),categories[i].duration);
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
        for (Category cat: categories) {
            intent = new Intent("org.butterbrot.floe.timetracker.Start",Uri.parse("foobar:"+cat.name),this,Receiver.class);
            current = PendingIntent.getBroadcast(this, 0, intent, 0);
            Log.d("TimeTracker","creating action "+cat.name);
            notificationBuilder.addAction(cat.imgid, cat.name, current);
        }
    }

    public void issue_notification() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        notificationBuilder.setContentText("Current: "+categories[current_category].name+" (since "+df.format(start_time.getTime())+")");
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public boolean about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_baseline_info_24)
            .setTitle("About TimeTracker")
            .setMessage("a minimalistic time tracking app\n(c) 2021 by Florian Echtler <floe@butterbrot.org>\nhttps://github.com/floe/TimeTracker")
            .setPositiveButton("Close",null);
        builder.create().show();
        return true;
    }

    public boolean export() {
        // TODO: actually export something :-)
        Toast.makeText( this,"Exporting data... (Warning: not yet implemented)",Toast.LENGTH_LONG).show();
        return true;
    }

    public boolean reset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle("Clear data?")
            .setMessage("Reset all durations to zero?")
            .setIcon(R.drawable.ic_baseline_warning_24)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Reset",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    for (Category cat: categories) cat.duration = 0L;
                    start_time = Calendar.getInstance();
                    start_tracking(0);
                    iva.notifyDataSetChanged();
                }
            });
        builder.create().show();
        return true;
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
            case R.id.action_reset:  return  reset();
            case R.id.action_about:  return  about();
            case R.id.action_export: return export();
            default: return super.onOptionsItemSelected(item);
        }
    }

    public void start_tracking(int category) {
        Toast.makeText(this,"Now tracking: "+categories[category].name,Toast.LENGTH_SHORT).show();
        if (current_category == category) return;
        Calendar now = Calendar.getInstance();
        long difference = now.getTimeInMillis() - start_time.getTimeInMillis();
        categories[current_category].duration += Math.round(difference/1000.0);
        start_time = now;
        current_category = category;
        issue_notification();
        iva.notifyDataSetChanged();
    }
}
