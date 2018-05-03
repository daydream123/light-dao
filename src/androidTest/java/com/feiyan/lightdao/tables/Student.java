package com.feiyan.lightdao.tables;

import com.feiyan.lightdao.annotation.Column;
import com.feiyan.lightdao.annotation.Table;
import com.feiyan.lightdao.sqlite.BaseTable;

/**
 * Created by zhangfei on 2017/4/29.
 */
@Table(Student.TABLE_NAME)
public class Student extends BaseTable {
    public static final String TABLE_NAME = "student";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_ARTICLE = "article";

    @Column(name = COLUMN_NAME, notnull = true)
    public String name;

    @Column(name = COLUMN_AGE, notnull = true)
    public Integer age;

    @Column(name = COLUMN_ARTICLE)
    public byte[] article;

}
