package org.butterbrot.floe.timetracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.NotificationManagerCompat;
//import androidx.core.app.NotificationManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // FIXME: nasty hack to keep a reference to the activity for the receiver
    // (from http://stackoverflow.com/questions/21508858/android-calling-function-from-broadcastreceiver-unable-to-start-receiver-j)
    // FIXME: this should be solved by using a separate service
    public static MainActivity instance = null;

    String[] init_values = { "Pause", "Work", "Fun", "Sport", "Travel" };
    Long[] times = { 0l, 0l, 0l, 0l, 0l };
    // FIXME: vector icons don't work on Moto 360
    Integer[] imgid = {
        R.drawable.ic_pause_white_24dp,
        R.drawable.ic_work_white_bitmap,
        R.drawable.ic_mood_white_bitmap,
        R.drawable.ic_bike_white_bitmap,
        R.drawable.ic_flight_white_bitmap
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

        Log.d("TimeTracker","onCreate");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        load_settings();

        // TODO: use helper class instead of 3 separate arrays
        iva = new ItemViewAdapter(this, init_values, imgid, times);

        // set content adapter for listview
        // TODO: should be RecyclerView? nah, probably fine
        ListView lv = (ListView) findViewById(R.id.mainlist);
        lv.setAdapter(iva);

        notification_setup();
        issue_notification();
    }

    private void load_settings() {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        for (int i = 0; i< times.length; i++)
            times[i] = settings.getLong(Integer.toString(i),0l);
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
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("TimeTracker","onStop - saving data");
        save_settings();
    }

    public void notification_setup() {
        // create notification

        notificationManager = NotificationManagerCompat.from(this);
        notificationBuilder = new NotificationCompat.Builder(this, "floe");

        notificationBuilder.setSmallIcon(R.drawable.ic_alarm_on_white_24dp)
            .setContentTitle("TimeTracker")
            .setContentText("Current: Pause")
            // FIXME: ongoing notifications are not shown on wearable
            // FIXME: either create full wear app or re-create notification on dismissal
            // see: http://stackoverflow.com/questions/24631932/android-wear-notification-is-not-displayed-if-flag-no-clear-is-used
            //.setOngoing(true)
            .setDeleteIntent(PendingIntent.getBroadcast(this, 0, new Intent("org.butterbrot.floe.timetracker.Notify"), 0))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
            .setStyle(new androidx.core.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(new int[]{0,1,2,3}));

        for (int i = 0; i < init_values.length; i++) {
            Intent intent = new Intent("org.butterbrot.floe.timetracker.Start",Uri.parse("foobar:"+init_values[i]));
            PendingIntent current = PendingIntent.getBroadcast(this, 0, intent, 0);
            Log.d("TimeTracker","creating action "+init_values[i]);
            notificationBuilder.addAction(imgid[i], init_values[i], current);
            // FIXME: need to use a bitmap icon resource here, vector drawables don't work on (some) Android Wear devices
            // see https://stackoverflow.com/questions/33078751/android-wear-vector-drawable-not-visible-on-real-device
            // also doesn't seem to work with Icon and Notification.Action.Builder :-(
            // Icon icon = Icon.createWithBitmap(getBitmap(this,imgid[i]));
            // notificationBuilder.addAction(new Notification.Action.Builder(icon,init_values[i],current).build());

        }
    }

    public void issue_notification() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
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
        issue_notification();
        iva.notifyDataSetChanged();
    }

    // from https://stackoverflow.com/questions/33696488/getting-bitmap-from-vector-drawable
    private static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        VectorDrawable vectorDrawable = (VectorDrawable) drawable;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }
}
