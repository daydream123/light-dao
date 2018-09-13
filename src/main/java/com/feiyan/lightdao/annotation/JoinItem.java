package com.feiyan.lightdao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It's bundled with {@link InnerJoinItem}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinItem {
    String firstTable();

    String firstColumn();

    String secondTable();

    String secondColumn();
}