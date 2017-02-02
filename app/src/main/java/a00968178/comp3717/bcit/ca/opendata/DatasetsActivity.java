package a00968178.comp3717.bcit.ca.opendata;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DatasetsActivity extends ListActivity {


    String[] list = {"test1", "test2", "test3"};

    private static final String TAG = DatasetsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datasets);

        setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));
    }


    @Override
    public void onListItemClick(ListView list, final View view, int position, long id)
    {
        Log.d(TAG, "onItemClick begin");

        String text = ((TextView)view).getText().toString();


        //final Intent intent;
        //intent = new Intent(this, DatasetsActivity.class);

        //Bundle b = new Bundle();
        //intent.putExtra("name", text);
        //intent.putExtras(b);

        //startActivity(intent);

        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

        Log.d(TAG, "onItemClick end");
    }

}
