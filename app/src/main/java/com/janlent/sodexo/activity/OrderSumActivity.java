package com.janlent.sodexo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.jenlant.sodexo.R;

/**
 * 未使用的类
 * 
 * @author Administrator
 * 
 */
public class OrderSumActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ordersum);
		initTop();
	}

	/**
	 * 初始化上面title按钮
	 */
	private void initTop() {
		Button back = (Button) findViewById(R.id.back);
		Button camera = (Button) findViewById(R.id.camera);

		back.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				OrderSumActivity.this.finish();
			}
		});

		camera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(OrderSumActivity.this, CameraActivity.class);
				startActivity(intent);
			}
		});

	}
}
