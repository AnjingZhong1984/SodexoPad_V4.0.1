package com.janlent.sodexo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.PatientListAdapter;
import com.janlent.sodexo.widget.ArrayWheelAdapter;
import com.janlent.sodexo.widget.OnWheelChangedListener;
import com.janlent.sodexo.widget.OnWheelScrollListener;
import com.janlent.sodexo.widget.WheelView;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.Db;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

/**
 * 病员列表界面
 * 
 * @author Administrator
 * 
 */
public class  PatientsListActivity extends BaseActivity {
	/** Called when the activity is first created. */

	private PatientListAdapter mOderListAdapter;

	private List<Map<String, String>> list = new ArrayList<Map<String, String>>();

	private TextView all_patient;
	private TextView set_meal;
	private TextView no_meal;
	private TextView nobed;
	private ListView listView;

	private ProgressBar pro;
	private WheelView wheel_0;// 滚轮1
	private WheelView wheel_1;// 滚轮2

	private WheelView wheel_2;// 滚轮3
	private WheelView wheel_3;// 滚轮4
	private List<Map<String, Object>> wheelList_0 = new ArrayList<Map<String, Object>>();// 滚轮1的查询结果集;//
																							// 滚轮1的查询结果集
	private List<Map<String, Object>> wheelList_1 = new ArrayList<Map<String, Object>>();// 滚轮2的查询结果集
	private List<Map<String, Object>> wheelList_2 = new ArrayList<Map<String, Object>>();// 滚轮3的查询结果集
	public static List<Map<String, Object>> bedID = new ArrayList<Map<String, Object>>();// 床位号查询结果集
	public static List<Map<String, Object>> beds_list = new ArrayList<Map<String, Object>>();// 病员信息查询结果集

	private int currentID_0;// 记录滚轮1当前item values
	private int currentID_1;// 记录滚轮2当前item values
	private int currentID_2;// 记录滚轮3当前item values
	private int currentID_3;// 记录滚轮4当前item values

	private final int wheel0_mark = 0;
	private final int wheel1_mark = 1;
	private final int wheel2_mark = 2;
	private final int wheel3_mark = 3;
	public static int allPatients;

	private static final String[] order_status = new String[] { "未订餐", "已定餐", "全部" };

	public static int currentPatientID = -1;// 当前进行点餐病人在mPatientInfo_list中的id
	public static String currentBedID = "";// 当前进行点餐病人在mPatientInfo_list中的id

	private String Position_ID; // 用来显示楼层ID

	private String MemberID; // 用来显示设备ID

	public static boolean isFistEnter = true;
	private SharedPreferences sharedata;
	int mode = 0;

	private int size_all = 0;
	private int size_wei = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patients);

		try {
			init();
			sharedata = getSharedPreferences("deviceDate", 0);
			mode = sharedata.getInt("checkMode", 1);
		} catch (Exception e) {

			handler.obtainMessage(4).sendToTarget();
		}

		SharedPreferences user = getSharedPreferences("user", 0);
		MemberID = user.getString("MemberID", "");
		Position_ID = user.getString("Position_ID", "");

	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			// 判断是否初次进入程序 如果是 就展示最顶层的数据
			if (isFistEnter) {
				setPositions(null, 0);
				isFistEnter = false;
			}

			listView.setVisibility(View.GONE);

			// if (mode == 0 && PatientsListActivity.beds_list.size() > 0) {
			// mOderListAdapter.ad();
			// }
			mOderListAdapter.notifyDataSetChanged();
			listView.setVisibility(View.VISIBLE);
			updateWheel(3, currentID_3);
			// if (mode == 0) {
			// size_wei = getNewWeiPatientsInfo(bedID).size();
			// size_all = getAllPatientsInfo(bedID).size();
			// }
			new Thread(Count).start();
		} catch (Exception e) {

			handler.obtainMessage(4).sendToTarget();
		}

	}

	/**
	 * 初始化订餐界面
	 */
	private void init() {
		initTop();

		initWheel(R.id.building_num, 0);
		initWheel(R.id.floor_num, 1);
		initWheel(R.id.inpatient_area, 2);
		initWheel(R.id.order_status, 3);

		listView = (ListView) findViewById(R.id.patients_list);
		nobed = (TextView) findViewById(R.id.nobed);

		pro = (ProgressBar) findViewById(R.id.loading_progress1);

		TextView select_patient = (TextView) findViewById(R.id.select_patient);
		all_patient = (TextView) findViewById(R.id.all_patient);
		set_meal = (TextView) findViewById(R.id.set_meal);
		no_meal = (TextView) findViewById(R.id.no_meal);

		mOderListAdapter = new PatientListAdapter(PatientsListActivity.this, 1);

		listView.setAdapter(mOderListAdapter);
		beds_list.clear();
		listView.setVisibility(View.GONE);

		mOderListAdapter.notifyDataSetChanged();
		listView.setVisibility(View.VISIBLE);
		TextPaint tp = select_patient.getPaint();
		tp.setFakeBoldText(true);// 文字加粗设置
		setPositions(null, 0);

	}

	/**
	 * 初始化top按钮
	 */
	private void initTop() {
		Button back = (Button) findViewById(R.id.back);
		Button camera = (Button) findViewById(R.id.camera);

		back.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				PatientsListActivity.this.finish();

			}
		});

		camera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PatientsListActivity.this, CameraActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 初始化滚轮控件
	 * 
	 * @param id
	 *            滚轮的id
	 * @param num
	 *            标记初始化哪个滚轮
	 */
	private void initWheel(int id, int num) {
		switch (num) {
		case wheel0_mark:

			wheel_0 = getWheel(id);
			wheel_0.setVisibleItems(3);
			wheel_0.addChangingListener(changedListener);
			wheel_0.addScrollingListener(scrolledListener);
			wheel_0.setInterpolator(new AnticipateOvershootInterpolator());

			break;
		case wheel1_mark:

			wheel_1 = getWheel(id);
			wheel_1.setVisibleItems(3);
			wheel_1.addChangingListener(changedListener);
			wheel_1.addScrollingListener(scrolledListener);
			wheel_1.setInterpolator(new AnticipateOvershootInterpolator());

			break;
		case wheel2_mark:

			wheel_2 = getWheel(id);
			wheel_2.setVisibleItems(3);
			wheel_2.addChangingListener(changedListener);
			wheel_2.addScrollingListener(scrolledListener);
			wheel_2.setInterpolator(new AnticipateOvershootInterpolator());

			break;
		case wheel3_mark:

			wheel_3 = getWheel(id);
			wheel_3.setVisibleItems(3);
			wheel_3.addChangingListener(changedListener);
			wheel_3.addScrollingListener(scrolledListener);
			wheel_3.setInterpolator(new AnticipateOvershootInterpolator());

			break;

		default:
			break;
		}
	}

	/**
	 * Returns wheel by Id
	 * 
	 * @param id
	 *            the wheel Id
	 * @return the wheel with passed Id
	 */
	private WheelView getWheel(int id) {
		return (WheelView) findViewById(id);
	}

	// Wheel scrolled flag
	private boolean wheelScrolled = false;

	// Wheel scrolled listener
	OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
		public synchronized void onScrollingStarted(WheelView wheel) {
			wheelScrolled = true;
		}

		public synchronized void onScrollingFinished(WheelView wheel) {
			wheelScrolled = false;
			int id = wheel.getId();
			try {
				switch (id) {
				case R.id.building_num:

					if (!wheelScrolled) {

						updateWheel(0, currentID_0);
					}

					break;
				case R.id.floor_num:
					if (!wheelScrolled) {

						updateWheel(1, currentID_1);
					}
					break;
				case R.id.inpatient_area:
					if (!wheelScrolled) {

						updateWheel(2, currentID_2);
					}
					break;
				case R.id.order_status:
					if (!wheelScrolled) {
						updateWheel(3, currentID_3);
					}

					break;

				default:
					break;
				}
			} catch (Exception e) {
				Toast.makeText(PatientsListActivity.this, "查询失败，请重试", Toast.LENGTH_SHORT).show();
			}
		}

	};

	/**
	 * Wh
	 * 
	 * 
	 * eel changed listener
	 */
	private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
		public void onChanged(WheelView wheel, int oldValue, int newValue) {

			int id = wheel.getId();
			switch (id) {
			case R.id.building_num:

				pro.setVisibility(View.VISIBLE);

				currentID_0 = newValue;

				break;
			case R.id.floor_num:

				pro.setVisibility(View.VISIBLE);

				currentID_1 = newValue;

				break;
			case R.id.inpatient_area:

				pro.setVisibility(View.VISIBLE);

				currentID_2 = newValue;

				break;
			case R.id.order_status:

				pro.setVisibility(View.VISIBLE);

				currentID_3 = newValue;

				break;

			default:
				break;
			}

		}
	};
	/**
	 * 更新滚轮状态的handler
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);

			switch (msg.what) {
			case wheel0_mark:

				// 滚轮1数据显示操作
				if (wheelList_0 != null) {
					String[] wheelData_0 = new String[wheelList_0.size()];
					for (int i = 0; i < wheelData_0.length; i++) {
						wheelData_0[i] = (String) wheelList_0.get(i).get("Name");
					}
					wheel_0.setAdapter(new ArrayWheelAdapter<String>(wheelData_0));
				}
				break;

			case wheel1_mark:
				// 滚轮2数据显示操作
				if (wheelList_1 != null) {
					String[] wheelData_1 = new String[wheelList_1.size()];
					for (int i = 0; i < wheelData_1.length; i++) {
						wheelData_1[i] = (String) wheelList_1.get(i).get("Name");
					}
					wheel_1.setAdapter(new ArrayWheelAdapter<String>(wheelData_1));
				}
				break;

			case wheel2_mark:
				// 滚轮3数据显示操作
				if (wheelList_2 != null) {
					String[] wheelData_2 = new String[wheelList_2.size()];
					for (int i = 0; i < wheelData_2.length; i++) {
						wheelData_2[i] = (String) wheelList_2.get(i).get("Name");
					}
					wheel_2.setAdapter(new ArrayWheelAdapter<String>(wheelData_2));
					wheel_2.setCurrentItem(0);
				}
				break;

			case wheel3_mark:
				// 滚轮4数据显示操作
				wheel_3.setAdapter(new ArrayWheelAdapter<String>(order_status));
				break;

			case 4:
				try {

					// listview数据显示操作
					if (beds_list != null && beds_list.size() >= 1) {
						listView.setVisibility(View.GONE);
						mOderListAdapter.notifyDataSetChanged();
						listView.setVisibility(View.VISIBLE);
						pro.setVisibility(View.GONE);
						nobed.setVisibility(View.GONE);
					} else {

						listView.setVisibility(View.GONE);
						mOderListAdapter.notifyDataSetChanged();
						listView.setVisibility(View.VISIBLE);
						pro.setVisibility(View.GONE);
						nobed.setVisibility(View.VISIBLE);

					}

				} catch (Exception e) {

					listView.setVisibility(View.GONE);
					mOderListAdapter.notifyDataSetChanged();
					listView.setVisibility(View.VISIBLE);
				}
				break;

			case 5:

				all_patient.setText(msg.arg1 + "人");
				set_meal.setText(msg.arg2 + "人");
				no_meal.setText((msg.arg1 - msg.arg2) + "人");

				break;
			case 6:
				all_patient.setText(msg.arg1 + "人");
				no_meal.setText(msg.arg2 + "人");
				set_meal.setText((msg.arg1 - msg.arg2) + "人");
				break;
			default:
				break;
			}
		}
	};
	/**
	 * 用于计算全部病人，已定餐，未订餐的病人的数量
	 */
	Runnable Count = new Runnable() {

		@Override
		public void run() {

			Map<String, Object> allCount = Db.selectUnique("select count(id)num from patient where patpositionid in (select id from position where parentid in ("
					+ "select position_id from PositionMemberInfo where memberinfo_memberid=? )) and (OutHospitalTime >= ? or OutHospitalTime is null)", MemberID,
					ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));

			Map<String, Object> YCount = Db
					.selectUnique(
							"select count ((select max(Order_id) order_id from OrderDetail a inner join [Order] c on a.order_id=c.id where a.TakeFoodTime >= ? and c.PatPositionID=b.PatPositionID)) num  from  Patient b  where b.PatPositionID in (select id from position b where parentid in (select position_id from PositionMemberInfo where memberinfo_memberid=? )) and (OutHospitalTime >= ? or OutHospitalTime is null)",
							ConvertUtils.date2Str("yyyy-MM-dd"), MemberID, ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));
			if (mode == 0) {
				if (size_all > 0) {
					handler.obtainMessage(6, size_all, size_wei).sendToTarget();
				}
			} else {
				handler.obtainMessage(5, Integer.parseInt(allCount.get("num") + ""), Integer.parseInt(YCount.get("num") + "")).sendToTarget();
			}

		}
	};

	/**
	 * 滚轮滚动更新信息方法
	 * 
	 * @author Administrator
	 * @return
	 * 
	 */

	private void updateWheel(int id, int selfItemNum) {

		switch (id) {
		case wheel0_mark:
			setPositions(wheelList_0.get(selfItemNum).get("ID") + "", 1);

			// 这个地方绑定到它邮右边的滚轮
			break;

		case wheel1_mark:
			setPositions(wheelList_1.get(selfItemNum).get("ID") + "", 2);

			// 这个地方绑定到它邮右边的滚轮
			break;

		case wheel2_mark: // 假设是病区

			setPositions(wheelList_2.get(selfItemNum).get("ID") + "", 3);

			// 把获取到的数据绑定到下面
			break;

		case wheel3_mark:
			setPositions(null, 4);

			break;

		default:
			break;
		}
	};

	/**
	 * 获取全部病人信息
	 * 
	 * @param bedID
	 * @return 全部病员信息List
	 */
	private List<Map<String, Object>> getAllPatientsInfo(List<Map<String, Object>> bedID) {
		List<Map<String, Object>> bedList = new ArrayList<Map<String, Object>>();

		if (bedID != null) {
			if (beds_list != null) {
				beds_list.clear();
				// handler.obtainMessage(4).sendToTarget();
			}
			for (int i = 0; i < bedID.size(); i++) {
				Map<String, Object> map = Db
						.selectUnique(
								"select d.[Name],b.*,(select max(Order_id) order_id from OrderDetail a inner join [Order] c on a.order_id=c.id where a.TakeFoodTime > ? and c.PatPositionID=d.id) order_id from position d left join Patient b on d.[ID]=b.PatPositionID where d.[ID]=? and (OutHospitalTime >= ? or OutHospitalTime is null)",
								ConvertUtils.date2Str("yyyy-MM-dd"), bedID.get(i).get("ID"), ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));
				if (map != null) {
					// TODO 7-29:床位有病人，查病人医嘱
					if (map.get("ID") != null) {
						List<Map<String, Object>> list_2 = Db.select(
								"select a.Advice,a.ID ,a.AllowOrderFood from FoodAdvice a left join PatientFoodAdvice b on b.FoodAdvice_ID=a.ID where b.Patient_ID=? ORDER BY a.DefaultFoodID ASC",
								map.get("ID"));
						StringBuffer Advice = new StringBuffer();
						if (list_2 != null) {

							for (int j = 0; j < list_2.size(); j++) {

								Advice.append(list_2.get(j).get("Advice") + "，");

								if ((list_2.get(j).get("AllowOrderFood") + "").equals("False")) {
									map.put("AllowOrderFood", "False");
								}
							}
							map.put("Advice", Advice + "");
						}
					} else {
						// TODO 7-28:没有病人,如果已点餐，则查询订单里面的医嘱
						if (map.get("order_id") != null) {
							Map<String, Object> adviceIds = Db.selectUnique("select FoodAdviceIDList from [order] where id=?", map.get("order_id"));
							String[] adviceArr;
							if (adviceIds != null) {
								String adviceStr = "";
								String str = adviceIds.get("FoodAdviceIDList").toString();
								if (!str.equals("")) {
									str = str.substring(0, str.length() - 1);
									adviceArr = str.split(",");
									if (adviceArr != null && adviceArr.length > 0) {
										for (int k = 0; k < adviceArr.length; k++) {
											if (k != adviceArr.length - 1) {
												adviceStr += "'" + adviceArr[k] + "'" + ",";
											} else {
												adviceStr += "'" + adviceArr[k] + "'";
											}
										}
									}
								}
								if (!adviceStr.equals("")) {
									List<Map<String, Object>> adviceIDList = Db.select("select a.Advice,a.ID ,a.AllowOrderFood  from FoodAdvice A WHERE ID IN (" + adviceStr + ")");
									StringBuffer Advice = new StringBuffer();
									if (adviceIDList != null) {

										for (int j = 0; j < adviceIDList.size(); j++) {

											Advice.append(adviceIDList.get(j).get("Advice") + "，");
											if ((adviceIDList.get(j).get("AllowOrderFood") + "").equals("False")) {
												map.put("AllowOrderFood", "False");
											}

										}
										map.put("Advice", Advice.toString());
									}
								}
							}
						}
					}
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				} else {
					map = new HashMap<String, Object>();
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				}
				bedList.add(map);
			}
		}
		return bedList;

	}

	// TODO

	private List<Map<String, Object>> getNewWeiPatientsInfo(List<Map<String, Object>> bedID) {
		List<Map<String, Object>> bedList = new ArrayList<Map<String, Object>>();

		if (bedID != null) {
			if (beds_list != null) {
				beds_list.clear();
				// handler.obtainMessage(4).sendToTarget();
			}

			for (int i = 0; i < bedID.size(); i++) {
				Map<String, Object> map = Db
						.selectUnique(
								"select b.*,(select max(Order_id) order_id from OrderDetail a inner join [Order] c on a.order_id=c.id where a.TakeFoodTime > ? and c.PatPositionID=b.PatPositionID) order_id from  Patient b  where b.PatPositionID=? and (OutHospitalTime >= ? or OutHospitalTime is null)",
								ConvertUtils.date2Str("yyyy-MM-dd"), bedID.get(i).get("ID"), ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));

				// Map<String, Object> map = Db
				// .selectUnique(
				// "select d.[Name],b.*,(select max(Order_id) order_id from
				// OrderDetail a inner join [Order] c on a.order_id=c.id where
				// a.TakeFoodTime >= ? and c.PatPositionID=d.id) order_id from
				// position d left join Patient b on d.[ID]=b.PatPositionID
				// where d.[ID]=? and (OutHospitalTime >= ? or OutHospitalTime
				// is null)",
				// ConvertUtils.date2Str("yyyy-MM-dd"),
				// bedID.get(i).get("ID"),
				// ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));
				if (map != null && map.get("order_id") != null) {
					continue;
				}
				if (map != null) {
					List<Map<String, Object>> list_2 = Db.select(
							"select a.Advice,a.ID ,a.AllowOrderFood from FoodAdvice a left join PatientFoodAdvice b on b.FoodAdvice_ID=a.ID where b.Patient_ID=? ORDER BY a.DefaultFoodID ASC",
							map.get("ID"));

					StringBuffer Advice = new StringBuffer();
					if (list_2 != null) {

						for (int j = 0; j < list_2.size(); j++) {

							Advice.append(list_2.get(j).get("Advice") + "，");

							if ((list_2.get(j).get("AllowOrderFood") + "").equals("False")) {
								map.put("AllowOrderFood", "False");
							}
						}

						map.put("Advice", Advice + "");

					}
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				} else {
					map = new HashMap<String, Object>();
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				}
				bedList.add(map);
				// if (mode == 0) {
				// if ((map.get("order_id") == null)) {
				// bedList.add(map);
				// }
				// } else {
				// bedList.add(map);
				// }
			}
		}

		// if (mode == 0) {
		// for (int i = 0; i < bedList.size(); i++) {
		//
		// if (!(bedList.get(i).get("order_id") == null)) {
		// bedList.remove(i);
		// }
		//
		// }
		// }
		return bedList;

	}

	private List<Map<String, Object>> getNewYiPatientsInfo(List<Map<String, Object>> bedID) {
		List<Map<String, Object>> bedList = new ArrayList<Map<String, Object>>();

		if (bedID != null) {
			if (beds_list != null) {
				beds_list.clear();
				// handler.obtainMessage(4).sendToTarget();
			}

			for (int i = 0; i < bedID.size(); i++) {
				// Map<String, Object> map = Db
				// .selectUnique(
				// "select b.*,(select max(Order_id) order_id from OrderDetail a
				// inner join [Order] c on a.order_id=c.id where a.TakeFoodTime
				// > ? and c.PatPositionID=b.PatPositionID) order_id from
				// Patient b where b.PatPositionID=? and (OutHospitalTime >= ?
				// or OutHospitalTime is null)",
				// ConvertUtils.date2Str("yyyy-MM-dd"),
				// bedID.get(i).get("ID"),
				// ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));
				Map<String, Object> map = Db.selectUnique("select d.[Name] ,b.*,"
						+ " (select max(Order_id) order_id from OrderDetail a inner join [Order] c on a.order_id=c.id where a.TakeFoodTime > ? and c.PatPositionID=d.id) order_id "
						+ " from position d " + " left join Patient b on d.[ID]=b.PatPositionID where b.PatPositionID=? and (OutHospitalTime >= ? or OutHospitalTime is null)",
						ConvertUtils.date2Str("yyyy-MM-dd"), bedID.get(i).get("ID"), ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));
				Log.e("XX@#", bedID.get(i).get("ID") + ": " + map);
				if (map != null) {

					List<Map<String, Object>> list_2 = Db.select(
							"select a.Advice,a.ID ,a.AllowOrderFood from FoodAdvice a left join PatientFoodAdvice b on b.FoodAdvice_ID=a.ID where b.Patient_ID=? ORDER BY a.DefaultFoodID ASC",
							map.get("ID"));

					StringBuffer Advice = new StringBuffer();
					if (list_2 != null) {

						for (int j = 0; j < list_2.size(); j++) {

							Advice.append(list_2.get(j).get("Advice") + "，");

							if ((list_2.get(j).get("AllowOrderFood") + "").equals("False")) {
								map.put("AllowOrderFood", "False");
							}
						}

						map.put("Advice", Advice + "");

					}
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				} else {
					map = new HashMap<String, Object>();
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				}
				if (mode == 0) {
					if (!(map.get("order_id") == null)) {
						bedList.add(map);
					}
				} else {
					bedList.add(map);
				}

			}
		}

		// if (mode == 0) {
		// for (int i = 0; i < bedList.size(); i++) {
		//
		// if (!(bedList.get(i).get("order_id") == null)) {
		// bedList.remove(i);
		// }
		//
		// }

		// }
		return bedList;

	}

	/**
	 * 获取未点餐病人信息
	 * 
	 * @param bedID
	 * @return 未点餐病员信息List
	 */
	private List<Map<String, Object>> getWeiPatientsInfo(List<Map<String, Object>> bedID) {
		List<Map<String, Object>> WPatientList = new ArrayList<Map<String, Object>>();

		if (bedID != null) {

			if (beds_list != null) {
				beds_list.clear();
				// handler.obtainMessage(4).sendToTarget();
			}

			for (int i = 0; i < bedID.size(); i++) {
				// Map<String, Object> map = Db
				// .selectUnique(
				// "select b.*,(select max(Order_id) order_id from OrderDetail a
				// inner join [Order] c on a.order_id=c.id where a.TakeFoodTime
				// > ? and c.PatPositionID=b.PatPositionID) order_id from
				// Patient b where b.PatPositionID=? and (OutHospitalTime >= ?
				// or OutHospitalTime is null)",
				// ConvertUtils.date2Str("yyyy-MM-dd"),
				// bedID.get(i).get("ID"),
				// ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));

				Map<String, Object> map = Db
						.selectUnique(
								"select d.[Name],b.*,(select max(Order_id) order_id from OrderDetail a "
										+ " inner join [Order] c on a.order_id=c.id where a.TakeFoodTime > ? and c.PatPositionID=d.id) order_id from position d left join Patient b on d.[ID]=b.PatPositionID where d.[ID]=? and (OutHospitalTime >= ? or OutHospitalTime is null)",
								ConvertUtils.date2Str("yyyy-MM-dd"), bedID.get(i).get("ID"), ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));
				// if (map == null || map.get("order_id") != null) {
				// continue;
				// }
				if (map != null && map.get("order_id") != null) {
					continue;
				}
				if (map != null) {

					List<Map<String, Object>> adviceIDList = Db.select(
							"select a.Advice,a.ID ,a.AllowOrderFood from FoodAdvice a left join PatientFoodAdvice b on b.FoodAdvice_ID=a.ID where b.Patient_ID=? ORDER BY a.DefaultFoodID ASC",
							map.get("ID"));

					StringBuffer Advice = new StringBuffer();
					if (adviceIDList != null) {

						for (int j = 0; j < adviceIDList.size(); j++) {

							Advice.append(adviceIDList.get(j).get("Advice") + "，");
							if ((adviceIDList.get(j).get("AllowOrderFood") + "").equals("False")) {
								map.put("AllowOrderFood", "False");
							}

						}
						map.put("Advice", Advice.toString());

					}
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				} else {
					map = new HashMap<String, Object>();
					map.put("bedNum", bedID.get(i).get("Name"));
					map.put("PatPositionID", bedID.get(i).get("ID"));
				}

				WPatientList.add(map);
			}

		}
		return WPatientList;

	}

	/**
	 * 获取已点餐病人信息
	 * 
	 * @param bedID
	 * @return 已点餐病人信息List
	 */
	private List<Map<String, Object>> getYiPatientsInfo(List<Map<String, Object>> bedID) {
		List<Map<String, Object>> YPatientList = new ArrayList<Map<String, Object>>();
		if (bedID != null) {

			if (beds_list != null) {
				beds_list.clear();
				// handler.obtainMessage(4).sendToTarget();
			}

			for (int i = 0; i < bedID.size(); i++) {
				// TODO 08-06 解决了不能显示今天的点餐,修改： TakeFoodTime >=今天的日期
				Map<String, Object> YpatientMap = Db
						.selectUnique(
								"select d.[Name],b.*,(select max(Order_id) order_id from OrderDetail a inner join [Order] c on a.order_id=c.id where a.TakeFoodTime > ? and c.PatPositionID=d.id) order_id from position d left join Patient b on d.[ID]=b.PatPositionID where d.[ID]=? and (OutHospitalTime >= ? or OutHospitalTime is null)",
								ConvertUtils.date2Str("yyyy-MM-dd"), bedID.get(i).get("ID"), ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));
				if (YpatientMap != null && YpatientMap.get("order_id") != null) {
					// 判断床位是否有病人
					if (YpatientMap.get("ID") != null) {
						// TODO 7-28
						List<Map<String, Object>> adviceIDList = Db.select(
								"select a.Advice,a.ID ,a.AllowOrderFood  from FoodAdvice a left join PatientFoodAdvice b on b.FoodAdvice_ID=a.ID where b.Patient_ID=?", YpatientMap.get("ID"));

						StringBuffer Advice = new StringBuffer();
						if (adviceIDList != null) {
							for (int j = 0; j < adviceIDList.size(); j++) {

								Advice.append(adviceIDList.get(j).get("Advice") + "，");
								if ((adviceIDList.get(j).get("AllowOrderFood") + "").equals("False")) {
									YpatientMap.put("AllowOrderFood", "False");
								}

							}
							YpatientMap.put("Advice", Advice.toString());
						}
					}
					// 根据订单查询
					else {
						Map<String, Object> adviceIds = Db.selectUnique("select FoodAdviceIDList from [order] where id=?", YpatientMap.get("order_id"));
						String[] adviceArr;
						if (adviceIds != null) {
							String adviceStr = "";
							String str = adviceIds.get("FoodAdviceIDList").toString();
							if (!str.equals("")) {
								adviceArr = str.split(",");
								if (adviceArr != null && adviceArr.length > 0) {
									for (int k = 0; k < adviceArr.length; k++) {
										if (k != adviceArr.length - 1) {
											adviceStr += "'" + adviceArr[k] + "'" + ",";
										} else {
											adviceStr += "'" + adviceArr[k] + "'";
										}
									}
								}
							}
							if (!adviceStr.equals("")) {
								List<Map<String, Object>> adviceIDList = Db.select("select a.Advice,a.ID ,a.AllowOrderFood  from FoodAdvice A WHERE ID IN (" + adviceStr + ")");
								StringBuffer Advice = new StringBuffer();
								if (adviceIDList != null) {
									for (int j = 0; j < adviceIDList.size(); j++) {
										Advice.append(adviceIDList.get(j).get("Advice") + "，");
										if ((adviceIDList.get(j).get("AllowOrderFood") + "").equals("False")) {
											YpatientMap.put("AllowOrderFood", "False");
										}

									}
									YpatientMap.put("Advice", Advice.toString());
								}

							} else {
							}
						}
					}
					YpatientMap.put("bedNum", bedID.get(i).get("Name"));
					YpatientMap.put("PatPositionID", bedID.get(i).get("ID"));
					YPatientList.add(YpatientMap);
				}
			}
		}

		return YPatientList;
	}

	/**
	 * 显示病员数据
	 * 
	 * @param parentId
	 *            滚轮的父级ID
	 * @param deep
	 *            组件ID
	 */
	private synchronized void setPositions(String parentId, int deep) {
		// Log.e(this, "parent_id = " + parentId + "/deep = " + deep);
		switch (deep) {
		case 0:
			wheelList_0 = Db.select(
					"select ID,HisCode,PID,ParentID ,Name ,InnerCode ,Deep ,TypeName ,CheckCode from position where id in ( select  substr(innercode,1,32)  from position where id in ( "
							+ " select position_id from PositionMemberInfo where memberinfo_memberid=?) and deep=3) order by pid  ", MemberID);
			handler.obtainMessage(0).sendToTarget();

		case 1:

			if (wheelList_0 != null && wheelList_0.size() >= 1) {
				int curr = currentID_0 >= wheelList_0.size() ? 0 : currentID_0;
				String _parentId = parentId;
				if (!StringUtils.hasLength(_parentId)) {
					_parentId = (String) wheelList_0.get(curr).get("ID");
				}

				if (deep != 1) {
					_parentId = (String) wheelList_0.get(curr).get("ID");
				}

				wheelList_1 = Db.select(
						"select ID,HisCode,PID,ParentID ,Name ,InnerCode ,Deep ,TypeName ,CheckCode  from Position where ParentID = ? and id in (select  substr(innercode,33,32)  from position where id in ( "
								+ " select position_id from PositionMemberInfo where memberinfo_memberid=?) and deep=3) order by pid  ", _parentId, MemberID);
				handler.obtainMessage(1).sendToTarget();
			}

		case 2:

			if (wheelList_1 != null && wheelList_1.size() >= 1) {

				int curr = currentID_1 >= wheelList_1.size() ? 0 : currentID_1;

				String _parentId = parentId;
				if (!StringUtils.hasLength(_parentId)) {
					_parentId = (String) wheelList_1.get(curr).get("ID");
				}
				if (deep != 2) {
					_parentId = (String) wheelList_1.get(curr).get("ID");
				}
				wheelList_2 = Db
						.select("select ID,HisCode,PID,ParentID ,Name ,InnerCode ,Deep ,TypeName ,CheckCode  from Position where ParentID = ? and id in (select position_id from PositionMemberInfo where memberinfo_memberid=?) order by pid ",
								_parentId, MemberID);
				handler.obtainMessage(2).sendToTarget();
			}
		case 3:

			if (wheelList_2 != null && wheelList_2.size() >= 1) {
				int curr = currentID_2 >= wheelList_2.size() ? 0 : currentID_2;
				String _parentId = parentId;
				if (!StringUtils.hasLength(_parentId)) {
					_parentId = (String) wheelList_2.get(curr).get("ID");
				}
				if (deep != 3) {
					_parentId = (String) wheelList_2.get(curr).get("ID");
				}
				bedID = Db.select("select ID,HisCode,PID,ParentID ,Name ,InnerCode ,Deep ,TypeName ,CheckCode from Position where ParentID = ? order by name", _parentId);

				if (mode == 0) {
					size_wei = getNewWeiPatientsInfo(bedID).size();
					size_all = getAllPatientsInfo(bedID).size();
				}
				handler.obtainMessage(3).sendToTarget();
			}

		case 4:
			// TODO
			switch (currentID_3) {
			case 0:
				// 未订餐
				// if (mode == 0) {
				// beds_list = getNewWeiPatientsInfo(bedID);
				// } else {
				// beds_list = getWeiPatientsInfo(bedID);
				// }
				beds_list = getWeiPatientsInfo(bedID);
				// TODO 7-22:新增，用于更新统计点餐人数
				size_wei = beds_list.size();
				handler.obtainMessage(6, size_all, size_wei).sendToTarget();
				break;
			case 1:
				// 已点餐
				beds_list = getYiPatientsInfo(bedID);
				size_wei = size_all - beds_list.size();
				handler.obtainMessage(6, size_all, size_wei).sendToTarget();
				break;
			case 2:
				// 全部病人
				beds_list = getAllPatientsInfo(bedID);
				size_all = beds_list.size();
				handler.obtainMessage(6, size_all, size_wei).sendToTarget();
				break;
			}

			handler.obtainMessage(4).sendToTarget();

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			beds_list.clear();
			handler.obtainMessage(4).sendToTarget();
			PatientsListActivity.this.finish();
			overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);

			return true;

		}

		return false;
	}

	@Override
	protected void onPause() {

		super.onPause();

		handler.obtainMessage(4).sendToTarget();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

	}

}