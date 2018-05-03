package com.feiyan.lightdao.sqlite;

/**
 * Define {@link BaseView} to distinguish {@link BaseTable}, but the content are
 * the same.
 * 
 * @author zf08526
 * 
 */
public class BaseView extends BaseTable {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isTable() {
		return false;
	}
}
