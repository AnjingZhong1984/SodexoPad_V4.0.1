package com.janlent.sodexo.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.janlent.sodexo.activity.ShowDatabaseActivity;
import com.jenlant.sodexo.R;

/**
 * 数据库数据界面中listView的适配器，这里主要显示数据名
 * 
 * @author Administrator
 * 
 */
public class ShowDataAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<String> datas;
	private LayoutInflater mInflater;

	public ShowDataAdapter(Context context, ArrayList<String> it) {
		mContext = context;
		datas = it;

		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {

		if (datas == null || datas.size() < 1) {
			return 0;
		}

		return datas.size();
	}

	@Override
	public String getItem(int arg0) {
		return datas.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.showdataitem, null);
			holder = new ViewHolder();
			holder.f_text = (TextView) convertView
					.findViewById(R.id.textViewName);
			holder.pic = (ImageView) convertView
					.findViewById(R.id.pic_database);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();

			holder.f_text.setText("");
		}

		if ("DownOffLineData".equals(ShowDatabaseActivity.mark)) {
			holder.pic.setBackgroundResource(R.drawable.download_database);
		}
		if ("DBbackup".equals(ShowDatabaseActivity.mark)) {
			holder.pic.setBackgroundResource(R.drawable.bg_database);
		}
		if ("UpOffLineData".equals(ShowDatabaseActivity.mark)) {
			holder.pic.setBackgroundResource(R.drawable.upload_database);
		}

		holder.f_text.setText(datas.get(position).toString());

		return convertView;
	}

	private class ViewHolder {
		TextView f_text;

		ImageView pic;
	}
}
