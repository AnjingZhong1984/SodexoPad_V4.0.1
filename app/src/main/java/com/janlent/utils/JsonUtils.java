package com.janlent.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 数据类型的转换
 * 
 * @author Administrator
 * 
 */
public class JsonUtils {

	/**
	 * 将Json类型数据转换成List类型数据
	 * 
	 * @param arr
	 *            Json数据
	 * @return List数据
	 * @throws Exception
	 */
	public static List<Object> jsonToArray(JSONArray arr) throws Exception {
		List<Object> ret = new ArrayList<Object>(arr.length());

		for (int i = 0; i < arr.length(); i++) {
			Object elem = arr.get(i);
			if (elem instanceof JSONObject) {
				ret.add(jsonToMap((JSONObject) elem));
			} else if (elem instanceof JSONArray) {
				ret.add(jsonToArray((JSONArray) elem));
			} else if (elem instanceof String) {
				ret.add(elem);
			} else if (elem instanceof Number) {
				ret.add(elem);
			} else {
			}
		}
		return ret;
	}

	/**
	 * 将Json类型数据转换成Map类型数据
	 * 
	 * @param json
	 *            Json数据
	 * @return Map数据
	 * @throws Exception
	 */
	public static Map<String, Object> jsonToMap(JSONObject json) throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();
		Iterator<?> keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object value = json.get(key);
			if (value instanceof JSONObject) {
				ret.put(key, jsonToMap((JSONObject) value));
			} else if (value instanceof JSONArray) {
				ret.put(key, jsonToArray((JSONArray) value));
			} else if (value instanceof String) {
				ret.put(key, value);
			} else if (value instanceof Number) {
				ret.put(key, value);
			} else if (value == JSONObject.NULL) {
				ret.put(key, null);
			} else {
			}
		}
		return ret;
	}

	/**
	 * 通过网络获取Map类型数据
	 * 
	 * @param url
	 *            指定url接口
	 * @return Map数据
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getMap(String url) {

		try {
			Map<String, Object> map;
			String result = "";
			WeakReference cache = new WeakReference(result);
			Log.e("url--->" + url);
			result = new HttpUtils().queryStringForGet(url);
			Log.e("result--->" + result);
			if (result != null) {
				JSONObject json = new JSONObject(result);
				map = jsonToMap(json);
				result = null;
				if (map != null) {
					return map;
				}
			}
			return null;

		} catch (Exception e) {
			Log.e(e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将字符串转换成Map数据
	 * 
	 * @param data
	 *            字符串
	 * @return Map数据
	 */
	public static Map<String, Object> getDataMap(String data) {

		try {
			Map<String, Object> map;
			if (data != null) {
				JSONObject json = new JSONObject(data);

				map = jsonToMap(json);
				if (map != null) {
					return map;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
