package com.feiyan.lightdao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.feiyan.lightdao.annotation.Column;
import com.feiyan.lightdao.annotation.Foreign;
import com.feiyan.lightdao.annotation.ID;
import com.feiyan.lightdao.annotation.Table;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provider APIs to generate SQL for delete, insert, update, create and so on.
 *
 * @author zhangfei
 */
public final class SQLBuilder {
    private SQLBuilder() {}

    /**
     * build table creation sql
     */
    public static SQL buildTableCreateSQL(Class<? extends Entity> tableClass) {
        final StringBuilder buffer = new StringBuilder();
        String tableName = ReflectTools.getTableName(tableClass);
        buffer.append("CREATE TABLE IF NOT EXISTS ").append(tableName);
        buffer.append(" (");
        Field[] fields = ReflectTools.getClassFields(tableClass);

        int index = 0;// no need ", " for last column
        for (Field field : fields) {
            index++;

            // ignore field without @Column
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }

            // add column definition
            String columnName = ReflectTools.getColumnInfo(field).getName();

            validateFieldType(field, tableName);

            // add column name and type definition
            String columnType = ReflectTools.getDataTypeByField(field);
            buffer.append(columnName).append(" ").append(columnType);

            // add id definition if it was id column
            ID id = field.getAnnotation(ID.class);
            if (id != null) {
                buffer.append(" PRIMARY KEY AUTOINCREMENT");
            }

            // add 'default value' definition
            appendDefaultValue(column, field, buffer);

            // add unique definition
            boolean unique = column.unique();
            if (unique) {
                buffer.append(" UNIQUE");
            }

            // add 'not null' definition
            if (column.notnull()) {
                buffer.append(" NOT NULL");
            }

            // add foreign key definition
            Foreign foreign = field.getAnnotation(Foreign.class);
            if (foreign != null) {
                Class<? extends Entity> refTableClass = foreign.value();
                Table refTable = refTableClass.getAnnotation(Table.class);
                String refTableName = refTable.value();
                String refColumnName = Entity._ID;
                buffer.append(" REFERENCES ").append(refTableName).append("(").append(refColumnName).append(")");
            }

            if (index != fields.length) {
                buffer.append(", ");
            }
        }
        buffer.append(");");
        return new SQL(buffer.toString());
    }

    /**
     * build sql for inserting
     */
    public static <T extends Entity> SQL buildInsertSQL(T table) {
        List<KeyValue<Object>> keyValueList = table2KeyValueList(table);
        if (keyValueList.size() == 0) {
            return null;
        }

        StringBuilder buffer = new StringBuilder();
        SQL sql = new SQL();
        buffer.append("INSERT INTO ");
        buffer.append(ReflectTools.getTableName(table.getClass()));
        buffer.append(" (");
        for (KeyValue<Object> kv : keyValueList) {
            if (Entity._ID.equals(kv.key)) {
                continue;
            }
            buffer.append(kv.key).append(",");
            sql.addBindArg(kv.value);
        }
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(") VALUES (");

        for (KeyValue<Object> kv : keyValueList) {
            if (Entity._ID.equals(kv.key)) {
                continue;
            }
            buffer.append("?,");
        }

        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(")");
        sql.setSql(buffer.toString());
        return sql;
    }

    /**
     *  build sql for deleting with table class object
     */
    public static <T extends Entity> SQL buildDeleteSQL(T table) {
        if (table.id == Entity.NOT_SAVED) {
            throw new SQLiteException("This table[" + table.getClass().getName() + "]'s id value is not illegal.");
        }

        return new SQL("DELETE FROM " + ReflectTools.getTableName(table.getClass()) + " WHERE " + Entity._ID + "=" + table.id);
    }

    /**
     *  build sql for deleteing all with table class
     */
    public static <T extends Entity> SQL buildDeleteSQL(Class<T> tableClass) {
        return new SQL("DELETE FROM " + ReflectTools.getTableName(tableClass));
    }

    /**
     * build sql for deleting with id
     */
    public static <T extends Entity> SQL buildDeleteSQL(Class<T> tableClass, long id) {
        if (id == Entity.NOT_SAVED) {
            throw new SQLiteException("The record with id '(" + id +")' is not exist.");
        }

        return new SQL("DELETE FROM " + ReflectTools.getTableName(tableClass) + " WHERE " + Entity._ID + "=" + id);
    }

    /**
     * build sql for deleting with selection and args
     */
    public static <T extends Entity> SQL buildDeleteSQL(Class<T> tableClass, String where,
                                                        Object... whereArgs) {
        StringBuilder buffer = new StringBuilder("DELETE FROM " + ReflectTools.getTableName(tableClass));
        if (where != null && where.length() > 0) {
            buffer.append(" WHERE ").append(buildWhere(where, whereArgs));
        }
        return new SQL(buffer.toString());
    }

    /**
     * build sql for updating with id
     */
    public static <T extends Entity> SQL buildUpdateSQL(Class<T> tableClass, long id, ContentValues values) {
        if (values == null || values.size() == 0) {
            throw new SQLException("ContentValues is empty, nothing will be update.");
        }

        String tableName = ReflectTools.getTableName(tableClass);
        if (id == Entity.NOT_SAVED) {
            throw new SQLiteException("This table [" + tableName + "]'s id value is null");
        }

        SQL sql = new SQL();
        StringBuilder sqlBuffer = new StringBuilder("UPDATE ");
        sqlBuffer.append(tableName);
        sqlBuffer.append(" SET ");
        Set<String> columnNames = values.keySet();

        for (String columnName : columnNames) {
            sqlBuffer.append(columnName).append("=?,");
            sql.addBindArg(values.get(columnName));
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(" WHERE ").append(Entity._ID + "=").append(id);

        sql.setSql(sqlBuffer.toString());
        return sql;
    }

    /**
     * build sql for updating with selection and args
     */
    public static <T extends Entity> SQL buildUpdateSQL(Class<T> tableClass, ContentValues values,
                                                        String where, Object... whereArgs) {
        if (values == null || values.size() == 0) {
            throw new SQLException("ContentValues is empty, nothing will be update.");
        }

        SQL result = new SQL();
        StringBuilder buffer = new StringBuilder("UPDATE ");
        buffer.append(ReflectTools.getTableName(tableClass));
        buffer.append(" SET ");
        Set<String> keys = values.keySet();
        for (String columnName : keys) {
            // ignore _id: primary key cannot be updated
            if (!columnName.equals(Entity._ID)) {
                buffer.append(columnName).append("=?,");
                result.addBindArg(values.get(columnName));
            }
        }
        buffer.deleteCharAt(buffer.length() - 1);
        if (where != null && where.length() > 0) {
            buffer.append(" WHERE ").append(buildWhere(where, whereArgs));
        }

        result.setSql(buffer.toString());
        return result;
    }

    private static <T extends Entity> KeyValue<Object> column2KeyValue(T table, Field field, Column column) {
        KeyValue<Object> keyValue = null;
        String key = column.name();
        if (!TextUtils.isEmpty(key)) {
            Object value = ReflectTools.getFieldValue(table, field);
            if (value == null) {
                value = getDefaultValueOfField(field.getType());
            }
            keyValue = new KeyValue<>(key, value);
        }
        return keyValue;
    }

    private static Object getDefaultValueOfField(Class<?> typeClass) {
        if (typeClass == Integer.class || typeClass == int.class) {
            return 0;
        } else if (typeClass == Short.class || typeClass == short.class) {
            return 0;
        } else if (typeClass == Double.class || typeClass == double.class) {
            return 0;
        } else if (typeClass == Float.class || typeClass == float.class) {
            return 0f;
        } else if (typeClass == Long.class || typeClass == long.class) {
            return 0L;
        } else if (typeClass == Boolean.class || typeClass == boolean.class) {
            return false;
        } else if (typeClass == String.class) {
            return "";
        } else if (typeClass == byte[].class || typeClass == Byte[].class){
            return new byte[0];
        } else {
            throw new SQLiteException("type [" + typeClass.toString() + "] is not supported in SQLITE");
        }
    }

    private static <T extends Entity> ArrayList<KeyValue<Object>> table2KeyValueList(T table) {
        ArrayList<KeyValue<Object>> keyValueList = new ArrayList<>();
        Field[] fields = ReflectTools.getClassFields(table.getClass());

        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                KeyValue<Object> kv = column2KeyValue(table, field, column);
                if (kv != null) {
                    keyValueList.add(kv);
                }
            }
        }
        return keyValueList;
    }

    private static String buildWhere(String where, Object[] whereArgs) {
        if (whereArgs != null && whereArgs.length > 0) {
            for (Object arg : whereArgs) {
                int index = where.indexOf("?");
                if (index > 0) {
                    String convertedArg = SQL.convertEscapeChar(arg).toString();
                    where = where.replaceFirst("\\?", convertedArg);
                }
            }
        }
        return where;
    }

    private static void appendDefaultValue(Column column, Field field, StringBuilder buffer){
        String defVal = column.defVal();
        if (!TextUtils.isEmpty(defVal)) {
            Class<?> typeClass = field.getType();
            if (typeClass == Byte[].class || typeClass == byte[].class) {
                throw new SQLiteException("SQLITE does not support 'BLOB' data type for " + field.getName());
            }

            buffer.append(" DEFAULT '").append(defVal).append("'");
        }
    }

    private static void validateFieldType(Field field, String tableName) {
        Class<?> typeClass = field.getType();
        if (typeClass != Integer.class
                && typeClass != int.class
                && typeClass != Short.class
                && typeClass != short.class
                && typeClass != Double.class
                && typeClass != double.class
                && typeClass != Float.class
                && typeClass != float.class
                && typeClass != Long.class
                && typeClass != long.class
                && typeClass != Boolean.class
                && typeClass != boolean.class
                && typeClass != String.class
                && typeClass != byte[].class
                && typeClass != Byte[].class) {
            throw new SQLiteException(field.getName() + " in " + tableName
                    + " is not in supported data type in SQLITE");
        }
    }

    private static class KeyValue<T> implements Serializable {
        final String key;
        final T value;

        KeyValue(String key, T value) {
            this.key = key;
            this.value = value;
        }
    }
}
