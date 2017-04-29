package com.goodluck.dao.sqlite;

import android.content.ContentValues;

import java.util.ArrayList;

/**
 * A container to contain more than one CRUD jobs, and should be executed by
 * {@link DBUtils#executeBatchJobs(BatchJobs)}.
 * 
 * @author zf08526
 * 
 */
public final class BatchJobs {
	private ArrayList<SQL> batchJobs;

	public BatchJobs() {
		this.batchJobs = new ArrayList<SQL>();
	}

	public <T extends BaseTable> void addInsertJob(T table) {
		batchJobs.add(SQLBuilder.buildInsertSQL(table));
	}

	public <T extends BaseTable> void addUpdateJob(Class<T> tableClass, long id, ContentValues values) {
		batchJobs.add(SQLBuilder.buildUpdateSQL(tableClass, id, values));
	}

	public <T extends BaseTable> void addUpdateJob(Class<T> tableClass, String where, String[] selectionArgs,
			ContentValues values) {
		batchJobs.add(SQLBuilder.buildUpdateSQL(tableClass, where, selectionArgs, values));
	}

	public <T extends BaseTable> void addDeleteJob(T table) {
		batchJobs.add(SQLBuilder.buildDeleteSQL(table));
	}

	public <T extends BaseTable> void addDeleteJob(Class<T> tableClass, long id) {
		batchJobs.add(SQLBuilder.buildDeleteSQL(tableClass, id));
	}

	public <T extends BaseTable> void addDeleteJob(Class<T> tableClass, String where, String[] whereArgs) {
		batchJobs.add(SQLBuilder.buildDeleteSQL(tableClass, where, whereArgs));
	}

	public ArrayList<SQL> getBatchJobs() {
		return batchJobs;
	}
}