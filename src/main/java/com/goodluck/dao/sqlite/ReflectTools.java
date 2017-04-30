package com.goodluck.dao.sqlite;

import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.goodluck.dao.annotation.Column;

import java.lang.reflect.Field;

/**
 * Created by zhangfei on 2017/4/29.
 */
class ReflectTools {

    static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column == null) {
            throw new SQLiteException("@Column was not defined for field ["
                    + field.getName() + "]");
        }
        String columnName = column.name();
        if (TextUtils.isEmpty(columnName)) {
            columnName = field.getName();
        }
        return columnName;
    }

    static <T extends BaseTable> Object getFieldValue(T table, Field field) {
        try {
            field.setAccessible(true);
            return field.get(table);
        } catch (Throwable e) {
            throw new SQLiteException("Field '[" + field.getName() + "]' is not accessible.");
        }
    }
}
