package com.janlent.sodexo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.ImageListAdapter;
import com.jenlant.sodexo.R;

/**
 * 图片文件夹列表界面
 * 
 * @author Administrator
 * 
 */
public class ImageListActivity extends BaseActivity implements OnItemClickListener {

	private ListView listView; // 显示图片文件
	private List<String> imgFile; // 用来保存文件路径

	private ImageListAdapter adapter; // 文件适配器

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.img_file);

		init();
	}

	/**
	 * 初始化组件
	 */
	private void init() {
		listView = (ListView) findViewById(R.id.listViewFile);
		listView.setOnItemClickListener(this);
		new Thread(run).start();

	}

	/**
	 * 获取图片目录，返回文件路径
	 * 
	 * @return 返回图片的路径List
	 */
	private List<String> getImagesFromSD() {
		List<String> imgFile = new ArrayList<String>();

		File f;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			f = new File(android.os.Environment.getExternalStorageDirectory() + "/Sodexo/pictures/");
			if (!f.exists()) {
				f.mkdirs();
			}

		} else {
			Toast.makeText(ImageListActivity.this, "请确认插入SD卡", 1).show();
			return null;
		}

		File[] files = f.listFiles();

		if (files == null || files.length == 0) {
			return null;
		}

		/**
		 * 将所有图像文件的路径存入ArrayList列表
		 */
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory())
				imgFile.add(file.getName());
		}
		return imgFile;
	}

	/**
	 * 处理接受到的消息
	 */
	Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			// 初始化适配器，显示文件夹
			case 0:

				adapter = new ImageListAdapter(ImageListActivity.this, imgFile);
				if (imgFile != null) {
					listView.setAdapter(adapter);
				}
				break;

			// 图片删除后通知适配器，改变状态，消除已删除的图片显示
			case 1:
				adapter.notifyDataSetChanged();

				break;

			default:
				break;
			}
		}
	};

	/**
	 * 获取图片的路径线程
	 */
	Runnable run = new Runnable() {

		@Override
		public void run() {

			imgFile = getImagesFromSD();

			hand.obtainMessage(0).sendToTarget();

		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Intent intent = new Intent(this, Image3DActivity.class);
		intent.putExtra("imgPath", android.os.Environment.getExternalStorageDirectory() + "/Sodexo/pictures/" + imgFile.get(position));
		startActivity(intent);

	}

}
