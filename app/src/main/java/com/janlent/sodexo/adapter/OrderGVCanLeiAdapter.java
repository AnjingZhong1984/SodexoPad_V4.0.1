package com.janlent.sodexo.adapter;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.janlent.sodexo.activity.OrderActivity;
import com.janlent.sodexo.activity.PatientsListActivity;
import com.janlent.utils.Log;
import com.janlent.utils.Db;
import com.jenlant.sodexo.R;

/**
 * 点餐界面中list_canlei所使用的适配器，这里主要显示餐类信息
 * 
 * @author Administrator
 * 
 */
public class OrderGVCanLeiAdapter extends BaseAdapter {

	private Activity context;
	private List<Map<String, Object>> list; // 餐类集合

	private LayoutInflater inflater;

	public static String current_canleiID = "";// 记录当前选择的餐类ID
	public static String current_FoodAdviceIDList = "";// 记录当前选择的餐类ID

	private Handler handler;

	public OrderGVCanLeiAdapter(Activity context, List<Map<String, Object>> list, Handler handler) {

		this.context = context;
		this.list = list;
		inflater = context.getLayoutInflater();
		this.handler = handler;

	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = inflater.inflate(R.layout.food_category_btn, null);

		Button select_btn = (Button) view.findViewById(R.id.select_btn);
		TextView numMark = (TextView) view.findViewById(R.id.select_num);

		if (list.size() > position) {

			select_btn.setText(list.get(position).get("Name").toString());
			select_btn.setTag(R.id.adviceID, list.get(position).get("ID"));

			if (list.get(position).get("ID") != null) {

				if (current_canleiID.equals((String) list.get(position).get("ID"))) {

					select_btn.setBackgroundResource(R.drawable.btn_cp_c2);

				} else {
					select_btn.setBackgroundResource(R.drawable.btn_cp2);
				}
			}

			Map<String, Object> map = Db.selectUnique("SELECT Nums FROM NumsMark WHERE FoodTypeID = ? AND TakeFoodTime = ? AND PatPositionID =?", list.get(position).get("ID"),
					OrderActivity.currentOrderDate, PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("PatPositionID"));

			if (map != null && Integer.parseInt(map.get("Nums") + "") > 0) {
				numMark.setText(map.get("Nums").toString());

				numMark.setVisibility(View.VISIBLE);
			}

			// 如果医嘱个数大于1，就禁止选择菜品种类，只能选择小锅菜
			if (OrderActivity.adviceIDList != null && OrderActivity.adviceIDList.size() <= 1) {
				select_btn.setOnClickListener(new MyListener(view));
			}
		}
		return view;
	}

	/**
	 * 给按钮注册监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyListener implements OnClickListener {
		View view;

		public MyListener(View view) {

			this.view = view;

		}

		@Override
		public void onClick(View v) {

			if (OrderActivity.adviceIDList != null) {
				if (OrderActivity.adviceIDList.size() <= 1) {

					if (!current_canleiID.equals(v.getTag(R.id.adviceID) + "")) {
						OrderActivity.potFoodTypeID = "";
					}

					current_canleiID = v.getTag(R.id.adviceID) + "";

					if ("xiaoguocai".equals(current_canleiID)) {
						Map<String, Object> map = Db.selectUnique("select id from FoodAdvice where Advice=?", "小锅菜");
						if (map != null) {
							current_FoodAdviceIDList = (String) map.get("ID") + ",";
						}
						// current_FoodAdviceIDList =
						// OrderActivity.FoodAdviceIDList+ "";

					} else {
						// TODO 7-22除小锅菜外，其他为普食
						/*
						 * Map<String, Object> map = Db.selectUnique(
						 * "select id from FoodAdvice where Advice=?", "普食"); if
						 * (map != null) { current_FoodAdviceIDList = (String)
						 * map.get("ID") + ","; }
						 */
						//TODO 2017-12-05除小锅菜外，其他为套餐
//						 Map<String, Object> map = Db.selectUnique("select id from FoodAdvice where Advice=?", "套餐");
//						 if(map != null)
//						 {
//							 current_FoodAdviceIDList = (String) map.get("ID") + ",";
//						 }

//						current_FoodAdviceIDList = OrderActivity.FoodAdviceIDList + "";
					}

					handler.obtainMessage(2).sendToTarget();
					handler.obtainMessage(3).sendToTarget();
					handler.obtainMessage(4).sendToTarget();
					handler.obtainMessage(5).sendToTarget();

				}
			}
		}

	}

}
