package a00968178.comp3717.bcit.ca.opendata;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import a00968178.comp3717.bcit.ca.opendata.databases.*;
import a00968178.comp3717.bcit.ca.opendata.databases.CustomContentProvider;


public class SingleDatasetActivity extends AppCompatActivity  {

    private ArrayAdapter list;

    private DatasetsOpenHelper openHelper;
    private SimpleCursorAdapter adapter;

    private CustomContentProvider provider;

    private int datasetId;
    private String URL;

    //String[] list = {"test1", "test2", "test3"};

    private static final String TAG = SingleDatasetActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_dataset);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String title = intent.getStringExtra("name");
        this.datasetId = intent.getIntExtra("id", 0);
        //Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG).show();

        getSupportActionBar().setTitle(title);



        display();

    }



    private void display()
    {
       // final SQLiteDatabase db;
        Cursor cursor;
        OpenHelper openHelper = new DatasetsOpenHelper(getApplicationContext());

        //db     = openHelper.getReadableDatabase();
        //cursor = openHelper.dumpTable(getApplicationContext());


        cursor = openHelper.getRows(getApplicationContext(), "_id = " + datasetId );
        cursor.moveToNext();
        //name = cursor.get
        Log.d(TAG, cursor.getString(0)); // dataset id
        Log.d(TAG, cursor.getString(1)); // category id
        Log.d(TAG, cursor.getString(2)); // name
        Log.d(TAG, cursor.getString(3)); // desc
        Log.d(TAG, cursor.getString(4)); // link


        //Toast.makeText(getApplicationContext(), cursor.getString(0), Toast.LENGTH_LONG).show();

        // Log.d(TAG, id + "-" + name);

        ((TextView)this.findViewById(R.id.dataset_title)).setText(cursor.getString(2));
        ((TextView)this.findViewById(R.id.dataset_description)).setText(cursor.getString(3));
        this.URL = cursor.getString(4);

        cursor.close();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // we can do this because this is only ever invoked from DatasetsActivity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openWebview(final View view)
    {
        //Log.d(TAG, "openWebview begin");
        final Intent intent;

        intent = new Intent(this, DatasetWebview.class);
        intent.putExtra("URL", this.URL);
        startActivity(intent);
        //Log.d(TAG, "goOwnRoutesCreate end");
    }

}
