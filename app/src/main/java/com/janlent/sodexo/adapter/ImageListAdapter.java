package com.janlent.sodexo.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jenlant.sodexo.R;

/**
 * 图片文件夹列表界面中ListView所使用的适配器，这里主要显示文件夹名
 * 
 * @author Administrator
 * 
 */
public class ImageListAdapter extends BaseAdapter {

	private List<String> imgFilePaths; // 图片文件夹列表
	private Activity mContext;
	private LayoutInflater mInflater;

	public ImageListAdapter(Activity context, List<String> it) {

		mContext = context;
		imgFilePaths = it;

		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return imgFilePaths.size();
	}

	@Override
	public Object getItem(int position) {
		return imgFilePaths.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.img_fileitem, null);
			holder = new ViewHolder();
			holder.tv1 = (TextView) convertView
					.findViewById(R.id.textViewfileName);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.imageViewFile);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv1.setText(imgFilePaths.get(position));
		return convertView;
	}

	public class ViewHolder {

		TextView tv1;
		ImageView imageView;

	}

}
