package com.goodluck.dao.sqlite;

import android.os.AsyncTask;

import java.util.List;

/**
 * For some special SQL query which need more time to get result, it should be
 * queried in background thread, but you can use this for convenience.
 * 
 * @author zf08526
 * 
 */
public abstract class HeavyQuery<T extends BaseTable> extends AsyncTask<Void, Void, List<T>> {
	private DBUtils mDBUtils;
	private Class<T> mTableClass;
	private String mSelection;
	private String[] mSelectionArgs;
	private String mGroupBy;
	private String mHaving;
	private String mOrderBy;
	private int mLimitOffset;
	private int mLimitSize;

	/**
	 * Query result will return via this callback.
	 * 
	 * @param result
	 *            A List contains search result.
	 */
	public abstract  void onQueryComplete(List<T> result);

	public HeavyQuery withDbUtils(DBUtils DBUtils){
		mDBUtils = DBUtils;
		return this;
	}

	public HeavyQuery withTableClass(Class<T> tableClass){
		mTableClass = tableClass;
		return this;
	}

	public HeavyQuery withSelectionAndArgs(String selection, String[] args){
		mSelection = selection;
		mSelectionArgs = args;
		return this;
	}

	public HeavyQuery withGroupBy(String groupBy) {
		mGroupBy = groupBy;
		return this;
	}

	public HeavyQuery withHaving(String having){
		mHaving = having;
		return this;
	}

	public HeavyQuery withOrderBy(String orderBy){
		mOrderBy = orderBy;
		return this;
	}

	public HeavyQuery withLimit(int limitOffset, int limitSize) {
		mLimitOffset = limitOffset;
		mLimitSize = limitSize;
		return this;
	}

	@Override
	protected List<T> doInBackground(Void... params) {
		if (mDBUtils == null) {
			throw new IllegalArgumentException("DBUtils cannot be null");
		}

		if (mTableClass == null) {
			throw new IllegalArgumentException("TableClass cannot be null");
		}

		if (mLimitOffset > 0 && mLimitSize > 0) {
			return mDBUtils.findWithLimit(mTableClass, mSelection,
					mSelectionArgs, mGroupBy, mHaving, mOrderBy,
					mLimitOffset, mLimitSize);
		} else {
			return mDBUtils.find(mTableClass, mSelection, mSelectionArgs, mGroupBy, mHaving, mOrderBy);
		}
	}

	@Override
	protected void onPostExecute(List<T> result) {
		onQueryComplete(result);
	}

	/**
	 * Start heavy SQL query, the query result will be responsed in
	 * {@link HeavyQuery#onQueryComplete(List)}
	 */
	public void startQuery() {
		execute();
	}

}
