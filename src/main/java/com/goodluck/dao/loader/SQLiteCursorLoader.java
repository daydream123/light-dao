package com.goodluck.dao.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.content.CursorLoader;

import com.goodluck.dao.sqlite.BaseTable;
import com.goodluck.dao.sqlite.TableInfoCache;

/**
 * Used for huge data loading from database, about the usage @see
 * {@link CursorLoader}.
 * <p/>
 * <pre>
 * public class MainActivity extends ListActivity {
 * 	private DBUtils mDbUtils;
 * 	private SimpleCursorAdapter mAdapter;
 *
 * 	&#064;Override
 * 	protected void onCreate(Bundle savedInstanceState) {
 * 		super.onCreate(savedInstanceState);
 * 		setContentView(R.layout.activity_main);
 * 		mDbUtils = TestDbUtils.getDBUtils(this);
 *
 * 		String[] from = { Account.COLUMN_DISPLAYING_NAME };
 * 		int[] to = { android.R.id.text1 };
 * 		mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0);
 * 		getListView().setAdapter(mAdapter);
 * 		getLoaderManager().initLoader(0, null, mLoadCallbacks);
 *    }
 *
 * 	private LoaderCallbacks&lt;Cursor&gt; mLoadCallbacks = new LoaderCallbacks&lt;Cursor&gt;() {
 *
 * 		&#064;Override
 * 		public Loader&lt;Cursor&gt; onCreateLoader(int id, Bundle args) {
 * 			Cursor cursor = mDbUtils.findAllToCursor(Account.class);
 * 			return new SQLiteCursorLoader(MainActivity.this, cursor);
 *        }
 *
 * 		&#064;Override
 * 		public void onLoadFinished(Loader&lt;Cursor&gt; loader, Cursor data) {
 * 			mAdapter.changeCursor(data);
 *        }
 *
 * 		&#064;Override
 * 		public void onLoaderReset(Loader&lt;Cursor&gt; loader) {
 * 			mAdapter.changeCursor(null);
 *        }
 *    };
 *
 * 	&#064;Override
 * 	protected void onDestroy() {
 * 		super.onDestroy();
 * 		mDbUtils.closeQuietly();
 *    }
 * }
 * </pre>
 *
 * @author zf08526
 */
public class SQLiteCursorLoader extends AbstractCursorLoader {
    // query mode
    private static final int MODE_CURSOR = 0;
    private static final int MODE_RAW_QUERY = 1;
    private static final int MODE_QUERY = 2;

    private SQLiteDatabase db;
    private Class<? extends BaseTable> tableClass;
    private String rawQuery;
    private String[] selectionArgs;
    private String selection;
    private String groupBy;
    private String having;
    private String orderBy;
    private Cursor cursor;

    private int queryMode = -1;

    public SQLiteCursorLoader(Context context, Cursor cursor) {
        super(context);
        this.cursor = cursor;
        this.queryMode = MODE_CURSOR;
    }

    public SQLiteCursorLoader(Context context, SQLiteDatabase db, String rawQuery, String[] selectionArgs) {
        super(context);
        this.db = db;
        this.rawQuery = rawQuery;
        this.selectionArgs = selectionArgs;
        this.queryMode = MODE_RAW_QUERY;
    }

    public SQLiteCursorLoader(Context context, SQLiteDatabase db, Class<? extends BaseTable> tableClass,
                              String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        super(context);
        this.db = db;
        this.tableClass = tableClass;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
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
            String table = TableInfoCache.getTableName(tableClass);
            return db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);
        } else if (queryMode == MODE_RAW_QUERY) {
            return db.rawQuery(rawQuery, selectionArgs);
        } else {
            throw new SQLiteException("invalid query mode for " + queryMode);
        }
    }
}
