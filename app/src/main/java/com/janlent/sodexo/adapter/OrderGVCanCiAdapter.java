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
 * 点餐界面中list_canci所使用的适配器，这里主要显示餐次信息
 * 
 * @author Administrator
 * 
 */
public class OrderGVCanCiAdapter extends BaseAdapter {

	private Activity context;
	private List<Map<String, Object>> list; // 餐次集合

	private LayoutInflater inflater;

	public static String current_canciID = "";// 记录当前选择的餐次ID

	private Handler handler;

	public OrderGVCanCiAdapter(Activity context,
			List<Map<String, Object>> list, Handler handler) {

		this.context = context;
		this.list = list;
		this.handler = handler;
		inflater = context.getLayoutInflater();

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

		View view = inflater.inflate(R.layout.food_category_btn2, null);

		Button select_btn = (Button) view.findViewById(R.id.select_btn);
		TextView numMark = (TextView) view.findViewById(R.id.select_num);

		
		if (list.size() > position) {

			select_btn.setText(list.get(position).get("Name").toString());

			select_btn.setOnClickListener(new MyListener(view));

			select_btn.setTag(list.get(position).get("ID"));

			if (list.get(position).get("ID") != null) {
				if (current_canciID.equals((String) list.get(position)
						.get("ID"))) {

					select_btn.setBackgroundResource(R.drawable.btn_cp_c);
					select_btn.setId(0);

				}
			}

			Map map = Db
					.selectUnique(
							"SELECT Nums FROM NumsMark WHERE MealTypeID = ? AND TakeFoodTime  =? AND PatPositionID =?",
							list.get(position).get("ID"),
							OrderActivity.currentOrderDate,
							PatientsListActivity.beds_list.get(
									PatientsListActivity.currentPatientID).get(
									"PatPositionID"));
			if (map != null && Integer.parseInt(map.get("Nums") + "") > 0) {
				numMark.setText(map.get("Nums").toString());

				numMark.setVisibility(View.VISIBLE);
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

			OrderLVAdapter.sumCanCi = 0;

			current_canciID = (String) v.getTag();


			handler.obtainMessage(1).sendToTarget();
			handler.obtainMessage(3).sendToTarget();

		}

	}

}
