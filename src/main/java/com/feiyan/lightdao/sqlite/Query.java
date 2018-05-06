package com.feiyan.lightdao.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.feiyan.lightdao.annotation.Column;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Multi-table query result is list of java entity and the entity class must extend it.
 *
 * @author zhangfei
 */
public class Query implements Serializable {
    private static final long serialVersionUID = -6833637753877258272L;

    /**
     * Write the content into a ContentValues container
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        Field[] fields = ReflectTools.getClassFields(getClass());

        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }

            String columnName = column.name();
            if (TextUtils.isEmpty(columnName)) {
                columnName = field.getName();
            }

            // record which not saved into database yet, its id should not be put into content values,
            // since its id will be generated automatically by auto increment
            try {
                if (columnName.equals(Entity._ID) && field.getLong(this) == Entity.NOT_SAVED) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                throw new SQLiteException("IllegalAccessException: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new SQLiteException("IllegalArgumentException: " + e.getMessage());
            }

            try {
                // put field value into ContentValues which is not null
                Object value = field.get(this);
                if (value != null) {
                    if (value instanceof Boolean) {
                        boolean boolVal = Boolean.valueOf(value.toString());
                        values.put(columnName, boolVal ? 1 : 0);
                    } else {
                        values.put(columnName, value.toString());
                    }
                }
            } catch (IllegalAccessException e) {
                throw new SQLiteException("IllegalAccessException:" + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new SQLiteException("IllegalArgumentException:" + e.getMessage());
            }
        }
        return values;
    }

    /**
     * Read the Content from a ContentCursor.
     */
    void restore(Cursor cursor, String[] columns) {
        List<String> columnList;
        if (columns != null) {
            columnList = Arrays.asList(columns);
        } else {
            columnList = Collections.emptyList();
        }

        Field[] fields = ReflectTools.getClassFields(getClass());
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }

            String columnName = column.name();
            if (TextUtils.isEmpty(columnName)) {
                columnName = field.getName();
            }

            // set field value
            if (columnList.isEmpty() || columnList.contains(columnName)) {
                setField(field, this, cursor, columnName);
            }
        }
    }

    private <T extends Query> void setField(Field field, T columns, Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            Class<?> dataTypeClass = field.getType();

            if ((dataTypeClass == Integer.class || dataTypeClass == int.class)) {
                field.set(columns, cursor.getInt(columnIndex));
            } else if (dataTypeClass == Long.class || dataTypeClass == long.class) {
                field.set(columns, cursor.getLong(columnIndex));
            } else if (dataTypeClass == String.class) {
                field.set(columns, cursor.getString(columnIndex));
            } else if (dataTypeClass == Short.class || dataTypeClass == short.class) {
                field.set(columns, cursor.getShort(columnIndex));
            } else if (dataTypeClass == Double.class || dataTypeClass == double.class) {
                field.set(columns, cursor.getDouble(columnIndex));
            } else if (dataTypeClass == Float.class || dataTypeClass == float.class) {
                field.set(columns, cursor.getFloat(columnIndex));
            } else if (dataTypeClass == Boolean.class || dataTypeClass == boolean.class) {
                field.set(columns, cursor.getInt(columnIndex) == 1);
            } else if (dataTypeClass == Byte[].class || dataTypeClass == byte[].class) {
                field.set(columns, cursor.getBlob(columnIndex));
            } else {
                throw new SQLiteException("Field [" + field.getName() + "] is not supported.");
            }
        } catch (IllegalAccessException e) {
            throw new SQLiteException("IllegalAccessException:" + e.getMessage());
        } catch (IllegalArgumentException e) {
            com.feiyan.lightdao.annotation.Table table = this.getClass().getAnnotation(com.feiyan.lightdao.annotation.Table.class);
            throw new SQLiteException("Ursor value cannot be converted to field's value for field [" + field.getName()
                    + "] in table [" + table.value() + "]");
        }
    }
}
