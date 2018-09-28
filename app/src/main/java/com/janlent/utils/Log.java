package com.janlent.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;

public final class Log {

	private static final String TAG = "Sodexo";

	public static void d(String paramString) {
		android.util.Log.d(TAG, paramString);
	}

	public static void d(Context context, String paramString) {
		android.util.Log.d(TAG, context.getClass().getName() + ":"
				+ paramString);
	}

	public static void i(String paramString) {
		android.util.Log.i(TAG, paramString);
	}

	public static void i(Context context, String paramString) {
		android.util.Log.i(TAG, context.getClass().getName() + ":"
				+ paramString);
	}

	public static void w(String paramString) {
		android.util.Log.w(TAG, paramString);
	}

	public static void w(Context context, String paramString) {
		android.util.Log.w(TAG, context.getClass().getName() + ":"
				+ paramString);
	}

	public static void w(String paramString, Throwable paramThrowable) {
		android.util.Log.w(TAG, paramString + '\n'
				+ getStackTraceString(paramThrowable));
	}

	public static void w(Context context, String paramString,
			Throwable paramThrowable) {
		android.util.Log.w(TAG, context.getClass().getName() + ":"
				+ paramString + '\n' + getStackTraceString(paramThrowable));
	}

	public static void w(Throwable paramThrowable) {
		android.util.Log.w(TAG, getStackTraceString(paramThrowable));
	}

	public static void w(Context context, Throwable paramThrowable) {
		android.util.Log.w(TAG, context.getClass().getName() + ":"
				+ getStackTraceString(paramThrowable));
	}

	public static void e(String paramString) {
		android.util.Log.e(TAG, paramString);
	}

	public static void e(Context context, String paramString) {
		android.util.Log.e(TAG, context.getClass().getName() + ":"
				+ paramString);
	}

	public static void e(String paramString, Throwable paramThrowable) {
		android.util.Log.e(TAG, paramString + '\n'
				+ getStackTraceString(paramThrowable));
	}

	public static void e(Context context, String paramString,
			Throwable paramThrowable) {
		android.util.Log.e(TAG, context.getClass().getName() + ":"
				+ paramString + '\n' + getStackTraceString(paramThrowable));
	}

	public static void e(Throwable paramThrowable) {
		android.util.Log.e(TAG, getStackTraceString(paramThrowable));
	}

	public static void e(Context context, Throwable paramThrowable) {
		android.util.Log.e(TAG, context.getClass().getName() + ":"
				+ getStackTraceString(paramThrowable));
	}

	public static String getStackTraceString(Throwable paramThrowable) {
		StringWriter localStringWriter = new StringWriter();
		paramThrowable.printStackTrace(new PrintWriter(localStringWriter));
		return localStringWriter.toString();
	}

}
