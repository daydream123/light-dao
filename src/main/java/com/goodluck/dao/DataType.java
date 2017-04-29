package com.goodluck.dao;

import android.database.sqlite.SQLiteException;

import java.lang.reflect.Field;

/**
 * Here types are java field types,
 * 
 * @author zf08526
 * 
 */
public final class DataType {
	public static final String NULL = "NULL";

	public static final String INTEGER = "INTEGER";

	public static final String BLOB = "BLOB";

	public static final String TEXT = "TEXT";

	// Note: all number type will be treat as INTEGER in sqlite3
	public static final String getDataTypeByField(Field field) {
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
			return DataType.INTEGER;
		} else if (dataTypeClass == Float.class || dataTypeClass == float.class) {
			return DataType.INTEGER;
		} else if (dataTypeClass == Boolean.class || dataTypeClass == boolean.class) {
			return DataType.INTEGER;
		} else if (dataTypeClass == Byte[].class || dataTypeClass == byte[].class) {
			return DataType.BLOB;
		} else {
			throw new SQLiteException("field [" + field.getName() + "] is not primitive data type.");
		}
	}
}
