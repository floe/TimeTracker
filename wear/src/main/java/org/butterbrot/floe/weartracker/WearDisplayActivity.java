package org.butterbrot.floe.weartracker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class WearDisplayActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity_display);
        mTextView = findViewById(R.id.text);
    }
}
