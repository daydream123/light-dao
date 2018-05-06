package com.feiyan.lightdao.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A help class sub-class of SQLiteOpenHelper, every sub-class of this should be
 * a singleton like below:
 *
 * @author zhangfei
 */
public abstract class DBHelper extends SQLiteOpenHelper {
    private final List<Class<? extends Entity>> mTableClasses = new ArrayList<>();

    protected abstract void onClassLoad(List<Class<? extends Entity>> tableClasses);

    protected DBHelper(Context context, String databaseName, int version) {
        super(context, databaseName, null, version);
        onClassLoad(mTableClasses);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<? extends Entity> clazz : mTableClasses) {
            // ignore class with View annotation
            com.feiyan.lightdao.annotation.Table tableView = clazz.getAnnotation(com.feiyan.lightdao.annotation.Table.class);
            if (tableView != null) {
                continue;
            }

            // create table
            db.execSQL(SQLBuilder.buildTableCreateSQL(clazz).getSql());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
