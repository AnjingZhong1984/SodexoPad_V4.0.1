package com.janlent.sodexo.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.ConfirmListAdapter;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.Db;
import com.janlent.utils.Log;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

/**
 * 订单确认界面，可以修改订单明晰的数量
 * 
 * @author Administrator
 * 
 */
public class ConfirmOrderActivity extends BaseActivity implements OnClickListener {

	private TextView select_patient; // 显示被选的病员
	private TextView breakfast_num; // 显示早餐数量
	private TextView sumcash; // 显示现金总额
	private Button submitOrder; // 用于提交订单

	private TextView bed_num; // 显示床位号
	private TextView bed_patient_num; // 显示床位上病员号
	private TextView bed_patient_name; // 显示床位上病员名
	private TextView advice; // 显示医嘱
	private TextView noorder; // 显示没有订单

	private ListView selected_foods_list; // 被选菜单集合
	public static List<Map<String, Object>> Orderlist = new ArrayList<Map<String, Object>>(); // 订单集合
	private AlertDialog alertDialog;

	private ConfirmListAdapter mConfirmListAdapter;

	private String tag; // 显示Intent数据

	private String payWayName; // 显示支付方式

	private Map<String, Object> xianjinMap; // 记录现金的单价
	private Map<String, String> mealSumMap = new LinkedHashMap<String, String>(); // 记录餐类的信息

	public boolean isUpdate; // 用来区分医嘱数据是否已经更新
	private SharedPreferences sharedata;
	int mode = 0;
	private String select_Advice="";  //医嘱

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.confirm_order);

		sharedata = getSharedPreferences("deviceDate", 0);
		mode = sharedata.getInt("checkMode", 1);
		isUpdate = true;

		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("PID") == null) { // 判断床位信息为空
			android.util.Log.i(
					"--判断床位-",
					"----PID--"
							+ PatientsListActivity.beds_list.get(
									PatientsListActivity.currentPatientID).get(
									"PID"));
			isUpdate = false;
		}

		// 初始化控件
		select_patient = (TextView) findViewById(R.id.confirmation_order);
		breakfast_num = (TextView) findViewById(R.id.breakfast_num);
		sumcash = (TextView) findViewById(R.id.sumcash);

		TextPaint tp = select_patient.getPaint();
		tp.setFakeBoldText(true);

		bed_num = (TextView) findViewById(R.id.bed_num);
		bed_patient_num = (TextView) findViewById(R.id.bed_patient_num);
		bed_patient_name = (TextView) findViewById(R.id.bed_patient_name);
		advice = (TextView) findViewById(R.id.advice);

		Button patint_detail = (Button) findViewById(R.id.patint_detail);
		selected_foods_list = (ListView) findViewById(R.id.selected_foods_list);
		submitOrder = (Button) findViewById(R.id.confirm_order);

		noorder = (TextView) findViewById(R.id.noorder);
		// 添加监听
		patint_detail.setOnClickListener(this);
		submitOrder.setOnClickListener(this);

		mConfirmListAdapter = new ConfirmListAdapter(ConfirmOrderActivity.this,
				handler);
		selected_foods_list.setAdapter(mConfirmListAdapter);
		selected_foods_list.setVisibility(View.GONE);
		mConfirmListAdapter.notifyDataSetChanged();
		selected_foods_list.setVisibility(View.VISIBLE);
		Intent intent = getIntent();
		tag = intent.getStringExtra("tag");
		if(intent.getStringExtra("select_Advice")!=null){
			select_Advice=intent.getStringExtra("select_Advice");
		}
		initTop();

		payWayName = getSharedPreferences("deviceDate", 0).getString(
				"orderWay", "weizhi");

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("bedNum") != null
				&& PatientsListActivity.beds_list.size() > PatientsListActivity.currentPatientID) {
			bed_num.setText(PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("bedNum")
					+ "");
		}
		// 显示病人住院号
		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("PID") != null) {
			bed_patient_num.setText(PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("PID")
					+ "");
		}

		// 显示病人名称
		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("PatientName") != null) {
			bed_patient_name.setText(PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("PatientName")
					+ "");
		}

		// 显示医嘱信息

		//TODO 查询数据库
		//TODO 7-23:添加,默认普食
/*		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("Advice") != null) {
			String advice_a = PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("Advice")
					+ "";
			advice.setText(advice_a.substring(0, advice_a.length() - 1));
		}
		else{
			advice.setText("普食"
			);
		}*/
		if(tag.equals("modify")){
			//修改界面跳转过来的
			if (PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("Advice") != null) {
				String advice_a = PatientsListActivity.beds_list.get(
						PatientsListActivity.currentPatientID).get("Advice")
						+ "";
				advice.setText(advice_a.substring(0, advice_a.length() - 1));
			}
		}else{
			//下订单界面跳转过来的
			advice.setText(select_Advice);
		}
		handler.obtainMessage(0).sendToTarget();
	}

	/**
	 * 初始化上面title按钮
	 */
	private void initTop() {
		Button back = (Button) findViewById(R.id.back);
		Button camera = (Button) findViewById(R.id.camera);

		if ("modify".equals(tag)) {
			back.setBackgroundResource(R.drawable.huancai_bg);
		}
		// 注册监听器
		back.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(ConfirmOrderActivity.this,
						OrderActivity.class);
				if ("modify".equals(tag)) {
					intent.putExtra("tag", "modify");
					startActivity(intent);
					ConfirmOrderActivity.this.finish();
				} else {
					intent.putExtra("tag", "putorder");

					ConfirmOrderActivity.this.finish();

				}
			}
		});
		// 注册监听器
		camera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ConfirmOrderActivity.this,
						CameraActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {

		int id = v.getId();

		switch (id) {
		case R.id.plus:

			break;
		case R.id.reduction:

			break;
		case R.id.confirm_order:
			// TODO

			if (mode == 0) {
				// ad();
			}
			putOrder();

			break;

		case R.id.patint_detail:
			Intent intent = new Intent(ConfirmOrderActivity.this,
					PatientInfoActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

	}

	/**
	 * 提示对话框
	 */
	private void ShowConfirmPrompt() {

		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.show();

		Window window = alertDialog.getWindow();

		ImageView iv = new ImageView(this);
		iv.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		iv.setBackgroundResource(R.drawable.ord_tishi_bg);
		// *** 主要就是在这里实现这种效果的.
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(iv);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent mainIntent = new Intent(ConfirmOrderActivity.this,
						PatientsListActivity.class);

				ConfirmOrderActivity.this.startActivity(mainIntent);

				ConfirmOrderActivity.this.finish();
				alertDialog.dismiss();

			}

		}, 300);

	}

	/**
	 * 处理事件
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			//
			super.handleMessage(msg);

			switch (msg.what) {

			// 开启获取订单明细的线程
			case 0:
				synchronized (msg) {
					new Thread(getOrderDetail).start();
				}

				break;

			// 首次进入刷新订单明细的listview
			case 1:

				if (Orderlist == null || Orderlist.size() == 0) {
					noorder.setVisibility(View.VISIBLE);
				} else {
					noorder.setVisibility(View.GONE);

				}

				selected_foods_list.setVisibility(View.GONE);
				mConfirmListAdapter.notifyDataSetChanged();
				selected_foods_list.setVisibility(View.VISIBLE);

				break;

			// 修改订单明细时刷新订单界面
			case 2:

				selected_foods_list.setVisibility(View.GONE);
				mConfirmListAdapter.notifyDataSetChanged();
				selected_foods_list.setVisibility(View.VISIBLE);
				ShowConfirmPrompt();
				break;

			case 3:
				// 从设备配置参数中获取 现金汇总关键字 用与统计小锅菜现金总量
				SharedPreferences sharedata = getSharedPreferences(
						"deviceDate", 0);
				String orderWay = sharedata.getString("orderWay", null);

				break;

			case 4:
				// 刷新现金界面
				if (xianjinMap != null && xianjinMap.get("sales") != null) {
					sumcash.setText(payWayName + ":" + xianjinMap.get("sales")
							+ "元");
				} else {
					sumcash.setText(payWayName + ":" + "0元");

				}

				StringBuffer mealTypeSum = new StringBuffer();

				for (String key : mealSumMap.keySet()) {

					mealTypeSum.append(key + ": " + mealSumMap.get(key) + " ");

				}

				Log.e("breakfast_num----->"+mealTypeSum.toString());
				breakfast_num.setText(mealTypeSum.toString());
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 获取订单的数据
	 */
	Runnable getOrderDetail = new Runnable() {

		@Override
		public void run() {
			//

//			Orderlist = Db
//					.select("select b.ID,b.HisCode ,b.PID ,b.TakeFoodTime ,b.MealTypeID ,b.PayWayID ,b.Count ,b.Order_ID,b.Food_ID,b.FoodTypeID, a.FoodName "
//							+ ",a.UnitPrice ,a.PicAddress from food a,orderdetail b,[Order] c,EnumBase d where a.id=b.food_id and  b.order_id=c.id and "
//							+ " b.MealTypeID = d.id and c.PatPositionID =? and TakeFoodTime > ? order by b.TakeFoodTime,d.Sort ASC",
//							PatientsListActivity.currentBedID,
//							ConvertUtils.date2Str("yyyy-MM-dd"));
			//20171205修改查询数据库今天日期以及以后日期的数据
			Orderlist = Db
					.select("select b.ID,b.HisCode ,b.PID ,b.TakeFoodTime ,b.MealTypeID ,b.PayWayID ,b.Count ,b.Order_ID,b.Food_ID,b.FoodTypeID, a.FoodName "
							+ ",a.UnitPrice ,a.PicAddress from food a,orderdetail b,[Order] c,EnumBase d where a.id=b.food_id and  b.order_id=c.id and "
							+ " b.MealTypeID = d.id and c.PatPositionID =? and TakeFoodTime >= ? order by b.TakeFoodTime,d.Sort ASC",
							PatientsListActivity.currentBedID,
							ConvertUtils.date2Str("yyyy-MM-dd"));
			Log.i("PatientsListActivity.currentBedID = "+PatientsListActivity.currentBedID+" ConvertUtils.date2Str = "+ConvertUtils.date2Str("yyyy-MM-dd"));
			handler.obtainMessage(1).sendToTarget();

			xianjinMap = Db
					.selectUnique(
							"select sum(a.UnitPrice*b.count) sales  from food a inner join orderdetail b on a.id=b.food_id and b.paywayid=(select id from EnumBase where name=?) and b.order_id in (select id from [order] where PatPositionID =?) and b.takefoodtime >= ?",
							payWayName, PatientsListActivity.currentBedID,
							ConvertUtils.date2Str("yyyy-MM-dd"));

			if (Orderlist != null) {

				//TODO 08-06:修改，早餐，中餐数量查询错误
				List<Map<String, Object>> mealSumList = Db
						.select(" SELECT b.name,sum(a.[Count]) mealSumitem FROM OrderDetail a inner join EnumBase b on a.mealtypeid=b.id where order_id = ? group by b.name order by b.sort",
								Orderlist.get(0).get("Order_ID"));
				for (Map<String, Object> mealSum : mealSumList) {
					mealSumMap.put((String) mealSum.get("Name"),
							mealSum.get("mealSumitem").toString());
				}

			}
			// 通知餐类统计数量和现金总额更新
			handler.obtainMessage(4).sendToTarget();

		}
	};

	/**
	 * 下订单类
	 */
	private void putOrder() {
		android.util.Log.i("ConputOrdeer--", "------------");
		// 删除订单明细中订单数量为0的记录
		Db.delete("delete from OrderDetail where count = ?  ", 0);
		// 检测此床位有没有订单 如果有将其ordermark记录更新为true
		Map<String, Object> OrderIDMap = Db
				.selectUnique(
						"select a.ID from [Order] a  inner join OrderDetail b on a.id = b.Order_ID where PatPositionID = ? and b.TakeFoodTime >= ?",
						PatientsListActivity.currentBedID,
						ConvertUtils.date2Str("yyyy-MM-dd"));

		android.util.Log.i("ConputOrdeer--", "------------"
				+ (OrderIDMap == null));
		if (OrderIDMap != null) {
			// 将order表中的订单状态更新为true
			int a = Db.update("update [Order] set OrderMark  = ? WHERE ID=?",
					"True", OrderIDMap.get("ID"));
			// 将orderdetail表中的订单状态跟新为true
			int b = Db.update(
					"update orderdetail set OrderMark = ? where Order_ID = ?",
					"True", OrderIDMap.get("ID"));
			// 将numsmark 表中跟订单相关的关键字 状态跟新为true
			int c = Db.update(
					"update NumsMark set OrderMark = ? where Order_ID =?",
					"True", OrderIDMap.get("ID"));
			//判断是否同步
			Map<String, Object> sy = Db
					.selectUnique(
							"select HaveSync from [Order] where ID= ? ",
							OrderIDMap.get("ID"));
			//TODO 08-05:已经同步，改为“未同步”
			if(sy!=null){
				if(sy.get("HaveSync")!=null){
					int aa=Db.update("update [Order] set HaveSync = ? WHERE ID= ? ",
							"null", OrderIDMap.get("ID"));
				}
			}
			// TODO
			// Db.delete("delete from NumsMark where  OrderMark is null");
			// Db.delete("delete from OrderDetail where  OrderMark is null");
			// Db.delete("delete from [Order] where OrderMark is null ");
			// 判断是否为修改订单还是新订单
			if ("modify".equals(tag)) {
				handler.obtainMessage(2).sendToTarget();
			} else {
				// 新订单就将此床位从未订餐的list集合中删除
				if (PatientsListActivity.beds_list.size() > PatientsListActivity.currentPatientID) {
					PatientsListActivity.beds_list
							.remove(PatientsListActivity.currentPatientID);
				}
				handler.obtainMessage(2).sendToTarget();
			}

		} else {
			// 如果此床位在orderdetail表中没有订单记录了 就清除一些垃圾数据
			Db.delete(
					"delete from [Order] where ID = (select Order_ID from NumsMark where TakeFoodTime > ? and PatPositionID = ?)",
					ConvertUtils.date2Str("yyyy-MM-dd"),
					PatientsListActivity.currentBedID);

			Db.delete("delete from OrderDetail where count = '0'");
			//
			Db.delete("delete from NumsMark  where nums = '0'");

			if (ConfirmOrderActivity.Orderlist != null) {
				ConfirmOrderActivity.Orderlist.clear();
				mConfirmListAdapter.notifyDataSetChanged();
			}
			// 判断是否是修改订单记录，如果是修改 此时此床位订单已全部删除，就从已定餐病人的list中将此床位删除
			if ("modify".equals(tag)) {

				if (PatientsListActivity.beds_list.size() > PatientsListActivity.currentPatientID) {
					PatientsListActivity.beds_list
							.remove(PatientsListActivity.currentPatientID);
				}
			}

			Intent intent = new Intent(ConfirmOrderActivity.this,
					PatientsListActivity.class);

			startActivity(intent);

			ConfirmOrderActivity.this.finish();

		}
	}

	public static List<String> advice_ID_list = new ArrayList<String>();// 保存病人已有医嘱ID
	// private StringBuffer allergyText = new StringBuffer(); // 用来保存医嘱信息
	private StringBuffer firstAllergyText = new StringBuffer(); // 用来保存医嘱信息
	public static List<String> allergy_ID_list = new ArrayList<String>();// 保存病人过敏ID

	// TODO 修改过的地方
	/**
	 * 插入一条病人的信息 信息为空
	 */
	private void ad() {
		List<Map<String, Object>> advice_list_a = Db.select(
				"SELECT * FROM PatientFoodAdvice A WHERE A.Patient_ID = ?",
				PatientsListActivity.beds_list.get(
						PatientsListActivity.currentPatientID).get("ID"));

		if (advice_list_a != null) {

			for (int i = 0; i < advice_list_a.size(); i++) {

				Map<String, Object> map = Db.selectUnique(
						"SELECT A.Advice FROM FoodAdvice A WHERE ID = ?",
						advice_list_a.get(i).get("FoodAdvice_ID"));

				if (map != null) {

					advice_ID_list.add(map.get("Advice") + "，");
					android.util.Log.i("confir",
							"--advice--" + map.get("Advice") + "，");

				}

			}
		}
		List<Map<String, Object>> list = Db
				.select("select Name from Allergy where id in (select Allergy_ID from PatientAllergy where Patient_ID = ?)",
						PatientsListActivity.beds_list.get(
								PatientsListActivity.currentPatientID)
								.get("ID"));

		if (list != null) {
			firstAllergyText = new StringBuffer();
			for (int i = 0; i < list.size(); i++) {

				firstAllergyText.append(list.get(i).get("Name") + "，");
				allergy_ID_list.add(list.get(i).get("Name") + "，");
			}

		}

		String PID = "未知";
		String PatientName = "未知";
		String PatientSex = "未知";
		String InHospitalTime = "";
		String OutHospitalTime = "";
		String advice = "";
		String allergyStr = "";
		// if (PatientsListActivity.beds_list.get(
		// PatientsListActivity.currentPatientID).get("PID") == null) { //
		// 判断床位信息为空
		//
		// PID = PatientsListActivity.beds_list.get(
		// PatientsListActivity.currentPatientID).get("PID")
		// + "";
		// }else{
		// PID=("未知");
		//
		// }
		//
		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("PatientName") != null) { // 判断病员名字不为空
			PatientName = PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("PatientName")
					+ "";
		}

		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("PatientSex") != null) { // 判断病员性别不为空
			PatientSex = PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("PatientSex")
					+ "";
		}

		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("InHospitalTime") != null) { // 判断入院时间不为空

			String date = (PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID)
					.get("InHospitalTime") + "");

			if (ConvertUtils.str2Date(date).after(
					ConvertUtils.str2Date("1950-00-00 00:00"))) {

				date = date.replace(":", "-");

				if (date.length() >= 17) {
					InHospitalTime = (date.substring(0, 16));
				} else {
					InHospitalTime = (date.substring(0, date.length()));
				}
			}

		} else {
			InHospitalTime = (ConvertUtils.date2Str(new Date(),
					"yyyy-MM-dd HH-mm"));
		}

		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("OutHospitalTime") != null) { // 判断出院时间不为空
			String date = (PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get(
					"OutHospitalTime") + "");

			date = date.replace(":", "-");

			Log.e(this,
					"ConvertUtils.str2Date(date)" + ConvertUtils.str2Date(date));
			Log.e(this, "ConvertUtils.str2Date(1950-00-00 00:00)"
					+ ConvertUtils.str2Date("1950-00-00 00:00"));

			if (ConvertUtils.str2Date(date).after(
					ConvertUtils.str2Date("1950-00-00 00:00"))) {
				if (date.length() >= 17) {
					OutHospitalTime = (date.substring(0, 16));
				} else {
					OutHospitalTime = (date.substring(0, date.length()));
				}

			}
		}

		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("Advice") != null) { // 判断医嘱不为空
			String advice_a = PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("Advice")
					+ "";
			advice = (advice_a.substring(0, advice_a.length() - 1));
		}

		if (firstAllergyText.length() > 1) {
			allergyStr = firstAllergyText.substring(0,
					firstAllergyText.length() - 1);
		}
		new Thread(new Run2(PID, PatientName, PatientSex, advice, allergyStr,
				InHospitalTime, OutHospitalTime)).start();
	}

	/**
	 * 更新数据线程
	 * 
	 * @author Administrator
	 * 
	 */
	public class Run2 implements Runnable {

		final String PID;
		final String PatientName;
		final String PatientSex;
		final String advice;
		final String allergyStr;
		final Date InHospitalTime;
		final Date OutHospitalTime;

		public Run2(String PID, String PatientName, String PatientSex,
				String advice, String allergyStr, String InHospitalTime,
				String OutHospitalTime) {

			this.PID = PID;
			this.PatientName = PatientName;
			this.PatientSex = PatientSex;
			this.advice = advice;
			this.allergyStr = allergyStr;

			if (InHospitalTime == null || InHospitalTime.length() == 0) {

				InHospitalTime = ConvertUtils.date2Str(new Date(),
						"yyyy-MM-dd HH-mm");
			}

			this.InHospitalTime = ConvertUtils.str2Date(InHospitalTime,
					"yyyy-MM-dd HH-mm");

			this.OutHospitalTime = ConvertUtils.str2Date(OutHospitalTime,
					"yyyy-MM-dd HH-mm");

		}

		@Override
		public void run() {
			if (advice.length() < 1) {

				// 插入新病人数据

				String ID = StringUtils.uid();

				int a = Db
						.insert("INSERT INTO Patient(ID ,PID ,HisCode,PatientName, PatientSex  ,WriteTime ,LastUpdateTime,PatPositionID,InHospitalTime,OutHospitalTime,WriteAdminName,LastUpdateAdminName) "
								+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
								ID,
								PID,
								getSharedPreferences("deviceDate", 0)
										.getString("hisCode", "松江"),
								PatientName,
								PatientSex,
								ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"),
								ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"),
								PatientsListActivity.currentBedID,
								ConvertUtils.date2Str2(InHospitalTime,
										"yyyy-MM-dd HH:mm"),
								ConvertUtils.date2Str2(OutHospitalTime,
										"yyyy-MM-dd HH:mm"),
								getSharedPreferences("user", 0).getString(
										"user", "乐pad").trim(),
								getSharedPreferences("user", 0).getString(
										"user", "乐pad"));
				android.util.Log.i("------", "currentBedID--"
						+ PatientsListActivity.currentBedID);
				android.util.Log.i("------", "PID--" + PID);
				android.util.Log.i("-1-----",
						"hisCode--"
								+ getSharedPreferences("deviceDate", 0)
										.getString("hisCode", "松江"));
				// 插入病人医嘱信息
				// TODO 修改过地方
				int c = 0;
				// for (int i = 0; i < advice_ID_list.size(); i++) {
				//
				// c = Db.insert(
				// "INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID) VALUES(?,?)",
				// ID, advice_ID_list.get(i));
				// }

				if (mode == 0) {
					c = Db.insert(
							"INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID) VALUES(?,?)",
							ID, "57aaedec9d824aaba2ec59640bcefc85,");
					android.util.Log.i("--普食--", "--asdadsa=");

					// 插入病人过敏信息

					Db.insert(
							"INSERT INTO PatientAllergy (Patient_ID,Allergy_ID) VALUES(?,?)",
							ID, "");
				}
				Log.e("--2--a=" + a + "/" + "c=" + c);

				if (a == -1 && c == -1) {

					Map<String, Object> newPatientmap = Db.selectUnique(
							"select * from patient where ID = ?",
							PatientsListActivity.beds_list.get(
									PatientsListActivity.currentPatientID).get(
									"ID"));
					if (newPatientmap != null) {
						newPatientmap.put("Advice", advice + "，");

						PatientsListActivity.beds_list
								.remove(PatientsListActivity.currentPatientID);

						PatientsListActivity.beds_list.add(
								PatientsListActivity.currentPatientID,
								newPatientmap);
					}
					android.util.Log.i("--普食-3-", "----asdadsa=");
					// handler.obtainMessage(1).sendToTarget();
				} else {
					// handler.obtainMessage(3).sendToTarget();
				}

			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (Orderlist != null) {
			Orderlist.clear();
		}

		selected_foods_list.setVisibility(View.GONE);
		mConfirmListAdapter.notifyDataSetChanged();
		selected_foods_list.setVisibility(View.VISIBLE);

	}

}
