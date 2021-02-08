package org.butterbrot.floe.timetracker;

// adapted from http://www.androidinterview.com/android-custom-listview-with-image-and-text-using-arrayadapter/

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ItemViewAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final MainActivity.Category[] items;

    public ItemViewAdapter(Activity context, MainActivity.Category[] items) {
        super(context, R.layout.itemview);

        this.context = context;
        this.items = items;

        for (MainActivity.Category cat: items) add(cat.name);
    }

    @NonNull
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.itemview, null, true);

        TextView txtTitle = rowView.findViewById(R.id.content);
        ImageView imageView = rowView.findViewById(R.id.icon);
        //TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        txtTitle.setText(items[position].name+" "+df.format(new Date(items[position].duration*1000L)));
        imageView.setImageResource(items[position].imgid);
        // turn white icons black for list view
        imageView.getDrawable().setTint(0xFF000000);
        //extratxt.setText("Description "+itemname[position]);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = MainActivity.instance;
                activity.start_tracking(position);
            }
        });

        if (position == MainActivity.instance.current_category)
            txtTitle.setTypeface(null, Typeface.BOLD);

        return rowView;
    }
}
