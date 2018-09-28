package com.janlent.sodexo.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.adapter.LeftmenuAdapter;
import com.janlent.sodexo.adapter.OrderGVCanCiAdapter;
import com.janlent.sodexo.adapter.OrderGVCanLeiAdapter;
import com.janlent.sodexo.adapter.OrderLVAdapter;
import com.janlent.sodexo.view.HorizontalListView;
import com.janlent.sodexo.view.HorizontalListView.btnListener;
import com.janlent.sodexo.widget.MyGridView;
import com.janlent.utils.CollectionUtils;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.DateUtils;
import com.janlent.utils.Db;
import com.janlent.utils.Log;
import com.jenlant.sodexo.R;

/**
 * 点餐界面
 * 
 * @author Administrator
 * 
 */
public class OrderActivity extends BaseActivity implements OnClickListener {

	private Button confirm; // 确定
	private TextView select_patient; // 显示所选病员
	private TextView bed_num; // 显示床位号
	private TextView bed_patient_num; // 显示床位上病员号
	private TextView bed_patient_name; // 显示床位上病员名
	private TextView advice; // 显示医嘱
	private TextView nomeal; // 显示没有餐
	private Button pro_day; // 用来选择上一天
	private Button next_day; // 用来选择下一天
	private TextView order_day; // 显示订单日期
	private TextView patient_expect_outHospital_time;// 显示病员预出院时间;

	private MyGridView mMyGridView1;
	private MyGridView mMyGridView2;
	public static OrderGVCanCiAdapter mOrderGVAdapter_canci;
	public static OrderGVCanLeiAdapter mOrderGVAdapter_canlei;

	private GridView mGridView;
	public static OrderLVAdapter mOrderLVAdapter;

	private String minTime;// 用来显示用的日期最小值
	private String orderTime;// 用来显示用的日期当天点餐值
	private String minTime_a;// 用来查询用的日期最小值
	private String orderTime_a;// 用来查询用的日期当天点餐值
	public static String currentOrderDate;// 用来查询用的点餐值日期
	public static String currentOrderDate_a;// 用来查询用的点餐值日期界限比currentOrderDate大24小时

	public static Map<String, Object> hashOrderDay = new HashMap<String, Object>();// 记录已定餐日期

	private List<Map<String, Object>> list_canci;// 餐次集合
	private List<Map<String, Object>> list_canlei;// 餐类集合
	private List<Map<String, Object>> list_menu = new ArrayList<Map<String, Object>>();// 菜单集合
	public static List<Map<String, Object>> foodsList = new ArrayList<Map<String, Object>>();// 套餐集合
	public static String IsCombination = "True";// 用来区别是套餐还是小锅菜

	public static List<Map<String, Object>> adviceIDList;// 记录病人医嘱的list集合

	public static int PID_A = 00001;// 订单编号后缀

	private String tag;// 用来标记intent是否是修改记录跳转过来的

	public static StringBuffer FoodAdviceIDList = new StringBuffer();

	public static boolean isInitData = true;// 用来判断是否从新加载菜单数据

	private SharedPreferences sharedata;
	int mode = 0;

	private PopupWindow advicePopup; // 修改医嘱
	private StringBuffer text = new StringBuffer(); // 用来保存医嘱信息
	private ImageView iv = null;
	// TODO 7-25,修改，不要使用静态
	public List<String> advice_ID_list = new ArrayList<String>();// 已有医嘱ID
	private List<Map<String, Object>> advice_list; // 所有医嘱
	public boolean adviceIsFirstTouch = true; // 用来区分数据是否已经初始化
	private Map<String, Object> advice_map = new HashMap<String, Object>();// 所有医嘱advice，以id为key值

	public static String select_Advice = ""; // 记录最后的医嘱，用于传到其他界面

	private ListView leftMenu;
	private LeftmenuAdapter adapter;
	private List<String> menuList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);

		initTop();
		initGV();
		initTitle();
		initListView();
		initPtInfo();
		handler.obtainMessage(1).sendToTarget();
		handler.obtainMessage(2).sendToTarget();
		handler.obtainMessage(5).sendToTarget();

		Intent intent = getIntent();
		tag = intent.getStringExtra("tag");
		sharedata = getSharedPreferences("deviceDate", 0);
		mode = sharedata.getInt("checkMode", 1);
		Log.i("mode ===== " + mode);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO 08-06:数组越界
		if (PatientsListActivity.beds_list.size() > 0) {
			if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("bedNum") != null && PatientsListActivity.beds_list.size() > PatientsListActivity.currentPatientID) {
				bed_num.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("bedNum") + "");
			}
			// 显示病人住院号
			if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PID") != null) {
				bed_patient_num.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PID") + "");
			}

			// 显示病人名称
			if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientName") != null) {
				bed_patient_name.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatientName") + "");
			}
			// 显示病人预出院时间
			System.out.println(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID));
			if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ExpectOutHospitalTime") != null) {
				patient_expect_outHospital_time.setText(PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ExpectOutHospitalTime") + "");
			}
			// TODO 7:28如果是修改订单，没有病人的床位可能有医嘱
			if (tag.equals("modify")) {
				if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("Advice") != null) {
					String advice_a = PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("Advice") + "";
					if (!advice_a.equals("")) {
						advice.setText(advice_a.substring(0, advice_a.length() - 1));
					} else {
						advice.setText("");
					}
				}
			} else {
				// 如果是下订单
				// TODO 7-25:有病人则查询数据库
				// TODO 2016.09.27 为病人遗嘱添加 排序（）
				if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID") != null) {
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
					advice.setText(adviceSb.toString());
				} else {
					if (!select_Advice.equals("")) {
						advice.setText(select_Advice);
					} else {
						// advice.setText("普食");
					}
				}
			}
			// TODO 查询所有医嘱
			advice_list = Db.select("SELECT A.ID,A.Advice,A.DefaultFoodID FROM FoodAdvice A ORDER BY DefaultFoodID");

			if (advice_list != null) {
				for (int i = 0; i < advice_list.size(); i++) {
					advice_map.put(advice_list.get(i).get("ID") + "", advice_list.get(i).get("Advice"));
					// 2016.09.27 添加； 为每个医嘱添加排序号
					advice_map.put(advice_list.get(i).get("ID") + "_s", advice_list.get(i).get("DefaultFoodID"));
				}
			}
			adviceIsFirstTouch = true;
			// 修改医嘱
			advice.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 7-25:不是修改订单
					if (!"modify".equals(tag)) {
						Map<String, Object> map = Db.selectUnique("SELECT Nums FROM NumsMark WHERE TakeFoodTime = ? AND PatPositionID =?", currentOrderDate,
								PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatPositionID"));
						// TODO 7-25:没有点过菜，可修改医嘱
						if (map == null || Integer.parseInt(map.get("Nums") + "") <= 0) {
							showAdvicePopup();
						}
					}
				}
			});
			if (isInitData) {
				// TODO 7-22:getSharedPreferences("deviceDate",
				// 0).getString("generFood")改成“普食”
				// TODO 2016.08.19 将默认普食取消！
				// Map<String, Object> generalFood = Db.selectUnique("select id
				// FoodAdvice_ID from FoodAdvice where Advice=?", "普食");
				String currentPatientID = (String) PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID");
				if (currentPatientID == null) { // 默认普食

					if (tag.equals("modify")) {// 修改
						adviceIDList = new ArrayList<Map<String, Object>>();
						Map<String, Object> YpatientMap = Db
								.selectUnique(
										"select d.[Name],b.*,(select max(Order_id) order_id from OrderDetail a inner join [Order] c on a.order_id=c.id where a.TakeFoodTime >= ? and c.PatPositionID=d.id) order_id from position d left join Patient b on d.[ID]=b.PatPositionID where d.[ID]=? and (OutHospitalTime >= ? or OutHospitalTime is null)",
										ConvertUtils.date2Str("yyyy-MM-dd"), PatientsListActivity.currentBedID, ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"));

						if (YpatientMap != null && YpatientMap.get("order_id") != null) {
							Map<String, Object> adviceIds = Db.selectUnique("select FoodAdviceIDList from [order] where id=?", YpatientMap.get("order_id"));
							// TODO 7-30:查询到的是以,隔开的，循环添加
							String FoodAdviceIDList = adviceIds.get("FoodAdviceIDList") + "";
							String[] list = FoodAdviceIDList.split(",");
							Map<String, Object> map;
							for (int i = 0; i < list.length; i++) {
								map = new HashMap<String, Object>();
								map.put("FoodAdvice_ID", list[i]);
								adviceIDList.add(map);
							}
						} else {
							// TODO 2016.08.19 将默认普食取消！
							// adviceIDList.add(generalFood);
						}
					} else {
						if (adviceIDList == null) {
							adviceIDList = new ArrayList<Map<String, Object>>();
							// TODO 2016.08.19 将默认普食取消！
							// adviceIDList.add(generalFood);
						} else {
						}
					}
				} else {
					adviceIDList = Db.select("select FoodAdvice_ID from PatientFoodAdvice where Patient_ID = ?", currentPatientID);
				}
				FoodAdviceIDList.delete(0, FoodAdviceIDList.length());
				if (adviceIDList != null) {
					for (int i = 0; i < adviceIDList.size(); i++) {
						FoodAdviceIDList.append(adviceIDList.get(i).get("FoodAdvice_ID") + ",");
					}

					if (adviceIDList.size() > 1) {
						OrderGVCanLeiAdapter.current_canleiID = "xiaoguocai";
						Map<String, Object> map = Db.selectUnique("select id from FoodAdvice where Advice=?", "小锅菜");
						if (map != null) {
							OrderGVCanLeiAdapter.current_FoodAdviceIDList = (String) map.get("ID") + ",";
						}
					} else {
						// TODO 7-30
						OrderGVCanLeiAdapter.current_canleiID = "taochan";
						// OrderGVCanLeiAdapter.current_FoodAdviceIDList =
						// (String)generalFood.get("FoodAdvice_ID") + ",";
						// TODO 7-27修改菜谱医嘱为病人医嘱不对
						OrderGVCanLeiAdapter.current_FoodAdviceIDList = FoodAdviceIDList.toString();
					}
				} else {
					OrderGVCanLeiAdapter.current_canleiID = "xiaoguocai";
					// TODO 7-27没有病人，菜谱医嘱默认普食
					// TODO 2016.08.19 将默认普食取消！
					// OrderGVCanLeiAdapter.current_FoodAdviceIDList = (String)
					// generalFood.get("FoodAdvice_ID") + ",";
				}
				handler.obtainMessage(1).sendToTarget();
				handler.obtainMessage(2).sendToTarget();
				handler.obtainMessage(3).sendToTarget();
			}
			isInitData = true;
			if (mode == 0 && FoodAdviceIDList.length() < 1) {

			}
			select_Advice = advice.getText().toString().trim();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (advicePopup != null && advicePopup.isShowing()) {
			advicePopup.dismiss();
		}
		advicePopup = null;
	}

	// TODO 修改医嘱后更新菜谱
	public void updateFoodByAdvice() {

		Map<String, Object> generalFood = Db.selectUnique("select id FoodAdvice_ID from FoodAdvice where Advice=?", "普食");
		if (adviceIDList != null) {
			adviceIDList.clear();
		}
		adviceIDList = null;
		Map<String, Object> advice_Object;
		if (advice_ID_list.size() >= 0) {
			adviceIDList = new ArrayList<Map<String, Object>>();
		}
		for (int i = 0; i < advice_ID_list.size(); i++) {
			advice_Object = new HashMap<String, Object>();
			advice_Object.put("FoodAdvice_ID", advice_ID_list.get(i));
			adviceIDList.add(i, advice_Object);
		}

		FoodAdviceIDList.delete(0, FoodAdviceIDList.length());
		// TODO 7-25:修改病人医嘱
		if (adviceIDList != null) {
			for (int i = 0; i < adviceIDList.size(); i++) {
				FoodAdviceIDList.append(adviceIDList.get(i).get("FoodAdvice_ID") + ",");
			}
			if (adviceIDList.size() > 1) {
				OrderGVCanLeiAdapter.current_canleiID = "xiaoguocai";
				Map<String, Object> map = Db.selectUnique("select id from FoodAdvice where Advice=?", "小锅菜");
				if (map != null) {
					OrderGVCanLeiAdapter.current_FoodAdviceIDList = (String) map.get("ID") + ",";
				}
			} else {
				OrderGVCanLeiAdapter.current_canleiID = "taochan";
				OrderGVCanLeiAdapter.current_FoodAdviceIDList = FoodAdviceIDList + "";
				// OrderGVCanLeiAdapter.current_FoodAdviceIDList =
				// (String)generalFood.get("FoodAdvice_ID") + ",";
			}
		} else {
			OrderGVCanLeiAdapter.current_canleiID = "xiaoguocai";
			OrderGVCanLeiAdapter.current_FoodAdviceIDList = (String) generalFood.get("FoodAdvice_ID") + ",";
		}
		handler.obtainMessage(1).sendToTarget();
		handler.obtainMessage(2).sendToTarget();
		handler.obtainMessage(3).sendToTarget();
		// 如果床位有病人，修改病人医嘱
		// 更新病人医嘱信息
		int b = 0;
		int c = 0;
		if (PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID") != null) {
			// TODO 7-28修改病人更新时间
			int a = Db.update("UPDATE Patient SET LastUpdateTime=? WHERE PatPositionID = ?", ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"), PatientsListActivity.currentBedID);
			b = Db.delete("DELETE  FROM PatientFoodAdvice WHERE  Patient_ID= ?", PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"));
			for (int i = 0; i < advice_ID_list.size(); i++) {
				c = Db.insert("INSERT INTO PatientFoodAdvice (Patient_ID,FoodAdvice_ID) VALUES(?,?)", PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID"),
						advice_ID_list.get(i));
			}
		}

	}

	/**
	 * TODO 7-24: 显示医嘱信息的pupopwindow窗口
	 */
	private void showAdvicePopup() {
		// 初始化为查询的医嘱，没有病人就是普食
		List<Map<String, Object>> advice_list_a = adviceIDList;
		if (advicePopup == null) {
			View myView = OrderActivity.this.getLayoutInflater().inflate(R.layout.advice_pop, null);

			LinearLayout body = (LinearLayout) myView.findViewById(R.id.advice_body);

			if (!CollectionUtils.isEmpty(advice_list)) {

				for (int i = 0; i < advice_list.size(); i++) {

					View advice_item = OrderActivity.this.getLayoutInflater().inflate(R.layout.advice_pop_item, null);

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
			advicePopup = new PopupWindow(myView, 400, 850);
			myView.setBackgroundColor(Color.WHITE);
			myView.setBackgroundResource(R.drawable.pop_download_bg);

			advicePopup.setFocusable(false);

			advicePopup.setTouchable(true);
			advicePopup.setOutsideTouchable(true);
			advicePopup.setBackgroundDrawable(new ColorDrawable(00000000));
		}
		if (!advicePopup.isShowing()) {
			advicePopup.showAsDropDown(advice);
		}
	}

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
				advice_ID_list.clear();
				adviceIsFirstTouch = false;
			}

		}

		@Override
		public void onClick(View v) {

			if (advice_ID_list.contains(v.getTag())) {// 判断是否包含在已有医嘱中，有删除

				advice_ID_list.remove(v.getTag().toString());
				text.delete(0, text.length());

				// 重置stringbuffer
				for (int i = 0; i < advice_ID_list.size(); i++) {
					text.append(advice_map.get(advice_ID_list.get(i)) + "，");
				}
				// 删除后显示
				if (text.toString().length() > 2) {
					advice.setText("");
					advice.setText(text.toString().substring(0, text.toString().length() - 1));
				} else {
					advice.setText("");
				}
				updateFoodByAdvice();
				adviceText.setTextColor(Color.BLACK);
				v.setBackgroundColor(Color.alpha(Color.TRANSPARENT));
				iv.setBackgroundResource(R.drawable.icon_q01);

			} else { // 没有则添加
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
					advice.setText("");
					advice.setText(text.toString().substring(0, text.toString().length() - 1));
				} else {
					advice.setText("");
				}
				updateFoodByAdvice();
				v.setBackgroundColor(Color.rgb(3, 106, 235));
				adviceText.setTextColor(Color.WHITE);
				iv.setBackgroundResource(R.drawable.icon_q02);
			}
			select_Advice = advice.getText().toString().trim();
		}
	}

	/**
	 * 初始化上面title按钮
	 */
	private void initTitle() {

		select_patient = (TextView) findViewById(R.id.select_patient);
		TextPaint tp = select_patient.getPaint();
		tp.setFakeBoldText(true);
		pro_day = (Button) findViewById(R.id.pro_day);
		next_day = (Button) findViewById(R.id.next_day);
		order_day = (TextView) findViewById(R.id.current_day);
		pro_day.setOnClickListener(this);
		next_day.setOnClickListener(this);

		minTime = DateUtils.getFormatDate("MM-dd");
		minTime_a = DateUtils.getFormatDate("yyyy-MM-dd");
		hashOrderDay.put(minTime_a, minTime_a);
		orderTime = DateUtils.getDay(minTime, "MM-dd", 1);

		currentOrderDate = DateUtils.getDay(minTime_a, "yyyy-MM-dd", 1);// 点餐时间界限——前
		Log.e("currentOrderDate"+currentOrderDate);

		hashOrderDay.put(currentOrderDate, currentOrderDate);

		//2017-12-13 Kim, 点餐时间至少为当前时间的后面一天
		order_day.setText(orderTime);
		//order_day.setText(minTime);

	}

	/**
	 * 初始化组件
	 */
	private void initTop() {
		Button back = (Button) findViewById(R.id.back);
		Button camera = (Button) findViewById(R.id.camera);

		back.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				clearUnPutOrder();
				Db.delete("delete from OrderDetail where count = ?  ", 0);
				if (!"modify".equals(tag)) {
					Db.delete("delete from [Order] where ID = (select Order_ID from NumsMark where TakeFoodTime>? and PatPositionID = ?)", ConvertUtils.date2Str("yyyy-MM-dd"),
							PatientsListActivity.currentBedID);
				}
				Db.delete("delete from OrderDetail where count = '0'");
				Db.delete("delete from NumsMark  where nums = '0'");
				// startActivity(new Intent(OrderActivity.this,
				// PatientsListActivity.class));
				OrderActivity.this.finish();

			}
		});

		camera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(OrderActivity.this, CameraActivity.class);

				startActivity(intent);
			}
		});

	}

	/**
	 * 初始化病人详情
	 */
	private void initPtInfo() {

		bed_num = (TextView) findViewById(R.id.bed_num);
		bed_patient_num = (TextView) findViewById(R.id.bed_patient_num);
		bed_patient_name = (TextView) findViewById(R.id.bed_patient_name);
		advice = (TextView) findViewById(R.id.advice);
		patient_expect_outHospital_time = (TextView) findViewById(R.id.expect_outHospital_time);

		Button patint_detail = (Button) findViewById(R.id.patint_detail);

		patint_detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(OrderActivity.this, PatientInfoActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initGV() {

		list_canci = new ArrayList();
		list_canlei = new ArrayList();
		Map<String, Object> map_taocan1 = new HashMap<String, Object>();
		Map<String, Object> map_taocan2 = new HashMap<String, Object>();
		map_taocan1.put("Name", "套餐");
		map_taocan2.put("Name", "小锅菜");
		map_taocan1.put("ID", "taochan");
		map_taocan2.put("ID", "xiaoguocai");
		Map<String, Object> map_taocan3 = new HashMap<String, Object>();
		Map<String, Object> map_taocan4 = new HashMap<String, Object>();
		map_taocan3.put("Name", "套餐(西)");
		map_taocan4.put("Name", "小锅菜(西)");
		map_taocan3.put("ID", "taochanx");
		map_taocan4.put("ID", "xiaoguocaix");
		list_canlei.add(map_taocan1);
		// list_canlei.add(map_taocan3);
		list_canlei.add(map_taocan2);
		// list_canlei.add(map_taocan4);

		mMyGridView1 = (MyGridView) findViewById(R.id.food_gv1);
		mMyGridView2 = (MyGridView) findViewById(R.id.food_gv2);

		// leftMenu = (ListView) findViewById(R.id.left_menu_lv);
		// adapter = new LeftmenuAdapter(this, menuList);
		// menuList.add("荤菜");
		// menuList.add("素菜");
		// menuList.add("汤");
		// menuList.add("点心");
		// menuList.add("饮料");
		// leftMenu.setAdapter(adapter);
		// adapter.notifyDataSetChanged();
		// leftMenu.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// ii("onItemClick:" + arg2);
		// adapter.setSelectPos(arg2);
		// adapter.notifyDataSetChanged();
		// }
		// });
		// ininThirdList();
		linearLayout = (LinearLayout) findViewById(R.id.deal_ll);
	}

	private Map<String, Object> getDatas(String TypeName, String ID) {
		Map<String, Object> datas = new HashMap<String, Object>();
		datas.put("ID", ID);
		datas.put("TypeName", TypeName);
		return datas;
	}

	private void ininThirdList() {
		linearLayout = (LinearLayout) findViewById(R.id.deal_ll);
		linearLayout.removeAllViews();
		hListView = new HorizontalListView(this);

		if (hldata != null) {
			hldata.clear();
		}

		// hldata.add(getDatas("荤菜","1231241"));
		// hldata.add(getDatas("素菜","1231241"));
		// hldata.add(getDatas("汤","1231241"));
		// hldata.add(getDatas("点心","1231241"));
		// hldata.add(getDatas("饮料","1231241"));

		// hldata.add("荤菜");
		// hldata.add("素菜");
		// hldata.add("汤");
		// hldata.add("点心");
		// hldata.add("饮料");

		hldata = Db.select("SELECT a.ID,a.TypeName FROM FoodType a ORDER BY a.PID ASC");

		hListView.setDate(hldata, new btnListener() {

			@Override
			public void btnOnclick(Map<String, Object> map) {
				potFoodTypeID = (String) map.get("ID");
				handler.obtainMessage(2).sendToTarget();
				handler.obtainMessage(3).sendToTarget();
				handler.obtainMessage(4).sendToTarget();
			}
		});
		linearLayout.addView(hListView.getView());
	}

	private HorizontalListView hListView;
	private LinearLayout linearLayout;
	private List<Map<String, Object>> hldata = new ArrayList<Map<String, Object>>();
	public static String potFoodTypeID = "";

	/**
	 * 实例化用于呈现餐单的的listview
	 */
	private void initListView() {

		mGridView = (GridView) findViewById(R.id.foods_list);
		confirm = (Button) findViewById(R.id.confirmation);
		nomeal = (TextView) findViewById(R.id.nomeal);

		confirm.setOnClickListener(this);

		mOrderLVAdapter = new OrderLVAdapter(OrderActivity.this, handler);

		// mGridView.setAdapter(mOrderLVAdapter);

		handler.obtainMessage(3).sendToTarget();
		list_canci = Db.select("SELECT a.ID,a.Name FROM EnumBase a WHERE a.TypeCode = ? ORDER BY a.Sort ASC", 0);
		if (list_canci != null && list_canci.size() >= 2) {

			OrderGVCanCiAdapter.current_canciID = list_canci.get(2).get("ID") + "";

		} else if (list_canci != null && list_canci.size() < 2) {

			OrderGVCanCiAdapter.current_canciID = list_canci.get(0).get("ID") + "";
		}

		mOrderLVAdapter = new OrderLVAdapter(OrderActivity.this, handler);
		mGridView.setAdapter(mOrderLVAdapter);

	}

	@Override
	public void onClick(View v) {

		int id = v.getId();

		switch (id) {
		case R.id.confirmation:
		//点击确认按钮的事件响应
			Intent intent = new Intent(OrderActivity.this, ConfirmOrderActivity.class);

			select_Advice = advice.getText().toString().trim();
			if ("modify".equals(tag)) {

				intent.putExtra("tag", "modify");

			} else {
				intent.putExtra("tag", "putorder");

			}
			intent.putExtra("select_Advice", select_Advice);
			startActivity(intent);

			break;
		// 订上一天的订单
		case R.id.pro_day:
			// TODO 7-28 能点当前天之后的餐
			String currentDay = DateUtils.getDay(minTime, "MM-dd", 1);
			Log.e("日期currentDay"+currentDay);
			if (order_day.getText().toString().equals(currentDay)) {
				//前一天的按钮就不能点击了
				//order_day.setEnabled(false);
			} else {
				order_day.setText(DateUtils.getProDay(order_day.getText().toString(), "MM-dd"));
				currentOrderDate = DateUtils.getProDay(currentOrderDate, "yyyy-MM-dd");

			}

			handler.obtainMessage(1).sendToTarget();
			handler.obtainMessage(2).sendToTarget();
			handler.obtainMessage(3).sendToTarget();

			break;
		// 订下一天的订单
		case R.id.next_day:

			order_day.setText(DateUtils.getNextDay(order_day.getText().toString(), "MM-dd"));
			// 更新当前订餐日期
			currentOrderDate = DateUtils.getNextDay(currentOrderDate, "yyyy-MM-dd");
			// 如果记录已定餐map中不存在此天 就新建记录订单的map
			if (!hashOrderDay.containsKey(currentOrderDate)) {
				hashOrderDay.put(currentOrderDate, currentOrderDate);
			}
			// 通知界面刷新
			handler.obtainMessage(1).sendToTarget();
			handler.obtainMessage(2).sendToTarget();
			handler.obtainMessage(3).sendToTarget();

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
			case 1:
				// 菜品展示界面的刷新
				mOrderGVAdapter_canci = new OrderGVCanCiAdapter(OrderActivity.this, list_canci, handler);
				if (list_canci != null) {
					mMyGridView1.setAdapter(mOrderGVAdapter_canci);
				}

				mMyGridView1.setVisibility(View.GONE);
				mOrderGVAdapter_canci.notifyDataSetChanged();
				mMyGridView1.setVisibility(View.VISIBLE);

				break;
			case 2:
				// 餐类界面的刷新
				/*
				 * if (PatientsListActivity.beds_list != null) { adviceIDList =
				 * Db .select(
				 * "select FoodAdvice_ID from PatientFoodAdvice where Patient_ID = ?"
				 * , PatientsListActivity.beds_list
				 * .get(PatientsListActivity.currentPatientID) .get("ID")); }
				 */
				mOrderGVAdapter_canlei = new OrderGVCanLeiAdapter(OrderActivity.this, list_canlei, handler);

				mMyGridView2.setAdapter(mOrderGVAdapter_canlei);
				mMyGridView2.setVisibility(View.GONE);

				mOrderGVAdapter_canlei.notifyDataSetChanged();

				mMyGridView2.setVisibility(View.VISIBLE);

				break;
			case 3:
				// 此方法是用来判断展示 是显示小锅菜还是对应医嘱的菜品
				updateFoodMenu(OrderGVCanCiAdapter.current_canciID);

				break;

			case 4:
				// 菜品界面的刷新
				if (foodsList == null || foodsList.size() < 1) {
					nomeal.setVisibility(View.VISIBLE);
				} else {
					nomeal.setVisibility(View.GONE);
				}
				mGridView.setAdapter(mOrderLVAdapter);
				mGridView.setVisibility(View.GONE);
				mOrderLVAdapter.notifyDataSetChanged();
				mGridView.setVisibility(View.VISIBLE);
				break;
			case 5:
				// 刷新三级菜单 add:2017-8-17
				if (linearLayout == null) {
					return;
				}

				// 刷新三级菜单 add:2017-9-14
				linearLayout.setVisibility(View.VISIBLE);
				ininThirdList();
				// potFoodTypeID = "";
				// if
				// (OrderGVCanLeiAdapter.current_canleiID.equals("xiaoguocai"))
				// {
				// linearLayout.setVisibility(View.VISIBLE);
				// ininThirdList();
				// } else {
				// linearLayout.setVisibility(View.GONE);
				// potFoodTypeID = "";
				// }
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 
	 * 此方法是用来判断展示 是显示小锅菜还是对应医嘱的菜品
	 * 
	 * @author Administrator
	 * @return
	 * 
	 */
	private void updateFoodMenu(String MealTypeID) {

		synchronized (this) {
			// 首先判断病人的医嘱可为空

			/*
			 * List<Map<String, Object>> adviceIDList = Db .select(
			 * "select a.FoodAdvice_ID from PatientFoodAdvice a where a.Patient_ID = ?"
			 * , PatientsListActivity.beds_list.get(
			 * PatientsListActivity.currentPatientID).get( "ID"));
			 */
			Date day = ConvertUtils.str2Date(currentOrderDate);
			if (adviceIDList != null) {
				// 如果当前的菜类的id是xiaoguocai或者病人的医嘱数量大于1就显示小锅菜
				if ("xiaoguocai".equals(OrderGVCanLeiAdapter.current_canleiID) || adviceIDList.size() > 1) {

					// 判断三级菜单选项是否为空，若是则显示所有小锅菜
					if (TextUtils.isEmpty(potFoodTypeID)) {
						foodsList = Db.select("select b.* from PotFood a inner join Food b on a.Food_ID=b.id ");
					} else {
						foodsList = Db.select("select b.* from PotFood a inner join Food b on a.Food_ID=b.id where b.Type_ID=?", potFoodTypeID);
					}
					if (foodsList == null || foodsList.size() == 0) {
						handler.obtainMessage(4).sendToTarget();
						return;
					}
					// TODO 7-23:小锅菜限定支付为现金是不对的
					/*
					 * Map<String, Object> xianjingID = Db.selectUnique(
					 * "select id from EnumBase where name = ?",
					 * OrderActivity.this.getSharedPreferences( "deviceDate", 0)
					 * .getString("orderWay", "现金"));
					 */
					List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();

					for (Map<String, Object> food : foodsList) {
						// TODO 7-23:增加的，根据菜名获取支付方式
						Map<String, Object> xianjingID = Db.selectUnique("select Enum_PayWayIDList from food where FoodName = ?", food.get("FoodName"));
						food.put("Enum_PayWayIDList", xianjingID.get("Enum_PayWayIDList"));

						mapList.add(food);
					}

					foodsList = mapList;

				} else {
					// 显示病人医嘱对应的菜类
					String adviceID = "";
					if (adviceIDList.size() > 0) {
						adviceID = adviceIDList.get(0).get("FoodAdvice_ID") + "";
						if (adviceID.endsWith(",")) {
							adviceID = adviceID.substring(0, adviceID.length() - 1);
						}
					}

					// update:2017-9-14 索迪斯
					if (TextUtils.isEmpty(potFoodTypeID)) {
						foodsList = Db.select("select b.* from FoodMenu a inner join Food b on a.FoodID=b.id " + " where a.daytime=? and a.MealTypeID=? and FoodAdviceId=? ORDER BY a.Sort ASC",
								ConvertUtils.date2Str(day, "yyyy-MM-dd HH:mm:ss"), OrderGVCanCiAdapter.current_canciID, adviceID);
					} else {
						foodsList = Db.select(
								"select b.* from FoodMenu a inner join Food b on a.FoodID=b.id where b.Type_ID=? and a.daytime=? and a.MealTypeID=? and FoodAdviceId=? ORDER BY a.Sort ASC",
								potFoodTypeID, ConvertUtils.date2Str(day, "yyyy-MM-dd HH:mm:ss"), OrderGVCanCiAdapter.current_canciID, adviceID);
					}
					if (foodsList == null || foodsList.size() == 0) {
						handler.obtainMessage(4).sendToTarget();
						return;
					}

					// TODO 7-23:套餐限定支付为月结是不对的
					/*
					 * Map<String, Object> yuejieID = Db.selectUnique(
					 * "select id from EnumBase where name = ?",
					 * OrderActivity.this.getSharedPreferences( "deviceDate",
					 * 0).getString("yuejie", "月结"));
					 */

					List<Map<String, Object>> _mapList = new ArrayList<Map<String, Object>>();
					if (foodsList == null) {
						foodsList = null;
					} else {
						for (Map<String, Object> food : foodsList) {
							// TODO 7-23:增加的，根据菜名获取支付方式
							Map<String, Object> yuejieID = Db.selectUnique("select Enum_PayWayIDList from food where FoodName = ?",

							food.get("FoodName"));
							food.put("Enum_PayWayIDList", yuejieID.get("Enum_PayWayIDList"));
							_mapList.add(food);
						}
						foodsList = _mapList;
					}
				}
			} else {
				// 如果病人没有医嘱就直接显示小锅菜
				// 判断三级菜单选项是否为空，若是则显示所有小锅菜
				if (TextUtils.isEmpty(potFoodTypeID)) {
					foodsList = Db.select("select b.* from PotFood a inner join Food b on a.Food_ID=b.id ");
				} else {
					foodsList = Db.select("select b.* from PotFood a inner join Food b on a.Food_ID=b.id where b.Type_ID=?", potFoodTypeID);
				}
				if (foodsList == null || foodsList.size() == 0) {
					handler.obtainMessage(4).sendToTarget();
					return;
				}
				// TODO 7-23:小锅菜限定支付为现金是不对的
				/*
				 * Map<String, Object> xianjingID = Db.selectUnique(
				 * "select id from EnumBase where name = ?", OrderActivity.this
				 * .getSharedPreferences("deviceDate", 0) .getString("orderWay",
				 * "现金"));
				 */

				List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> food : foodsList) {
					// TODO 7-23:增加的，根据菜名获取支付方式
					Map<String, Object> xianjingID = Db.selectUnique("select Enum_PayWayIDList from food where FoodName = ?", food.get("FoodName"));
					food.put("Enum_PayWayIDList", xianjingID.get("Enum_PayWayIDList"));

					mapList.add(food);
				}
				foodsList = mapList;
			}

			handler.obtainMessage(4).sendToTarget();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			clearUnPutOrder();

			OrderActivity.this.finish();

			return true;

		}

		return false;
	}

	/**
	 * 清除未下单订单
	 */
	private void clearUnPutOrder() {
		Db.delete("delete from NumsMark where  OrderMark is null");
		Db.delete("delete from OrderDetail where  OrderMark is null");
		Db.delete("delete from [Order] where OrderMark is null ");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		OrderGVCanLeiAdapter.current_canleiID = "";
		// 界面销毁，处理静态变量
		select_Advice = "";
		adviceIDList = null;
		clearUnPutOrder();
	}
}
