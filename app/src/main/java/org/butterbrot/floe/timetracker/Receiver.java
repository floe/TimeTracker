package org.butterbrot.floe.timetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity activity = MainActivity.instance;
        Log.d("TimeTracker","got broadcast: "+intent.getAction());
        if (intent.getAction() == null) return;
        if (intent.getAction().equals("org.butterbrot.floe.timetracker.Notify")) {
            activity.issue_notification();
            return;
        }
        if (intent.getData() == null) return;
        Log.d("TimeTracker","starting category: "+intent.getData().getSchemeSpecificPart());
        for (int i = 0; i < activity.categories.length; i++) {
            if (intent.getData().getSchemeSpecificPart().equals(activity.categories[i].name)) activity.start_tracking(i);
        }
    }
}
