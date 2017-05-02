package com.goodluck.dao.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.goodluck.dao.annotation.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A help class sub-class of SQLiteOpenHelper, every sub-class of this should be
 * a singleton like below:
 *
 * @author zhangfei
 */
public abstract class BaseDBHelper extends SQLiteOpenHelper {
    private List<Class<? extends BaseTable>> mTableClasses = new ArrayList<>();

    protected abstract void onClassLoad(List<Class<? extends BaseTable>> tableClasses);

    protected BaseDBHelper(Context context, String databaseName, int version) {
        super(context, databaseName, null, version);

        onClassLoad(mTableClasses);
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        for (Class<? extends BaseTable> clazz : mTableClasses) {
            // ignore class with View annotation
            View tableView = clazz.getAnnotation(View.class);
            if (tableView != null) {
                continue;
            }

            // ignore class whose father class is BaseView
            try {
                if (!clazz.newInstance().isTable()) {
                    continue;
                }
            } catch (InstantiationException e) {
                throw new SQLiteException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new SQLiteException(e.getMessage());
            }

            // create table
            db.execSQL(SQLBuilder.buildTableCreateSQL(clazz).getSql());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
