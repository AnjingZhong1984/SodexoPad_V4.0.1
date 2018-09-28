package com.janlent.sodexo.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.ImgUtil;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

/**
 * 相机界面
 * 
 * @author Administrator
 * 
 */
public class CameraActivity extends BaseActivity implements OnClickListener {

	private ImageView img; // 用来显示图片
	private Button langch_camera;
	private Button scan_img;
	private String filepath; // 显示文件路径
	private String device_num; // 显示设备编号
	private String userName; // 显示用户名
	private String fileDir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.camera);

		// 获取SharedPreferences的参数数据
		SharedPreferences sp = getSharedPreferences("deviceDate", 0);

		SharedPreferences userShareData = getSharedPreferences("user", 0);

		device_num = sp.getString("device_num", null);
		userName = userShareData.getString("user", null);

		if (device_num == null) {
			device_num = "未知编号";
		}

		if (userName == null) {
			userName = "";
		}

		fileDir = Environment.getExternalStorageDirectory() + "/Sodexo/pictures/" + ConvertUtils.date2Str("yyyy-MM-dd") + "/";

		File file = new File(fileDir);

		if (!file.exists()) {
			file.mkdirs();
		}

		init();
	}

	/**
	 * 初始化自定义组件
	 */
	private void init() {

		img = (ImageView) findViewById(R.id.img);
		langch_camera = (Button) findViewById(R.id.langch_camera);

		scan_img = (Button) findViewById(R.id.scan_img);
		langch_camera.setOnClickListener(this);
		scan_img.setOnClickListener(this);

	}

	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {

		int id = v.getId();
		switch (id) {
		case R.id.langch_camera:

			filepath = fileDir + StringUtils.uid() + ".jpg";

			Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			intent1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filepath)));

			startActivityForResult(intent1, 1);

			break;
		case R.id.scan_img:

			Intent intent2 = new Intent(this, ImageListActivity.class);
			startActivity(intent2);

			break;

		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 水印的字符串
		String infro = "时间:" + ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss") + "   设备编号:" + device_num + "   用户:" + userName;

		switch (requestCode) {
		case 1:

			// 获取资源图片

			Bitmap bitmap = ImgUtil.getBitmapFromFile(filepath, 2);

			// 将时间和IMEI水印入图片中

			if (bitmap != null) {

				Bitmap newBitmap = ImgUtil.createBitmapForWatermark(bitmap, infro, 35, 50, 50);

				img.setImageBitmap(newBitmap);

				// 将bitmap保存

				ImgUtil.saveJPGE_After(newBitmap, filepath);

			}

			break;
		}
	}

}
