package com.janlent.sodexo.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.janlent.utils.Log;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.server.UpDownDataServer;
import com.janlent.utils.BackupDBTask;
import com.janlent.utils.ConvertUtils;
import com.jenlant.sodexo.R;

/**
 * 设置界面 包含了数据库备份 恢复 生成离线上传文件 离线同步文件 还有 查看设备参数
 * 
 * @author Administrator
 * 
 */
public class SettingActivity extends BaseActivity implements OnClickListener {

	private Button back_up; // 进行备份操作
	private Button recovery; // 进行还原操作
	private Button make_off_line_data; // 生成离线文件操作
	private Button off_line_sh_data; // 离线同步数据操作
	private Button device_data; // 设置设备参数

	private LinearLayout l_pro;

	private TextView actionTishi;

	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		init();

	}

	private void init() {
		back_up = (Button) findViewById(R.id.back_up);
		recovery = (Button) findViewById(R.id.recovery);
		make_off_line_data = (Button) findViewById(R.id.make_off_line_data);
		off_line_sh_data = (Button) findViewById(R.id.off_line_sh_data);
		device_data = (Button) findViewById(R.id.device_data);
		l_pro = (LinearLayout) findViewById(R.id.l_pro);
		actionTishi = (TextView) findViewById(R.id.action_tishi);

		back_up.setOnClickListener(this);
		recovery.setOnClickListener(this);
		make_off_line_data.setOnClickListener(this);
		off_line_sh_data.setOnClickListener(this);

		device_data.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();
		switch (id) {
		// 备份
		case R.id.back_up:
			new BackupDBTask(SettingActivity.this, handler, null).execute("backupDatabase");
			Toast.makeText(this, "数据库备份成功！", Toast.LENGTH_LONG).show();
			break;
		// 恢复
		case R.id.recovery:
			intent = new Intent(SettingActivity.this, ShowDatabaseActivity.class);
			File DBbackup_filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/DBbackup/");
			if (!DBbackup_filePath.exists()) {
				DBbackup_filePath.mkdirs();
			}
			intent.putExtra("filePath", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/DBbackup/");
			intent.putExtra("mark", "DBbackup");

			startActivity(intent);
			break;
		// 生成离线同步数据
		case R.id.make_off_line_data:
			intent = new Intent(SettingActivity.this, ShowDatabaseActivity.class);
			File UpOffLine_filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/UpOffLineData/");
			if (!UpOffLine_filePath.exists()) {
				UpOffLine_filePath.mkdirs();
			}
			intent.putExtra("filePath", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/UpOffLineData/");
			intent.putExtra("mark", "UpOffLineData");

			startActivity(intent);
			// new Thread(makeOffLineData).start();

			break;
		// 离线同步数据
		case R.id.off_line_sh_data:
			intent = new Intent(SettingActivity.this, ShowDatabaseActivity.class);
			File DownOffLineData_filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/DownOffLineData/");
			if (!DownOffLineData_filePath.exists()) {
				DownOffLineData_filePath.mkdirs();
			}
			intent.putExtra("filePath", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/DownOffLineData/");
			intent.putExtra("mark", "DownOffLineData");
			startActivity(intent);
			break;
		// 设置设备参数
		case R.id.device_data:
			intent = new Intent(this, DeviceActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:

				actionTishi.setText(msg.obj + "");
				l_pro.setVisibility(View.VISIBLE);

				break;

			case 1:

				actionTishi.setText(msg.obj + "");
				l_pro.setVisibility(View.GONE);

				break;

			case 2:

				break;

			case 3:
				break;

			default:
				break;
			}

		}

	};

	// 生成离线同步上传数据线程

	Runnable makeOffLineData = new Runnable() {

		@Override
		public void run() {

			handler.obtainMessage(0, "离线数据生成中...").sendToTarget();

			String upOfflineDate = UpDownDataServer.upData(null, null, SettingActivity.this);

			FileOutputStream file = null;

			// 生成离线同步文件
			if (upOfflineDate != null) {
				try {
					File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/UpOffLineData/");

					if (!filePath.exists()) {
						filePath.mkdirs();
					}

					file = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/UpOffLineData/" + "Sodexo" + ConvertUtils.date2Str("yyyyMMddHHmmss")
							+ ".data"));

					file.write(upOfflineDate.getBytes("UTF-8"));

					file.flush();

				} catch (FileNotFoundException e) {
					Log.e(SettingActivity.this, "FileNotFoundException");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(SettingActivity.this, "IOException");
					e.printStackTrace();
				} finally {
					if (file != null) {
						try {
							file.close();

							handler.obtainMessage(1, "离线数据生成成功！").sendToTarget();

							intent = new Intent(SettingActivity.this, ShowDatabaseActivity.class);

							intent.putExtra("filePath", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/UpOffLineData/");
							intent.putExtra("mark", "UpOffLineData");

							startActivity(intent);

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();

			return true;

		}
		return false;
	}
}
