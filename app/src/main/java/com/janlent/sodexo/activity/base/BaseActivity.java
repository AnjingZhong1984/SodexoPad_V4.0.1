package com.janlent.sodexo.activity.base;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.janlent.utils.Config;

public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("sodexo_1", this.getClass().getName());
		super.onCreate(savedInstanceState);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Config.SCREEN_WIDTH = dm.widthPixels;
		Config.SCREEN_HEIGHT = dm.heightPixels;
		Config.DENSITY = this.getResources().getDisplayMetrics().density;
	}

	protected void ii(String s) {
		Log.i("----------------", s);
	}
}
