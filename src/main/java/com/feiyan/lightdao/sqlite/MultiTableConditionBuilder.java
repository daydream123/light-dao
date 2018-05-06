package com.feiyan.lightdao.sqlite;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.feiyan.lightdao.annotation.ID;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Smilier with {@link ConditionBuilder} but expose methods for
 * multi-table query only.
 *
 * @author zhangfei
 */
public class MultiTableConditionBuilder<T extends Query> {
    private final SQLiteDatabase database;

    private Class<T> clazz;
    private String[] columns;
    private String whereClause;
    private String[] whereArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private Integer limitOffset;
    private Integer limitSize;
    private boolean distinct;
    private String[] tableNames;

    /**
     * used in database query
     */
    private String[] aliasColumns;


    public MultiTableConditionBuilder(SQLiteDatabase database) {
        this.database = database;
    }

    public MultiTableConditionBuilder<T> withTable(Class<T> tableClass) {
        this.clazz = tableClass;
        return this;
    }

    public final MultiTableConditionBuilder<T> withColumns(String... columns) {
        this.columns = columns;
        return this;
    }

    public MultiTableConditionBuilder<T> withWhere(String whereClause, Object... whereArgs) {
        this.whereClause = whereClause;
        this.whereArgs = new String[whereArgs.length];

        for (int i = 0; i < whereArgs.length; i++) {
            Object arg = whereArgs[i];

            if (arg instanceof String
                    || arg instanceof Integer
                    || arg instanceof Long
                    || arg instanceof Float
                    || arg instanceof Double) {
                this.whereArgs[i] = arg.toString();
            } else if (arg instanceof Boolean) {
                this.whereArgs[i] = Boolean.valueOf(arg.toString()) ? "1" : "0";
            } else {
                throw new SQLException(arg.toString() + " is not supported as where argument in SQLite");
            }
        }

        return this;
    }

    public MultiTableConditionBuilder<T> withGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public MultiTableConditionBuilder<T> withHaving(String having) {
        this.having = having;
        return this;
    }

    public MultiTableConditionBuilder<T> withOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public MultiTableConditionBuilder<T> withLimit(int limitOffset, int limitSize) {
        this.limitOffset = limitOffset;
        this.limitSize = limitSize;
        return this;
    }

    public MultiTableConditionBuilder<T> withDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public T applySearchById(long id) {
        this.whereClause = Entity._ID + "=?";
        this.whereArgs = new String[]{String.valueOf(id)};

        return applySearchFirst();
    }

    public int applyCount() {
        this.columns = Entity.COUNT_COLUMNS;

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
        } catch (SQLiteException e) {
            Log.e(DBUtils.TAG, "applyCount() error: " + DBUtils.getTraceInfo(e));
            return 0;
        } finally {
            c.close();
        }
    }

    /**
     * Apply search and return cursor as result
     *
     * @return query cursor
     */
    public Cursor applySearch() {
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < tableNames.length; i++) {
            nameBuilder.append(tableNames[i]);

            if (i < tableNames.length - 1) {
                nameBuilder.append(",");
            }
        }

        String limit = null;
        if (limitOffset != null && limitSize != null) {
            limit = limitOffset + "," + limitSize;
        }

        String query = SQLiteQueryBuilder.buildQueryString(
                distinct, nameBuilder.toString(), aliasColumns, whereClause,
                groupBy, having, orderBy, limit);
        return database.rawQuery(query, whereArgs);
    }

    /**
     * Apply search with condition and return list as result
     *
     * @return list of table class object as result
     */
    public List<T> applySearchAsList() {
        Cursor c = applySearch();
        List<T> entities = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                T table = getContent(c, clazz);
                if (table != null) {
                    entities.add(table);
                }
            }
        } catch (SQLiteException e) {
            Log.e(DBUtils.TAG, "applySearchAsList() error: " + DBUtils.getTraceInfo(e));
            return entities;
        } finally {
            c.close();
        }
        return entities;
    }

    /**
     * Apply search first row with condition
     *
     * @return first item of result
     */
    public T applySearchFirst() {
        Cursor c = applySearch();

        if (c == null) {
            throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
        }

        try {
            if (c.moveToFirst()) {
                return getContent(c, clazz);
            } else {
                return null;
            }
        } catch (SQLiteException e) {
            Log.e(DBUtils.TAG, "applySearchFirst() error: " + DBUtils.getTraceInfo(e));
            return null;
        } finally {
            c.close();
        }
    }

    private T getContent(Cursor cursor, Class<T> tableClass) {
        try {
            T content = tableClass.newInstance();
            content.restore(cursor, columns);
            return content;
        } catch (IllegalAccessException e) {
            Log.e(DBUtils.TAG, "getContent() error: " + DBUtils.getTraceInfo(e));
        } catch (InstantiationException e) {
            Log.e(DBUtils.TAG, "getContent() error: " + DBUtils.getTraceInfo(e));
        }
        return null;
    }

    public MultiTableConditionBuilder<T> withColumns(Class<T> clazz) {
        this.clazz = clazz;

        // read columns from class
        Field[] fields = ReflectTools.getClassFields(clazz);

        List<String> columns = new ArrayList<>();
        List<String> aliasColumns = new ArrayList<>();
        for (Field field : fields) {
            // ignore _id field
            if (field.isAnnotationPresent(ID.class)) {
                continue;
            }

            ColumnInfo columnInfo = ReflectTools.getColumnInfo(field);
            columns.add(columnInfo.getName());
            aliasColumns.add(columnInfo.getAliasName());
        }

        this.columns = columns.toArray(new String[columns.size()]);
        this.aliasColumns = aliasColumns.toArray(new String[aliasColumns.size()]);
        return this;
    }

    public MultiTableConditionBuilder<T> withTableNames(String... tableNames) {
        this.tableNames = tableNames;
        return this;
    }

}
