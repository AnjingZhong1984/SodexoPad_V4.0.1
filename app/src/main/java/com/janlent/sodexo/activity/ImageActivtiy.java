package com.janlent.sodexo.activity;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.widget.MulitPointTouchListener;
import com.jenlant.sodexo.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivtiy extends BaseActivity {
	private ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.image);

		image = (ImageView) findViewById(R.id.image);
		image.setOnTouchListener(new MulitPointTouchListener(image));

		Intent intent = getIntent();

		String imagePath = intent.getStringExtra("imgPath");

		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

		image.setImageBitmap(bitmap);

	}

}
