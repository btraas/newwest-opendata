package a00968178.comp3717.bcit.ca.opendata.databases.categories;

import android.content.Context;
import android.net.Uri;

import a00968178.comp3717.bcit.ca.opendata.databases.OpenHelper;

/**
 * Created by darcy on 2016-10-16.
 */

public final class CategoriesOpenHelper
    extends OpenHelper
{
    public static final String NAME_COLUMN = "category_name";

    private static final String TAG = CategoriesOpenHelper.class.getName();
    private static final String DB_NAME = "categories.db";
    private static final String TABLE_NAME = "categories";
    private static final String ID_COLUMN_NAME = "_id";
    private static final String[] COLUMNS = {"category_name"};

    public CategoriesOpenHelper(final Context ctx)
    {
        super(ctx, DB_NAME, TABLE_NAME, ID_COLUMN_NAME, COLUMNS, new String[] {"TEXT NOT NULL"});
    }
}
