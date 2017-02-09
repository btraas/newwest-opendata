package a00968178.comp3717.bcit.ca.opendata.databases;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentProvider;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;


/**
 * Created by Brayden on 2/6/2017.
 */

public class CustomLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    private Activity activity;
    private CursorAdapter adapter;
    private Uri contentUri;
   // private ContentProvider provider;

    public CustomLoaderCallbacks(Activity activity, CursorAdapter adapter, Uri contentUri) {
        this.activity = activity;
        this.adapter = adapter;
        this.contentUri = contentUri;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int    id,
                                         final Bundle args)
    {
        final Uri uri;
        final CursorLoader loader;

        loader = new CursorLoader(activity, contentUri, null, null, null, null);

        return (loader);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader,
                               final Cursor         data)
    {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader)
    {
        adapter.swapCursor(null);
    }
}