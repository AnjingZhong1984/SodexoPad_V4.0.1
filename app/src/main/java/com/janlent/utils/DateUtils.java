package com.janlent.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 * 
 * @author Administrator
 * 
 */
public class DateUtils {

	private DateUtils() {
	};

	public final static long ONE_MINUTE = 60 * 1000;

	public final static long ONE_HOUR = 60 * ONE_MINUTE;

	public final static long ONE_DATE = 24 * ONE_HOUR;

	public final static long ONE_MONTH = 30 * ONE_DATE;

	public final static long ONE_WEEK = 7 * ONE_DATE;

	// public static String getMillisecond() {
	// Date nextDate = ConvertUtils.str2Date( "10-20", "MM-dd");
	// nextDate = new Date( nextDate.getTime() + ONE_DATE);
	// ConvertUtils.date2Str( nextDate, "yyyy-MM-dd");
	// }

	/**
	 * 将字符串转换成指定格式的日期
	 * 
	 * @param str
	 *            日期格式
	 * @return 指定格式的日期
	 */
	public static String getFormatDate(String str) {

		if (str == null) {

			return null;

		} else {
			SimpleDateFormat s = new SimpleDateFormat(str);
			String remaintime = s.format(new Date());

			if (remaintime != null) {
				return remaintime;
			} else {
				return null;
			}
		}

	}

	/**
	 * 获取后一天的日期
	 * 
	 * @param day
	 *            当前日期
	 * @param type
	 *            输出日期格式
	 * @return 后一天的日期
	 */
	public static String getNextDay(String day, String type) {

		Calendar cal = Calendar.getInstance();
		Date date2 = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(type);
		try {
			date2 = sdf.parse(day);
			cal.setTime(date2);
			cal.add(cal.DATE, 1);

			String nextDay = sdf.format(cal.getTime());

			if (nextDay != null) {
				return nextDay;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 获取前一天的日期
	 * 
	 * @param day
	 *            当前日期
	 * @param type
	 *            输出日期格式
	 * @return 前一天的日期
	 */
	public static String getProDay(String day, String type) {
		Calendar cal = Calendar.getInstance();
		Date date2 = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(type);
		try {
			date2 = sdf.parse(day);
			cal.setTime(date2);
			cal.add(cal.DATE, -1);

			String nextDay = sdf.format(cal.getTime());

			if (nextDay != null) {
				return nextDay;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 获取指定日期
	 * 
	 * @param day
	 *            当前日期
	 * @param type
	 *            输出日期格式
	 * @param a
	 *            当前日期加值或减值
	 * @return
	 */

	public static String getDay(String day, String type, int a) {

		Calendar cal = Calendar.getInstance();
		Date date2 = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(type);
		try {
			date2 = sdf.parse(day);
			cal.setTime(date2);
			cal.add(cal.DATE, a);

			String nextDay = sdf.format(cal.getTime());

			if (nextDay != null) {
				return nextDay;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
