package a00968178.comp3717.bcit.ca.opendata;

import android.app.LoaderManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import a00968178.comp3717.bcit.ca.opendata.databases.CustomContentProvider;

public class DatasetsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    //String[] list = {"test1", "test2", "test3"};

    private static final String TAG = DatasetsActivity.class.getName();

    private ListView listView;
    private ArrayAdapter list;
    private String title;
    private int categoryId;
    int resource; // Array resource to populate from

    private DatasetsOpenHelper    openHelper;
    private SimpleCursorAdapter adapter;

    private CustomContentProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datasets);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Toast.makeText(getApplicationContext(), " onCreate! ", Toast.LENGTH_SHORT).show();



        if(getIntent().getIntExtra("id", -1) != -1) {
            Intent intent = getIntent();
            categoryId = intent.getIntExtra("id", -1);
       } else {
            Toast.makeText(getApplicationContext(), "No id!!!", Toast.LENGTH_LONG).show();
            categoryId = -1;
            return;
        }
        if(getIntent().getStringExtra("name") != null) {
            Intent intent = getIntent();
            title = intent.getStringExtra("name");
        } else {
            Toast.makeText(getApplicationContext(), "No title!!!", Toast.LENGTH_LONG).show();
            return;
        }

        //Toast.makeText(getApplicationContext(), "ID: "+categoryId, Toast.LENGTH_LONG).show();




        //Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG).show();
        getSupportActionBar().setTitle(title);

        String identifier = DatasetTools.escape(title);
        resource = getResources().getIdentifier(identifier, "array", getPackageName());

        //resource = R.array.Business_and_Economy;

        //List<String> items = Arrays.asList(getResources().getStringArray(resource));

        //list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        listView = (ListView)findViewById(R.id.datasets_list);
        //listView.setAdapter(list);
        listView.setOnItemClickListener(this);

        init();

    }

    private void init()
    {
        final SQLiteDatabase db;
        final long           numEntries;
        final LoaderManager manager;

        openHelper = new DatasetsOpenHelper(getApplicationContext());

        adapter = new SimpleCursorAdapter(getBaseContext(),
                android.R.layout.simple_list_item_1,
                openHelper.getRows(getApplicationContext(), "category_id = ?", new String[] {""+this.categoryId}),
                new String[]
                        {
                                openHelper.nameColumn(),
                                //"category_id"
                        },
                new int[]
                        {
                                android.R.id.text1,
                                //android.R.id.text1,
                        },
                0);

        listView.setAdapter(adapter);

        /*
        manager = getLoaderManager();
        manager.initLoader(0, null,
                new CustomLoaderCallbacks(DatasetsActivity.this, adapter, openHelper.getContentUri()));
*/
        //setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));

        //db = openHelper.getWritableDatabase();
        //openHelper.limit("category_id", categoryId);
        //numEntries = openHelper.getNumberOfRows();



        //String[] data = getResources().getStringArray(resource);

       // Toast.makeText(getApplicationContext(), "Number of entries: " + numEntries, Toast.LENGTH_LONG).show();


        //db.close();

        //display();

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", title);
    }



    @Override
    public void onItemClick(AdapterView<?> adapter, final View view, int position, long id)
    {
        Log.d(TAG, "onItemClick begin");

        String text = ((TextView)view).getText().toString();


        //Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

        final Intent intent;
        intent = new Intent(this, SingleDatasetActivity.class);

        int datasetId = (int)id;


        //Bundle b = new Bundle();
        intent.putExtra("name", text);
        intent.putExtra("id", datasetId);
        //intent.putExtras(b);

        startActivity(intent);

        Log.d(TAG, "onItemClick end");
    }

}
