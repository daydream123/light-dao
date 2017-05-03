package com.goodluck.dao.sqlite;

import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.goodluck.dao.annotation.Column;
import com.goodluck.dao.annotation.OrderBy;
import com.goodluck.dao.annotation.Table;
import com.goodluck.dao.annotation.View;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangfei on 2017/4/29.
 */
class ReflectTools {

    interface DataType {
        String NULL = "NULL";

        String INTEGER = "INTEGER";

        String BLOB = "BLOB";

        String TEXT = "TEXT";

        String REAL = "REAL";
    }

    static <T extends BaseTable> String getTableName(Class<T> tableClass) {
        Table table = tableClass.getAnnotation(Table.class);
        View view = tableClass.getAnnotation(View.class);
        if (table == null && view == null) {
            throw new SQLiteException(
                    "Neither Table annotation nor View are not defined on ["
                            + tableClass.getSimpleName() + "]");
        }

        if (table != null) {
            return table.value();
        } else {
            return view.value();
        }
    }

    static <T extends BaseTable> String getDefaultOrderBy(Class<T> tableClass){
        OrderBy orderBy = tableClass.getAnnotation(OrderBy.class);
        if (orderBy != null) {
            return orderBy.value();
        }

        return null;
    }

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

    static Field[] getTableClassFields(Class<? extends BaseTable> tableClass) {
        List<Field> totalFields = new ArrayList<>();
        Field[] tableClassFields = tableClass.getDeclaredFields();
        totalFields.addAll(Arrays.asList(tableClassFields));

        // cache fields and return
        String objectClassStr = Object.class.toString();
        Class<?> superClass = tableClass.getSuperclass();
        while (superClass != null && !superClass.toString().equals(objectClassStr)) {
            Field[] superClassFields = superClass.getDeclaredFields();
            totalFields.addAll(0, Arrays.asList(superClassFields));
            superClass = superClass.getSuperclass();
        }

        // filter out static fields which are not table field
        List<Field> fieldsToRemove = new ArrayList<>();
        for (Field field : totalFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                fieldsToRemove.add(field);
            }
        }
        totalFields.removeAll(fieldsToRemove);

        return totalFields.toArray(new Field[totalFields.size()]);
    }

    static String getDataTypeByField(Field field) {
        Class<?> dataTypeClass = field.getType();

        // all number type will be treat as INTEGER in sqlite3
        if ((dataTypeClass == Integer.class || dataTypeClass == int.class)) {
            return DataType.INTEGER;
        } else if (dataTypeClass == Long.class || dataTypeClass == long.class) {
            return DataType.INTEGER;
        } else if (dataTypeClass == String.class) {
            return DataType.TEXT;
        } else if (dataTypeClass == Short.class || dataTypeClass == short.class) {
            return DataType.INTEGER;
        } else if (dataTypeClass == Double.class || dataTypeClass == double.class) {
            return DataType.REAL;
        } else if (dataTypeClass == Float.class || dataTypeClass == float.class) {
            return DataType.REAL;
        } else if (dataTypeClass == Boolean.class || dataTypeClass == boolean.class) {
            return DataType.INTEGER;
        } else if (dataTypeClass == Byte[].class || dataTypeClass == byte[].class){
            return DataType.BLOB;
        }else {
            throw new SQLiteException("field [" + field.getName() + "] is not supported data type.");
        }
    }
}
