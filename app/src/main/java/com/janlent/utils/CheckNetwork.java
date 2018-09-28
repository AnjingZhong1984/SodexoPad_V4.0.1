package com.janlent.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 检测设备是否连接网络
 * 
 * @author Administrator
 * 
 */
public class CheckNetwork {

	private void CheckNetwork() {

	}

	public static boolean checkNetworking(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nwi = cm.getActiveNetworkInfo();
		if (nwi != null) {
			return nwi.isAvailable();
		}
		return false;
	}

}
