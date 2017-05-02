package com.goodluck.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.goodluck.dao.sqlite.BaseDBHelper;
import com.goodluck.dao.sqlite.BaseTable;
import com.goodluck.dao.sqlite.DBUtils;
import com.goodluck.dao.sqlite.SQL;
import com.goodluck.dao.sqlite.SQLBuilder;
import com.goodluck.dao.tables.Student;

import java.util.List;

/**
 * Created by zhangfei on 2017/4/29.
 */
public class DBHelper extends BaseDBHelper {
    @SuppressLint("StaticFieldLeak")
    private static DBHelper mSingleton;

    private DBHelper(Context context) {
        super(context, "school", 1);
    }

    private static DBHelper getSingleton(Context context) {
        if (mSingleton == null) {
            synchronized (DBHelper.class) {
                mSingleton = new DBHelper(context.getApplicationContext());
            }
        }
        return mSingleton;
    }

    /**
     * create DBUtils with context
     */
    static DBUtils with(Context context){
        return DBUtils.create(getSingleton(context));
    }

    @Override
    protected void onClassLoad(List<Class<? extends BaseTable>> tableClasses) {
        tableClasses.add(Student.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        SQL sql = SQLBuilder.buildTableCreateSQL(Student.class);
    }
}
