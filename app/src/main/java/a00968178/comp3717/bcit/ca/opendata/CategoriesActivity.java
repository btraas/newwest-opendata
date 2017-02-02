package a00968178.comp3717.bcit.ca.opendata;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CategoriesActivity extends ListActivity {

    String[] list = Categories.getNames();
    private static final String TAG = CategoriesActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_categories);



        setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));

        Log.d(TAG, "onCreate end");
    }

    @Override
    public void onListItemClick(ListView list, final View view, int position, long id)
    {
        Log.d(TAG, "onItemClick begin");

        String text = ((TextView)view).getText().toString();


        final Intent intent;
        intent = new Intent(this, DatasetsActivity.class);

        //Bundle b = new Bundle();
        intent.putExtra("name", text);
        //intent.putExtras(b);

        startActivity(intent);

        Log.d(TAG, "onItemClick end");
    }

}
