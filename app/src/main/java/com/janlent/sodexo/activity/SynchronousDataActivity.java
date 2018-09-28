package com.janlent.sodexo.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.OrderSummaryAdapter;
import com.janlent.sodexo.bean.OrderSummary;
import com.janlent.sodexo.server.UpDownDataServer;
import com.janlent.sodexo.widget.MySeekBar;
import com.janlent.utils.CheckNetwork;
import com.janlent.utils.DateUtils;
import com.janlent.utils.Db;
import com.janlent.utils.FileUtils;
import com.janlent.utils.JsonUtils;
import com.janlent.utils.Log;
import com.janlent.utils.Utils;
import com.jenlant.sodexo.R;

/**
 * 数据同步界面
 * 
 * @author Administrator
 * 
 */
public class SynchronousDataActivity extends BaseActivity implements OnClickListener {

	public static String DOWN_SYNC_MOBILE_URL;// 同步数据的url接口
	public static String UP_SYNC_MOBILE_URL;// 上传数据的url接口

	private Button to_order;
	private Button to_SH;
	private Button setting;
	private Button camera;

	private View ShData;
	private RelativeLayout main;

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

	// Position进度条
	private int progressPosition;
	// EnumBase进度条
	private int progressEnumBase;
	// FoodMenu进度条
	private int progressFoodMenu;
	// Patient进度条
	private int progressPatient;
	// Json进度条
	private int progressJson;

	// Position最大进度
	private int tallPosition;
	// EnumBase最大进度
	private int tallEnumBase;
	// FoodMenu最大进度
	private int tallFoodMenu;
	// Patient最大进度
	private int tallPatient;

	private String headerUrl;
	private String mark;

	AlertDialog alertDialog;
	private final static String DB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/database/Sodexo.sqlite";
	private final static String DB_BK = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/database/Sodexo_back.sqlite";

	// TODO 7-29订单汇总
	private Dialog summaryDialog;
	private Button btn_confirm, btn_cancle;
	private TextView total_bednum, total_count, total_money;
	private ListView order_list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Intent intent = getIntent();
		mark = intent.getStringExtra("mark");

		// 获取SharedPreferences的参数数据

		initBtn();

	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedata = getSharedPreferences("deviceDate", 0);
		String bgApi = sharedata.getString("bgApi", null);
		String downApi = sharedata.getString("downApi", null);
		String upApi = sharedata.getString("upApi", null);
		String hisCode = sharedata.getString("hisCode", null);
		String device = sharedata.getString("device_num", null);
		String MemberID = getSharedPreferences("user", 0).getString("MemberID", "###");

		headerUrl = getSharedPreferences("deviceDate", 0).getString("bgApi", null);

		if (bgApi == null || !bgApi.startsWith("http://")) {
			bgApi = "http://" + bgApi;
		}

		if (bgApi != null && downApi != null) { // 判断数据是否为非空

			DOWN_SYNC_MOBILE_URL = bgApi + downApi;

		}

		if (bgApi != null && downApi != null && hisCode != null && device != null && MemberID != null) {
			DOWN_SYNC_MOBILE_URL = bgApi + downApi + "?hisCode=" + hisCode + "&device=" + device + "&MemberID=" + MemberID;
		}

		if (bgApi != null && upApi != null) { // 判断数据是否为非空

			UP_SYNC_MOBILE_URL = bgApi + upApi;

		}
		if (bgApi != null && upApi != null && hisCode != null && device != null && MemberID != null) {
			UP_SYNC_MOBILE_URL = bgApi + upApi + "?hisCode=" + hisCode + "&device=" + device + "&MemberID=" + MemberID + "&mDeviceID=" + device;
		}

	}

	/**
	 * 初始化button按钮
	 */
	private void initBtn() {

		main = (RelativeLayout) findViewById(R.id.main);

		setting = (Button) findViewById(R.id.setting);
		camera = (Button) findViewById(R.id.camera);

		to_order = (Button) findViewById(R.id.to_order);
		to_SH = (Button) findViewById(R.id.to_shdata);
		to_order.setOnClickListener(this);
		to_SH.setOnClickListener(this);
		setting.setOnClickListener(this);
		camera.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.to_order:

			if ("InitUser".equals(mark)) {
				Toast.makeText(SynchronousDataActivity.this, "初次运行程序，请完成同步后返回重新登陆！", Toast.LENGTH_SHORT).show();
			} else {

				Intent intent1 = new Intent(SynchronousDataActivity.this, PatientsListActivity.class);
				PatientsListActivity.isFistEnter = true;
				startActivity(intent1);
				overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
			}

			break;

		case R.id.to_shdata:
			showOrderSummary();
			/*
			 * // 同步前先检验是否正常联网 if (CheckNetwork.checkNetworking(this)) { //TODO
			 * 7-28:先显示本地的订单框，再同步 ShowLoginPrompt(); if (headerUrl != null &&
			 * headerUrl.length() > 1) { new Thread(run).start(); // new
			 * Thread(SHOrderAndPatient).start(); } else { Toast.makeText(this,
			 * "网络连接异常，请检查网络接口是否正确！", Toast.LENGTH_LONG).show(); } } else {
			 * Toast.makeText(this, "网络连接异常，请检查wifi，3G是否连接正常！",
			 * Toast.LENGTH_LONG).show(); }
			 */
			break;
		case R.id.setting:

			Intent intent2 = new Intent(this, SettingActivity.class);
			startActivity(intent2);
			break;

		case R.id.camera:

			Intent intent3 = new Intent(this, CameraActivity.class);
			startActivity(intent3);
			break;
		case R.id.btn_confirm:
			summaryDialog.dismiss();
			if (CheckNetwork.checkNetworking(this)) {
				// TODO 7-28:先显示本地的订单框，再同步
				ShowLoginPrompt();
				if (headerUrl != null && headerUrl.length() > 1) {
					new Thread(run).start();
					// new Thread(SHOrderAndPatient).start();

				} else {
					Toast.makeText(this, "网络连接异常，请检查网络接口是否正确！", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "网络连接异常，请检查wifi，3G是否连接正常！", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btn_cancle:
			summaryDialog.dismiss();
			break;
		default:
			break;
		}
	}

	Handler handler = new Handler();
	/**
	 * 执行数据在线同步
	 */
	Runnable run = new Runnable() {

		@Override
		public void run() {

			try {
				// 备份数据库
				FileInputStream in_bak = new FileInputStream(new File(DB));
				FileOutputStream out_bak = new FileOutputStream(new File(DB_BK));
				FileUtils.transfer(in_bak, out_bak);
				// 上传数据
				try {
					String updateResult = UpDownDataServer.upData(hand, UP_SYNC_MOBILE_URL, SynchronousDataActivity.this);
				} catch (Exception e) {
					// TODO: handle exception
					Log.e("上传数据失败" + "/失败信息是" + e.getMessage());
					throw new RuntimeException(e);
				}

				// 下载数据
				Map<String, Object> sycnmobile = JsonUtils.getMap(DOWN_SYNC_MOBILE_URL);
				try {
					UpDownDataServer.downDate(sycnmobile, hand, headerUrl);
				} catch (Exception e) {
					// TODO: handle exception
					Log.e("下载数据失败" + "/失败信息是" + e.getMessage());
					throw new RuntimeException(e);

				}

			} catch (Exception e) {
				// TODO: handle exception
				Log.e("同步失败恢复数据库/错误信息" + e.getMessage());
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
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						AlertDialog.Builder builder = new AlertDialog.Builder(SynchronousDataActivity.this);
						builder.setMessage("同步失败，请重新同步！").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								SynchronousDataActivity.this.finish();
							}
						});
						AlertDialog alert = builder.create();
						if (alert != null && !alert.isShowing()) {
							alert.show();
						}

						alertDialog.dismiss();
					}
				});

			}

		}
	};

	/**
	 * 执行进度条更新线程
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
					// //下载完成，修改order表中今天以前的订单,HaveSync为True
					// String todaystr = DateUtils.getFormatDate("yyyy-MM-dd");
					// int a =
					// Db.update("update [Order] set HaveSync = ? WHERE
					// [writetime] < ?",
					// "True",todaystr);

				}
				break;

			case 8:

				// 订单和病员信息进度条更新最大字设定
				tvUpJson.setText("订单和病员信息同步中..." + "(" + progressJson + "/" + 4 + ")");
				skbUpJson.setMax(4);
				skbUpJson.postInvalidate();
				tvUpJson.postInvalidate();
				break;

			case -8:

				progressJson = msg.arg1;

				// 订单和病员信息进度条更新

				tvUpJson.setText("订单和病员信息同步..." + "(" + progressJson + "/" + 4 + ")");

				if (progressJson == 4) {
					tvUpJson.setText("订单和病员信息同步" + "(" + progressJson + "/" + 4 + ")");
					// TODO 08-05:上传成功,修改order表中HaveSync为True
					String todaystr = DateUtils.getFormatDate("yyyy-MM-dd");
					String nextday = DateUtils.getDay(todaystr, "yyyy-MM-dd", 1);
					int a = Db.update("update [Order] set HaveSync  = ? WHERE [writetime]>= ? and writetime < ?", "True", todaystr, nextday);
				}

				skbUpJson.setProgress(progressJson);
				skbUpJson.postInvalidate();
				tvUpJson.postInvalidate();
				break;

			default:
				break;
			}
		}
	};

	/**
	 * 显示进度条对话框
	 */
	private void ShowLoginPrompt() {

		alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setCanceledOnTouchOutside(false);
		if (alertDialog != null && !alertDialog.isShowing()) {
			alertDialog.show();
		}
		alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK) {
					Toast.makeText(SynchronousDataActivity.this, "数据正在同步中，请勿操作", Toast.LENGTH_SHORT).show();
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

		tvPosition = (TextView) window.findViewById(R.id.textView1);
		tvEnumBase = (TextView) window.findViewById(R.id.textView2);
		tvFoodMenu = (TextView) window.findViewById(R.id.textView4);
		tvPatient = (TextView) window.findViewById(R.id.textView7);
		tvUpJson = (TextView) window.findViewById(R.id.textView9);
		cancel_ok = (Button) window.findViewById(R.id.cancel_ok);

		cancel_ok.setBackgroundResource(R.drawable.cancel_bg);

		cancel_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();

			}
		});

	}

	// 显示订单汇总弹框
	public void showOrderSummary() {
		summaryDialog = new Dialog(this, R.style.pop_dialog);
		summaryDialog.setContentView(R.layout.order_summary);
		Window win = summaryDialog.getWindow();
		win.setGravity(Gravity.BOTTOM);
		win.setWindowAnimations(R.style.AnimBottom); // 设置窗口弹出动画
		summaryDialog.setCanceledOnTouchOutside(false);
		summaryDialog.show();
		win.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		btn_confirm = (Button) summaryDialog.findViewById(R.id.btn_confirm);
		btn_cancle = (Button) summaryDialog.findViewById(R.id.btn_cancle);
		btn_confirm.setOnClickListener(this);
		btn_cancle.setOnClickListener(this);

		total_bednum = (TextView) summaryDialog.findViewById(R.id.total_bednum);
		total_count = (TextView) summaryDialog.findViewById(R.id.total_count);
		total_money = (TextView) summaryDialog.findViewById(R.id.total_money);
		order_list = (ListView) summaryDialog.findViewById(R.id.order_list);
		initSummaryData();

	}

	// 初始化订单汇总列表
	public void initSummaryData() {
		String todaystr = DateUtils.getFormatDate("yyyy-MM-dd");
		String nextday = DateUtils.getDay(todaystr, "yyyy-MM-dd", 1);
		Map<String, Object> top = Db.selectUnique("select count(distinct a.id) num,sum(e.[UnitPrice] * b.[count]) total_payment,sum(b.[count])"
				+ " total_num from [Order] a inner join OrderDetail b on a.id=b.order_id" + " left join food e on e.id=b.[Food_ID] where [writetime]>= ? and writetime < ? ", todaystr, nextday);
		// 订单数
		String num = top.get("num") == null ? "0" : top.get("num") + "";
		// 总份数
		String total_num = top.get("total_num") == null ? "0" : top.get("total_num") + "";
		// 总金额
		String total_payment = top.get("total_payment") == null ? "0" : top.get("total_payment") + "";
		total_bednum.setText(num);
		total_count.setText(total_num);
		total_money.setText(total_payment);
		// TODO 08-06:修改订单汇总，数据不符
		List<Map<String, Object>> list = Db
				.select("select b.takefoodtime,d.name,count(distinct a.id) num,sum(e.[UnitPrice] * b.[count]) total_payment,sum(b.[count]) total_num"
						+ ",(select sum(e1.[UnitPrice] * b1.[count])  from [Order] a1 inner join OrderDetail b1 on a1.id=b1.order_id left join food e1 on e1.id=b1.[Food_ID] where  a1.[writetime] >= ? and a1.writetime < ? and b1.takefoodtime=b.takefoodtime and d.id=b1.[MealTypeID] and b1.[PayWayID]=(select ID from enumbase where name='现金')) cash"
						+ ",(select sum(e1.[UnitPrice] * b1.[count])  from [Order] a1 inner join OrderDetail b1 on a1.id=b1.order_id left join food e1 on e1.id=b1.[Food_ID] where  a1.[writetime] >= ? and a1.writetime < ? and b1.takefoodtime=b.takefoodtime and d.id=b1.[MealTypeID] and b1.[PayWayID]=(select ID from enumbase where name='月结')) mon"
						+ " from [Order] a inner join OrderDetail b on a.id=b.order_id" + " left join food e on e.id=b.[Food_ID]" + " left join enumbase d on d.id=b.[MealTypeID]"
						+ " where a.[writetime] >= ? and a.writetime < ? " + " group by b.takefoodtime,d.name,d.id ", todaystr, nextday, todaystr, nextday, todaystr, nextday);
		Log.e("todaystr---->" + todaystr);
		Log.e("nextday---->" + nextday);
		ArrayList<OrderSummary> orderSummaries;
		// 显示列表
		if (list != null && list.size() > 0) {
			orderSummaries = new ArrayList<OrderSummary>();
			double cash_total = 0;
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				OrderSummary summary = new OrderSummary();
				String date = map.get("TakeFoodTime") + "";
				summary.orderTime = date + (map.get("Name") + "");
				summary.bedNum = map.get("num") + "";
				summary.orderCount = map.get("total_num") + "";
				String cash = "";
				if (map.get("cash") == null) {
					cash = "0";
				} else {
					cash = map.get("cash") + "";
				}
				cash_total += Double.parseDouble(cash);
				String month = "";
				if (map.get("mon") == null) {
					month = "0";
				} else {
					month = map.get("mon") + "";
				}
				summary.cashMoney = cash;
				summary.monthMoney = month;
				summary.allMoney = map.get("total_payment") + "";
				orderSummaries.add(summary);
			}
			OrderSummary summary = new OrderSummary();
			summary.orderTime = "合计";
			summary.bedNum = num;
			summary.orderCount = total_num;
			summary.cashMoney = cash_total + "";
			summary.monthMoney = (Double.parseDouble(total_payment) - cash_total) + "";
			summary.allMoney = total_payment;
			orderSummaries.add(summary);
			OrderSummaryAdapter adapter = new OrderSummaryAdapter(this, orderSummaries);
			order_list.setAdapter(adapter);
		}

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
