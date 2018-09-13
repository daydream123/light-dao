package com.feiyan.lightdao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.feiyan.lightdao.annotation.Column;
import com.feiyan.lightdao.annotation.Table;

import java.lang.reflect.Field;
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
    public final void onCreate(SQLiteDatabase db) {
        for (Class<? extends Entity> clazz : mTableClasses) {
            // ignore class with View annotation
            Table tableView = clazz.getAnnotation(Table.class);
            if (tableView == null) {
                continue;
            }

            // create table
            db.execSQL(SQLBuilder.buildCreateSQL(clazz).getSql());
        }
    }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        autoMigrate(db, mTableClasses);
    }

    private void autoMigrate(SQLiteDatabase db, List<Class<? extends Entity>> tableClasses) {
        for (Class<? extends Entity> clazz : tableClasses) {
            String tableName = ReflectTools.getTableName(clazz);
            boolean exist = ReflectTools.isTableExist(db, tableName);
            if (exist) {
                Field[] fields = ReflectTools.getClassFields(clazz);
                for (Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    if (column == null) {
                        continue;
                    }

                    String columnName = !TextUtils.isEmpty(column.name()) ? column.name() : field.getName();
                    String dataType = ReflectTools.getDataTypeByField(field);
                    boolean columnExist = ReflectTools.isColumnExist(db, tableName, columnName);
                    if (!columnExist) {
                        db.execSQL("ALTER TABLE " + tableName + " ADD " + columnName + " " + dataType);
                    }
                }
            } else {
                db.execSQL(SQLBuilder.buildCreateSQL(clazz).getSql());
            }
        }
    }



}
