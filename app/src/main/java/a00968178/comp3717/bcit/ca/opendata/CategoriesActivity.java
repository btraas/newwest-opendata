package a00968178.comp3717.bcit.ca.opendata;

import android.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;

import a00968178.comp3717.bcit.ca.opendata.databases.CustomContentProvider;
import a00968178.comp3717.bcit.ca.opendata.databases.CustomLoaderCallbacks;

public class CategoriesActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

   // String[] list = Categories.getNames();
    private static final String TAG = CategoriesActivity.class.getName();
    private static boolean FIRST_TIME = true;

    private ListView listView;
    private CategoriesOpenHelper     openHelper;
    private CustomContentProvider provider;
    private SimpleCursorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);



        listView = (ListView)findViewById(R.id.categories_list);
        listView.setOnItemClickListener(this);

        init();

        Log.d(TAG, "onCreate end");
    }

    private void init()
    {
        final SQLiteDatabase db;
        final long           numEntries;
        final LoaderManager manager;

        openHelper = new CategoriesOpenHelper(getApplicationContext());

        adapter = new CustomAdapter(getBaseContext(),
                R.layout.category_row,
                null,
                new String[]
                        {
                                CategoriesOpenHelper.NAME_COLUMN,
                        },
                new int[]
                        {
                                android.R.id.text1,
                        }
                );

        listView.setAdapter(adapter);

        manager = getLoaderManager();
        manager.initLoader(0, null,
                new CustomLoaderCallbacks(CategoriesActivity.this, adapter, openHelper.getContentUri()));

        //setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));

        //openHelper = CategoriesOpenHelper.getInstance(getApplicationContext());



        db = openHelper.getWritableDatabase();
        numEntries = openHelper.getNumberOfRows();



        //String[] categories = getResources().getStringArray(R.array.categories);

       // Toast.makeText(getApplicationContext(), "Number of entries: " + numEntries, Toast.LENGTH_LONG).show();

        if(FIRST_TIME) {

            FIRST_TIME = false;

            DatabaseBuilder dbb = new DatabaseBuilder(getApplicationContext());

            dbb.cleanup(); // Delete tables
            dbb.populateCategories();   // insert data if not exists
            dbb.populateDatasets(); // insert data if not exists
        }

        display();

    }

    private void display()
    {
        final SQLiteDatabase db;
        Cursor cursor;

        db     = openHelper.getReadableDatabase();
        //cursor = openHelper.dumpTable(getApplicationContext());


        cursor = openHelper.getRows(getApplicationContext());
        while(cursor.moveToNext())
        {
            final String name;
            //name = cursor.get
            name = cursor.getString(0);
            String id   = cursor.getString(1);


            Log.d(TAG, id + "-" + name);
        }

        cursor.close();
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, final View view, int position, long id)
    {
        Log.d(TAG, "onItemClick begin");

        String text = ((TextView)(((RelativeLayout)view).findViewById(R.id.category_name))).getText().toString();
        //String text = ((TextView)view).getText().toString();
        //((TextView)view).get


        final Intent intent;
        intent = new Intent(this, DatasetsActivity.class);

        int categoryId = (int)id;

       // Toast.makeText(getApplicationContext(), "category id: "+categoryId, Toast.LENGTH_LONG).show();

        //Bundle b = new Bundle();
        intent.putExtra("id", categoryId);
        intent.putExtra("name", text);
        //intent.putExtras(b);

        startActivity(intent);

        Log.d(TAG, "onItemClick end for id: "+id);
    }

    private class SyncJob extends AsyncTask<String, Void, Bundle> {

        @Override
        protected Bundle doInBackground(String[] params) {
            // do above Server call here

            Bundle b = new Bundle();
            b.putInt("result", 0);

            int result, updated = 0;
            int datasets = (int)(new DatasetsOpenHelper(getApplicationContext())).getNumberOfRows();

            try {
                result = (new DatabaseBuilder(getApplicationContext())).sync();
                updated = result - datasets;

                if(updated == 0) b.putString("msg", "Already up-to-date");
                else b.putString("msg", "Success: Updated "+updated+" datasets");
            } catch(NoChangeException e) {
                b.putString("msg", e.getMessage());
            } catch (IOException e) {
               b.putString("msg", "Error: " + e.getMessage());
            }



            b.putInt("updated", updated);
            return b;
        }

        @Override
        protected void onPostExecute(Bundle b) {
            String message = b.getString("msg");
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            findViewById(R.id.sync_button).clearAnimation();

            if(b.getInt("updated") > 0)
            {
                finish();
                startActivity(getIntent()); // show changes
            }

        }
    }

    public void sync(View v) {
        int count = 0;

        Toast.makeText(getApplicationContext(), "Downloading datasets...", Toast.LENGTH_SHORT).show();

        Animation sync = AnimationUtils.loadAnimation(this, R.anim.sync);
        v.startAnimation(sync);

        //count = DatabaseBuilder.sync();

        SyncJob job = new SyncJob();

        job.execute();
    }

}
