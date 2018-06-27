package com.feiyan.lightdao.conditionbuilder;

import android.database.Cursor;

import com.feiyan.lightdao.Query;

import java.util.List;

public interface BuilderSupport<T extends Query> {

    BuilderSupport<T> withWhere(String whereClause, Object... whereArgs);

    BuilderSupport<T> withGroupBy(String groupBy);

    BuilderSupport<T> withHaving(String having);

    BuilderSupport<T> withOrderBy(String orderBy);

    BuilderSupport<T> withLimit(int limitOffset, int limitSize);

    BuilderSupport<T> withDistinct(boolean distinct);

    T applySearchById(long id);

    int applyCount();

    /**
     * Apply search and return cursor as result
     *
     * @return query cursor
     */
    Cursor applySearch();

    /**
     * Apply search with condition and return list as result
     *
     * @return list of table class object as result
     */
    List<T> applySearchAsList();

    /**
     * Apply search first row with condition
     *
     * @return first item of result
     */
    T applySearchFirst();
}
