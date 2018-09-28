package com.janlent.sodexo.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.janlent.sodexo.bean.OrderSummary;
import com.jenlant.sodexo.R;

/*
 * 回复列表
 */
public class OrderSummaryAdapter extends BaseAdapter {
	Context mContext;
	public ArrayList<OrderSummary> mOrderSummaries;
	LayoutInflater inflater = null;

	public OrderSummaryAdapter(Context context, ArrayList<OrderSummary> orderSummaries) {
		this.mContext = context;
		this.mOrderSummaries = orderSummaries;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mOrderSummaries.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	class ViewHolder {
		TextView order_time;
		TextView order_count;
		TextView order_bebnum;
		TextView cash_money;
		TextView month_money;
		TextView all_money;

	}

	public void setList(ArrayList<OrderSummary> orderSummaries) {
		this.mOrderSummaries = orderSummaries;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		OrderSummary orderSummary = mOrderSummaries.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.order_summary_item, null);
			viewHolder = new ViewHolder();
			viewHolder.order_time = (TextView) convertView
					.findViewById(R.id.order_time);
			viewHolder.order_bebnum = (TextView) convertView
					.findViewById(R.id.order_bebnum);
			viewHolder.order_count = (TextView) convertView
					.findViewById(R.id.order_count);
			viewHolder.cash_money = (TextView) convertView
					.findViewById(R.id.cash_money);
			viewHolder.month_money = (TextView) convertView
					.findViewById(R.id.month_money);
			viewHolder.all_money = (TextView) convertView
					.findViewById(R.id.all_money);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if(orderSummary.orderTime.equals("合计")){
			viewHolder.order_time.setTextColor(mContext.getResources().getColor(R.color.more_blue));
			viewHolder.order_time.setTextSize(22.0f);
		}else{
			viewHolder.order_time.setTextColor(mContext.getResources().getColor(R.color.black));
			viewHolder.order_time.setTextSize(16.0f);
		}
		viewHolder.order_time.setText(orderSummary.orderTime);
		viewHolder.order_bebnum.setText(orderSummary.bedNum);
		viewHolder.order_count.setText(orderSummary.orderCount);
		viewHolder.cash_money.setText(orderSummary.cashMoney);
		viewHolder.month_money.setText(orderSummary.monthMoney);
		viewHolder.all_money.setText(orderSummary.allMoney);
		return convertView;
	}
}
