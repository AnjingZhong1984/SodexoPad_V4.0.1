package com.janlent.sodexo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.janlent.sodexo.activity.PatientInfoActivity;
import com.jenlant.sodexo.R;

/**
 * 病员详情界面中sexList的适配器，主要显示性别的选项
 * 
 * @author Administrator
 * 
 */
public class SexAdapter extends BaseAdapter {

	private String[] sexList = new String[] { "男", "女", "未知" };

	private Activity context;

	private TextView sexText;

	private PopupWindow pop;
	private LayoutInflater inflater;

	public SexAdapter(Activity context, TextView sexText, PopupWindow pop) {

		this.context = context;

		this.sexText = sexText;

		this.pop = pop;

		inflater = context.getLayoutInflater();

	}

	@Override
	public int getCount() {
		return sexList.length;
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
		View view = inflater.inflate(R.layout.sex_item, null);

		TextView sex = (TextView) view.findViewById(R.id.sex_item);

		sex.setText(sexList[position]);

		final String str = sexList[position];

		sex.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				//TODO 08-06:解决了选择性别，崩溃，原因：事件分发出现空指针
				if(pop!=null&&pop.isShowing()){
					pop.dismiss();
				}
				PatientInfoActivity.isShow_1 = false;
				sexText.setText(str);
				return true;
			}
		});
		return view;
	}

}
