package com.janlent.sodexo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.Image3DAdapter;
import com.janlent.sodexo.widget.GalleryFlow;
import com.jenlant.sodexo.R;

public class Image3DActivity extends BaseActivity implements OnItemLongClickListener {

	private GalleryFlow mGalleryFlow;
	private String itemImagePath;
	private Image3DAdapter mImage3DAdapter;

	public static List<String> imageList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_gallery);
		mGalleryFlow = (GalleryFlow) findViewById(R.id.Gallery02);

		new Thread(run).start();

		mGalleryFlow.setOnItemClickListener(new GalleryFlow.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				itemImagePath = (String) view.getTag();
				Intent intent = new Intent(Image3DActivity.this, ImageActivtiy.class);
				intent.putExtra("imgPath", itemImagePath);

				startActivity(intent);
				overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			}
		});

		mGalleryFlow.setOnItemLongClickListener(this);

	}

	private void getImagesFromSD() {

		String imageFiles = getIntent().getStringExtra("imgPath");

		if (imageFiles != null) {

			File f = new File(imageFiles);
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				f = new File(imageFiles);
			} else {
				Toast.makeText(Image3DActivity.this, "请确认插入SD卡", 1).show();
			}

			File[] files = f.listFiles();

			/**
			 * 将所有图像文件的路径存入ArrayList列表
			 */
			if (files != null) {

				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (isImageFile(file.getPath())) {
						imageList.add(file.getPath());
					}
				}
			}

		}
	}

	/**
	 * @param fName
	 * @return
	 */
	private boolean isImageFile(String fName) {
		boolean re;
		String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

		/**
		 * 依据文件扩展名判断是否为图像文件
		 */
		if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
			re = true;
		} else {
			re = false;
		}
		return re;
	}

	Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:
				if (imageList != null) {
					mImage3DAdapter = new Image3DAdapter(Image3DActivity.this);
					mGalleryFlow.setAdapter(mImage3DAdapter);
				}
				break;

			case 1:

				mImage3DAdapter.notifyDataSetChanged();

				break;

			default:
				break;
			}

		}

	};

	Runnable run = new Runnable() {

		@Override
		public void run() {

			getImagesFromSD();

			hand.obtainMessage(0).sendToTarget();

		}
	};

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		// TODO Auto-generated method stub

		String imgPath = (String) view.getTag();
		final File image = new File(imgPath);

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("提示");
		alertDialog.setMessage("删除这张图片？");
		// 设置左面确定
		alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 执行删除
				if (image.exists()) {
					image.delete();
					imageList.remove(position);
					hand.obtainMessage(1).sendToTarget();
				}
			}
		});
		// 设置右边取消
		alertDialog.setNegativeButton("取消", null);
		alertDialog.show();
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		imageList.clear();
	}

}
