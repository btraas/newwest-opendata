package ca.bcit.comp3717.a00968178.OpenData;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * A simple Adapter for showing the number of datasets in a category
 */
public class CategoryAdapter extends SimpleCursorAdapter {

    private Context context;
    private int layout;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);

        int nameCol = c.getColumnIndex("category_name");
        String name = c.getString(nameCol);

        int countCol = c.getColumnIndex("dataset_count");
        int count = c.getInt(countCol);

        TextView name_text = (TextView) v.findViewById(R.id.category_name);
        if (name_text != null) {
            name_text.setText(name + " ("+count+")");
        }


        return v;
    }


    public CategoryAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context = context;
        this.layout = layout;
    }


    @Override
    public void bindView(View v, Context context, Cursor c) {

        int nameCol = c.getColumnIndex("category_name");
        String name = c.getString(nameCol);

        int countCol = c.getColumnIndex("dataset_count");
        int count = c.getInt(countCol);


        TextView name_text = (TextView) v.findViewById(R.id.category_name);
        if (name_text != null) {
            name_text.setText(name + " (" + count + ")");
        }

    }


}