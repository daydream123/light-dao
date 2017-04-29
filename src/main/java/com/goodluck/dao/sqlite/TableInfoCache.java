package com.goodluck.dao.sqlite;

import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.goodluck.dao.annotation.OrderBy;
import com.goodluck.dao.annotation.Table;
import com.goodluck.dao.annotation.View;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A singleton container to save the relation between table class and the cache
 * content, about the cache content @see {@link TableInfo}.
 * 
 * @author zhangfei
 * 
 */
public final class TableInfoCache {
	private Map<Class<? extends BaseTable>, TableInfo> tableCaches = new ConcurrentHashMap<>();

	private static TableInfoCache singleton = null;
	private static final Object lockObj = new Object();

	private TableInfoCache() {}

	private static TableInfoCache getSingleton() {
		if (singleton == null) {
			synchronized (lockObj) {
				singleton = new TableInfoCache();
			}
		}
		return singleton;
	}

	private static class TableInfo {
		String tableName;
		Field[] fields;
		String defaultOrderBy;
	}
	
	private static TableInfo getTableCache(Class<? extends BaseTable> tableClass){
		return getSingleton().tableCaches.get(tableClass);
	}

	private static void saveTableCache(Class<? extends BaseTable> tableClass, TableInfo tableInfo){
		getSingleton().tableCaches.put(tableClass, tableInfo);
	}
	
	/**
	 * Get cached table classes.
	 * 
	 */
	public static List<Class<? extends BaseTable>> getTableClasses() {
		List<Class<? extends BaseTable>> tableClasses = new ArrayList<>();
		Set<Class<? extends BaseTable>> set = getSingleton().tableCaches.keySet();
		for (Class<? extends BaseTable> aClass : set) {
			tableClasses.add(aClass);
		}
		return tableClasses;
	}

	/**
	 * Bind and save relations between table class and table into cache.
	 * 
	 */
	public static void addMapping(Class<? extends BaseTable> tableClass) {
		Table table = tableClass.getAnnotation(Table.class);
		View view = tableClass.getAnnotation(View.class);
		if (table == null && view == null) {
			throw new SQLiteException(
					"Neither Table annotation nor View are not defined on ["
							+ tableClass.getSimpleName() + "]");
		}

		TableInfo cachedObject = getTableCache(tableClass);

		// save mapping if not saved before
		if (cachedObject == null) {
			cachedObject = new TableInfo();
			if (table != null){
				cachedObject.tableName = table.value();
			} else {
				cachedObject.tableName = view.name();
			}
			saveTableCache(tableClass, cachedObject);
		}

		// cache default order by for tables
		Field[] fields = getTableClassFields(tableClass);
		String orderByStr;

		for (Field field : fields) {
			OrderBy orderBy = field.getAnnotation(OrderBy.class);
			if (orderBy != null) {
				String columnName = ReflectTools.getColumnName(field);
				orderByStr = columnName + " " + orderBy.sortType();
				TableInfoCache.saveDefaultOrderBy(tableClass, orderByStr);
				break;
			}
		}
	}

	public static String getTableName(Class<? extends BaseTable> tableClass) {
		TableInfo content = getSingleton().tableCaches.get(tableClass);
		if (content == null || TextUtils.isEmpty(content.tableName)) {
			throw new SQLiteException(
					"The Table mapping of table: "
							+ tableClass.getSimpleName()
							+ " not exists. Please add mapping in child class of BaseDBHelper");
		}
		return content.tableName;
	}

	public static String getDefaultOrderBy(Class<? extends BaseTable> tableClass) {
		TableInfo cache = getSingleton().tableCaches.get(tableClass);
		if (cache == null) {
			return null;
		} else {
			return cache.defaultOrderBy;
		}
	}

	private static void saveDefaultOrderBy(Class<? extends BaseTable> tableClass, String defaultOrderBy) {
		TableInfoCache mappingCache = getSingleton();
		TableInfo content = mappingCache.tableCaches.get(tableClass);
		if (content == null) {
			content = new TableInfo();
			content.defaultOrderBy = defaultOrderBy;
			mappingCache.tableCaches.put(tableClass, content);
		} else {
			content.defaultOrderBy = defaultOrderBy;
		}
	}
	
	/**
	 * Bind and save relations between table class and its fields.
	 */
	public static void cacheTableFields(Class<? extends BaseTable> tableClass,
			Field[] fields) {
		TableInfo content = TableInfoCache.getTableCache(tableClass);
		if (content == null) {
			content = new TableInfo();
			content.fields = fields;
			TableInfoCache.saveTableCache(tableClass, content);
		} else {
			content.fields = fields;
		}
	}

	/**
	 * Try Get cached fields of table class, if not exist bind and save first.
	 */
	static Field[] getTableClassFields(Class<? extends BaseTable> tableClass) {
		// return cached fields if cached before
		Field[] fields;
		TableInfo content = TableInfoCache.getTableCache(tableClass);
		if (content != null 
				&& content.fields != null
				&& content.fields.length > 0) {
			return content.fields;
		}

		List<Field> totalFields = new ArrayList<Field>();
		Field[] tableClassFields = tableClass.getDeclaredFields();
		totalFields.addAll(Arrays.asList(tableClassFields));
		
		// cache fields and return
		String objectClassStr = Object.class.toString();
		Class<?> superClass = tableClass.getSuperclass();
		while (superClass != null && !superClass.toString().equals(objectClassStr)) {
			Field[] superClassFields = superClass.getDeclaredFields();
			totalFields.addAll(0, Arrays.asList(superClassFields));
			superClass = superClass.getSuperclass();
		}

		// filter out static fields which are not table field
		List<Field> fieldsToRemove = new ArrayList<Field>();
		for (Field field : totalFields) {
			if (Modifier.isStatic(field.getModifiers())) {
				fieldsToRemove.add(field);
			}
		}
		totalFields.removeAll(fieldsToRemove);

		fields = totalFields.toArray(new Field[totalFields.size()]);

		// save cache
		content = TableInfoCache.getTableCache(tableClass);
		if (content == null) {
			content = new TableInfo();
			content.fields = fields;
			TableInfoCache.saveTableCache(tableClass, content);
		} else {
			content.fields = fields;
		}
		return fields;
	}
}
