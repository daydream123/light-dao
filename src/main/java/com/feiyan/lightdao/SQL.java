package com.feiyan.lightdao;

import java.util.LinkedList;

/**
 * A container class consists of SQL and arguments.
 * 
 * @author zhangfei
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
				LinkedList<Object> converted = new LinkedList<>();
				for (Object arg : bindArgs) {
					converted.add(convertEscapeChar(arg));
				}
				return converted.toArray();
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
					strings[i] = value == null ? null : convertEscapeChar(value).toString();
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
			bindArgs = new LinkedList<>();
		}
		bindArgs.add(arg);
	}

	public void addConvertedBindArg(Object arg) {
		if (bindArgs == null) {
			bindArgs = new LinkedList<>();
		}
		bindArgs.add(convertEscapeChar(arg));
	}

	public void addBindArgs(Object... bindArgs) {
		if (bindArgs != null) {
			for (Object arg : bindArgs) {
				addBindArg(arg);
			}
		}
	}
	
	static Object convertEscapeChar(Object value) {
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
