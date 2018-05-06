package com.feiyan.lightdao.tables;

import com.feiyan.lightdao.annotation.Column;
import com.feiyan.lightdao.annotation.Foreign;
import com.feiyan.lightdao.annotation.Table;
import com.feiyan.lightdao.sqlite.Entity;

/**
 * Created by zhangfei on 2017/4/29.
 */
@Table("student")
public class Student extends Entity {

    @Foreign(Teacher.class)
    @Column(name = "teacher_id", notnull = true)
    public long teacherId;

    @Column(name = "name", notnull = true)
    public String name;

    @Column(name = "age", notnull = true)
    public Integer age;
}
