package com.goodluck.dao.tables;

import com.goodluck.dao.annotation.Column;
import com.goodluck.dao.annotation.Table;
import com.goodluck.dao.sqlite.BaseTable;

/**
 * Created by zhangfei on 2017/4/29.
 */
@Table(Student.TABLE_NAME)
public class Student extends BaseTable {
    public static final String TABLE_NAME = "student";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";

    @Column(name = COLUMN_NAME, notnull = true)
    public String name;

    @Column(name = COLUMN_AGE, notnull = true)
    public Integer age;

}
