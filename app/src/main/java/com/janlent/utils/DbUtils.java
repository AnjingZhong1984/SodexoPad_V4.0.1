package com.janlent.utils;

import java.util.List;
import java.util.Map;

public final class DbUtils {

	private DbUtils() {
	}

	public static String insertSQL(String tableName, Map<String, Object> map,
			List params) {

		StringBuffer sql = new StringBuffer("insert into " + tableName + "(");
		StringBuffer values = new StringBuffer();
		for (String key : map.keySet()) {
				sql.append(key + ",");
				values.append("?,");
				params.add(map.get(key));
		}
		values.deleteCharAt(values.length() - 1);
		sql.deleteCharAt(sql.length() - 1).append(
				") values( " + values.toString() + ")");
		return sql.toString();
	}

	public static String updateSQL(String tableName, Map<String, Object> map,
			List params) {

		StringBuffer sql = new StringBuffer("update " + tableName + " set ");

		for (String key : map.keySet()) {
			sql.append(key + "=?,");
			params.add(map.get(key));
		}

		sql.deleteCharAt(sql.length() - 1).append(" where 1=1 ");
		System.out.println(sql.toString());
		return sql.toString();
	}

}
