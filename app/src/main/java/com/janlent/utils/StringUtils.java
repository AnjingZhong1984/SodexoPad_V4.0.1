package com.janlent.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 字符串工具类
 * 
 * @author Administrator
 * 
 */
public class StringUtils {

	public static boolean hasLength(String str) {
		return str != null && str.length() > 0;
	}

	/**
	 * 取掉字符串中的空格符
	 * 
	 * @param str
	 *            要处理的字符串
	 * @return String
	 */
	public static String trim(String str) {
		if (!hasLength(str)) {
			return "";
		}
		char[] arrs = str.toCharArray();
		int start = 0;
		int end = arrs.length - 1;
		while (start < arrs.length && Character.isWhitespace(arrs[start])) {
			start++;
		}
		while (end > 0 && Character.isWhitespace(arrs[end])) {
			end--;
		}
		if (start > end) {
			return "";
		}
		return new String(arrs, start, end - start + 1);
	}

	/**
	 * 生成md5校验码
	 * 
	 * @param str
	 *            原始字符串
	 * @return 校验码
	 */
	public static String md5(String str) {
		if (!hasLength(str)) {
			return str;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(str.getBytes("UTF-8"));
			byte hash[] = digest.digest();
			StringBuffer md5Password = new StringBuffer(hash.length * 2);
			for (int i = 0; i < hash.length; i++) {
				if (((int) hash[i] & 0xff) < 0x10) {
					md5Password.append("0");
				}

				md5Password.append(Long.toString((int) hash[i] & 0xff, 16));
			}
			return md5Password.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("md5 failure: " + str, e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("md5 failure: " + str, e);
		}
	}
	
	public static String lpad(String str, char pad, int length) {
		int size = hasLength(str) ? str.length() : 0;
		if (size > length) {
			return str;
		}
		StringBuffer ret = new StringBuffer(length);
		for (int i =0; i < length - size; i++) {
			ret.append( pad);
		}
		return ret.append( str).toString();
	}
	
	public static String rpad(String str, char pad, int length) {
		int size = hasLength(str) ? str.length() : 0;
		if (size > length) {
			return str;
		}
		StringBuffer ret = new StringBuffer(str);
		for (int i =0; i < length - size; i++) {
			ret.append( pad);
		}
		return ret.toString();
	}
	
	public static void main(String...args) {
	
		System.out.println(lpad("1", '0', 3));
	}

	/**
	 * 生成唯一码
	 * 
	 * @return
	 */
	public static String uid() {
		return (java.util.UUID.randomUUID() + "").replace("-", "");

	}

}
