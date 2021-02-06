package org.butterbrot.floe.timetracker;

// adapted from http://www.androidinterview.com/android-custom-listview-with-image-and-text-using-arrayadapter/

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ItemViewAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Integer[] imgid;
    private final Long[] values;

    public ItemViewAdapter(Activity context, String[] itemname, Integer[] imgid, Long[] values) {
        super(context, R.layout.itemview, itemname);

        this.context = context;
        this.itemname = itemname;
        this.imgid = imgid;
        this.values = values;
    }

    @NonNull
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.itemview, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.content);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        //TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        txtTitle.setText(itemname[position]+" "+df.format(new Date(values[position]*1000l)));
        imageView.setImageResource(imgid[position]);
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
        return rowView;
    };
}
