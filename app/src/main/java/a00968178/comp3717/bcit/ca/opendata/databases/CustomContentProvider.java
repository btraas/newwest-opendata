package a00968178.comp3717.bcit.ca.opendata.databases;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import a00968178.comp3717.bcit.ca.opendata.CategoriesOpenHelper;
import a00968178.comp3717.bcit.ca.opendata.DatasetsOpenHelper;

public class CustomContentProvider
    extends ContentProvider
{
    private static final String TAG = CustomContentProvider.class.getName();
    private static final UriMatcher uriMatcher;

    //public static final String BASE_CONTENT_URI = "content://comp3717.bcit.ca.database/";

    private static final String INVALID_URI = "Unsupported Custom URI: ";

    //private static final String TABLE_NAME = CategoriesOpenHelper.NAME_TABLE_NAME;

    private static final int CATEGORIES_URI = 0;
    private static final int DATASETS_URI = 1;
    private static final String[] TABLES = {"categories", "datasets"};
    private OpenHelper categoriesHelper;
    private OpenHelper datasetsHelper;

    static
    {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


        for(int i = 0; i < TABLES.length; i++) {
            uriMatcher.addURI("a00968178.comp3717.bcit.ca.opendata", TABLES[i], i);
        }
    }

    static
    {
        //CONTENT_URI = Uri.parse("content://a00968178.comp3717.bcit.ca.opendata/"+TABLE_NAME );
    }

    @Override
    public boolean onCreate()
    {
        //namesOpenHelper = CategoriesOpenHelper.getInstance(getContext());
        categoriesHelper = new CategoriesOpenHelper(getContext());
        datasetsHelper   = new DatasetsOpenHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(final Uri      uri,
                        final String[] projection,
                        final String   selection,
                        final String[] selectionArgs,
                        final String   sortOrder)
    {
        final Cursor cursor;

        //Log.d(TAG,"query: "+uri.getPath());
        //if(projection != null && projection.length > 0) Log.d(TAG, "proj: "+projection[0]);
        //Log.d(TAG, "selection: "+selection);
        //if(selectionArgs != null && selectionArgs.length > 0) Log.d(TAG, "args: "+selectionArgs[0]);

        //Log.d(TAG, "segmenT: "+uri.getLastPathSegment());

        switch (uriMatcher.match(uri))
        {
            case CATEGORIES_URI:
            {
                final SQLiteDatabase db;

                db     = categoriesHelper.getWritableDatabase();
                //cursor = categoriesHelper.dumpTable(getContext());
                cursor = categoriesHelper.getRows(getContext(), projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            }
            case DATASETS_URI:
            {
                final SQLiteDatabase db;

                db     = datasetsHelper.getWritableDatabase();
                cursor = datasetsHelper.getRows(getContext(), projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            }
            //case CATEGORY_DATASETS_URI:

            default:
            {
                throw new IllegalArgumentException(INVALID_URI + uri);
            }
        }

        return (cursor);
    }

    @Override
    public String getType(final Uri uri)
    {
        Log.d(TAG,"getType: "+uri.getPath());
        final String type;

        switch(uriMatcher.match(uri))
        {
            case CATEGORIES_URI:
                type = "vnd.android.cursor.dir/vnd.a00968178.comp3717.bcit.ca.opendata."+categoriesHelper.getDatabaseName();
                break;
            case DATASETS_URI:
                type = "vnd.android.cursor.dir/vnd.a00968178.comp3717.bcit.ca.opendata."+datasetsHelper.getDatabaseName();
                break;

            default:
                throw new IllegalArgumentException(INVALID_URI + uri);
        }

        return (type);
    }

    @Override
    public int delete(final Uri      uri,
                      final String   selection,
                      final String[] selectionArgs)
    {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(final Uri           uri,
                      final ContentValues values)
    {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(final Uri           uri,
                      final ContentValues values,
                      final String        selection,
                      final String[]      selectionArgs)
    {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
