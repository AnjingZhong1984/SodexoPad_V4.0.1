package com.janlent.sodexo.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.janlent.utils.Config;
import com.jenlant.sodexo.R;

/**
 * 横向ListView
 * 
 * 
 */
public class HorizontalListView extends LinearLayout {
	private View view;
	private btnListener listener;
	private List<TextView> textViews = new ArrayList<TextView>();
	private Context myContext;
	private LinearLayout layout;
	private List<Map<String, Object>> list;

	public HorizontalListView(Context context) {
		super(context);
		myContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.view_orizontal_listview, null);
		layout = (LinearLayout) view.findViewById(R.id.layout_view_select_btn);
	}

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.view_orizontal_listview, null);
		layout = (LinearLayout) view.findViewById(R.id.layout_view_select_btn);
	}

	private TextView getTextView(String title, final int position) {
		TextView titleView = new TextView(myContext);
		titleView.setGravity(Gravity.CENTER);
		titleView.setTextColor(getResources().getColor(R.color.order_left_btn_click));

		titleView.setText(title);
		LayoutParams params = new LayoutParams(getWeightItem(), (int) (60 * Config.DENSITY));
		params.setMargins(1, 10, 1, 10);
		titleView.setLayoutParams(params);
		titleView.setBackgroundColor(myContext.getResources().getColor(R.color.white_bt));
		titleView.setTextSize(myContext.getResources().getDimension(R.dimen.text18));
		titleView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setOnclick(position);
			}

		});
		return titleView;
	}

	private int getWeightItem() {
		// int weightAll = (int) (Config.SCREEN_WIDTH - 46 * Config.DENSITY);
		int weightItem = (int) (120 * Config.DENSITY);

		return weightItem;
	}

	public void setDate(List<Map<String, Object>> list, btnListener listener) {
		this.listener = listener;
		this.list = list;
		layout.removeAllViews();
		textViews.clear();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				String text = (String) list.get(i).get("TypeName");
				TextView _tv = getTextView(text, i);
				_tv.setTextSize(18);
				layout.addView(_tv);
				textViews.add(_tv);
			}
		}
	}

	public View getView() {
		return view;
	}

	public void setOnclick(int id) {
		listener.btnOnclick(list.get(id));
		layout.removeAllViews();
		for (int i = 0; i < textViews.size(); i++) {
			TextView _view = textViews.get(i);
			if (i == id) {
				_view.setBackgroundColor(myContext.getResources().getColor(R.color.title_blue));
				// _view.setTextColor(myContext.getResources().getColor(R.color.order_left_btn_click));
			} else {
				_view.setBackgroundColor(myContext.getResources().getColor(R.color.white_bt));
				// _view.setTextColor(myContext.getResources().getColor(R.color.black));
			}
			layout.addView(_view);
		}
	}

	public interface btnListener {
		public void btnOnclick(Map<String, Object> map);
	}

}
