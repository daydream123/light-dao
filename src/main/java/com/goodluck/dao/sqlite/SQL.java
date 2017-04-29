package com.goodluck.dao.sqlite;

import java.util.LinkedList;

/**
 * A container class consists of SQL and arguments.
 * 
 * @author zf08526
 * 
 */
public final class SQL {
	private String sql;
	private LinkedList<Object> bindArgs;

	public SQL() {
	}

	public SQL(String sql) {
		this.sql = sql;
	}

	public SQL(String sql, Object... bindArgs) {
		this.sql = sql;
		addBindArgs(bindArgs);
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public LinkedList<Object> getBindArgs() {
		return bindArgs;
	}

	public Object[] getBindArgsAsArray(boolean needConvert) {
		if (bindArgs != null) {
			if (needConvert) {
				LinkedList<Object> conveted = new LinkedList<Object>();
				for (Object arg : bindArgs) {
					conveted.add(convert2DBValue(arg));
				}
				return conveted.toArray();
			} else {
				return bindArgs.toArray();
			}
		}
		return null;
	}

	public String[] getBindArgsAsStrArray(boolean needConvert) {
		if (bindArgs != null) {
			String[] strings = new String[bindArgs.size()];
			for (int i = 0; i < bindArgs.size(); i++) {
				Object value = bindArgs.get(i);
				if (needConvert) {
					strings[i] = value == null ? null : convert2DBValue(value).toString();
				} else {
					strings[i] = value == null ? null : value.toString();
				}
			}
			return strings;
		}
		return null;
	}

	public void addBindArg(Object arg) {
		if (bindArgs == null) {
			bindArgs = new LinkedList<Object>();
		}
		bindArgs.add(arg);
	}

	public void addConvertedBindArg(Object arg) {
		if (bindArgs == null) {
			bindArgs = new LinkedList<Object>();
		}
		bindArgs.add(convert2DBValue(arg));
	}

	public void addBindArgs(Object... bindArgs) {
		if (bindArgs != null) {
			for (Object arg : bindArgs) {
				addBindArg(arg);
			}
		}
	}

	/**
	 * Replace Boolean value as Integer value('1' or '0'); Replace some char
	 * with escape character, like "'", "/", "[", "]", "%", "&", "_".
	 * 
	 * @param value
	 *            original string
	 * @return converted string
	 */
	public static Object convert2DBValue(Object value) {
		if (value instanceof Boolean) {
			return Boolean.valueOf(value.toString()) ? 1 : 0;
		} else if (value instanceof String) {
			String strValue = String.valueOf(value);
			if (strValue.contains("'")) {
				strValue = strValue.replaceAll("'", "''");
			}

			if (strValue.contains("/")) {
				strValue = strValue.replaceAll("/", "//");
			}

			if (strValue.contains("[")) {
				strValue = strValue.replaceAll("\\[", "/[");
			}

			if (strValue.contains("]")) {
				strValue = strValue.replaceAll("]", "/]");
			}

			if (strValue.contains("%")) {
				strValue = strValue.replaceAll("%", "/%");
			}

			if (strValue.contains("&")) {
				strValue = strValue.replaceAll("&", "/&");
			}

			if (strValue.contains("_")) {
				strValue = strValue.replaceAll("_", "/_");
			}

			if (strValue.contains("(")) {
				strValue = strValue.replaceAll("\\(", "/(");
			}

			if (strValue.contains(")")) {
				strValue = strValue.replaceAll("\\)", "/)");
			}
			return "'" + strValue + "'";
		} else {
			return value.toString();
		}
	}

}
