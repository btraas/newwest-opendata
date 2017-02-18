package ca.bcit.comp3717.a00968178.OpenData;

import android.content.Context;

import android.util.Log;

import ca.bcit.comp3717.a00968178.OpenData.databases.OpenHelper;

/**
 * Created by darcy on 2016-10-16.
 */



public final class DatasetsOpenHelper
    extends OpenHelper {

    private static final String DEFAULT_DESC = "No Description Available";
    public static final String NAME_COLUMN = "dataset_name";

    private static final String TAG = DatasetsOpenHelper.class.getName();
    private static final String DB_NAME = "datasets.db";
    private static final String TABLE_NAME = "datasets";
    private static final String ID_COLUMN_NAME = "_id";
    private static final String[] DATA_COLUMN_NAMES = {"category_id", "dataset_name", "dataset_desc", "dataset_link"};
    private static final String[] DATA_COLUMN_TYPES =
            {"INT NOT NULL", "TEXT NOT NULL", "TEXT NOT NULL DEFAULT '"+DEFAULT_DESC+"'", "TEXT NOT NULL"};


    public DatasetsOpenHelper(final Context ctx) {
        super(ctx, DB_NAME, TABLE_NAME, ID_COLUMN_NAME, DATA_COLUMN_NAMES, DATA_COLUMN_TYPES);

        this.nameColumn = "dataset_name"; // override default first... this is what's displayed.

        Log.d(TAG, "ctor: " + DB_NAME);
    }
}
