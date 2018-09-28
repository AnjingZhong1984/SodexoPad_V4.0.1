package com.janlent.utils;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * thread safety
 */
public final class ConvertUtils {

	private ConvertUtils() {
	}

	private final static String[] PATTERNS = new String[] {
			"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd" };

	/**
	 * thread safety
	 */
	public static Date str2Date(String str) {
		if (!StringUtils.hasLength(str)) {
			return null;
		}
		for (String pattern : PATTERNS) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
				return dateFormat.parse(str);
			} catch (ParseException e) {

			}
		}
		throw new RuntimeException("covert data error: " + str);
	}

	/**
	 * thread safety
	 */
	public static String date2Str(String pattern) {
		return date2Str(new Date(), pattern);
	}

	/**
	 * thread safety
	 */
	public static String date2Str(Date date, String pattern) {
		if (date == null) {
			date = new Date();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		return dateFormat.format(date);

	}
	
	/**
	 * thread safety
	 */
	public static String date2Str2(Date date, String pattern) {
		if (date == null) {
			return null;
		}
		
		
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		return dateFormat.format(date);
		
	}
	
	

	/**
	 * thread safety
	 */
	public static Date str2Date(String str, String pattern) {
		if (!StringUtils.hasLength(str)) {
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		try {
			return dateFormat.parse(str);
		} catch (ParseException e) {
			throw new RuntimeException("covert data error: " + str, e);
		}
	}

	/**
	 * thread safety
	 */
	public static String num2String(Number num, String pattern) {
		if (num == null) {
			return null;
		}
		NumberFormat numberFormat = new DecimalFormat(pattern);
		return numberFormat.format(num);
	}

	/**
	 * thread safety
	 */
	public static Integer integer(String str) {
		return convert(str, Integer.class, null);
	}

	/**
	 * thread safety
	 */
	public static Integer integer(String str, Integer defValue) {
		return convert(str, Integer.class, defValue);
	}

	/**
	 * thread safety
	 */
	public static Long lon(String str) {
		return convert(str, Long.class, null);
	}

	/**
	 * thread safety
	 */
	public static Long lon(String str, Long defValue) {
		return convert(str, Long.class, defValue);
	}

	/**
	 * thread safety
	 */
	public static Double dou(String str) {
		return convert(str, Double.class, null);
	}

	/**
	 * thread safety
	 */
	public static Double dou(String str, Double defValue) {
		return convert(str, Double.class, defValue);
	}

	/**
	 * thread safety
	 */
	public static <T> T convert(String str, Class<T> clazz) {
		return convert(str, clazz, null);
	}

	/**
	 * thread safety
	 */
	private static <T> T convert(String str, Class<T> clazz, T defValue) {
		if (!StringUtils.hasLength(str)) {
			return defValue;
		}
		try {
			Method valueOf = clazz.getMethod("valueOf",
					new Class[] { String.class });
			return (T) valueOf.invoke(null, StringUtils.trim(str));
		} catch (Exception e) {
			e.printStackTrace();

		}
		return defValue;
	}

	public  static String int2ip(long ipInt) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}

}
