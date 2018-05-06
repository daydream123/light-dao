package com.feiyan.lightdao.sqlite;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A convenient tool to do CRUD jobs on SQLite database.
 *
 * @author zhangfei
 */
public final class DBUtils {
    public static final String TAG = "DBUtils";
    private SQLiteDatabase database;
    private final BaseDBHelper dbHelper;

    /**
     * Create or retrieve SQLLite utils instance.
     *
     * @param dbHelper your DBHelper
     * @return singleton of SQLLite Utils.
     */
    public static DBUtils create(BaseDBHelper dbHelper) {
        return new DBUtils(dbHelper);
    }

    private DBUtils(BaseDBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.database = dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        if (database != null && database.isOpen()) {
            return database;
        } else {
            return dbHelper.getWritableDatabase();
        }
    }

    /**
     * Close database, should called after db operations are completed.
     */
    public void close() {
        if (database != null) {
            database.close();
            database = null;
        }
    }

    public <T extends Entity> ConditionBuilder<T> withTable(Class<T> tableClass) {
        return new ConditionBuilder<T>(getDatabase()).withTable(tableClass);
    }

    public <T extends Query> MultiTableConditionBuilder<T> withColumns(Class<T> columns){
        return new MultiTableConditionBuilder<T>(getDatabase()).withColumns(columns);
    }

    /**
     * Insert table with one record.
     *
     * @param table table object
     * @return row id of inserted row
     */
    public <T extends Entity> long save(T table) {
        String tableName = ReflectTools.getTableName(table.getClass());
        try {
            return getDatabase().insert(tableName, null, table.toContentValues());
        } catch (SQLiteException e){
            Log.e(TAG, "save(T) error: " + getTraceInfo(e));
            return 0;
        }
    }

    /**
     * Insert table with more than one records.
     *
     * @param tables records to save into database.
     * @return saved count
     */
    public <T extends Entity> int saveAll(List<T> tables) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (T table : tables) {
                SQL sql = SQLBuilder.buildInsertSQL(table);
                if (sql != null) {
                    db.execSQL(sql.getSql(), sql.getBindArgsAsArray(false));
                }
            }
            db.setTransactionSuccessful();
            return tables.size();
        } catch (SQLiteException e) {
            Log.e(TAG, "saveAll() error: " + getTraceInfo(e));
            return 0;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Execute more than one SQL job with transaction.
     *
     * @param batchJobs @see {@link BatchJobs}
     */
    public boolean applyBatchJobs(BatchJobs batchJobs) {
        SQLiteDatabase database = getDatabase();
        try {
            database.beginTransaction();
            ArrayList<SQL> bindArgs = batchJobs.getBatchJobs();
            for (SQL job : bindArgs) {
                Object[] args = job.getBindArgsAsArray(false);
                if (args != null && args.length > 0) {
                    database.execSQL(job.getSql(), args);
                } else {
                    database.execSQL(job.getSql());
                }
            }
            database.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "applyBatchJobs() error: " + getTraceInfo(e));
            return false;
        } finally {
            database.endTransaction();
        }
    }

    public static String getTraceInfo(Throwable e) {
        PrintWriter printWriter = null;
        Writer info = new StringWriter();
        try {
            printWriter = new PrintWriter(info);
            e.printStackTrace(printWriter);
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            return info.toString();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }
}
