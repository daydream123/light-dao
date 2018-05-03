package com.feiyan.lightdao.annotation;

import com.feiyan.lightdao.sqlite.BaseTable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Foreign {
    Class<? extends BaseTable> tableClass();
}
