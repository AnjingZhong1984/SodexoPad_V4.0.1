package com.janlent.sodexo.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.SexAdapter;
import com.janlent.utils.CollectionUtils;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.Db;
import com.janlent.utils.Log;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

/**
 * 病员详情界面
 * 
 * @author Administrator
 * 
 */
public class PatientInfoActivity extends BaseActivity implements OnClickListener {

	private TextView bed_Num; // 显示床位号
	private TextView doctor_advice; // 显示医嘱
	private TextView allergy; // 显示
	private TextView sex; // 显示性别

	private EditText enter_time; // 显示入院时间
	private EditText out_time; // 显示出院时间
	private EditText admission_num; // 显示管理员编号
	private EditText name; // 显示病员姓名

	private PopupWindow sexPop;
	private PopupWindow advicePopup;
	private PopupWindow allergyPopup;

	private Button ok;

	private ListView sexList; // 性别集合

	public static boolean isShow_1 = false;
	public static boolean isShow_2 = false;
	public static boolean isShow_3 = false;

	private ImageView iv = null;
	List<Map<String, Object>> advice_list;
	List<Map<String, Object>> allergy_list;
	private Map<String, Object> advice_map = new HashMap<String, Object>();// 保存医嘱advice，以id为key值
	private Map<String, Object> allergy_map = new HashMap<String, Object>();// 保存过敏Allergy，以id为key值
	public static List<String> advice_ID_list = new ArrayList<String>();// 保存病人已有医嘱ID
	public static List<String> allergy_ID_list = new ArrayList<String>();// 保存病人过敏ID

	public static boolean adviceIsFirstTouch = true; // 用来区分数据是否已经初始化
	public static boolean allergyIsFirstTouch = true; // 用来区分数据是否已经初始化

	public boolean isUpdate; // 用来区分医嘱数据是否已经更新

	private StringBuffer text = new StringBuffer(); // 用来保存医嘱信息
	private StringBuffer allergyText = new StringBuffer(); // 用来保存医嘱信息
	private StringBuffer firstAllergyText = new StringBuffer(); // 用来保存医嘱信息

	private SharedPreferences sharedata;
	int mode = 0;
	private View menuPopView;// 时间选择器
	private PopupWindow menuPop;
	private int year;// 日期
	private int month;
	private int day;
	private LinearLayout ll_parent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.patient_info);

		initTop();

		advice_list = Db.select("SELECT A.ID,A.Advice,A.DefaultFoodID FROM FoodAdvice A ORDER BY DefaultFoodID");

		if (advice_list != null) {
			for (int i = 0; i < advice_list.size(); i++) {
				advice_map.put(advice_list.get(i).get("ID") + "", advice_list.get(i).get("Advice"));
				// 2016.09.27 添加； 为每个医嘱添加排序号
				advice_map.put(advice_list.get(i).get("ID") + "_s", advice_list.get(i).get("DefaultFoodID"));
			}
		}

		allergy_list = Db.select("select ID,Name from Allergy Order by PID");

		if (allergy_list != null) {

			for (int i = 0; i < allergy_list.size(); i++) {
				allergy_map.put(allergy_list.get(i).get("ID") + "", allergy_list.get(i).get("Name"));
			}

		}
		sharedata = getSharedPreferences("deviceDate", 0);
		mode = sharedata.getInt("checkMode", 1);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initView();
	}

	/**
	 * 初始化上面title按钮
	 * 
	 * 
	 */
	private void initTop() {
		Button back = (Button) findViewById(R.id.back);
		Button camera = (Button) findViewById(R.id.camera);

		back.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				PatientInfoActivity.this.finish();
				OrderActivity.isInitData = false;
			}
		});

		camera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PatientInfoActivity.this, CameraActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 初始化界面组件
	 */
	private void initView() {

		bed_Num = (TextView) findViewById(R.id.bed_id);
		enter_time = (EditText) findViewById(R.id.enter_time);
		out_time = (EditText) findViewById(R.id.out_time);
		admission_num = (EditText) findViewById(R.id.admission_number);
		name = (EditText) findViewById(R.id.name);
		doctor_advice = (TextView) findViewById(R.id.doctor_advice);
		allergy = (TextView) findViewById(R.id.allergy);
		sex = (TextView) findViewById(R.id.sex);
		ll_parent = (LinearLayout) findViewById(R.id.ll_parent);
		isUpdate = true;
		adviceIsFirstTouch = true;
		allergyIsFirstTouch = true;

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PID") == null) { // 判断床位信息为空
			android.util.Log.i("--PatientInfo-", "----PID--" + PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PID"));
			isUpdate = false;
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("bedNum") != null) { // 判断床号不为空
			bed_Num.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("bedNum") + "");

			android.util.Log.i("--PatientInfo-", "----currentPatientID--" + PatientsListActivity.currentPatientID);
			android.util.Log.i("--PatientInfo-", "----bed_Num--" + PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("bedNum"));
		} else {
			android.util.Log.i("--PatientInfo-", "----currentPatientID--" + PatientsListActivity.currentPatientID);
			// bed_Num.setText("未知");
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PID") != null) { // 判断住院号不为空
			admission_num.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PID") + "");
			android.util.Log.i("--PatientInfo-", "----currentPatientID--" + PatientsListActivity.currentPatientID);
			android.util.Log.i("--PatientInfo-", "----admission--" + PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PID") + "");
		} else {
			android.util.Log.i("--PatientInfo-", "----currentPatientID--" + PatientsListActivity.currentPatientID);
			admission_num.setText("未知");
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientName") != null) { // 判断病员名字不为空
			name.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientName") + "");
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientSex") != null) { // 判断病员性别不为空
			sex.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientSex") + "");
		} else {
			sex.setText("未知");
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("InHospitalTime") != null) { // 判断入院时间不为空

			String date = (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("InHospitalTime") + "");

			if (ConvertUtils.str2Date(date).after(ConvertUtils.str2Date("1950-00-00 00:00"))) {

				date = date.replace(":", "-");

				if (date.length() >= 10) {
					enter_time.setText(date.substring(0, 10));
				} else {
					enter_time.setText(date.substring(0, date.length()));
				}
			}

		} else {
			enter_time.setText(ConvertUtils.date2Str(new Date(), "yyyy-MM-dd"));
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("OutHospitalTime") != null) { // 判断出院时间不为空
			String date = (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("OutHospitalTime") + "");

			date = date.replace(":", "-");

			Log.e(this, "ConvertUtils.str2Date(date)" + ConvertUtils.str2Date(date));
			Log.e(this, "ConvertUtils.str2Date(1950-00-00 00:00)" + ConvertUtils.str2Date("1950-00-00 00:00"));

			if (ConvertUtils.str2Date(date).after(ConvertUtils.str2Date("1950-00-00 00:00"))) {
				if (date.length() >= 10) {
					out_time.setText(date.substring(0, 10));
				} else {
					out_time.setText(date.substring(0, date.length()));
				}

			}
		}

		// TODO 7-25 如果有病人，则查询数据库
		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("Advice") != null) { // 判断医嘱不为空
			List<Map<String, Object>> adviceNewIDList = Db.select(
					"select a.Advice,a.ID from FoodAdvice a left join PatientFoodAdvice b on b.FoodAdvice_ID=a.ID where b.Patient_ID=? ORDER BY a.DefaultFoodID ASC",
					(String) PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));

			StringBuffer adviceSb = new StringBuffer();
			if (adviceNewIDList != null) {
				for (int i = 0; i < adviceNewIDList.size(); i++) {
					if (i != adviceNewIDList.size() - 1) {
						adviceSb.append(adviceNewIDList.get(i).get("Advice") + "，");
					} else {
						adviceSb.append(adviceNewIDList.get(i).get("Advice"));
					}
				}
			}
			doctor_advice.setText(adviceSb.toString());
		}

		List<Map<String, Object>> list = Db.select("select Name from Allergy where id in (select Allergy_ID from PatientAllergy where Patient_ID = ?)",
				PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));

		if (list != null) {
			firstAllergyText = new StringBuffer();
			for (int i = 0; i < list.size(); i++) {

				firstAllergyText.append(list.get(i).get("Name") + "，");

			}

			allergy.setText(firstAllergyText.substring(0, firstAllergyText.length() - 1));
		}

		ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(this);
		doctor_advice.setOnClickListener(this);
		sex.setOnClickListener(this);
		allergy.setOnClickListener(this);
		// TODO 添加入院出院选择日期

		enter_time.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					hideSoftKeyboard(enter_time);
					datePopWindow(enter_time);
				}
			}
		});
		enter_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				datePopWindow(enter_time);
			}
		});
		out_time.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					hideSoftKeyboard(out_time);
					datePopWindow(out_time);
				}
			}
		});
		out_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				datePopWindow(out_time);
			}
		});
		Calendar calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);

	}

	private void hideSoftKeyboard(EditText enter_time) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(enter_time.getWindowToken(), 0);
	}

	// 日期选择底部弹框
	public void datePopWindow(final EditText enter_time) {
		menuPopView = LayoutInflater.from(this).inflate(R.layout.select_time, null);
		menuPop = new PopupWindow(menuPopView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		menuPop.setBackgroundDrawable(new BitmapDrawable());
		menuPop.setOutsideTouchable(false);
		// menuPop.setAnimationStyle(R.style.AnimBottom);
		menuPop.setFocusable(true);

		menuPop.update();
		menuPop.showAtLocation(ll_parent, Gravity.BOTTOM, 0, 0);
		menuPopView.setFocusableInTouchMode(true);

		DatePicker dp = (DatePicker) menuPopView.findViewById(R.id.dp);

		dp.init(year, month, day, new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int years, int monthOfYear, int dayOfMonth) {
				// TODO Auto-generated method stub
				year = years;
				month = monthOfYear;
				day = dayOfMonth;
				String startTime = "";
				startTime = year + "-" + ((month + 1) < 10 ? ("0" + (month + 1)) : (month + 1)) + "-" + (day < 10 ? ("0" + day) : day);
				enter_time.setText(startTime);
			}
		});
	};

	@Override
	protected void onPause() {
		super.onPause();

		if (advicePopup != null && advicePopup.isShowing()) {
			advicePopup.dismiss();
		}

		if (sexPop != null && sexPop.isShowing()) {
			sexPop.dismiss();
		}

	}

	@Override
	public void onClick(View v) {

		int id = v.getId();
		switch (id) {
		case R.id.ok:

			String PID = admission_num.getText() + "";
			String PatientName = name.getText() + "";

			if (PID.length() == 0 || PID.trim().equals("")) {

				PID = "未知";
			}

			if (PatientName.length() == 0) {

				PatientName = "未知";

			}

			String PatientSex = sex.getText() + "";
			String advice = doctor_advice.getText() + "";
			String allergyStr = allergy.getText() + "";
			// TODO 7-24提交的时候加上时分
			String InHospitalTime = enter_time.getText() + "" + " 00-00";
			String OutHospitalTime = out_time.getText() + "" + " 00-00";
			// TODO 修改过的地方
			if (mode == 0 && advice.length() <= 0) {
				// if (advice.length() <= 0) {
				// handler.obtainMessage(6).sendToTarget();
				// } else
				if (!checkDate(InHospitalTime)) {
					handler.obtainMessage(4).sendToTarget();
				} else if (!checkDate(OutHospitalTime)) {
					handler.obtainMessage(5).sendToTarget();
				} else {
					new Thread(new Run(PID, PatientName, PatientSex, advice, allergyStr, InHospitalTime, OutHospitalTime)).start();
				}
			} else {
				if (advice.length() <= 0) {
					handler.obtainMessage(6).sendToTarget();
				} else if (!checkDate(InHospitalTime)) {
					handler.obtainMessage(4).sendToTarget();
				} else if (!checkDate(OutHospitalTime)) {
					handler.obtainMessage(5).sendToTarget();
				} else {
					new Thread(new Run(PID, PatientName, PatientSex, advice, allergyStr, InHospitalTime, OutHospitalTime)).start();
				}
			}

			break;

		case R.id.sex:
			showSexPopup();
			break;
		case R.id.doctor_advice:

			if (allergyPopup != null && allergyPopup.isShowing()) {
				allergyPopup.dismiss();
			}
			if (sexPop != null && sexPop.isShowing()) {
				sexPop.dismiss();
			}
			Map<String, Object> map = Db.selectUnique("SELECT Nums FROM NumsMark WHERE TakeFoodTime = ? AND PatPositionID =?", OrderActivity.currentOrderDate,
					PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatPositionID"));
			// TODO 7-25:没有点过菜，可修改医嘱
			if (map == null || Integer.parseInt(map.get("Nums") + "") <= 0) {
				showAdvicePopup();
			} else {
				Toast.makeText(PatientInfoActivity.this, "您已经点过菜，不可修改医嘱！", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.allergy:

			if (advicePopup != null && advicePopup.isShowing()) {

				advicePopup.dismiss();
			}

			List<Map<String, Object>> patientAllergyList = Db.select(
					"select ID,Name from Allergy where id in (select Allergy_ID from  PatientAllergy where Patient_ID = ?)",
					PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));
			if (patientAllergyList != null) {
				for (int i = 0; i < patientAllergyList.size(); i++) {
					allergyText.append(patientAllergyList.get(i).get("Name") + "，");
				}
			}

			showAllergyPopup(allergy_list, patientAllergyList, allergy_ID_list, "Name");

			break;
		default:
			break;
		}

	}

	/**
	 * 更新数据线程
	 * 
	 * @author Administrator
	 * 
	 */
	public class Run implements Runnable {

		final String PID;
		final String PatientName;
		final String PatientSex;
		final String advice;
		final String allergyStr;
		final Date InHospitalTime;
		final Date OutHospitalTime;

		public Run(String PID, String PatientName, String PatientSex, String advice, String allergyStr, String InHospitalTime, String OutHospitalTime) {

			this.PID = PID;
			this.PatientName = PatientName;
			this.PatientSex = PatientSex;
			this.advice = advice;
			this.allergyStr = allergyStr;

			if (InHospitalTime == null || InHospitalTime.length() == 6) {

				InHospitalTime = ConvertUtils.date2Str(new Date(), "yyyy-MM-dd HH-mm");
			}

			this.InHospitalTime = ConvertUtils.str2Date(InHospitalTime, "yyyy-MM-dd HH-mm");

			if (OutHospitalTime == null || OutHospitalTime.length() == 6) {
				OutHospitalTime = "";
			}
			this.OutHospitalTime = ConvertUtils.str2Date(OutHospitalTime, "yyyy-MM-dd HH-mm");
		}

		@Override
		public void run() {
			if (isUpdate) {
				// 如果住院号，姓名，医嘱为空就提示不能更新
				int b = 0;
				int c = 0;
				// 更新医嘱数据
				int a = Db.update(
						"UPDATE Patient SET PID=? ,PatientName =? ,PatientSex =?  ,LastUpdateTime=?,InHospitalTime=?,OutHospitalTime=?,LastUpdateAdminName=? WHERE PatPositionID = ?",
						PID, PatientName, PatientSex, ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"),

						ConvertUtils.date2Str2(InHospitalTime, "yyyy-MM-dd HH:mm"), ConvertUtils.date2Str2(OutHospitalTime, "yyyy-MM-dd HH:mm"),
						getSharedPreferences("user", 0).getString("user", "乐pad"), PatientsListActivity.currentBedID);

				if ((advice + "，").equals(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("Advice"))) {

					if (a == -1) {
						Map<String, Object> newPatientmap = Db.selectUnique("select * from patient where PatPositionID = ?", PatientsListActivity.currentBedID);

						if (newPatientmap != null) {

							newPatientmap.put("Advice", advice + "，");
							if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("OrderMark") != null) {
								newPatientmap.put("OrderMark", "True");
							}
							PatientsListActivity.beds_list.remove(PatientsListActivity.currentPatientID);
							PatientsListActivity.beds_list.add(PatientsListActivity.currentPatientID, newPatientmap);

							handler.obtainMessage(0).sendToTarget();
						}
					}
				} else {
					// 更新病人医嘱信息
					b = Db.delete("DELETE  FROM PatientFoodAdvice WHERE  Patient_ID= ?",
							PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));

					for (int i = 0; i < advice_ID_list.size(); i++) {

						c = Db.insert("INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID) VALUES(?,?)", PatientsListActivity.beds_list.get(

								PatientsListActivity.currentPatientID).get("ID"), advice_ID_list.get(i));
					}

				}

				if (!firstAllergyText.equals(allergyText)) {
					// 更新病人过敏信息
					Db.delete("delete from PatientAllergy where Patient_ID = ?",
							PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));

					Log.e(PatientInfoActivity.this, "allergy_ID_list=" + allergy_ID_list);

					for (int i = 0; i < allergy_ID_list.size(); i++) {

						Db.insert("INSERT INTO PatientAllergy (Patient_ID,Allergy_ID) VALUES(?,?)", PatientsListActivity.beds_list.get(

								PatientsListActivity.currentPatientID).get("ID"), allergy_ID_list.get(i));
					}

				}

				if (a == -1 && b == -1) {

					Map<String, Object> newPatientmap = Db.selectUnique("select * from patient where PatPositionID = ?", PatientsListActivity.currentBedID);

					if (newPatientmap != null) {

						newPatientmap.put("Advice", advice + "，");
						if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("OrderMark") != null) {
							newPatientmap.put("OrderMark", "True");
						}

						PatientsListActivity.beds_list.remove(PatientsListActivity.currentPatientID);
						PatientsListActivity.beds_list.add(PatientsListActivity.currentPatientID, newPatientmap);
						handler.obtainMessage(0).sendToTarget();
					}
				}

			} else {

				// 插入新病人数据

				String ID = StringUtils.uid();

				int a = Db.insert(
						"INSERT INTO Patient(ID ,PID ,HisCode,PatientName, PatientSex  ,WriteTime ,LastUpdateTime,PatPositionID,InHospitalTime,OutHospitalTime,WriteAdminName,LastUpdateAdminName) "
								+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
						ID, PID, getSharedPreferences("deviceDate", 0).getString("hisCode", "松江"), PatientName, PatientSex,
						ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"), ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"), PatientsListActivity.currentBedID,
						ConvertUtils.date2Str2(InHospitalTime, "yyyy-MM-dd HH:mm"), ConvertUtils.date2Str2(OutHospitalTime, "yyyy-MM-dd HH:mm"),
						getSharedPreferences("user", 0).getString("user", "乐pad").trim(), getSharedPreferences("user", 0).getString("user", "乐pad"));

				android.util.Log.i("------", "currentBedID--" + PatientsListActivity.currentBedID);
				android.util.Log.i("------", "PID--" + PID);
				android.util.Log.i("------", "hisCode--" + getSharedPreferences("deviceDate", 0).getString("hisCode", "松江"));
				// 插入病人医嘱信息
				// TODO 修改过地方
				int c = 0;
				for (int i = 0; i < advice_ID_list.size(); i++) {

					c = Db.insert("INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID) VALUES(?,?)", ID, advice_ID_list.get(i));
				}

				if (mode == 0 && advice_ID_list.size() <= 0) {
					c = Db.insert("INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID) VALUES(?,?)", ID, "普食");
					android.util.Log.i("----", "===asdadsa=");
				}
				// 插入病人过敏信息

				for (int i = 0; i < allergy_ID_list.size(); i++) {

					Db.insert("INSERT INTO PatientAllergy (Patient_ID,Allergy_ID) VALUES(?,?)", ID, allergy_ID_list.get(i));

				}

				Log.e("a=" + a + "/" + "c=" + c);

				if (a == -1 && c == -1) {

					Map<String, Object> newPatientmap = Db.selectUnique("select * from patient where ID = ?",
							PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));
					if (newPatientmap != null) {
						newPatientmap.put("Advice", advice + "，");

						PatientsListActivity.beds_list.remove(PatientsListActivity.currentPatientID);

						PatientsListActivity.beds_list.add(PatientsListActivity.currentPatientID, newPatientmap);
					}
					handler.obtainMessage(1).sendToTarget();
				} else {
					handler.obtainMessage(3).sendToTarget();
				}
				adviceIsFirstTouch = true;

			}
		}
	}

	/**
	 * 提示相应事件处理结果
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:

				Toast.makeText(PatientInfoActivity.this, "数据更新成功！", Toast.LENGTH_SHORT).show();
				PatientInfoActivity.this.finish();

				break;
			case 1:

				Toast.makeText(PatientInfoActivity.this, "数据添加成功！", Toast.LENGTH_SHORT).show();
				PatientInfoActivity.this.finish();
				break;

			case 2:

				Toast.makeText(PatientInfoActivity.this, "住院号不能为空！", Toast.LENGTH_SHORT).show();

				break;

			case 3:

				Toast.makeText(PatientInfoActivity.this, "住院号已存在，请重新输入", Toast.LENGTH_LONG).show();
				break;
			case 4:

				Toast.makeText(PatientInfoActivity.this, "入院时间输入有误，请重新输入", Toast.LENGTH_LONG).show();
				break;
			case 5:

				Toast.makeText(PatientInfoActivity.this, "出院时间输入有误，请重新输入", Toast.LENGTH_LONG).show();
				break;
			case 6:

				Toast.makeText(PatientInfoActivity.this, "医嘱不能为空！", Toast.LENGTH_LONG).show();

				break;
			case 7:

				Toast.makeText(PatientInfoActivity.this, "性别不能为空！", Toast.LENGTH_LONG).show();

				break;

			default:
				break;
			}
		}
	};

	/**
	 * advice弹出框条目上的监听类
	 * 
	 * @author Administrator
	 * 
	 */
	private class AdviceListItemListener implements OnClickListener {

		TextView adviceText;
		ImageView iv;
		String str;
		int position;

		public AdviceListItemListener(TextView adviceText, ImageView iv, String str, int position) {

			this.adviceText = adviceText;
			this.iv = iv;
			this.str = str;
			this.position = position;

			if (adviceIsFirstTouch) {
				PatientInfoActivity.advice_ID_list.clear();
				adviceIsFirstTouch = false;
			}

		}

		@Override
		public void onClick(View v) {

			if (advice_ID_list.contains(v.getTag())) {

				advice_ID_list.remove(v.getTag().toString());
				text.delete(0, text.length());

				for (int i = 0; i < advice_ID_list.size(); i++) {
					text.append(advice_map.get(advice_ID_list.get(i)) + "，");
				}

				if (text.toString().length() > 2) {
					doctor_advice.setText("");
					doctor_advice.setText(text.toString().substring(0, text.toString().length() - 1));
				} else {
					doctor_advice.setText("");
				}

				adviceText.setTextColor(Color.BLACK);

				v.setBackgroundColor(Color.alpha(Color.TRANSPARENT));
				iv.setBackgroundResource(R.drawable.icon_q01);

			} else {

				advice_ID_list.add(v.getTag().toString());

				// TODO 2016.09.27 为 advice_ID_list添加排序 (正序)

				Collections.sort(advice_ID_list, new Comparator<Object>() {
					public int compare(Object ob1, Object ob2) {
						int i1 = Integer.parseInt(advice_map.get(ob1.toString() + "_s").toString());
						int i2 = Integer.parseInt(advice_map.get(ob2.toString() + "_s").toString());
						if (i1 > i2)
							return 1;
						else
							return -1;
					}
				});

				text.delete(0, text.length());
				for (int i = 0; i < advice_ID_list.size(); i++) {

					text.append(advice_map.get(advice_ID_list.get(i)) + "，");

				}

				if (text.toString().length() > 2) {

					doctor_advice.setText("");
					doctor_advice.setText(text.toString().substring(0, text.toString().length() - 1));
				} else {
					doctor_advice.setText("");
				}

				v.setBackgroundColor(Color.rgb(3, 106, 235));
				adviceText.setTextColor(Color.WHITE);
				iv.setBackgroundResource(R.drawable.icon_q02);

			}

		}
	}

	/**
	 * 过敏弹出框中的item监听类
	 * 
	 * @author Administrator
	 * 
	 */
	private class AllergyListItemListener implements OnClickListener {

		TextView adviceText;
		ImageView iv;
		String str;
		int position;

		public AllergyListItemListener(TextView adviceText, ImageView iv, String str, int position) {

			this.adviceText = adviceText;
			this.iv = iv;
			this.str = str;
			this.position = position;

			if (allergyIsFirstTouch) {
				PatientInfoActivity.allergy_ID_list.clear();
				allergyIsFirstTouch = false;
			}

		}

		@Override
		public void onClick(View v) {

			if (allergy_ID_list.contains(v.getTag())) {

				allergy_ID_list.remove(v.getTag().toString());
				allergyText.delete(0, allergyText.length());

				for (int i = 0; i < allergy_ID_list.size(); i++) {
					allergyText.append(allergy_map.get(allergy_ID_list.get(i)) + "，");
				}

				if (allergyText.toString().length() > 2) {
					allergy.setText("");
					allergy.setText(allergyText.toString().substring(0, allergyText.toString().length() - 1));
				} else {
					allergy.setText("");
				}

				adviceText.setTextColor(Color.BLACK);

				v.setBackgroundColor(Color.alpha(Color.TRANSPARENT));
				iv.setBackgroundResource(R.drawable.icon_q01);

			} else {

				allergy_ID_list.add(v.getTag().toString());
				allergyText.delete(0, allergyText.length());
				for (int i = 0; i < allergy_ID_list.size(); i++) {

					allergyText.append(allergy_map.get(allergy_ID_list.get(i)) + "，");

				}

				if (allergyText.toString().length() > 2) {

					allergy.setText("");
					allergy.setText(allergyText.toString().substring(0, allergyText.toString().length() - 1));
				} else {
					allergy.setText("");
				}

				v.setBackgroundColor(Color.rgb(3, 106, 235));
				adviceText.setTextColor(Color.WHITE);
				iv.setBackgroundResource(R.drawable.icon_q02);

			}

		}
	}

	/**
	 * 显示性别选择提示框
	 */
	private void showSexPopup() {

		if (allergyPopup != null && allergyPopup.isShowing()) {
			allergyPopup.dismiss();
		}
		if (advicePopup != null && advicePopup.isShowing()) {
			advicePopup.dismiss();
		}
		if (sexPop == null) {
			sexList = new ListView(PatientInfoActivity.this);
			sexList.setAdapter(new SexAdapter(PatientInfoActivity.this, sex, sexPop));
			sexPop = new PopupWindow(sexList, sex.getWidth(), LayoutParams.WRAP_CONTENT, false);

			sexList.setBackgroundColor(Color.WHITE);
			sexList.setBackgroundResource(R.drawable.sex);
			sexPop.setFocusable(true);
			sexPop.setTouchable(true);
			sexPop.setOutsideTouchable(true);
			sexPop.setBackgroundDrawable(new ColorDrawable(00000000));

		}

		if (sexPop != null && !sexPop.isShowing()) {
			sexPop.showAsDropDown(sex);
		}

	}

	/**
	 * 显示医嘱信息的pupopwindow窗口
	 */
	private void showAdvicePopup() {
		List<Map<String, Object>> advice_list_a = Db.select("SELECT * FROM PatientFoodAdvice A WHERE A.Patient_ID = ?",
				PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));

		if (advice_list_a != null) {

			for (int i = 0; i < advice_list_a.size(); i++) {

				Map<String, Object> map = Db.selectUnique("SELECT A.Advice FROM FoodAdvice A WHERE ID = ?", advice_list_a.get(i).get("FoodAdvice_ID"));

				if (map != null) {

					text.append(map.get("Advice") + "，");

				}

			}

		}

		if (advicePopup == null) {
			View myView = PatientInfoActivity.this.getLayoutInflater().inflate(R.layout.advice_pop, null);

			LinearLayout body = (LinearLayout) myView.findViewById(R.id.advice_body);

			if (!CollectionUtils.isEmpty(advice_list)) {

				for (int i = 0; i < advice_list.size(); i++) {

					View advice_item = PatientInfoActivity.this.getLayoutInflater().inflate(R.layout.advice_pop_item, null);

					iv = (ImageView) advice_item.findViewById(R.id.advice_mark);

					TextView adviceText = (TextView) advice_item.findViewById(R.id.advice);

					adviceText.setText(advice_list.get(i).get("Advice").toString());

					advice_item.setOnClickListener(new AdviceListItemListener(adviceText, iv, advice_list.get(i).get("Advice").toString(), i));
					advice_item.setTag(advice_list.get(i).get("ID"));

					if (!CollectionUtils.isEmpty(advice_list_a)) {

						for (int j = 0; j < advice_list_a.size(); j++) {
							if (advice_list_a.get(j).get("FoodAdvice_ID").toString().equals(advice_list.get(i).get("ID").toString())) {

								advice_ID_list.add(advice_list_a.get(j).get("FoodAdvice_ID").toString());

								advice_item.setBackgroundColor(Color.rgb(3, 106, 235));
								iv.setBackgroundResource(R.drawable.icon_q02);

								adviceText.setTextColor(Color.WHITE);

							}
						}
					}

					body.addView(advice_item);

				}

			}

			advicePopup = new PopupWindow(myView, doctor_advice.getWidth(), 850);

			myView.setBackgroundColor(Color.WHITE);
			myView.setBackgroundResource(R.drawable.pop_download_bg);

			advicePopup.setFocusable(false);

			advicePopup.setTouchable(true);
			advicePopup.setOutsideTouchable(true);
			advicePopup.setBackgroundDrawable(new ColorDrawable(00000000));
		}

		if (!advicePopup.isShowing()) {
			advicePopup.showAsDropDown(doctor_advice, -270, -400);
		}

	}

	/**
	 * 显示过敏信息的popupwindow窗口
	 * 
	 * @param list1
	 *            系统的全部过敏信息集合
	 * @param list2
	 *            病人过敏信息集合
	 * @param list3
	 *            病人经过选择改变后的过敏ID信息集合
	 * @param str1
	 *            获取过敏信息的字段 Name
	 */
	private void showAllergyPopup(List<Map<String, Object>> list1, List<Map<String, Object>> list2, List<String> list3, String str1) {

		if (allergyPopup == null) {
			View myView = PatientInfoActivity.this.getLayoutInflater().inflate(R.layout.advice_pop, null);

			LinearLayout body = (LinearLayout) myView.findViewById(R.id.advice_body);

			if (!CollectionUtils.isEmpty(list1)) {

				for (int i = 0; i < list1.size(); i++) {

					View advice_item = PatientInfoActivity.this.getLayoutInflater().inflate(R.layout.advice_pop_item, null);

					iv = (ImageView) advice_item.findViewById(R.id.advice_mark);

					TextView adviceText = (TextView) advice_item.findViewById(R.id.advice);

					adviceText.setText(list1.get(i).get(str1).toString());

					advice_item.setOnClickListener(new AllergyListItemListener(adviceText, iv, list1.get(i).get(str1).toString(), i));
					advice_item.setTag(list1.get(i).get("ID"));

					if (!CollectionUtils.isEmpty(list2)) {

						for (int j = 0; j < list2.size(); j++) {
							if ((list2.get(j).get("ID") + "").equals(list1.get(i).get("ID") + "")) {

								list3.add(list2.get(j).get("ID") + "");

								advice_item.setBackgroundColor(Color.rgb(3, 106, 235));
								iv.setBackgroundResource(R.drawable.icon_q02);
								adviceText.setTextColor(Color.WHITE);

							}
						}
					}

					body.addView(advice_item);

				}

			}

			allergyPopup = new PopupWindow(myView, allergy.getWidth(), 800);

			myView.setBackgroundColor(Color.WHITE);
			myView.setBackgroundResource(R.drawable.allergy_pop_download_bg);

			allergyPopup.setFocusable(false);
			allergyPopup.setTouchable(true);
			allergyPopup.setOutsideTouchable(true);

			allergyPopup.setBackgroundDrawable(new ColorDrawable(00000000));
		}

		if (!allergyPopup.isShowing()) {
			allergyPopup.showAsDropDown(allergy, -270, -260);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			PatientInfoActivity.this.finish();
			OrderActivity.isInitData = false;
			return true;

		}

		return false;
	}

	// 检验日期输入格式是否合法
	private boolean checkDate(String date) {

		StringBuffer dateStr = new StringBuffer();
		// 时间为空
		if (date == null || date.length() == 6) {

			return true;

		}

		for (int i = 0; i < date.length(); i++) {
			String indexStr = date.substring(i, i + 1);

			if (!"-".equals(indexStr)) {

				dateStr.append("A");

			} else {
				dateStr.append("-");
			}

		}

		if ("AAAA-AA-AAAAA-AA".equals(dateStr.toString())) {
			return true;
		}

		return false;
	}
}
