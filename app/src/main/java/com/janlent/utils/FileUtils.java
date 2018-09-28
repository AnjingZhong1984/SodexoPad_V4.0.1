package com.janlent.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * thread safety
 */
public final class FileUtils {

	private FileUtils() {
	}

	/**
	 * thread unsafety
	 */
	public static void close(InputStream is) {
		if (is == null) {
			return;
		}
		try {
			is.close();
		} catch (IOException e) {
		}
	}

	public static void close(OutputStream os) {
		if (os == null) {
			return;
		}
		try {
			os.close();
		} catch (IOException e) {
		}
	}

	/**
	 * 数据流的写操作
	 * 
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void transfer(InputStream is, OutputStream os)
			throws Exception {
		byte[] buffer = new byte[1024];
		try {

			int length = -1;

			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
		} finally {
			close(is);
			close(os);
		}
	}

}
