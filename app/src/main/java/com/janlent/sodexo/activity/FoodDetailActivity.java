package com.janlent.sodexo.activity;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.sodexo.widget.MulitPointTouchListener;
import com.janlent.utils.Db;
import com.janlent.utils.ImgUtil;
import com.janlent.utils.Log;
import com.jenlant.sodexo.R;

/**
 * 套餐详情界面
 * 
 * @author Administrator
 * 
 */
public class FoodDetailActivity extends BaseActivity implements OnClickListener {

	private TextView foodOrderDetail; // 显示菜单订单详情
	private TextView bed_num; // 显示床位号
	private TextView bed_patient_num; // 显示床位病员号
	private TextView bed_patient_name; // 显示床位病员名
	private TextView advice; // 显示医嘱
	private TextView food_name; // 显示菜单名
	private TextView food_price; // 显示菜单价格
	private TextView stuff; // 显示菜单简介
	private TextView flavor; // 显示菜单简介
	private TextView fitPeople; // 显示菜单简介
	private TextView technology; // 显示菜单简介
	private TextView remark; // 显示菜单简介
	private TextView allergy; // 显示过敏原

	private ImageView food_detail_img; // 显示菜单图片

	private Intent intent;

	private int position; // 显示楼层信息
	private Bitmap pic;

	private String ID;
	private String select_Advice="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.food_detail);

		initTop();
		initPtInfo();

		foodOrderDetail = (TextView) findViewById(R.id.food_detail);

		TextPaint tp = foodOrderDetail.getPaint();
		tp.setFakeBoldText(true);

	}

	/**
	 * 初始化上面title按钮
	 */
	private void initTop() {
		Button back = (Button) findViewById(R.id.back);
		Button camera = (Button) findViewById(R.id.camera);

		back.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				FoodDetailActivity.this.finish();
			}
		});

		camera.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FoodDetailActivity.this,
						CameraActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("bedNum") != null
				&& PatientsListActivity.beds_list.size() > PatientsListActivity.currentPatientID) {
			bed_num.setText(PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("bedNum")
					+ "");
		}
		// 显示病人住院号
		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("PID") != null) {
			bed_patient_num.setText(PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("PID")
					+ "");
		}

		// 显示病人名称
		if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("PatientName") != null) {
			bed_patient_name.setText(PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("PatientName")
					+ "");
		}
        //TODO 7-29显示传递的医嘱
		if(getIntent().getStringExtra("select_Advice")!=null){
			select_Advice=getIntent().getStringExtra("select_Advice");
			advice.setText(select_Advice);
		}
		// 显示医嘱信息
	/*	if (PatientsListActivity.beds_list.get(
				PatientsListActivity.currentPatientID).get("Advice") != null) {
			String advice_a = PatientsListActivity.beds_list.get(
					PatientsListActivity.currentPatientID).get("Advice")
					+ "";
			advice.setText(advice_a.substring(0, advice_a.length() - 1));
		}
		else{
			advice.setText("普食");
		}*/
		// 显示食物详情
		initFoodInformation();
	}

	/**
	 * 初始化病人详情
	 */
	private void initPtInfo() {

		bed_num = (TextView) findViewById(R.id.bed_num);
		bed_patient_num = (TextView) findViewById(R.id.bed_patient_num);
		bed_patient_name = (TextView) findViewById(R.id.bed_patient_name);
		advice = (TextView) findViewById(R.id.advice);

		Button patint_detail = (Button) findViewById(R.id.patint_detail);

		patint_detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FoodDetailActivity.this,
						PatientInfoActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 初始化套餐详情
	 */
	private void initFoodInformation() {

		food_detail_img = (ImageView) findViewById(R.id.food_detail_img);

		food_name = (TextView) findViewById(R.id.food_name);

		food_price = (TextView) findViewById(R.id.food_price);

		stuff = (TextView) findViewById(R.id.stuff);
		flavor = (TextView) findViewById(R.id.flavor);
		fitPeople = (TextView) findViewById(R.id.fitPeople);
		technology = (TextView) findViewById(R.id.technology);
		remark = (TextView) findViewById(R.id.remark);
		allergy = (TextView) findViewById(R.id.allergy);

		Intent intent = getIntent();

		ID = intent.getStringExtra("ID");

		Map<String, Object> foodMap = Db
				.selectUnique(
						"select ID,FoodName,UnitPrice,Stuff,PicAddress,Flavor,FitPeople,Technology,Remark from food where ID =? ",
						ID);

		if (foodMap != null) {

			List<Map<String, Object>> list = Db
					.select("select Name from Allergy where id in (select Allergy_ID from FoodAllergy where food_id = ?)",
							ID);

			StringBuffer str = null;
			if (list != null) {
				str = new StringBuffer();
				for (int i = 0; i < list.size(); i++) {

					str.append(list.get(i).get("Name") + "，");

				}

			}

			food_name.setText(foodMap.get("FoodName") + "");
			food_price.setText(foodMap.get("UnitPrice") + "元/份");

			stuff.setText("原料：" + foodMap.get("Stuff"));
			allergy.setText("饮食禁忌："
					+ (str == null ? "" : str.substring(0, str.length() - 1)));

			flavor.setText("口味：" + foodMap.get("Flavor"));
			fitPeople.setText("适合人群：" + foodMap.get("FitPeople"));
			technology.setText("制造工艺：" + foodMap.get("Technology"));
			remark.setText("营养描述：" + foodMap.get("Remark"));

		}

		String picPath = foodMap.get("PicAddress") + "";

		// 如果数据库图片地址不为空，就根据地址到文件夹中取图片,如果为空就设置默认显示图片
		if (picPath != null && picPath.length() > 1) {

			pic = ImgUtil.showImage(picPath, 512);

			// 如果获取的bitmap不为空，就设置显示图片,或者显示默认提示图片

			if (pic != null) {

				food_detail_img.setImageBitmap(pic);

			} else {

				food_detail_img.setBackgroundResource(R.drawable.wu2);

			}

		} else {

			food_detail_img.setBackgroundResource(R.drawable.wu2);

		}

		food_detail_img.setOnClickListener(this);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.finish();

			OrderActivity.isInitData = false;

			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// Intent intent = new Intent(FoodDetailActivity.this,
		// ShowFoodPicActivity.class);

		showImgDialog(pic);

	}

	private void showImgDialog(Bitmap bitmap) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.show();

		Window window = alertDialog.getWindow();
		// *** 主要就是在这里实现这种效果的.
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(R.layout.showfoodpic);

		ImageView imageView = (ImageView) window.findViewById(R.id.foodimage);

		imageView.setOnTouchListener(new MulitPointTouchListener(imageView));

		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.wu2);
		}

	}
}
