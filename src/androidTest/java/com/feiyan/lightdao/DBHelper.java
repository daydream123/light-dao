package com.feiyan.lightdao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.feiyan.lightdao.sqlite.BaseDBHelper;
import com.feiyan.lightdao.sqlite.BaseTable;
import com.feiyan.lightdao.sqlite.DBUtils;
import com.feiyan.lightdao.sqlite.SQL;
import com.feiyan.lightdao.sqlite.SQLBuilder;
import com.feiyan.lightdao.tables.Student;

import java.util.List;

/**
 * Created by zhangfei on 2017/4/29.
 */
public class DBHelper extends BaseDBHelper {
    @SuppressLint("StaticFieldLeak")
    private static DBHelper mSingleton;

    private DBHelper(Context context) {
        super(context, "school.db", 1);
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
