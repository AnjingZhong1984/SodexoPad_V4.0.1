package com.janlent.sodexo.adapter;

import java.util.List;
import java.util.Map;

import com.janlent.sodexo.activity.FoodDetailActivity;
import com.janlent.sodexo.activity.OrderActivity;
import com.janlent.sodexo.activity.PatientsListActivity;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.Db;
import com.janlent.utils.ImgUtil;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 点餐界面中GridView所使用的适配器，这里主要显示菜单信息
 * 
 * @author Administrator
 * 
 */
@SuppressWarnings("unused")
public class OrderLVAdapter extends BaseAdapter {

	private Activity context;

	private LayoutInflater inflater;

	public static int sumCanCi = 0;
	public static int sumCanLei = 0;

	public static int sumXianJin_YueJie = 0;

	private String currentClickBtnID = "";

	private List<Map<String, Object>> list_foods; // 菜单集合

	private Handler handler;

	private String Order_ID; // 显示订单ID
	private SharedPreferences sharedata;
	int mode = 0;

	public OrderLVAdapter(Activity context, Handler handler) {

		this.context = context;
		this.handler = handler;
		sharedata = context.getSharedPreferences("deviceDate", 0);
		mode = sharedata.getInt("checkMode", 1);
		inflater = context.getLayoutInflater();
		Db.delete("delete from OrderDetail where count = ?  ", 0);
		// Db.delete(
		// "delete from [Order] where ID = (select Order_ID from NumsMark where
		// TakeFoodTime>? and PatPositionID = ?)",
		// ConvertUtils.date2Str("yyyy-MM-dd"),
		// PatientsListActivity.currentBedID);
		// android.util.Log
		// .i("----", "--1---" + PatientsListActivity.currentBedID);
		Db.delete("delete from OrderDetail where count = '0'");
		//
		Db.delete("delete from NumsMark  where nums = '0'");
	}

	@Override
	public int getCount() {

		if (OrderActivity.foodsList != null) {
			return OrderActivity.foodsList.size();
		} else {
			return 0;
		}

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

		ViewHolder mViewHolder;
		if (convertView == null) {

			convertView = inflater.inflate(R.layout.food_item, null);
			mViewHolder = new ViewHolder();

			mViewHolder.pic = (ImageView) convertView.findViewById(R.id.food_img);

			mViewHolder.name = (TextView) convertView.findViewById(R.id.food_id);

			mViewHolder.price = (TextView) convertView.findViewById(R.id.food_price);

			mViewHolder.num_yuejie = (TextView) convertView.findViewById(R.id.select_num_item_a);

			mViewHolder.xianjin = (Button) convertView.findViewById(R.id.select_btn_item_xianjin);
			mViewHolder.num_xianjin = (TextView) convertView.findViewById(R.id.select_num_item_b);
			mViewHolder.yuejie = (Button) convertView.findViewById(R.id.select_btn_item_yuejie);

			mViewHolder.num_third = (TextView) convertView.findViewById(R.id.select_num_item_b1);
			mViewHolder.thirdBtn = (Button) convertView.findViewById(R.id.select_btn_item_xianjin1);

			convertView.setTag(mViewHolder);
			//
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
			// 清除复用控件缓存参数

			mViewHolder.pic.setImageResource(R.drawable.wu);
			mViewHolder.name.setText("");
			mViewHolder.price.setText("");

			mViewHolder.xianjin.setVisibility(View.GONE);
			mViewHolder.yuejie.setVisibility(View.GONE);
			mViewHolder.thirdBtn.setVisibility(View.GONE);
			mViewHolder.num_yuejie.setVisibility(View.GONE);
			mViewHolder.num_xianjin.setVisibility(View.GONE);
			mViewHolder.num_third.setVisibility(View.GONE);
			mViewHolder.num_yuejie.setText("");
			mViewHolder.num_xianjin.setText("");
			mViewHolder.num_third.setText("");

		}

		if (OrderActivity.foodsList != null && OrderActivity.foodsList.size() > position) { // 菜单中的床位是否非空

			if (OrderActivity.foodsList.get(position) != null) {

				mViewHolder.xianjin.setTag(OrderActivity.foodsList.get(position).get("ID") + "");
				mViewHolder.yuejie.setTag(OrderActivity.foodsList.get(position).get("ID") + "");
				mViewHolder.pic.setTag(OrderActivity.foodsList.get(position).get("ID") + "");
				// 如果数据库图片地址不为空，就根据地址到文件夹中取图片,如果为空就设置默认显示图片
				if (OrderActivity.foodsList.size() > position && OrderActivity.foodsList.get(position).get("PicAddress") != null
						&& (OrderActivity.foodsList.get(position).get("PicAddress") + "").length() > 1) {

					Bitmap pic = ImgUtil.showImage(OrderActivity.foodsList.get(position).get("PicAddress") + "", 70);

					// 如果获取的bitmap不为空，就设置显示图片,或者显示默认提示图片
					if (pic != null) {
						mViewHolder.pic.setImageBitmap(pic);
					} else {
						mViewHolder.pic.setImageResource(R.drawable.wu);
					}

				} else {

					mViewHolder.pic.setImageResource(R.drawable.wu);

				}

				if (OrderActivity.foodsList.size() > position && OrderActivity.foodsList.get(position).get("FoodName") != null) {

					mViewHolder.name.setText(OrderActivity.foodsList.get(position).get("FoodName") + "");

				}

				if (OrderActivity.foodsList.size() > position && OrderActivity.foodsList.get(position).get("UnitPrice") != null) {
					if ("True".equals(OrderActivity.IsCombination)) {

					} else {
						mViewHolder.price.setText(ConvertUtils.num2String(ConvertUtils.dou((String) OrderActivity.foodsList.get(position).get("UnitPrice")), "￥#,##0.00"));
					}

				}
				if (OrderActivity.foodsList.size() > position && OrderActivity.foodsList.get(position).get("Remark") != null) {
					// mViewHolder.info.setText(OrderActivity.foodsList.get(position)
					// .get("Remark") + "");
				}

				if (OrderActivity.foodsList.size() > position && OrderActivity.foodsList.get(position).get("Enum_PayWayIDList") != null) {

					String[] PayWayIDList = OrderActivity.foodsList.get(position).get("Enum_PayWayIDList").toString().split(",");

					// Map<String, Object> map2 =
					// Db.selectUnique("select ID from  EnumBase where Name =?",
					// "月结");
					// PayWayIDList = new String[1];
					// PayWayIDList[0] = map2.get("ID") + "";

					for (int i = 0; i < PayWayIDList.length; i++) { // 遍历支付方式ID集合

						Map<String, Object> map = Db.selectUnique("select Name from  EnumBase where ID =?", PayWayIDList[i]);

						switch (i) {
						case 0:
							mViewHolder.yuejie.setVisibility(View.VISIBLE);

							mViewHolder.yuejie.setTag(R.id.foodid, OrderActivity.foodsList.get(position).get("ID"));

							mViewHolder.num_yuejie.setTag(PayWayIDList[0]);

							mViewHolder.yuejie.setText(map.get("Name").toString());

							mViewHolder.yuejie.setOnClickListener(new myListener_smallbtn(

							PayWayIDList[0], mViewHolder.xianjin, mViewHolder.num_yuejie));

							break;
						case 1:

							mViewHolder.xianjin.setVisibility(View.VISIBLE);

							mViewHolder.xianjin.setTag(R.id.foodid, OrderActivity.foodsList.get(position).get("ID"));

							mViewHolder.num_xianjin.setTag(PayWayIDList[1]);

							mViewHolder.xianjin.setText(map.get("Name").toString());

							mViewHolder.xianjin.setOnClickListener(new myListener_smallbtn(

							PayWayIDList[1], mViewHolder.yuejie, mViewHolder.num_xianjin));
							break;
						case 2:

							mViewHolder.thirdBtn.setVisibility(View.VISIBLE);

							mViewHolder.thirdBtn.setTag(R.id.foodid, OrderActivity.foodsList.get(position).get("ID"));

							mViewHolder.num_third.setTag(PayWayIDList[2]);

							mViewHolder.thirdBtn.setText(map.get("Name").toString());

							mViewHolder.thirdBtn.setOnClickListener(new myListener_smallbtn(

							PayWayIDList[2], mViewHolder.thirdBtn, mViewHolder.num_third));
							break;

						default:
							break;
						}

					}

				}
				// TDOD 2016.08.24 若病人医嘱为空，则 默认为小锅菜；
				String xgc = "";
				List<Map<String, Object>> res = Db.select("select ID from FoodAdvice where Advice=?", "小锅菜");
				//.get(0).get("ID");

				if (res != null && res.size() > 0){
					xgc = (String) res.get(0).get("ID");
				}

				String foodAdviceIDList = TextUtils.isEmpty(OrderGVCanLeiAdapter.current_FoodAdviceIDList) ? xgc : OrderGVCanLeiAdapter.current_FoodAdviceIDList;

				List<Map<String, Object>> PayWayIDList = Db
						.select("select b.Count,b.PayWayID from [Order] a inner join OrderDetail b WHERE a.ID = b.Order_ID and b.TakeFoodTime = ? AND b.MealTypeID =? AND b.Food_ID = ? and a.[PatPositionID] = ? and b.FoodAdviceIDList = ?",
								OrderActivity.currentOrderDate, OrderGVCanCiAdapter.current_canciID, OrderActivity.foodsList.get(position).get("ID"), PatientsListActivity.currentBedID,
								foodAdviceIDList);

				if (PayWayIDList != null) { // 支付方式ID集合不为空
					for (int i = 0; i < PayWayIDList.size(); i++) {

						String PayWayID = PayWayIDList.get(i).get("PayWayID") + "";
						if (PayWayID.equals(mViewHolder.num_yuejie.getTag())) {

							if (ConvertUtils.integer(PayWayIDList.get(i).get("Count") + "", 0) > 0) {

								mViewHolder.num_yuejie.setVisibility(View.VISIBLE);

								mViewHolder.num_yuejie.setText(PayWayIDList.get(i).get("Count") + "");
							}

						}
						// TODO
						if (PayWayID.equals(mViewHolder.num_xianjin.getTag())) {

							if (ConvertUtils.integer(PayWayIDList.get(i).get("Count") + "", 0) > 0) {
								mViewHolder.num_xianjin.setVisibility(View.VISIBLE);
								mViewHolder.num_xianjin.setText(PayWayIDList.get(i).get("Count") + "");

							}
						}

					}

				}
			}

		}

		mViewHolder.pic.setOnClickListener(new myListener_ok());

		return convertView;
	}

	private class ViewHolder {
		ImageView pic;
		TextView name;
		TextView price;
		TextView num_yuejie;
		TextView num_xianjin;
		TextView num_third;
		Button xianjin;
		Button yuejie;
		Button thirdBtn;

	}

	/**
	 * 确定提交按钮监听
	 * 
	 * @author Administrator
	 * 
	 */
	private class myListener_ok implements OnClickListener {

		public myListener_ok() {
		}

		@Override
		public void onClick(View v) {

			Intent intent = new Intent(context, FoodDetailActivity.class);
			intent.putExtra("ID", v.getTag() + "");
			intent.putExtra("select_Advice", OrderActivity.select_Advice);
			context.startActivity(intent);

		}

	}

	/**
	 * 月结和现金按钮监听
	 * 
	 * @author Administrator
	 * 
	 */
	private class myListener_smallbtn implements OnClickListener {

		TextView summark;

		String payWayID;

		Button button;

		public myListener_smallbtn(String payWayID, Button button, TextView summark) {

			this.summark = summark;

			this.payWayID = payWayID;

			this.button = button;
		}

		@Override
		public void onClick(View v) {
			String id = (String) v.getTag(R.id.foodid);
			com.janlent.utils.Log.e("currentOrderDate1"+OrderActivity.currentOrderDate);

			new Thread(new SubmitOrder(id, payWayID, summark)).start();

		}
	}

	/**
	 * 增加订单
	 * 
	 * @param id
	 *            Food_ID
	 * @param nums
	 *            订单数量
	 * @param PayWayID
	 *            支付方式ID
	 */
	private void addMeal(String id, int nums, String PayWayID) {

		// TODO 08-06：TakeFoodTime
		Map<String, Object> patientOrder = Db.selectUnique("select b.ID from [Order] b   inner join OrderDetail a  on a.Order_ID = b.ID where b.PatPositionID =?  and a.TakeFoodTime > ?",
				PatientsListActivity.currentBedID, ConvertUtils.date2Str("yyyy-MM-dd"));
		// 判断此床位是否已纯在订单 如果不纯在就新增订单
		if (patientOrder == null) {
			// 生成订单id
			Order_ID = StringUtils.uid();
			// 获取设备名称
			String Device = context.getSharedPreferences("deviceDate", 0).getString("device_num", "乐pad");
			// 下订单的管理员
			String WriteAdminName = context.getSharedPreferences("user", 0).getString("user", "乐pad");
			// 医院代码
			String HisCode = context.getSharedPreferences("deviceDate", 0).getString("hisCode", "Sodex001");
			// 下订单时间
			String WriteTime = ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss");
			com.janlent.utils.Log.e("WriteTime--"+WriteTime);
			// 最后更新时间
			String LastUpdateTime = ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss");
			com.janlent.utils.Log.e("LastUpdateTime--"+LastUpdateTime);
			// 病人的id
			String Patient_ID = PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID).get("ID") + "";
			// 病人床位id
			String PatPositionID = PatientsListActivity.currentBedID;
			// 当前order表中最大订单号
			// Map max_pid_map =
			// Db.selectUnique("select max(b.pid) pid from OrderDetail a inner join [Order] b on a.Order_ID = b.id where a.TakeFoodTime >= ?",
			// ConvertUtils.date2Str("yyyy-MM-dd"));
			Map max_pid_map = Db.selectUnique("select max(b.pid) pid from OrderDetail a inner join [Order] b on a.Order_ID = b.id where b.pid like ?", ConvertUtils.date2Str("yyMMdd") + "%");
			String new_pid_num = "001";
			// 根据最大订单号 生成新的订单号
			if (max_pid_map != null && max_pid_map.get("pid") != null) {
				String max_pid = max_pid_map.get("pid") + "";

				int pid_length = max_pid.length();
				String max_pid_num = max_pid.substring(pid_length - 3, pid_length);
				try {
					new_pid_num = (Integer.parseInt(max_pid_num) + 1) + "";
				} catch (Exception e) {

					new_pid_num = "200";
				}

			}

			// Log.e("new_pid_num=" + new_pid_num);

			String PID = ConvertUtils.date2Str("yyMMdd") + context.getSharedPreferences("deviceDate", 0).getString("device_num", "00") + StringUtils.lpad(new_pid_num, '0', 3);
			// 往order表中添加订单数据
			// TODO 7-27
			// OrderGVCanLeiAdapter.current_FoodAdviceIDListOrderActivity.FoodAdviceIDList.toString()
			Db.insert("insert into [Order](ID,Device,PID,WriteAdminName,HisCode,WriteTime,LastUpdateAdminName,LastUpdateTime,Patient_ID,PatPositionID,FoodAdviceIDList) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
					Order_ID, Device, PID, WriteAdminName, HisCode, WriteTime, WriteAdminName, LastUpdateTime, Patient_ID, PatPositionID, OrderGVCanLeiAdapter.current_FoodAdviceIDList);
			// TDOD 2016.08.19 若病人医嘱为空，则 默认为小锅菜；
			// String
			// foodAdviceIDList=OrderGVCanLeiAdapter.current_FoodAdviceIDList;
			String xgc="";
			if(Db.select("select ID from FoodAdvice where Advice=?", "小锅菜")!=null&&Db.select("select ID from FoodAdvice where Advice=?", "小锅菜").size()>0) {
				xgc = (String) Db.select("select ID from FoodAdvice where Advice=?", "小锅菜").get(0).get("ID");
			}
			System.out.println("小锅菜ID=" + xgc);
			String foodAdviceIDList = TextUtils.isEmpty(OrderGVCanLeiAdapter.current_FoodAdviceIDList) ? xgc : OrderGVCanLeiAdapter.current_FoodAdviceIDList;

			// String
			// foodAdviceIDList=OrderActivity.FoodAdviceIDList.toString();

			// if(foodAdviceIDList!=null&&!foodAdviceIDList.equals("")){
			// foodAdviceIDList=foodAdviceIDList.substring(0,foodAdviceIDList.length()-1);
			// }
			com.janlent.utils.Log.e("currentOrderDate2--"+OrderActivity.currentOrderDate);
			Db.insert("insert into [OrderDetail](ID,PID,TakeFoodTime,MealTypeID,PayWayID,Count,Order_ID,Food_ID,FoodTypeID,FoodAdviceIDList,HisCode) VALUES(?,?,?,?,?,?,?,?,?,?,?)", StringUtils.uid(),
					null, OrderActivity.currentOrderDate, OrderGVCanCiAdapter.current_canciID, PayWayID, nums, Order_ID, id, OrderGVCanLeiAdapter.current_canleiID, foodAdviceIDList, HisCode);

		} else {
			// 如果这个床位已有订单 就更新病人订单
			Order_ID = (String) patientOrder.get("ID");

			// TDOD 2016.08.24 若病人医嘱为空，则 默认为小锅菜；
			// String
			// foodAdviceIDList=OrderGVCanLeiAdapter.current_FoodAdviceIDList;
			//String xgc = (String) Db.select("select ID from FoodAdvice where Advice=?", "小锅菜").get(0).get("ID");
			String xgc="";
			if(Db.select("select ID from FoodAdvice where Advice=?", "小锅菜") != null &&
					Db.select("select ID from FoodAdvice where Advice=?", "小锅菜").size()>0) {
				xgc = (String) Db.select("select ID from FoodAdvice where Advice=?", "小锅菜").get(0).get("ID");
			}
			System.out.println("小锅菜ID=" + xgc);
			String foodAdviceIDList = TextUtils.isEmpty(OrderGVCanLeiAdapter.current_FoodAdviceIDList) ? xgc : OrderGVCanLeiAdapter.current_FoodAdviceIDList;

			List list = Db.select("select id from OrderDetail where Food_ID = ? AND Order_ID = ? AND MealTypeID =?  and TakeFoodTime = ? and PayWayID = ? AND FoodAdviceIDList = ?", id,
					patientOrder.get("ID") + "", OrderGVCanCiAdapter.current_canciID, OrderActivity.currentOrderDate, PayWayID, foodAdviceIDList);
			// 判断订单详细中是否有当前菜品的记录 有就更新 没有就插入
			if (list != null) {
				// 更新orderdetail表
				// TODO 7-27:添加PayWayID，现金和月结同时存在

				Db.update("update OrderDetail set Count = ? where Food_ID = ? and Order_ID = ? AND MealTypeID =? and TakeFoodTime = ? and PayWayID = ?", nums, id, patientOrder.get("ID").toString(),
						OrderGVCanCiAdapter.current_canciID, OrderActivity.currentOrderDate, PayWayID);
			} else {
				// if(foodAdviceIDList!=null&&!foodAdviceIDList.equals("")){
				// foodAdviceIDList=foodAdviceIDList.substring(0,foodAdviceIDList.length()-1);
				// }
				// 用于存放订单的数据字段
				Db.insert("insert into [OrderDetail](ID,PID,TakeFoodTime,MealTypeID,PayWayID,Count,Order_ID,Food_ID,FoodTypeID,HisCode,FoodAdviceIDList) VALUES(?,?,?,?,?,?,?,?,?,?,?)",

				StringUtils.uid(), "", OrderActivity.currentOrderDate, OrderGVCanCiAdapter.current_canciID, PayWayID, nums, patientOrder.get("ID") + "", id, OrderGVCanLeiAdapter.current_canleiID,
						context.getSharedPreferences("deviceDate", 0).getString("hisCode", "Sodexo"), foodAdviceIDList);
			}
			// 更新最后更新时间
			String LastUpdateAdminName = context.getSharedPreferences("user", 0).getString("user", "乐pad");

			Db.update("update [Order] set LastUpdateAdminName = ?,LastUpdateTime = ? where ID = ?  ", LastUpdateAdminName, ConvertUtils.date2Str("yyyy-MM-dd HH:mm:ss"), patientOrder.get("ID"));

		}

	}

	/**
	 * 统计餐次和餐类已选数量
	 * 
	 * @param id
	 * @param payWayID
	 *            支付方式ID
	 */
	// TODO
	private void upcanci_canlei(String id, String payWayID) {

		Map mapCancI = Db.selectUnique("SELECT * FROM NumsMark WHERE MealTypeID = ? AND TakeFoodTime  = ? AND PatPositionID=?", OrderGVCanCiAdapter.current_canciID, OrderActivity.currentOrderDate,
				PatientsListActivity.currentBedID);

		// android.util.Log.i("---", "mapCanI==" + mapCancI);
		// 判断此餐次是否点餐 如果点餐就更新数据，没有就新增数据
		if (mapCancI == null) {

			sumCanCi = 0;

			sumCanCi++;

			Db.insert("INSERT INTO NumsMark (ID,MealTypeID,Nums,TakeFoodTime ,PatPositionID,PayWayID,Order_ID) VALUES(?,?,?,?,?,?,?)", StringUtils.uid(), OrderGVCanCiAdapter.current_canciID,
					sumCanCi, OrderActivity.currentOrderDate, PatientsListActivity.currentBedID, payWayID, Order_ID);
		} else {
			sumCanCi = Integer.parseInt(mapCancI.get("Nums") + "");
			sumCanCi++;

			Db.update("update NumsMark set Nums = ? WHERE MealTypeID = ? AND TakeFoodTime  = ? AND PatPositionID=?", sumCanCi, OrderGVCanCiAdapter.current_canciID, OrderActivity.currentOrderDate,
					PatientsListActivity.currentBedID);

		}
		Map mapCanLei = Db.selectUnique("SELECT * FROM NumsMark WHERE FoodTypeID = ? AND TakeFoodTime  = ? AND PatPositionID=? ", OrderGVCanLeiAdapter.current_canleiID,
				OrderActivity.currentOrderDate, PatientsListActivity.currentBedID);
		// 判断当前餐类是否有订餐记录 如果有就更新数量 没有就新增数据
		if (mapCanLei == null) {
			sumCanLei = 0;

			sumCanLei++;

			Db.insert("INSERT INTO NumsMark (ID,FoodTypeID,Nums,TakeFoodTime ,PatPositionID,PayWayID,Order_ID) VALUES(?,?,?,?,?,?,?)", StringUtils.uid(), OrderGVCanLeiAdapter.current_canleiID,
					sumCanLei, OrderActivity.currentOrderDate, PatientsListActivity.currentBedID, payWayID, Order_ID);

		} else {

			sumCanLei = Integer.parseInt(mapCanLei.get("Nums") + "");
			sumCanLei++;

			Db.update("update NumsMark set Nums = ? WHERE FoodTypeID = ? AND TakeFoodTime  = ? AND PatPositionID=?", sumCanLei, OrderGVCanLeiAdapter.current_canleiID, OrderActivity.currentOrderDate,
					PatientsListActivity.currentBedID);
		}
	}

	/**
	 * 统计现金和月结的已选数量
	 * 
	 * @param id
	 *            Food_ID
	 * @param payWayID
	 *            支付方式ID
	 * @param sumMark
	 *            总量
	 */
	private void upxian_yue(String id, String payWayID, final TextView sumMark) {

		Map map = Db
				.selectUnique(
						"select b.Count,b.PayWayID from [Order] a inner join OrderDetail b WHERE a.ID = b.Order_ID and b.TakeFoodTime = ? AND b.MealTypeID =? AND b.Food_ID = ? and a.[PatPositionID] = ? and b.PayWayID =? and b.FoodAdviceIDList=?",
						OrderActivity.currentOrderDate, OrderGVCanCiAdapter.current_canciID, id, PatientsListActivity.currentBedID, payWayID, OrderGVCanLeiAdapter.current_FoodAdviceIDList);
		// 判断当前订单是否为月结 如果是就更新月结数量 不是就新增数据
		if (map != null) {

			sumXianJin_YueJie = Integer.parseInt(map.get("Count") + "");
			// if(mode==0&&PatientsListActivity.beds_list.get(PatientsListActivity.currentPatientID)
			// .get("order_id") != null){
			// sumXianJin_YueJie=0;
			// }
			sumXianJin_YueJie++;

			sumMark.post(new Runnable() {

				@Override
				public void run() {
					sumMark.setText("" + sumXianJin_YueJie);

					sumMark.setVisibility(View.VISIBLE);

				}

			});

			addMeal(id, sumXianJin_YueJie, payWayID);

		} else {
			sumXianJin_YueJie = 0;
			sumXianJin_YueJie++;

			sumMark.post(new Runnable() {

				@Override
				public void run() {
					sumMark.setText("" + sumXianJin_YueJie);

					sumMark.setVisibility(View.VISIBLE);

				}

			});

			addMeal(id, sumXianJin_YueJie, payWayID);

		}

	}

	/**
	 * 下订单线程
	 * 
	 * @author Administrator
	 * 
	 */
	private class SubmitOrder implements Runnable {

		String id;
		String payWayID;

		TextView summark;

		public SubmitOrder(String id, String PayWayID, TextView summark) {

			this.id = id;
			this.payWayID = PayWayID;
			this.summark = summark;

		}

		@Override
		public void run() {

			upxian_yue(id, payWayID, summark);

			upcanci_canlei(id, payWayID);

			handler.obtainMessage(1).sendToTarget();
			handler.obtainMessage(2).sendToTarget();

		}
	}

}
