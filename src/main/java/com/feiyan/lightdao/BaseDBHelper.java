package com.feiyan.lightdao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.feiyan.lightdao.annotation.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * An enhanced SQLiteOpenHelper, it can auto create tables with table class,
 * in your project there should have one or more databases and every databases
 * should have its own Helper: create one and extend {@link BaseDBHelper}
 *
 * @author zhangfei
 */
public abstract class BaseDBHelper extends SQLiteOpenHelper {
    private final List<Class<? extends Entity>> mTableClasses = new ArrayList<>();

    protected abstract void onClassLoad(List<Class<? extends Entity>> tableClasses);

    protected BaseDBHelper(Context context, String databaseName, int version) {
        super(context, databaseName, null, version);
        onClassLoad(mTableClasses);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<? extends Entity> clazz : mTableClasses) {
            // ignore class with View annotation
            Table tableView = clazz.getAnnotation(Table.class);
            if (tableView == null) {
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
