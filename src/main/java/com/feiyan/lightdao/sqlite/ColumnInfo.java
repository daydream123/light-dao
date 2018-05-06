package com.feiyan.lightdao.sqlite;

import android.text.TextUtils;

/**
 *
 * Column info defined in table class.
 *
 * @author zhangfei
 */
public class ColumnInfo {
    private String name;
    private String aliasName;
    private boolean notNull;
    private boolean unique;
    private String defVal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasName() {
        if (TextUtils.isEmpty(aliasName)) {
            return name;
        }
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getDefVal() {
        return defVal;
    }

    public void setDefVal(String defVal) {
        this.defVal = defVal;
    }
}
