package com.janlent.sodexo.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.ShowDataAdapter;
import com.janlent.sodexo.server.UpDownDataServer;
import com.janlent.sodexo.widget.MySeekBar;
import com.janlent.utils.BackupDBTask;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.DataBaseHelper;
import com.janlent.utils.FileUtils;
import com.janlent.utils.JsonUtils;
import com.janlent.utils.Log;
import com.janlent.utils.Utils;
import com.jenlant.sodexo.R;

/**
 * 显示数据库数据界面
 * 
 * @author Administrator
 * 
 */
public class ShowDatabaseActivity extends BaseActivity implements OnItemClickListener {

	private ListView listView;
	Handler handler; // 用于保存和处理消息
	String dataItem; // 用于显示单个数据
	private TextView title; // 显示标题
	private TextView tv_path; // 显示标题
	public static String mark; // 用于标识要执行的操作

	ShowDataAdapter adapter;

	private TextView tvPosition; // 显示医院床位信息
	private TextView tvEnumBase; // 显示枚举信息
	private TextView tvFoodMenu; // 显示菜单信息
	private TextView tvPatient; // 显示病员信息
	private TextView tvUpJson; // 显示上传数据

	private MySeekBar skbPosition; // 显示医院床位信息进度
	private MySeekBar skbEnumBase; // 显示枚举信息进度
	private MySeekBar skbFoodMenu; // 显示菜单信息进度
	private MySeekBar skbPatient; // 显示病员信息进度
	private MySeekBar skbUpJson; // 显示上传数据进度

	private Button cancel_ok;

	private Button btn_get_offdb;
	private LinearLayout l_pro;
	private TextView actionTishi;

	final int PROGRESS_POSITION = 1;
	final int PROGRESS_ENUMBASE = 2;
	final int PROGRESS_FOODMENU = 4;
	final int PROGRESS_PATIENT = 7;
	final int PROGRESS_ORDER_AND_PATIENT = 8;

	final int POSITION_IN_CREASE = -1;
	final int ENUMBASE_IN_CREASE = -2;
	final int FOODMENU_IN_CREASE = -4;
	final int PATIENT_IN_CREASE = -7;
	final int ORDER_AND_PATIENT_IN_CREASE = -8;

	private int progressPosition; // Position进度条
	private int progressEnumBase; // EnumBase进度条
	private int progressFoodMenu; // FoodMenu进度条
	private int progressPatient; // Patient进度条
	private int progressJson; // Json进度条

	private int tallPosition; // Position最大进度
	private int tallEnumBase; // EnumBase最大进度
	private int tallFoodMenu; // FoodMenu最大进度
	private int tallPatient; // Patient最大进度
	private String fileName = "";
	private ArrayList<String> filelist;

	AlertDialog alertDialog;

	private final static String DB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/database/Sodexo.sqlite";
	private final static String DB_BK = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/database/Sodexo_back.sqlite";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showdata);

		init();

		Intent intent = getIntent();
		// 接受Intent传递过来的数据
		ArrayList<String> listData = intent.getStringArrayListExtra("SqliteData");

	}

	/**
	 * 初始化界面view
	 */
	private void init() {
		listView = (ListView) findViewById(R.id.listView_sqlitedata);

		title = (TextView) findViewById(R.id.tatile);
		tv_path = (TextView) findViewById(R.id.tv_path);
		btn_get_offdb = (Button) findViewById(R.id.btn_get_offdb);
		l_pro = (LinearLayout) findViewById(R.id.l_pro);
		actionTishi = (TextView) findViewById(R.id.action_tishi);
		// 获取传递过来的Intent
		Intent intent = getIntent();

		// 取得Intent传递过来的路径
		String filePath = intent.getStringExtra("filePath");
		// 取得Intent传递过来的mark
		mark = intent.getStringExtra("mark");

		if ("DownOffLineData".equals(mark)) { // 判断mark是否等于DownOffLineData

			title.setText("同步数据离线文件(点击同步数据)");

		}
		if ("UpOffLineData".equals(mark)) { // 判断mark是否等于UpOffLineData

			title.setText("上传数据离线文件");
			btn_get_offdb.setVisibility(View.VISIBLE);

		}
		if ("DBbackup".equals(mark)) { // 判断mark是否等于DBbackup

			title.setText("数据库备份文件(点击恢复备份数据库)");

		}
		tv_path.setText("SD卡根目录:" + filePath.substring(filePath.indexOf("/Sodexo")));
		filelist = new ArrayList<String>();

		File downOffLineDataFile = new File(filePath);

		File[] fileFiles = downOffLineDataFile.listFiles();

		if (fileFiles != null) {

			for (File file : fileFiles) { // 遍历文件
				filelist.add(file.getName());
			}

		}
		adapter = new ShowDataAdapter(this, filelist);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		//
		btn_get_offdb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 生成离线数据库
				new Thread(makeOffLineData).start();
			}
		});
		//
		JSONArray orderDetailItem2 = new JSONArray();
		for (int i = 0; i < 3; i++) { // 遍历orderDetailList的数据
			JSONObject json = new JSONObject();
			try {
				json.put(i + "", i + "");
				json.put("CheckCode", i + "");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			orderDetailItem2.put(json);
		}
		Log.e(orderDetailItem2.toString());
	}

	// 生成离线同步上传数据线程

	Runnable makeOffLineData = new Runnable() {

		@Override
		public void run() {

			hand.obtainMessage(8, "离线数据生成中...").sendToTarget();

			String upOfflineDate = UpDownDataServer.upData(null, null, ShowDatabaseActivity.this);
			FileOutputStream file = null;

			// 生成离线同步文件
			if (upOfflineDate != null) {
				try {
					File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/UpOffLineData/");

					if (!filePath.exists()) {
						filePath.mkdirs();
					}
					fileName = "Sodexo" + ConvertUtils.date2Str("yyyyMMddHHmmss") + ".csv";
					Utils.ExportOrderdetail(filePath.getAbsolutePath(), fileName);
					// fileName= "Sodexo"
					// + ConvertUtils.date2Str("yyyyMMddHHmmss")
					// + ".data";
					// file = new FileOutputStream(
					// new File(Environment.getExternalStorageDirectory()
					// .getAbsolutePath()
					// + "/Sodexo/UpOffLineData/"+fileName));
					//
					// file.write(upOfflineDate.getBytes("UTF-8"));
					//
					// file.flush();

				} catch (Exception e) {
					Log.e(ShowDatabaseActivity.this, "FileNotFoundException");
					e.printStackTrace();
				}
				// catch (IOException e) {
				// Log.e(ShowDatabaseActivity.this, "IOException");
				// e.printStackTrace();
				// }
				finally {
					hand.obtainMessage(9, "离线数据生成成功！").sendToTarget();
					// if (file != null) {
					// try {
					// file.close();
					//
					//
					//
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					// }
				}
			}

		}
	};

	// 导出csv格式表单
	public void exportCsv(String fileName) {

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		dataItem = adapter.getItem(arg2);

		if ("DownOffLineData".equals(mark)) {

			showDialog("确认同步离线数据？");

		}

		if ("DBbackup".equals(mark)) {
			showDialog("确认恢复备份数据库？");
		}

	}

	/**
	 * 提示用户操作的对话框
	 * 
	 * @param tatile
	 *            提示用户将做操作的字符串
	 */
	private void showDialog(String tatile) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(tatile);
		builder.setPositiveButton("确认", new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub

				if ("DownOffLineData".equals(mark)) {

					ShowPrompt();
					new Thread(run).start();
				}

				if ("DBbackup".equals(mark)) {

					new BackupDBTask(ShowDatabaseActivity.this, handler, dataItem).execute("restroeDatabase");

					Toast.makeText(ShowDatabaseActivity.this, "数据库恢复完毕。", Toast.LENGTH_SHORT).show();

				}

			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	/**
	 * 离线同步文件
	 */
	Runnable run = new Runnable() {

		@Override
		public void run() {

			try {
				FileInputStream in_bak = new FileInputStream(new File(DB));

				FileOutputStream out_bak = new FileOutputStream(new File(DB_BK));

				FileUtils.transfer(in_bak, out_bak);

				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/DownOffLineData/" + dataItem);
				FileInputStream inputStream = new FileInputStream(file);
				ByteArrayOutputStream array = new ByteArrayOutputStream();
				byte[] byteData = new byte[1024];
				int buffer = 0;

				while ((buffer = inputStream.read(byteData)) != -1) {
					array.write(byteData, 0, buffer);
				}
				Map<String, Object> map = JsonUtils.getDataMap(array.toString("UTF-8"));
				UpDownDataServer.downDate(map, hand, null);

			} catch (Exception e) {
				e.printStackTrace();
				FileInputStream r_in_bak = null;
				FileOutputStream r_out_bak = null;
				try {
					r_in_bak = new FileInputStream(new File(DB_BK));
					r_out_bak = new FileOutputStream(new File(DB));
					FileUtils.transfer(r_in_bak, r_out_bak);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
	};

	/**
	 * 进度条的更新
	 */
	Handler hand = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {

			case 1:

				tallPosition = msg.arg1;

				// 医院床位信息进度条更新最大值设定
				tvPosition.setText("床位信息同步中..." + "(" + progressPosition + "/" + tallPosition + ")");
				if (tallPosition == 0) {
					skbPosition.setMax(1);
				} else {
					skbPosition.setMax(tallPosition);
				}

				skbPosition.postInvalidate();
				tvPosition.postInvalidate();
				break;

			case -1:

				progressPosition = msg.arg1;

				if (progressPosition == 0) {
					skbPosition.setProgress(1);
				} else {
					skbPosition.setProgress(progressPosition);
				}

				// 医院床位信息进度条更新

				skbPosition.postInvalidate();
				tvPosition.setText("床位信息同步中..." + "(" + progressPosition + "/" + tallPosition + ")");
				if (progressPosition == tallPosition) {
					tvPosition.setText("床位信息同步" + "(" + progressPosition + "/" + tallPosition + ")");
					tvPosition.postInvalidate();
				}
				break;

			case 2:

				tallEnumBase = msg.arg1;

				// 枚举信息进度条更新最大字设定
				tvEnumBase.setText("基本信息同步中..." + "(" + progressEnumBase + "/" + tallEnumBase + ")");
				if (tallEnumBase == 0) {
					skbEnumBase.setMax(1);
				} else {
					skbEnumBase.setMax(tallEnumBase);
				}

				skbEnumBase.postInvalidate();
				tvEnumBase.postInvalidate();
				break;

			case -2:

				progressEnumBase = msg.arg1;

				// 枚举信息进度条更新
				if (progressEnumBase == 0) {
					skbEnumBase.setProgress(1);
				} else {
					skbEnumBase.setProgress(progressEnumBase);
				}

				skbEnumBase.postInvalidate();
				tvEnumBase.setText("基本信息同步中..." + "(" + progressEnumBase + "/" + tallEnumBase + ")");
				if (progressEnumBase == tallEnumBase) {
					tvEnumBase.setText("系统基本信息同步" + "(" + progressEnumBase + "/" + tallEnumBase + ")");
					tvEnumBase.postInvalidate();
				}
				break;

			case 4:

				tallFoodMenu = msg.arg1;

				// 菜单信息进度条更新最大字设定
				tvFoodMenu.setText("菜单信息同步中..." + "(" + progressFoodMenu + "/" + tallFoodMenu + ")");
				if (tallFoodMenu == 0) {
					skbFoodMenu.setMax(1);
				} else {
					skbFoodMenu.setMax(tallFoodMenu);
				}

				skbFoodMenu.postInvalidate();

				tvFoodMenu.postInvalidate();

				break;

			case -4:

				progressFoodMenu = msg.arg1;

				// 菜单信息进度条更新

				if (progressFoodMenu == 0) {
					skbFoodMenu.setProgress(1);
				} else {
					skbFoodMenu.setProgress(progressFoodMenu);
				}

				skbFoodMenu.postInvalidate();
				tvFoodMenu.setText("菜单信息同步中..." + "(" + progressFoodMenu + "/" + tallFoodMenu + ")");
				if (progressFoodMenu == tallFoodMenu) {
					tvFoodMenu.setText("菜单信息同步" + "(" + progressFoodMenu + "/" + tallFoodMenu + ")");
					tvFoodMenu.postInvalidate();
				}
				break;

			case 7:

				tallPatient = msg.arg1;

				// 病员信息进度条更新最大字设定
				tvPatient.setText("病员信息同步中..." + "(" + progressPatient + "/" + tallPatient + ")");
				if (tallPatient == 0) {
					skbPatient.setMax(1);
				} else {
					skbPatient.setMax(tallPatient);
				}

				skbPatient.postInvalidate();

				tvPatient.postInvalidate();

				break;

			case -7:

				progressPatient = msg.arg1;

				// 病员信息进度条更新
				if (progressPatient == 0) {
					skbPatient.setProgress(1);
				} else {
					skbPatient.setProgress(progressPatient);
				}

				skbPatient.postInvalidate();
				tvPatient.setText("病员信息同步中..." + "(" + progressPatient + "/" + tallPatient + ")");
				if (progressPatient == tallPatient) {
					tvPatient.setText("病员信息同步" + "(" + progressPatient + "/" + tallPatient + ")");
					tvPatient.postInvalidate();

					cancel_ok.setBackgroundResource(R.drawable.ok_bg);

				}
				break;
			case 8:

				actionTishi.setText(msg.obj + "");
				l_pro.setVisibility(View.VISIBLE);

				break;

			case 9:
				actionTishi.setText(msg.obj + "");
				l_pro.setVisibility(View.GONE);
				filelist.add(fileName);
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
			}

		}
	};

	/**
	 * 显示提示对话框
	 */
	private void ShowPrompt() {

		alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setCanceledOnTouchOutside(false);

		if (!alertDialog.isShowing()) {
			alertDialog.show();
		}
		alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK) {
					Toast.makeText(ShowDatabaseActivity.this, "数据正在同步中，请勿操作", Toast.LENGTH_SHORT).show();
					return true;
				}
				return false;
			}
		});

		Window window = alertDialog.getWindow();
		// *** 主要就是在这里实现这种效果的.
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(R.layout.pupop_shdata);

		skbPosition = (MySeekBar) window.findViewById(R.id.seekBar1);
		skbEnumBase = (MySeekBar) window.findViewById(R.id.seekBar2);
		skbFoodMenu = (MySeekBar) window.findViewById(R.id.seekBar4);
		skbPatient = (MySeekBar) window.findViewById(R.id.seekBar7);
		skbUpJson = (MySeekBar) window.findViewById(R.id.seekBar9);

		skbUpJson.setVisibility(View.GONE);

		tvPosition = (TextView) window.findViewById(R.id.textView1);
		tvEnumBase = (TextView) window.findViewById(R.id.textView2);
		tvFoodMenu = (TextView) window.findViewById(R.id.textView4);
		tvPatient = (TextView) window.findViewById(R.id.textView7);
		tvUpJson = (TextView) window.findViewById(R.id.textView9);

		tvUpJson.setVisibility(View.GONE);

		cancel_ok = (Button) window.findViewById(R.id.cancel_ok);

		cancel_ok.setBackgroundResource(R.drawable.cancel_bg);

		cancel_ok.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();

			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();

			return true;

		}

		return false;
	}
}
