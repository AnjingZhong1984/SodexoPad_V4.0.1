package com.janlent.sodexo.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.utils.Db;
import com.janlent.utils.Log;
import com.janlent.utils.LogService;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

/**
 * 登录界面
 * 
 * @author Administrator
 * 
 */
public class LoginActivity extends BaseActivity implements OnClickListener {

	private Spinner name; // 显示下拉菜单

	private EditText pass; // 用来输入密码
	private Button ok;
	private Button cancel;

	private TextView versionName;

	private List<String> users = new ArrayList<String>(); // 用户名集合

	private String padName; // 用来显示Pad名
	private String hisCode; // 用来显示医院代号
	private Map<String, Object> userMap = new HashMap<String, Object>(); // 用来保存用户帐号、设备编号

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		Intent intent = new Intent(this, LogService.class);
		startService(intent);

		// 检查数据库文件是否存在
		File databaseFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/database/Sodexo.sqlite");
		if (!databaseFile.exists()) {

			try {

				copyDataBase();

				Toast.makeText(this, "首次运行数据库初始化成功！", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!databaseFile.exists()) {
			Toast.makeText(this, "数据库丢失，请检查SDcard/Sodexo/database/Sodexo.sqlite是否存在！", Toast.LENGTH_LONG).show();

			this.finish();
		}
		init();

		initTop();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 初始化设备和用户数据
		SharedPreferences sharedata = getSharedPreferences("deviceDate", 0);
		padName = sharedata.getString("device_num", null);
		hisCode = sharedata.getString("hisCode", null);

		Editor userShareData = getSharedPreferences("user", 0).edit();
		userShareData.putString("user", null);

		userShareData.commit();

		new Thread(getUserdata).start();
	}

	private void copyDataBase() throws IOException {
		InputStream mInput = this.getAssets().open("Sodexo.sqlite");

		File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/database/");

		if (!filePath.exists()) {
			filePath.mkdirs();
		}

		String outFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/database/Sodexo.sqlite";
		OutputStream mOutput = new FileOutputStream(outFileName);
		byte[] mBuffer = new byte[1024];
		int mLength;
		while ((mLength = mInput.read(mBuffer)) > 0) {
			mOutput.write(mBuffer, 0, mLength);
		}
		mOutput.flush();
		mOutput.close();
		mInput.close();
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

				Intent intent2 = new Intent(LoginActivity.this, DeviceActivity.class);

				startActivity(intent2);

			}
		});

		camera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(LoginActivity.this, CameraActivity.class);

				startActivity(intent);

			}
		});
	}

	/**
	 * 初始化自定义组件
	 */
	private void init() {
		name = (Spinner) findViewById(R.id.user_name);

		pass = (EditText) findViewById(R.id.user_password);

		ok = (Button) findViewById(R.id.login);

		cancel = (Button) findViewById(R.id.cancel);
		versionName = (TextView) findViewById(R.id.version_name);

		versionName.setText("当前版本：" + getAppVersionName(this));

		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		int id = v.getId();
		switch (id) {
		case R.id.login:

			String uesrname = (name.getSelectedItem() + "").trim();

			String password = pass.getText() + "";

			password = StringUtils.md5(password);
			// 验证数据库中是否有用户
			Map<String, Object> memberIDMap = Db.selectUnique("select MemberID from MemberInfo where LoginName = ? and LOWER(Password) = LOWER(?)", uesrname, password);

			if (memberIDMap != null) {
				// 是初始化用户
				if ("InitUser".equals(uesrname)) {
					Intent intent = new Intent(LoginActivity.this, SynchronousDataActivity.class);

					intent.putExtra("mark", "InitUser");

					startActivity(intent);
				}
				// 看此用户是否用对应楼层信息
				Map<String, Object> position_IDMap = Db.selectUnique("select Position_ID from PositionMemberInfo where MemberInfo_MemberID = ?", memberIDMap.get("MemberID"));

				if (position_IDMap != null) {

					Editor userShareData = getSharedPreferences("user", 0).edit();
					userShareData.putString("user", uesrname);
					userShareData.putString("MemberID", memberIDMap.get("MemberID") + "");
					userShareData.putString("Position_ID", position_IDMap.get("Position_ID") + "");

					userShareData.commit();
					Intent intent = new Intent(LoginActivity.this, SynchronousDataActivity.class);

					startActivity(intent);

					Toast.makeText(this, "登录成功！", Toast.LENGTH_LONG).show();

				} else {

					Editor userShareData = getSharedPreferences("user", 0).edit();
					userShareData.putString("user", uesrname);

					userShareData.putString("MemberID", memberIDMap.get("MemberID") + "");

					userShareData.remove("Position_ID");

					userShareData.commit();

					Toast.makeText(this, "没有对应楼层信息，请联系管理员。", Toast.LENGTH_LONG).show();

				}
			} else {
				Toast.makeText(this, "密码不正确，请重新输入。", Toast.LENGTH_LONG).show();
			}

			break;

		case R.id.cancel:
			showDialog();
			// LoginActivity.this.finish();
			break;

		default:

			break;
		}

	}

	private void showDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("版权信息");
		SharedPreferences sharedata = getSharedPreferences("deviceDate", 0);
		String copyRight = "";
		// 判断是否设备初始化
		if (sharedata.getString("CopyRight", null) == null) {
			copyRight = "暂无版权信息，请先初始化设备参数!";
		} else {
			copyRight = sharedata.getString("CopyRight", null);
		}
		builder.setMessage(copyRight);

		builder.setPositiveButton("确定", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	/**
	 * 自定义适配器
	 * 
	 * @author Administrator
	 * 
	 */
	private class SpinnerAdapter extends ArrayAdapter<String> {
		Context context;
		String[] items = new String[] {};

		public SpinnerAdapter(final Context context, final int textViewResourceId, final String[] objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
			}

			TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
			tv.setText(items[position]);
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(25);
			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
			}

			TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
			tv.setText(items[position]);
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(25);
			return convertView;
		}
	}

	/**
	 * 执行用户数据的处理
	 */
	Runnable getUserdata = new Runnable() {

		@Override
		public void run() {

			// 清除未下单订单
			Db.delete("delete from NumsMark where  OrderMark is null");

			Db.delete("delete from OrderDetail where  OrderMark is null");

			Db.delete("delete from [Order] where OrderMark is null ");

			List<Map<String, Object>> MemberInfoList = Db.select("select LoginName,MemberID from MemberInfo where "
					+ " MemberID in (select MemberInfo_MemberID from PADMemberInfo where PAD_ID = (select ID from PAD where PadName =? and HisCode=?))", padName, hisCode);
			userMap.clear();
			users.clear();

			if (MemberInfoList != null) {

				for (int i = 0; i < MemberInfoList.size(); i++) {
					users.add(MemberInfoList.get(i).get("LoginName") + "");
					userMap.put(MemberInfoList.get(i).get("LoginName") + "", MemberInfoList.get(i).get("MemberID"));

					handler.obtainMessage(0).sendToTarget();

				}

			}
		}
	};

	/**
	 * 给Spinner组件设置适配器
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:
				ArrayAdapter<String> aa = new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_spinner_item, users); // 第二个参数表示spinner没有展开前的UI类型

				// 第三步：为适配器设置下拉列表下拉时的菜单样式。
				aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// 设置适配

				name.setAdapter(new SpinnerAdapter(LoginActivity.this, android.R.layout.simple_spinner_item, users.toArray(new String[0])));

				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Intent intent = new Intent(this, LogService.class);

		stopService(intent);

	}

	// 获取应用版本号
	public String getAppVersionName(Context context) {
		String versionName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			Log.e("Exception" + e.getMessage());
		}
		return versionName;
	}
}
