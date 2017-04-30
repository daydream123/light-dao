package com.goodluck.dao.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangfei on 2017/4/29.
 */
public class ConditionBuilder<T extends BaseTable> {
    private SQLiteDatabase database;

    private Class<T> tableClass;
    private String[] columns;
    private String whereClause;
    private String[] whereArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private Integer limitOffset;
    private Integer limitSize;
    private boolean distinct;

    private ConditionBuilder(SQLiteDatabase database, Class<T> tableClass) {
        this.database = database;
        this.tableClass = tableClass;
    }

    static <T extends BaseTable> ConditionBuilder<T> create(SQLiteDatabase database, Class<T> tableClass){
        if (database == null) {
            throw new SQLException("database cannot be null in ConditionBuilder");
        }

        if (tableClass == null) {
            throw new SQLException("tableClass cannot be null in ConditionBuilder");
        }
        return new ConditionBuilder<>(database, tableClass);
    }

    public ConditionBuilder withColumns(String[] columns){
        this.columns = columns;
        return this;
    }

    public ConditionBuilder withWhere(String whereClause, String[] whereArgs){
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
        return this;
    }

    public T findById(long id) {
        this.whereClause = BaseTable._ID + "=?";
        this.whereArgs = new String[]{String.valueOf(id)};

        return applyFindFirst();
    }

    public int applyCount(){
        this.columns = BaseTable.COUNT_COLUMNS;

        Cursor c = applySearch();
        if (c == null) {
            throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
        }
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            } else {
                return 0;
            }
        } finally {
            c.close();
        }
    }

    public ConditionBuilder withGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public ConditionBuilder withHaving(String having) {
        this.having = having;
        return this;
    }

    public ConditionBuilder withOrderBy(String orderBy){
        this.orderBy = orderBy;
        return this;
    }

    public ConditionBuilder withLimit(int limitOffset, int limitSize){
        this.limitOffset = limitOffset;
        this.limitSize = limitSize;
        return this;
    }

    public ConditionBuilder withDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public Cursor applySearch(){
        String limit = null;
        if (limitOffset != null && limitSize != null) {
            limit = limitOffset + "," + limitSize;
        }

        if (TextUtils.isEmpty(orderBy)) {
            orderBy = TableInfoCache.getDefaultOrderBy(tableClass);
        }

        String tableName = TableInfoCache.getTableName(tableClass);
        String query = SQLiteQueryBuilder.buildQueryString(distinct,
                tableName, columns, whereClause, groupBy, having, orderBy, limit);
        return database.rawQuery(query, whereArgs);
    }

    public List<T> applyFind(){
        Cursor c = applySearch();
        List<T> entities = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                T table = getContent(c, tableClass);
                if (table != null) {
                    entities.add(table);
                }
            }
        } finally {
            c.close();
        }
        return entities;
    }

    public T applyFindFirst(){
        Cursor c = applySearch();

        if (c == null) {
            throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
        }

        try {
            if (c.moveToFirst()) {
                return getContent(c, tableClass);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }

    private T getContent(Cursor cursor, Class<T> tableClass) {
        try {
            T content = tableClass.newInstance();
            content.id = cursor.getLong(0);
            content.restore(cursor);
            return content;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int applyDelete(){
        String tableName = TableInfoCache.getTableName(tableClass);
        int count = database.delete(tableName, whereClause, whereArgs);
        if (TextUtils.isEmpty(whereClause)) {
            resetPrimaryKeyWhenRequired(tableName);
        }
        return count;
    }

    public int applyDelete(T table) {
        return applyDeleteById(table.id);
    }

    public int applyDeleteById(long id){
        if (id == BaseTable.NOT_SAVED) {
            return 0;
        }

        this.whereClause = BaseTable._ID + "=?";
        this.whereArgs = new String[]{String.valueOf(id)};

        checkModifiable(tableClass, "applyDelete");
        return applyDelete();
    }

    public int applyUpdate(ContentValues values) {
        if (values == null || values.size() == 0) {
            throw new SQLiteException("ContentValues is empty, nothing can be updated");
        }

        String tableName = TableInfoCache.getTableName(tableClass);
        checkModifiable(tableClass, "applyUpdate");
        return database.update(tableName, values, whereClause, whereArgs);
    }

    public int applyUpdate(T table) {
        if (table == null) {
            throw new SQLException("table to update cannot be null");
        }

        this.whereClause = BaseTable._ID + "=?";
        this.whereArgs = new String[]{String.valueOf(table.id)};
        ContentValues values = table.toContentValues();
        return applyUpdate(values);
    }

    /**
     * Reset primary key as zero when it's too large(if exceed Long.MAX_VALUE, exception will be throw)
     *
     * @param tableName table's name
     */
    private void resetPrimaryKeyWhenRequired(String tableName){
        if (TextUtils.isEmpty(tableName)) {
            return;
        }

        long maxLimit = Long.MAX_VALUE /4 * 3;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM sqlite_sequence WHERE name == ?", new String[]{tableName});
            if (cursor != null && cursor.moveToNext()) {
                long seq = cursor.getLong(1);

                if (seq > maxLimit) {
                    database.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name == ?", new String[]{tableName});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * As we all known Table View is only used to search, its record cannot be updated or deleted.
     * @param tableClass table class
     * @param operation string flag
     */
    private void checkModifiable(Class<? extends BaseTable> tableClass, String operation){
        try {
            if (!tableClass.newInstance().isTable()){
                throw new SQLiteException("Failed to " + operation + " [" + tableClass.getSimpleName()
                        + "], since it is table view not table.");
            }
        } catch (InstantiationException e) {
            throw new SQLiteException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new SQLiteException(e.getMessage());
        }
    }
}