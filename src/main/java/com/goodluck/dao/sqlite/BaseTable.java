package com.goodluck.dao.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.goodluck.dao.annotation.Column;
import com.goodluck.dao.annotation.ID;
import com.goodluck.dao.annotation.Table;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * All table class must extends it so that DBUtils control them.
 * 
 * @author zhangfei
 *
 */
public abstract class BaseTable implements Serializable {
	private static final long serialVersionUID = -6833637753877258272L;

	// Newly created objects get this id
	public static final long NOT_SAVED = 0;

	// The id of the Content
	@ID
	@Column(name = _ID)
	public long id = NOT_SAVED;

	// All classes share this
	public static final String _ID = "_id";
	public static final String[] COUNT_COLUMNS = new String[] { "count(*)" };
	public static final String[] ID_PROJECTION = new String[] { _ID };
	public static final int ID_PROJECTION_COLUMN = 0;
	public static final String ID_SELECTION = _ID + " =?";

	/**
	 * Write the Content into a ContentValues container
	 */
	ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		Field[] fields = TableInfoCache.getTableClassFields(getClass());

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
				if (columnName.equals(_ID) && field.getLong(this) == NOT_SAVED) {
					continue;
				}
			} catch (IllegalAccessException e) {
				throw new SQLiteException("IllegalAccessException: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				throw new SQLiteException("IllegalArgumentException: " + e.getMessage());
			}

			try {
				// put field value into ContentValues which is not null
				if (field.get(this) != null) {
					putValue(values, columnName, field);
				}
			} catch (IllegalAccessException e) {
				throw new SQLiteException("IllegalAccessException:" + e.getMessage());
			} catch (IllegalArgumentException e) {
				throw new SQLiteException("IllegalArgumentException:" + e.getMessage());
			}
		}
		return values;
	}

	private void putValue(ContentValues values, String columnName, Field field) throws IllegalAccessException {
		Class<?> typeClass = field.getType();
		if (typeClass == Integer.class || typeClass == int.class) {
			values.put(columnName,  field.getInt(this));
		} else if (typeClass == Short.class || typeClass == short.class) {
			values.put(columnName,  field.getShort(this));
		} else if (typeClass == Double.class || typeClass == double.class) {
			values.put(columnName,  field.getDouble(this));
		} else if (typeClass == Float.class || typeClass == float.class) {
			values.put(columnName,  field.getFloat(this));
		} else if (typeClass == Long.class || typeClass == long.class) {
			values.put(columnName,  field.getLong(this));
		} else if (typeClass == Boolean.class || typeClass == boolean.class) {
			values.put(columnName,  field.getBoolean(this));
		} else if (typeClass == String.class) {
			values.put(columnName,  field.get(this).toString());
		} else if (typeClass == Byte[].class || typeClass == byte[].class) {
			values.put(columnName,  field.getByte(this));
		}
	}

	/**
	 * Read the Content from a ContentCursor.
	 */
	void restore(Cursor cursor) {
		Field[] fields = TableInfoCache.getTableClassFields(getClass());
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (column == null) {
				continue;
			}

			String columnName = column.name();
			if (TextUtils.isEmpty(columnName)) {
				columnName = field.getName();
			}

			setField(field, this, cursor, columnName);
		}
	}

	private <T extends BaseTable> void setField(Field field, T tableObj, Cursor cursor, String columnName) {
		try {
			int columnIndex = cursor.getColumnIndex(columnName);
			Class<?> dataTypeClass = field.getType();
			
			if ((dataTypeClass == Integer.class || dataTypeClass == int.class)) {
				field.set(tableObj, cursor.getInt(columnIndex));
			} else if (dataTypeClass == Long.class || dataTypeClass == long.class) {
				field.set(tableObj, cursor.getLong(columnIndex));
			} else if (dataTypeClass == String.class) {
				field.set(tableObj, cursor.getString(columnIndex));
			} else if (dataTypeClass == Short.class || dataTypeClass == short.class) {
				field.set(tableObj, cursor.getShort(columnIndex));
			} else if (dataTypeClass == Double.class || dataTypeClass == double.class) {
				field.set(tableObj, cursor.getDouble(columnIndex));
			} else if (dataTypeClass == Float.class || dataTypeClass == float.class) {
				field.set(tableObj, cursor.getFloat(columnIndex));
			} else if (dataTypeClass == Boolean.class || dataTypeClass == boolean.class) {
				field.set(tableObj, cursor.getInt(columnIndex) == 1);
			} else if (dataTypeClass == Byte[].class || dataTypeClass == byte[].class) {
				field.set(tableObj, cursor.getBlob(columnIndex));
			} else {
				throw new SQLiteException("Field [" + field.getName() + "] is not primitive data type.");
			}
		} catch (IllegalAccessException e) {
			throw new SQLiteException("IllegalAccessException:" + e.getMessage());
		} catch (IllegalArgumentException e) {
			Table table = this.getClass().getAnnotation(Table.class);
			throw new SQLiteException("Ursor value cannot be converted to field's value for field [" + field.getName()
					+ "] in table [" + table.value() + "]");
		}
	}

	public boolean isSaved() {
		return id != NOT_SAVED;
	}
	
	public boolean isTable(){
		return true;
	}
}
