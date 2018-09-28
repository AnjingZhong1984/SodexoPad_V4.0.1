package com.janlent.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.janlent.utils.Log;

/**
 * SQL操作
 * 
 * @author Administrator
 * 
 */
public final class Db {

	private Db() {
	}

	private static final String LOGGER_TAG = "SQL";

	public final static SQLiteDatabase DATABASE;
	private final static String SQLiteDatabasePath = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Sodexo/database/Sodexo.sqlite";

	static {

		DATABASE = SQLiteDatabase.openDatabase(SQLiteDatabasePath, null,
				SQLiteDatabase.CONFLICT_IGNORE);

	}

	/**
	 * 执行inset语句
	 * 
	 * @param sql
	 *            insert语句
	 * @param args
	 *            要插入的数据
	 * @return
	 */
	public static int insert(String sql, Object... args) {
		if (sql == null)
			return 0;
		try {
			DATABASE.execSQL(sql, args);
		} catch (Exception e) {

			throw new RuntimeException(e);

		}
		return -1;
	}

	/**
	 * 执行select语句
	 * 
	 * @param sql
	 *            select语句
	 * @param args
	 *            参数
	 * @return 一个数据集合List
	 */
	public static List<Map<String, Object>> select(String sql, Object... args) {
		if (sql == null)
			return null;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		String[] _args = new String[0];
		if (args != null) {
			_args = new String[args.length];

			for (int i = 0; i < args.length; i++) {
				_args[i] = (args[i] == null ? "null" : args[i].toString());

			}
		}
		Cursor cursor = DATABASE.rawQuery(sql, _args);
		String[] columnNames = cursor.getColumnNames();

		try {
			if (cursor.moveToFirst()) {

				for (int i = 0; i < cursor.getCount(); i++) {
					map = new HashMap<String, Object>();

					for (int j = 0; j < cursor.getColumnCount(); j++) {

						map.put(columnNames[j], cursor.getString(j));
					}

					cursor.moveToNext();

					list.add(map);
				}

				if (list != null) {
					return list;
				}

			}

		} catch (Exception e) {
			Log.e("查询多条数据出错/错误信息是：" + e.getMessage());
			throw new RuntimeException(e);

		} finally {

			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
	public static List<Map<String, Object>> select2(String sql, Object... args) {
		if (sql == null)
			return null;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		String[] _args = new String[0];
		if (args != null) {
			_args = new String[args.length];

			for (int i = 0; i < args.length; i++) {
				_args[i] = (args[i] == null ? null : args[i].toString());

			}
		}
		Cursor cursor = DATABASE.rawQuery(sql, _args);
		String[] columnNames = cursor.getColumnNames();

		try {
			if (cursor.moveToFirst()) {

				for (int i = 0; i < cursor.getCount(); i++) {
					map = new HashMap<String, Object>();

					for (int j = 0; j < cursor.getColumnCount(); j++) {

						map.put(columnNames[j], cursor.getString(j));
					}

					cursor.moveToNext();

					list.add(map);
				}

				if (list != null) {
					return list;
				}

			}

		} catch (Exception e) {
			Log.e("查询多条数据出错/错误信息是：" + e.getMessage());
			throw new RuntimeException(e);

		} finally {

			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * 执行selectUnique语句
	 * 
	 * @param sql
	 *            selectUnique语句
	 * @param args
	 *            参数
	 * @return 唯一一条数据
	 */
	public static Map<String, Object> selectUnique(String sql, Object... args) {
		if (sql == null)
			return null;
		Map<String, Object> map = new HashMap<String, Object>();
		String[] _args = new String[0];
		if (args != null) {
			_args = new String[args.length];

			for (int i = 0; i < args.length; i++) {
				_args[i] = (args[i] == null ? "null" : args[i].toString());

			}
		}
		Cursor cursor = DATABASE.rawQuery(sql, _args);
		String[] columnNames = cursor.getColumnNames();
		try {
			if (cursor.moveToFirst()) {
				for (int j = 0; j < cursor.getColumnCount(); j++) {
					map.put(columnNames[j], cursor.getString(j));
				}

				if (map != null) {
					return map;
				}
			}
		} catch (Exception e) {

			Log.e("查询一条数据出错/错误信息是：" + e.getMessage());

			throw new RuntimeException(e);

		} finally {

			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;

	}

	/**
	 * 执行update语句
	 * 
	 * @param sql
	 *            update语句
	 * @param args
	 *            参数
	 * @return
	 */
	public static int update(String sql, Object... args) {

		if (sql == null)
			return 0;

		try {
			DATABASE.execSQL(sql, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
		}
		return -1;
	}

	public static int delete(String sql, Object... args) {

		try {
			DATABASE.execSQL(sql, args);
		} catch (Exception e) {
			Log.e("更新数据出错/错误信息是：" + e.getMessage());
			throw new RuntimeException(e);
		} finally {
		}
		return -1;
	}
}
