package com.goodluck.dao.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.v4.content.CursorLoader;

import com.goodluck.dao.sqlite.ConditionBuilder;

/**
 * Used for huge data loading from database, about the usage @see {@link CursorLoader}.
 *
 * @author zf08526
 */
public class SQLiteCursorLoader extends AbstractCursorLoader {
    // query mode
    private static final int MODE_CURSOR = 0;
    private static final int MODE_QUERY = 1;

    private ConditionBuilder conditionBuilder;
    private Cursor cursor;

    private int queryMode = -1;

    public SQLiteCursorLoader(Context context, Cursor cursor) {
        super(context);
        this.cursor = cursor;
        this.queryMode = MODE_CURSOR;
    }

    public SQLiteCursorLoader(Context context, ConditionBuilder conditionBuilder) {
        super(context);
        this.conditionBuilder = conditionBuilder;
        this.queryMode = MODE_QUERY;
    }

    /**
     * Runs on a worker thread and performs the actual database query to
     * retrieve the Cursor.
     */
    @Override
    protected Cursor buildCursor() {
        if (queryMode == MODE_CURSOR) {
            return cursor;
        } else if (queryMode == MODE_QUERY) {
            return conditionBuilder.applySearch();
        } else {
            throw new SQLiteException("invalid query mode for " + queryMode);
        }
    }
}
