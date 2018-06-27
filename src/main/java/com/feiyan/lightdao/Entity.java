package com.feiyan.lightdao;

import com.feiyan.lightdao.annotation.Column;
import com.feiyan.lightdao.annotation.ID;

import java.io.Serializable;

/**
 * Table class should extend it so that every table instance will have id field inside.
 *
 * @author zhangfei
 *
 */
public class Entity extends Query implements Serializable {
    private static final long serialVersionUID = -6833637753877258272L;

    // ID value of new created object but not saved
    public static final long NOT_SAVED = 0;

    @ID
    @Column(name = _ID)
    public long id = NOT_SAVED;

    // All classes share this
    public static final String _ID = "_id";
    public static final String[] COUNT_COLUMNS = new String[]{"count(*)"};
}
