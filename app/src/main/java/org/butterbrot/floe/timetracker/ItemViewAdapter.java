package org.butterbrot.floe.timetracker;

// adapted from http://www.androidinterview.com/android-custom-listview-with-image-and-text-using-arrayadapter/

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// based on https://developer.android.com/guide/topics/ui/layout/recyclerview#java
public class ItemViewAdapter extends RecyclerView.Adapter<ItemViewAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = (TextView) view.findViewById(R.id.content);
            imageView = (ImageView) view.findViewById(R.id.icon);
            // FIXME: seems hackish. can't you get the ViewHolder from the view in onClick?
            final ViewHolder v = this;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity activity = MainActivity.instance;
                    activity.start_tracking(v.getLayoutPosition());
                }
            });
        }

        public TextView getTextView() { return textView; }
        public ImageView getImageView() { return imageView; }
    }

    private final MainActivity.Category[] items;

    public ItemViewAdapter(MainActivity.Category[] dataSet) {
        items = dataSet;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override public int getItemCount() {
        return items.length;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.itemview, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        TextView txtTitle = viewHolder.getTextView();
        ImageView imageView = viewHolder.getImageView();

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        txtTitle.setText(items[position].name+" "+df.format(new Date(items[position].duration*1000L)));
        imageView.setImageResource(items[position].imgid);
        // turn white icons black for list view
        imageView.getDrawable().setTint(0xFF000000);

        if (position == MainActivity.instance.current_category)
            txtTitle.setTypeface(null, Typeface.BOLD);
        else
            txtTitle.setTypeface(null, Typeface.NORMAL);
    }
}
