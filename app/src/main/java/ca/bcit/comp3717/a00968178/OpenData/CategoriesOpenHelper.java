package ca.bcit.comp3717.a00968178.OpenData;

import android.content.Context;

import ca.bcit.comp3717.a00968178.OpenData.databases.OpenHelper;

/**
 * Extends a modified version of D'Arcy's OpenHelper
 * Simply defines the table, not the methods.
 */

public final class CategoriesOpenHelper
    extends OpenHelper
{
    public static final String NAME_COLUMN = "category_name";

    private static final String TAG = CategoriesOpenHelper.class.getName();
    private static final String DB_NAME = "categories.db";
    private static final String TABLE_NAME = "categories";
    private static final String ID_COLUMN_NAME = "_id";
    private static final String[] COLUMNS = {"category_name", "dataset_count"};
    private static final String ORDER = NAME_COLUMN;

    public CategoriesOpenHelper(final Context ctx)
    {
        super(ctx, DB_NAME, TABLE_NAME, ID_COLUMN_NAME, COLUMNS,
                new String[] {"TEXT NOT NULL", "INTEGER NOT NULL DEFAULT 0"},
                ORDER);
    }
}
