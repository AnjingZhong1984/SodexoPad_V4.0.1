package com.janlent.sodexo.app;

import android.app.Application;

/**
 * 全局数据存储，程序异常文件
 * 
 * @author Administrator
 * 
 */
public class App extends Application {
	

	@Override
	public void onCreate() {
		super.onCreate();
	
		


		// 异常处理，不需要处理时注释掉这两句即可！
		//
		// CrashHandler crashHandler = CrashHandler.getInstance();
		//
		// // 注册crashHandler
		//
		// crashHandler.init(getApplicationContext());

	}
}
