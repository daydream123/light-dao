package com.feiyan.lightdao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.feiyan.lightdao.tables.Student;
import com.feiyan.lightdao.tables.Teacher;

import java.util.List;

/**
 * @author zhangfei
 */
public class DBHelper extends BaseDBHelper {
    private static final String DATABASE_NAME = "school.db";
    private static final int VERSION = 1;

    @SuppressLint("StaticFieldLeak")
    private static DBHelper sSingleton;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, VERSION);
    }

    private static DBHelper getSingleton(Context context) {
        if (sSingleton == null) {
            synchronized (DBHelper.class) {
                sSingleton = new DBHelper(context.getApplicationContext());
            }
        }
        return sSingleton;
    }

    public static DBUtils with(Context context) {
        return DBUtils.create(getSingleton(context));
    }

    /**
     * all table classes should configured here
     *
     * @param tableClasses table classes
     */
    @Override
    protected void onClassLoad(List<Class<? extends Entity>> tableClasses) {
        tableClasses.add(Student.class);
        tableClasses.add(Teacher.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);

        /* upgrade db version by version
        if (oldVersion < VERSION) {
            SQL sql = SQLBuilder.buildTableCreateSQL(Student.class);
            db.execSQL(sql.getSql());
        }
        */
    }
}
