package com.goodluck.dao.sqlite;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

/**
 * A convenient tool to do CRUD actions on SQLite database.
 * 
 * @author zf08526
 * 
 */
public final class DBUtils {
	private SQLiteDatabase database;
	private BaseDBHelper dbHelper;

	/**
	 * Create or retrieve sqlite utils instance.
	 * 
	 * @param dbHelper your DBHelper
	 * @return singleton of Sqlite Utils.
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

	private static <T extends BaseTable> T getContent(Cursor cursor, Class<T> tableClass) {
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

	/**
	 * Close database, should called after db operations are completed.
	 */
	public void close() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	public <T extends BaseTable> ConditionBuilder<T> withTable(Class<T> tableClass) {
		return ConditionBuilder.create(getDatabase(), tableClass);
	}

	/**
	 * Insert table with one record.
	 * 
	 * @param record
	 *            subclass of {@link BaseTable}'s instance
	 */
	public <T extends BaseTable> long save(T record) {
		checkModifiable(record.getClass(), "save");
		String tableName = TableInfoCache.getTableName(record.getClass());
		return getDatabase().insert(tableName, null, record.toContentValues());
	}

	/**
	 * Insert table with more than one records.
	 * 
	 * @param tables
	 *            records to save into database.
	 */
	public <T extends BaseTable> void saveAll(List<T> tables) {
		SQLiteDatabase db = getDatabase();
		try {
			db.beginTransaction();
			for (T table : tables) {
				checkModifiable(table.getClass(), "saveAll");
				SQL sql = SQLBuilder.buildInsertSQL(table);
				if (sql != null) {
					db.execSQL(sql.getSql(), sql.getBindArgsAsArray(false));
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Execute more than one SQL job with transaction.
	 * 
	 * @param batchJobs @see {@link BatchJobs}
	 */
	public <T extends BaseTable> boolean executeBatchJobs(BatchJobs batchJobs) {
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
		} catch (SQLException e){
			return false;
		}finally {
			database.endTransaction();
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
