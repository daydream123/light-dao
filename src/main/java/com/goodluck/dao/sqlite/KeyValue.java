package com.goodluck.dao.sqlite;

import java.io.Serializable;

/**
 * Created by zhangfei on 2017/4/29.
 */
class KeyValue<T> implements Serializable {
    public final String key;
    public final T value;

    public KeyValue(String key, T value) {
        this.key = key;
        this.value = value;
    }
}
