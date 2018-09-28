package com.janlent.sodexo.adapter;

import java.util.Date;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.janlent.sodexo.activity.ConfirmOrderActivity;
import com.janlent.sodexo.activity.PatientsListActivity;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.Db;
import com.janlent.utils.ImgUtil;
import com.janlent.utils.Log;
import com.jenlant.sodexo.R;

/**
 * 确认订单中listview所使用的适配器，这里面主要展示，套餐名称，送餐时间，数量，总价和支付方式
 * 
 * @author Administrator
 * 
 */
public class ConfirmListAdapter extends BaseAdapter {

	private Activity context;

	private LayoutInflater inflater;

	private Handler handler;

	private SharedPreferences sharedata;
	int mode = 0;

	public ConfirmListAdapter(Activity context, Handler handler) {

		this.context = context;

		inflater = context.getLayoutInflater();
		sharedata = context.getSharedPreferences("deviceDate", 0);
		mode = sharedata.getInt("checkMode", 1);
		this.handler = handler;

	}

	@Override
	public int getCount() {
		if (ConfirmOrderActivity.Orderlist != null) {
			return ConfirmOrderActivity.Orderlist.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return ConfirmOrderActivity.Orderlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder mViewHolder;

		if (convertView == null) {

			mViewHolder = new ViewHolder();

			convertView = inflater.inflate(R.layout.co_list_item, null);

			mViewHolder.pic = (ImageView) convertView
					.findViewById(R.id.co_food_img);
			mViewHolder.name = (TextView) convertView
					.findViewById(R.id.food_id);
			mViewHolder.sendTime = (TextView) convertView
					.findViewById(R.id.send_food_time);
			mViewHolder.mealType = (TextView) convertView
					.findViewById(R.id.food_class);
			mViewHolder.nums = (TextView) convertView
					.findViewById(R.id.food_nums);
			mViewHolder.allMoney = (TextView) convertView
					.findViewById(R.id.total_price);
			mViewHolder.payWay = (TextView) convertView
					.findViewById(R.id.pay_mode);

			mViewHolder.addition = (Button) convertView.findViewById(R.id.plus);
			mViewHolder.subtraction = (Button) convertView
					.findViewById(R.id.reduction);

			convertView.setTag(mViewHolder);

		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
			mViewHolder.pic.setImageResource(R.drawable.wu);
			mViewHolder.name.setText("");
			mViewHolder.sendTime.setText("");
			mViewHolder.mealType.setText("");
			mViewHolder.nums.setText("");
			mViewHolder.allMoney.setText("");
			mViewHolder.payWay.setText("");

		}
		if (ConfirmOrderActivity.Orderlist.size() > position) {
			if (ConfirmOrderActivity.Orderlist.get(position) != null) { // 判断楼层的订单集合是否非空

				if (ConfirmOrderActivity.Orderlist.size() > position
						&& ConfirmOrderActivity.Orderlist.get(position).get(
								"PicAddress") != null
						&& (ConfirmOrderActivity.Orderlist.get(position).get(
								"PicAddress") + "").length() > 1) {

					Bitmap pic = ImgUtil.showImage(
							ConfirmOrderActivity.Orderlist.get(position).get(
									"PicAddress")
									+ "", 70);

					// 如果获取的bitmap不为空，就设置显示图片,或者显示默认提示图片
					if (pic != null) {
						mViewHolder.pic.setImageBitmap(pic);
					} else {
						mViewHolder.pic.setImageResource(R.drawable.wu);
					}

				} else {

					mViewHolder.pic.setImageResource(R.drawable.wu);

				}

				if (ConfirmOrderActivity.Orderlist.size() > position
						&& ConfirmOrderActivity.Orderlist.get(position).get(
								"FoodName") != null) { // 判断菜单名是否非空
					mViewHolder.name.setText(ConfirmOrderActivity.Orderlist
							.get(position).get("FoodName").toString());
				}

				if (ConfirmOrderActivity.Orderlist.size() > position
						&& ConfirmOrderActivity.Orderlist.get(position).get(
								"TakeFoodTime") != null) { // 判断送餐时间是否非空
					Date day = ConvertUtils
							.str2Date(ConfirmOrderActivity.Orderlist.get(
									position).get("TakeFoodTime")
									+ "");

					mViewHolder.sendTime.setText(ConvertUtils.date2Str(day,
							"M月d日"));

				}

				if (ConfirmOrderActivity.Orderlist.size() > position
						&& ConfirmOrderActivity.Orderlist.get(position).get(
								"MealTypeID") != null) { // 判断餐次类型ID是否非空

					Map<String, Object> PayWayName = Db.selectUnique(
							"select Name from EnumBase where ID = ?",
							ConfirmOrderActivity.Orderlist.get(position)
									.get("MealTypeID").toString());
					mViewHolder.mealType.setText(PayWayName.get("Name")
							.toString());

				}
				if (ConfirmOrderActivity.Orderlist.size() > position
						&& ConfirmOrderActivity.Orderlist.get(position).get(
								"PayWayID") != null) { // 判断支付方式ID是否非空

					Map<String, Object> PayWayName = Db.selectUnique(
							"select Name from EnumBase where ID = ?",
							ConfirmOrderActivity.Orderlist.get(position)
									.get("PayWayID").toString());
					mViewHolder.payWay.setText(PayWayName.get("Name")
							.toString());

				}

				if (ConfirmOrderActivity.Orderlist.size() > position
						&& ConfirmOrderActivity.Orderlist.get(position).get(
								"Count") != null) { // 判断数量是否非空

					mViewHolder.nums.setText(ConfirmOrderActivity.Orderlist
							.get(position).get("Count").toString());

				}

				double allUnitPrice = 0;

				if (ConfirmOrderActivity.Orderlist.size() > position
						&& ConfirmOrderActivity.Orderlist.get(position).get(
								"UnitPrice") != null) { // 判断单个价格是否非空

					int Count = Integer.parseInt(ConfirmOrderActivity.Orderlist
							.get(position).get("Count").toString());

					allUnitPrice = ConvertUtils
							.dou(ConfirmOrderActivity.Orderlist.get(position)
									.get("UnitPrice") + "")
							* Count;

					mViewHolder.allMoney.setText(ConvertUtils.num2String(
							allUnitPrice, "￥#,##0.00"));

				}

				int nums = ConvertUtils
						.integer(mViewHolder.nums.getText() + "");

				mViewHolder.addition.setOnClickListener(new MyListener(

				position, nums, 1));

				mViewHolder.subtraction.setOnClickListener(new MyListener(

				position, nums, -1));

			}
		}

		return convertView;
	}

	private class ViewHolder {
		ImageView pic;
		TextView name;
		TextView sendTime;
		TextView nums;
		TextView allMoney;
		TextView payWay;
		TextView mealType;

		Button addition;
		Button subtraction;
	}

	/**
	 * 用于增加和减少用的监听类
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyListener implements OnClickListener {

		int position;
		int type;
		int nums;

		public MyListener(int position, int nums, int type) {

			this.nums = nums;
			this.type = type;

			this.position = position;

		}

		@Override
		public void onClick(View v) {
			if (nums > 0 || (nums == 0 && type == 1)) {

				nums = nums + type;

				if (nums < 0) {
					nums = 0;
				}

				// 获取餐类的数量
				Map<String, Object> mealMap = Db
						.selectUnique(
								"select Nums,Order_ID from NumsMark  where MealTypeID = ? AND TakeFoodTime   = ? AND PatPositionID = ?",
								ConfirmOrderActivity.Orderlist.get(position)
										.get("MealTypeID") + "",
								ConfirmOrderActivity.Orderlist.get(position)
										.get("TakeFoodTime") + "",
								PatientsListActivity.currentBedID);

				if (mealMap != null) {

					int mealNums = Integer.parseInt(mealMap.get("Nums") + "");

					mealNums = mealNums + type;

					Log.e("mealNums=" + mealNums);

					if (mealNums < 0) {
						mealNums = 0;
					}

					// 更新餐类的数量
					Db.update(
							"update NumsMark  set Nums =? where MealTypeID = ? AND TakeFoodTime   = ? AND PatPositionID =? ",
							mealNums,
							ConfirmOrderActivity.Orderlist.get(position).get(
									"MealTypeID")
									+ "",
							ConfirmOrderActivity.Orderlist.get(position).get(
									"TakeFoodTime")
									+ "", PatientsListActivity.currentBedID);
					// 获取套餐或小锅菜的数量
					Map<String, Object> foodMap = Db
							.selectUnique(
									"select Nums from NumsMark  where FoodTypeID = ? AND TakeFoodTime   = ? AND PatPositionID = ?",
									ConfirmOrderActivity.Orderlist
											.get(position).get("FoodTypeID")
											+ "",
									ConfirmOrderActivity.Orderlist
											.get(position).get("TakeFoodTime")
											+ "",
									PatientsListActivity.currentBedID);

					int foodNums = Integer.parseInt(foodMap.get("Nums") + "");

					foodNums = foodNums + type;

					Log.e("foodNums=" + foodNums);

					if (foodNums < 0) {
						foodNums = 0;
					}

					// 更新套餐或小锅菜的数量
					Db.update(
							"update NumsMark  set Nums =? where FoodTypeID = ? AND TakeFoodTime   = ? AND PatPositionID =? ",
							foodNums,
							ConfirmOrderActivity.Orderlist.get(position).get(
									"FoodTypeID")
									+ "",
							ConfirmOrderActivity.Orderlist.get(position).get(
									"TakeFoodTime")
									+ "", PatientsListActivity.currentBedID);
					// 如果数量不为零就更新其数量
					Db.update(
							"update OrderDetail set Count = ? where ID = ? and TakeFoodTime  = ? and MealTypeID  = ? and FoodTypeID =? and Order_ID = ?",
							nums,
							ConfirmOrderActivity.Orderlist.get(position).get(
									"ID")
									+ "",
							ConfirmOrderActivity.Orderlist.get(position).get(
									"TakeFoodTime")
									+ "",
							ConfirmOrderActivity.Orderlist.get(position).get(
									"MealTypeID")
									+ "",
							ConfirmOrderActivity.Orderlist.get(position).get(
									"FoodTypeID")
									+ "",
							ConfirmOrderActivity.Orderlist.get(position).get(
									"Order_ID")
									+ "");

					Db.update(
							"update [Order] set LastUpdateTime = ?,LastUpdateAdminName=? where ID  = ?",
							ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"),
							context.getSharedPreferences("user", 0).getString(
									"user", "乐pad"), mealMap.get("Order_ID"));

				}
			}
			handler.obtainMessage(0).sendToTarget();
		}
	}
}
