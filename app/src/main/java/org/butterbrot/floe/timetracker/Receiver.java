package org.butterbrot.floe.timetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        MainActivity activity = MainActivity.instance;
        Log.d("TimeTracker","got broadcast: "+intent.getAction()+" "+intent.getData().getSchemeSpecificPart());
        for (int i = 0; i < activity.init_values.length; i++) {
            if (intent.getData().getSchemeSpecificPart().equals(activity.init_values[i])) activity.start_tracking(i);
        }
    }
}
