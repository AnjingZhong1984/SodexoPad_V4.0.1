     package com.janlent.utils;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * thread safety
 */
public final class CollectionUtils {

	private CollectionUtils() {
	}

	/**
	 * thread safety
	 */
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof Collection) {
			Collection c = (Collection) obj;
			return c.isEmpty();
		}
		if (obj instanceof Map) {
			Map map = (Map) obj;
			return map.isEmpty();
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}
		return false;
	}

	public static <K, V> Map<K, V> copy(Map<K, V> target) {
		Map<K, V> copy = new HashMap<K, V>(target);
		return copy;
	}
}
