package ca.bcit.comp3717.a00968178.OpenData;

import android.app.LoaderManager;
import android.content.Intent;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;

import ca.bcit.comp3717.a00968178.OpenData.databases.*;

public class CategoriesActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

   // String[] list = Categories.getNames();
    private static final String TAG = CategoriesActivity.class.getName();
    private static boolean FIRST_TIME = true;

    private ListView listView;
    private CategoriesOpenHelper     categoriesOpenHelper;
    private DatasetsOpenHelper       datasetsOpenHelper;
    private CustomContentProvider provider;
    private SimpleCursorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        listView = (ListView)findViewById(R.id.categories_list);
        listView.setOnItemClickListener(this);

        // Set instance vars (element references)



        init();

        Log.d(TAG, "onCreate end");
    }

    private void init()
    {
        final SQLiteDatabase db;
        final long           numEntries;
        final LoaderManager manager;

        categoriesOpenHelper = new CategoriesOpenHelper(getApplicationContext());
        datasetsOpenHelper   = new DatasetsOpenHelper(getApplicationContext());

        adapter = new CategoryAdapter(getBaseContext(),
                R.layout.category_row,
                null, //catOpenHelper.getRows(getApplicationContext()), // dont do this
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
                new CustomLoaderCallbacks(CategoriesActivity.this, adapter, categoriesOpenHelper.getContentUri()));


        if(FIRST_TIME) {

            FIRST_TIME = false;



            int numCategories = (int) categoriesOpenHelper.getNumberOfRows();

            if (numCategories == 0) {

                Toast.makeText(getApplicationContext(), "Populating Database from strings...", Toast.LENGTH_SHORT).show();

                SyncJob job = new SyncJob();
                job.execute(new String[] {"local"}); // Insert data from local (hard-coded strings) as per A1

            }
        }

    }


    @Override
    public void onItemClick(AdapterView<?> adapter, final View view, int position, long id)
    {
        Log.d(TAG, "onItemClick begin");

        // Must be called here because it's relative to *this* view (category row)
        TextView nameText = (TextView)(view.findViewById(R.id.category_name));

        // text -> Used for the title of the next view
        String text = nameText.getText().toString();

        // categoryId -> Used for the datasets selection query in the next view.
        int categoryId = (int)id;

        final Intent intent;
        intent = new Intent(this, DatasetsActivity.class);

        intent.putExtra("id", categoryId);
        intent.putExtra("name", text);

        startActivity(intent);

        Log.d(TAG, "onItemClick end for id: "+id);
    }

    // Performs a sync of data, either from local or online (opendata.newwestcity.ca)
    private class SyncJob extends AsyncTask<String, Void, Bundle> {

        @Override
        protected Bundle doInBackground(String[] params) {

            if(params.length > 0 && params[0].equals("local")) {

                // Populate locally

                DatabaseBuilder dbb = new DatabaseBuilder(getApplicationContext());

                dbb.cleanup();              // Delete tables
                dbb.populateCategories();   // insert data if not exists
                int datasets = dbb.populateDatasets();     // insert data if not exists

                Bundle b = new Bundle();
                b.putInt("result", 0);
                b.putInt("updated", datasets);
                b.putString("msg", "Success: Inserted "+datasets+" datasets");

                return b;

            }


            Bundle b = new Bundle();
            b.putInt("result", 0);


            int result, updated = 0;
            int datasets = (int)datasetsOpenHelper.getNumberOfRows();

            try {
                (new DatabaseBuilder(getApplicationContext())).sync();

                // Get difference (now - before)
                updated = (int)datasetsOpenHelper.getNumberOfRows() - datasets;

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

            // Toast the returned message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

            // Stop sync animation if it's running.
            findViewById(R.id.sync_button).clearAnimation();

            // Log the results
            Log.d(TAG, "updated = "+b.getInt("updated"));

            // If datasets were added, reload this activity
            if(b.getInt("updated") > 0)
            {
                finish();
                startActivity(getIntent()); // show changes
            }

        }
    }

    // Sync button onClick
    public void sync(View v) {

        String message = DatabaseBuilder.isSynced()
                ? "Refreshing datasets..."
                : "Loading from "+DatabaseBuilder.OPENDATA_DOMAIN+"...";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        // Spinning sync animation
        Animation sync = AnimationUtils.loadAnimation(this, R.anim.sync);
        v.startAnimation(sync);

        //count = DatabaseBuilder.sync();

        // AsyncTask for downloading datasets.
        (new SyncJob()).execute();

    }

}
