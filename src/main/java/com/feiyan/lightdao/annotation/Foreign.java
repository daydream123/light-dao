package com.feiyan.lightdao.annotation;

import com.feiyan.lightdao.sqlite.Entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define foreign key on field.
 *
 * @author zhangfei
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Foreign {
    Class<? extends Entity> value();
}
