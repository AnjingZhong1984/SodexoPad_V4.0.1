package com.janlent.sodexo.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.janlent.sodexo.activity.ConfirmOrderActivity;
import com.janlent.sodexo.activity.OrderActivity;
import com.janlent.sodexo.activity.PatientInfoActivity;
import com.janlent.sodexo.activity.PatientsListActivity;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.Db;
import com.janlent.utils.Log;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

/**
 * 病员列表界面中orderlistview的适配器，主要显示病员详情，床位号、病员姓名、医嘱、订单状态
 * 
 * @author Administrator
 * 
 */

public class PatientListAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private Activity context;

	private int n;
	private SharedPreferences sharedata;
	int mode = 0;

	public PatientListAdapter(Activity context, int n) {

		this.n = n;
		this.context = context;
		inflater = context.getLayoutInflater();
		sharedata = context.getSharedPreferences("deviceDate", 0);
		mode = sharedata.getInt("checkMode", 1);

	}

	@Override
	public int getCount() {

		if (n == 1) {
			if (PatientsListActivity.beds_list != null) {
				return PatientsListActivity.beds_list.size();
			} else {
				return 0;
			}

		} else {
			return 0;
		}

	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		try {

			ViewHold mViewHold = null;

			if (convertView == null) {

				convertView = inflater.inflate(R.layout.patient_list_item, null);
				convertView.setBackgroundColor(0Xcf5950);

				mViewHold = new ViewHold();

				mViewHold.star = (TextView) convertView.findViewById(R.id.star);
				mViewHold.star.setVisibility(View.GONE);
				mViewHold.tv1 = (TextView) convertView.findViewById(R.id.bed_num);
				mViewHold.tv2 = (TextView) convertView.findViewById(R.id.bed_patient_name);
				mViewHold.tv3 = (TextView) convertView.findViewById(R.id.advice);
				mViewHold.tv4 = (TextView) convertView.findViewById(R.id.order_state);
				mViewHold.sync_state = (TextView) convertView.findViewById(R.id.sync_state);

				mViewHold.btn = (Button) convertView.findViewById(R.id.order);

				convertView.setTag(mViewHold);
				convertView.setBackgroundColor(Color.TRANSPARENT);
				//
			} else {

				mViewHold = (ViewHold) convertView.getTag();
				convertView.setBackgroundColor(Color.TRANSPARENT);
				mViewHold.star.setVisibility(View.GONE);
				mViewHold.tv1.setText("");
				mViewHold.tv2.setText("");
				mViewHold.tv3.setText("");
				mViewHold.tv4.setBackgroundResource(R.drawable.btn_wdc);
				mViewHold.btn.setBackgroundResource(R.drawable.order_btn);
				mViewHold.sync_state.setVisibility(View.GONE);

			}
			if (PatientsListActivity.beds_list.size() > position) {

				if (PatientsListActivity.beds_list.get(position) != null) { // 判断床位列表中的楼层不为空

					if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null
							&& PatientsListActivity.beds_list.get(position).get("bedNum") != null) { // 判断床位号不为空
						mViewHold.tv1.setText(PatientsListActivity.beds_list.get(position).get("bedNum") + "");
					}
					// TODO 判断床位号不为空 并跳转到 病人详情页面 可修改信息
					// if (PatientsListActivity.beds_list.size() > position
					// && PatientsListActivity.beds_list.get(position) != null
					// && PatientsListActivity.beds_list.get(position)
					// .get("PatientName") == null
					// ) { // 判断床位没有病员
					//
					// mViewHold.btn
					// .setOnClickListener(new PatientInfoListener(
					// position));
					// // TODO 如已点餐 则点餐按钮变为修改按钮 点击跳转到订单详情页面
					// } else
					if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null
							&& PatientsListActivity.beds_list.get(position).get("order_id") != null) { // 判断订单ID不为空
						if (PatientsListActivity.beds_list.get(position).get("PatientName") != null) {
							mViewHold.tv2.setText(PatientsListActivity.beds_list.get(position).get("PatientName").toString());

						}
						mViewHold.btn.setOnClickListener(new ModifyListener(position));

						mViewHold.btn.setBackgroundResource(R.drawable.btn_xg_bg);

						mViewHold.tv4.setBackgroundResource(R.drawable.btn_ydc);
						// 判断是否同步
						Map<String, Object> sy = Db.selectUnique("select HaveSync from [Order] where ID= ? ",
								PatientsListActivity.beds_list.get(position).get("order_id"));
						if (sy != null) {
							mViewHold.sync_state.setVisibility(View.VISIBLE);

							if (sy.get("HaveSync") != null) {
								if (sy.get("HaveSync").equals("True")) {
									mViewHold.sync_state.setBackgroundResource(R.drawable.btn_ytb);

								} else if (sy.get("HaveSync").equals("null")) {
									mViewHold.sync_state.setBackgroundResource(R.drawable.btn_wtb);
								}
							} else {
								mViewHold.sync_state.setBackgroundResource(R.drawable.btn_wtb);
							}
						}
					} else { // 订单ID为空
						if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null) {
							if (PatientsListActivity.beds_list.get(position).get("PatientName") != null) {
								mViewHold.tv2.setText(PatientsListActivity.beds_list.get(position).get("PatientName").toString());

							}
							mViewHold.btn.setOnClickListener(new OrderListener(position));
						}
					}

					if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null
							&& PatientsListActivity.beds_list.get(position).get("Advice") != null) { // 判断医嘱不为空
						/*
						 * Log.e("advice------>"+(PatientsListActivity.beds_list
						 * .get( position).get("Advice") + "") .substring( 0,
						 * PatientsListActivity.beds_list .get(position)
						 * .get("Advice") .toString().length() - 1));
						 */
						mViewHold.tv3.setText((PatientsListActivity.beds_list.get(position).get("Advice") + "").substring(0,
								PatientsListActivity.beds_list.get(position).get("Advice").toString().length() - 1));
					}
					if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null
							&& PatientsListActivity.beds_list.get(position).get("Advice") != null) { // 判断医嘱不为空
						String[] s = (PatientsListActivity.beds_list.get(position).get("Advice") + "").split("，");
						if (s.length > 1) {
							convertView.setBackgroundColor(Color.rgb(232, 195, 190));
						} else if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null
								&& PatientsListActivity.beds_list.get(position).get("AllowOrderFood") != null) {
							convertView.setBackgroundColor(Color.rgb(232, 195, 190));
						}
					}
					if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null
							&& PatientsListActivity.beds_list.get(position).get("PatPositionID") != null) {
						mViewHold.btn.setTag(PatientsListActivity.beds_list.get(position).get("PatPositionID"));
					}
					// 判断 预出院时间不为空
					if (PatientsListActivity.beds_list.size() > position && PatientsListActivity.beds_list.get(position) != null
							&& PatientsListActivity.beds_list.get(position).get("ExpectOutHospitalTime") != null
							&& !PatientsListActivity.beds_list.get(position).get("ExpectOutHospitalTime").toString().trim().equals("")) {
						mViewHold.star.setVisibility(View.VISIBLE);

					}

				}
			}

		} catch (Exception e) {

			this.notifyDataSetChanged();
		}
		return convertView;

	}

	/**
	 * 给按钮注册监听器，处理点击事件
	 * 
	 * @author Administrator
	 * 
	 */
	private class OrderListener implements OnClickListener {

		int id;

		public OrderListener(int id) {

			this.id = id;
		}

		@Override
		public void onClick(View v) {

			PatientsListActivity.currentPatientID = id;

			PatientsListActivity.currentBedID = v.getTag() + "";

			Intent intent = new Intent(context, OrderActivity.class);

			intent.putExtra("tag", "putorder");
			context.startActivity(intent);

		}

	}

	/**
	 * 给按钮注册监听器，处理点击事件
	 * 
	 * @author Administrator
	 * 
	 */
	private class PatientInfoListener implements OnClickListener {

		int id;

		public PatientInfoListener(int id) {

			this.id = id;
		}

		// TODO 修改过的地方
		@Override

		public void onClick(View v) {
			if (mode == 0) {
				// 床位没有病人，病人id=position
				PatientsListActivity.currentPatientID = id;
				// 创建“未知”病人
				// ad();
				PatientsListActivity.currentBedID = v.getTag() + "";
				Intent intent = new Intent(context, OrderActivity.class);
				intent.putExtra("tag", "putorder");
				context.startActivity(intent);

			} else {
				PatientsListActivity.currentPatientID = id;
				PatientsListActivity.currentBedID = (String) v.getTag();
				Intent intent = new Intent(context, PatientInfoActivity.class);
				context.startActivity(intent);
			}

		}

	}

	/**
	 * 给按钮注册监听器，处理点击事件
	 * 
	 * @author Administrator
	 * 
	 */
	private class ModifyListener implements OnClickListener {

		int id;

		public ModifyListener(int id) {

			this.id = id;
		}

		@Override
		public void onClick(View v) {

			PatientsListActivity.currentPatientID = id;
			PatientsListActivity.currentBedID = v.getTag() + "";

			Intent intent = new Intent(context, ConfirmOrderActivity.class);
			intent.putExtra("tag", "modify");
			context.startActivity(intent);

		}

	}

	private class ViewHold {
		private TextView star;
		private TextView tv1;
		private TextView tv2;
		private TextView tv3;
		private TextView tv4;
		private TextView sync_state;
		private Button btn;
	}

	public static List<String> advice_ID_list = new ArrayList<String>();// 保存病人已有医嘱ID
	// private StringBuffer allergyText = new StringBuffer(); // 用来保存医嘱信息
	private StringBuffer firstAllergyText = new StringBuffer(); // 用来保存医嘱信息
	public static List<String> allergy_ID_list = new ArrayList<String>();// 保存病人过敏ID

	// TODO 修改过的地方
	/**
	 * 插入一条病人的信息 信息为空
	 */
	public void ad() {
		// 2016.09.27 为遗嘱查询 添加 FoodAdvice.DefaultFoodID 字段升序排序
		List<Map<String, Object>> advice_list_a = Db.select(
				"SELECT * FROM PatientFoodAdvice A inner join FoodAdvice B on A.FoodAdvice_ID=B.ID WHERE A.Patient_ID = ?  ORDER BY B.DefaultFoodID ASC",
				PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));

		if (advice_list_a != null) {

			for (int i = 0; i < advice_list_a.size(); i++) {

				Map<String, Object> map = Db.selectUnique("SELECT A.Advice FROM FoodAdvice A WHERE ID = ?", advice_list_a.get(i).get("FoodAdvice_ID"));

				if (map != null) {

					advice_ID_list.add(map.get("Advice") + "，");
					android.util.Log.i("confir", "--advice--" + map.get("Advice") + "，");

				}

			}
		}
		List<Map<String, Object>> list = Db.select("select Name from Allergy where id in (select Allergy_ID from PatientAllergy where Patient_ID = ?)",
				PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));

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
		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientName") != null) { // 判断病员名字不为空
			PatientName = PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientName") + "";
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientSex") != null) { // 判断病员性别不为空
			PatientSex = PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientSex") + "";
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("InHospitalTime") != null) { // 判断入院时间不为空

			String date = (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("InHospitalTime") + "");

			if (ConvertUtils.str2Date(date).after(ConvertUtils.str2Date("1950-00-00 00:00"))) {

				date = date.replace(":", "-");

				if (date.length() >= 17) {
					InHospitalTime = (date.substring(0, 16));
				} else {
					InHospitalTime = (date.substring(0, date.length()));
				}
			}

		} else {
			InHospitalTime = (ConvertUtils.date2Str(new Date(), "yyyy-MM-dd HH-mm"));
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("OutHospitalTime") != null) { // 判断出院时间不为空
			String date = (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("OutHospitalTime") + "");

			date = date.replace(":", "-");

			if (ConvertUtils.str2Date(date).after(ConvertUtils.str2Date("1950-00-00 00:00"))) {
				if (date.length() >= 17) {
					OutHospitalTime = (date.substring(0, 16));
				} else {
					OutHospitalTime = (date.substring(0, date.length()));
				}

			}
		}

		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("Advice") != null) { // 判断医嘱不为空
			String advice_a = PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("Advice") + "";
			advice = (advice_a.substring(0, advice_a.length() - 1));
		}

		if (firstAllergyText.length() > 1) {
			allergyStr = firstAllergyText.substring(0, firstAllergyText.length() - 1);
		}
		new Thread(new Run2(PID, PatientName, PatientSex, advice, allergyStr, InHospitalTime, OutHospitalTime)).start();
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

		public Run2(String PID, String PatientName, String PatientSex, String advice, String allergyStr, String InHospitalTime, String OutHospitalTime) {

			this.PID = PID;
			this.PatientName = PatientName;
			this.PatientSex = PatientSex;
			this.advice = advice;
			this.allergyStr = allergyStr;

			if (InHospitalTime == null || InHospitalTime.length() == 0) {

				InHospitalTime = ConvertUtils.date2Str(new Date(), "yyyy-MM-dd HH-mm");
			}

			this.InHospitalTime = ConvertUtils.str2Date(InHospitalTime, "yyyy-MM-dd HH-mm");

			this.OutHospitalTime = ConvertUtils.str2Date(OutHospitalTime, "yyyy-MM-dd HH-mm");

		}

		@Override
		public void run() {
			if (advice.length() < 1) {

				// 插入新病人数据

				String ID = StringUtils.uid();

				int a = Db.insert(
						"INSERT INTO Patient(ID ,PID ,HisCode,PatientName, PatientSex  ,WriteTime ,LastUpdateTime,PatPositionID,InHospitalTime,OutHospitalTime,WriteAdminName,LastUpdateAdminName) "
								+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
						ID, PID, context.getSharedPreferences("deviceDate", 0).getString("hisCode", "松江"), PatientName, PatientSex,
						ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"), ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"), PatientsListActivity.currentBedID,
						ConvertUtils.date2Str2(InHospitalTime, "yyyy-MM-dd HH:mm"), ConvertUtils.date2Str2(OutHospitalTime, "yyyy-MM-dd HH:mm"),
						context.getSharedPreferences("user", 0).getString("user", "乐pad").trim(),
						context.getSharedPreferences("user", 0).getString("user", "乐pad"));
						// android.util.Log.i("------", "currentBedID--"
						// + PatientsListActivity.currentBedID);
						// android.util.Log.i("------", "PID--" + PID);

				// 插入病人医嘱信息
				// TODO 修改过地方
				int c = 0;
				// for (int i = 0; i < advice_ID_list.size(); i++) {
				//
				// c = Db.insert(
				// "INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID)
				// VALUES(?,?)",
				// ID, advice_ID_list.get(i));
				// }

				if (mode == 0) {
					c = Db.insert("INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID) VALUES(?,?)", ID, "57aaedec9d824aaba2ec59640bcefc85,");
					android.util.Log.i("--普食--", "--asdadsa=");

					// 插入病人过敏信息

					Db.insert("INSERT INTO PatientAllergy (Patient_ID,Allergy_ID) VALUES(?,?)", ID, "");
				}
				// android.util.Log
				// .e("-------a--", "--2--a=" + a + "/" + "c=" + c);

				if (a == -1 && c == -1) {

					Map<String, Object> newPatientmap = Db.selectUnique("select * from patient where ID = ?",
							PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));
					if (newPatientmap != null) {
						newPatientmap.put("Advice", advice + "，");

						PatientsListActivity.beds_list.remove(PatientsListActivity.currentPatientID);

						PatientsListActivity.beds_list.add(PatientsListActivity.currentPatientID, newPatientmap);
					}
					android.util.Log.i("--普食-3-", "----asdadsa=");
					// handler.obtainMessage(1).sendToTarget();
				} else {
					// handler.obtainMessage(3).sendToTarget();
				}

			}
		}
	}
}
