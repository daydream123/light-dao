package com.feiyan.lightdao.tables;

import com.feiyan.lightdao.annotation.Column;
import com.feiyan.lightdao.annotation.Table;
import com.feiyan.lightdao.sqlite.Entity;

/**
 * Created by zhangfei on 2017/4/29.
 */
@Table("teacher")
public class Teacher extends Entity {

    @Column(name = "name", notnull = true)
    public String name;

}
