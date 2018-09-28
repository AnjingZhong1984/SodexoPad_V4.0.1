package com.janlent.sodexo.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.janlent.sodexo.activity.base.BaseActivity;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.Db;
import com.janlent.utils.StringUtils;
import com.jenlant.sodexo.R;

/**
 * 设备参数设置界面
 * 
 * @author Administrator
 * 
 */
public class DeviceActivity extends BaseActivity implements OnClickListener {

	private Button ok; // 用来提交数据
	private Button modify; // 用来修改数据

	private EditText deviceNum; // 用来输入设备编码
	private EditText hisCode; // 用来输入医院编号
	private EditText bg_address; // 用来输入数据地址
	private EditText down_api; // 用来输入下载接口
	private EditText up_api; // 用来输入上传接口
	private EditText orderSumWay; // 每个订单钱数汇总方式
	private EditText yueJieWay; // 月结方式
	private EditText generalFood; // 普食字段，这里输的字段必须和服务端指定的普食字段相同
	private EditText androidId; // 设备唯一标识码
	private EditText deviceIp; // 设备ip地址
	private EditText modeDis; // 配送方式
	private EditText copyright; // 版权信息

	private LinearLayout l_pro;// 显示初始化提示的布局

	private TextView actionTishi;// 显示初始化提示的文本
	String modifyIp = ""; // 要修改的后台地址
	String modifyCopyRight = ""; // 要修改的版权信息
	String modifyHisCode = ""; // 医院代码

	// TODO 修改过的类
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.device);
		ok = (Button) findViewById(R.id.ok);
		modify = (Button) findViewById(R.id.modify);

		deviceNum = (EditText) findViewById(R.id.device_num);
		hisCode = (EditText) findViewById(R.id.hiscode);
		bg_address = (EditText) findViewById(R.id.bg_address);
		down_api = (EditText) findViewById(R.id.down_api);
		up_api = (EditText) findViewById(R.id.up_api);
		orderSumWay = (EditText) findViewById(R.id.order_sum_way);
		yueJieWay = (EditText) findViewById(R.id.yuejie_way);
		generalFood = (EditText) findViewById(R.id.general_food);
		androidId = (EditText) findViewById(R.id.android_id);
		deviceIp = (EditText) findViewById(R.id.device_ip);
		modeDis = (EditText) findViewById(R.id.device_mode);
		copyright = (EditText) findViewById(R.id.copyright);

		l_pro = (LinearLayout) findViewById(R.id.l_pro);
		actionTishi = (TextView) findViewById(R.id.action_tishi);

		ok.setOnClickListener(this);
		modify.setOnClickListener(this);

		// 获取SharedPreferences的参数数据
		SharedPreferences sharedata = getSharedPreferences("deviceDate", 0);
		String num = sharedata.getString("device_num", "000");
		String code = sharedata.getString("hisCode", null);
		String bgApi = sharedata.getString("bgApi", null);
		String downApi = sharedata.getString("downApi", null);
		String upApi = sharedata.getString("upApi", null);
		String orderWay = sharedata.getString("orderWay", null);
		String yueJie = sharedata.getString("yueJieWay", null);
		String generalFoodStr = sharedata.getString("generalFood", null);
		String copyRight = sharedata.getString("CopyRight", null);

		String mode = sharedata.getString("mode", null);
		Log.i("device", "mode===" + mode);
		String mDeviceID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);// 获取设备的唯一标识码
//		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
		String ip = null;
		if (null != info) {
			ip = ConvertUtils.int2ip(info.getIpAddress()); // 获取设备的ip地址

		}

		if (num != null) {
			deviceNum.setText(num);
		}

		if (code != null) {
			hisCode.setText(code);
		}

		if (bgApi != null) {
			bg_address.setText(bgApi);
		}

		if (downApi != null) {
			down_api.setText(downApi);
		}

		if (upApi != null) {
			up_api.setText(upApi);
		}

		if (orderWay != null) {
			orderSumWay.setText(orderWay);
		}

		if (yueJieWay != null) {
			yueJieWay.setText(yueJie);
		}

		if (generalFoodStr != null) {
			generalFood.setText(generalFoodStr);
		}
		if (mDeviceID != null) {
			androidId.setText(mDeviceID);
		}

		if (ip != null) {
			deviceIp.setText(ip);
		}
		if (mode != null) {
			modeDis.setText(mode);
		}
		if (copyRight != null) {
			copyright.setText(copyRight);
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {

		case R.id.ok:
			// 设置设备参数
			showDialog();
			break;
		case R.id.modify:
			// 设置设备参数
			modifyIp = bg_address.getText().toString().trim();
			modifyCopyRight = copyright.getText().toString().trim();
			modifyHisCode = hisCode.getText().toString().trim();
			Editor sharedata = getSharedPreferences("deviceDate", 0).edit();
			sharedata.putString("bgApi", modifyIp);
			sharedata.putString("hisCode", modifyHisCode);
			sharedata.putString("CopyRight", modifyCopyRight);
			sharedata.commit();
			Toast.makeText(DeviceActivity.this, "修改成功!", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}

	}

	private Map<String, Object> initConfig() {
		try {

			Map<String, Object> configMap = new HashMap<String, Object>();

			XmlPullParser xmlparser = Xml.newPullParser();
			ii("start");
			FileInputStream in = new FileInputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/" + "sodexo_config.xml"));
			ii("in");
			if (in != null) {
				ii("in != null");
				xmlparser.setInput(in, "UTF-8");

				int evtType = xmlparser.getEventType();
				while (evtType != XmlPullParser.END_DOCUMENT) {
					ii("evtType:" + evtType);
					switch (evtType) {
					case XmlPullParser.START_TAG:
						String tag = xmlparser.getName();
						if (tag.equalsIgnoreCase("HisCode")) {

							configMap.put("HisCode", xmlparser.nextText());

						}

						if (tag.equalsIgnoreCase("DeviceName")) {

							configMap.put("DeviceName", xmlparser.nextText());

						}
						if (tag.equalsIgnoreCase("ServerUrl")) {

							configMap.put("ServerUrl", xmlparser.nextText());

						}
						if (tag.equalsIgnoreCase("SyncMoblie")) {

							configMap.put("SyncMoblie", xmlparser.nextText());

						}
						if (tag.equalsIgnoreCase("SyncServer")) {

							configMap.put("SyncServer", xmlparser.nextText());

						}
						if (tag.equalsIgnoreCase("StatisticsName")) {

							configMap.put("StatisticsName", xmlparser.nextText());

						}
						if (tag.equalsIgnoreCase("YueJie")) {

							configMap.put("YueJie", xmlparser.nextText());
						}

						if (tag.equalsIgnoreCase("PotFoodAdvice")) {

							configMap.put("PotFoodAdvice", xmlparser.nextText());
						}
						if (tag.equalsIgnoreCase("ModeDistribution")) {

							configMap.put("Mode", xmlparser.nextText());

						}
						if (tag.equalsIgnoreCase("CopyRight")) {
							configMap.put("CopyRight", xmlparser.nextText());
						}
						break;

					default:

						break;

					}

					evtType = xmlparser.next();
				}
			}

			return configMap;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 提示用户操作的对话框
	 * 
	 * @param tatile
	 *            提示用户将做操作的字符串
	 */
	private void showDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("提示");
		builder.setMessage("点击确定将会删除数据库所有数据，恢复到数据库初始化状态，建议点击确定前先备份数据库。");

		builder.setPositiveButton("确定", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// try {
				// copyConfigBase();
				// } catch (IOException e1) {
				// e1.printStackTrace();
				// }
				final Map<String, Object> configMap = initConfig();

				// 如果初始化配置文件没找到，就提示用户检查文件，要是初始化胚子文件纯在，就初始化数据库和配置文件
				// 这时数据库将会被清空，恢复到软件初始安装状态

				if (configMap == null) {
					Toast.makeText(DeviceActivity.this, "确认sodexo_config.xml初始化文件是否存在？", Toast.LENGTH_SHORT).show();

				} else {

					hand.obtainMessage(0, "软件初始化中...").sendToTarget();

					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								copyDataBase();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							Editor sharedata = getSharedPreferences("deviceDate", 0).edit();

							sharedata.putString("hisCode", configMap.get("HisCode") + "");
							sharedata.putString("device_num", configMap.get("DeviceName") + "");

							String ServerUrl = configMap.get("ServerUrl") + "";

							if (!ServerUrl.endsWith("/")) {
								ServerUrl = ServerUrl + "/";
							}
							sharedata.putString("bgApi", ServerUrl);

							sharedata.putString("downApi", configMap.get("SyncMoblie") + "");
							sharedata.putString("upApi", configMap.get("SyncServer") + "");
							sharedata.putString("orderWay", configMap.get("StatisticsName") + "");
							sharedata.putString("yueJieWay", configMap.get("YueJie") + "");
							sharedata.putString("generalFood", configMap.get("PotFoodAdvice") + "");
							sharedata.putString("CopyRight", configMap.get("CopyRight") + "");
							// String hisCode=configMap.get("HisCode") + "";
							// //如果输入医院代码不为空且不等于xml文件中，则修改
							// if(!modifyHisCode.equals("")&&!modifyHisCode.equals(hisCode)){
							// sharedata.putString("hisCode", modifyHisCode);
							// }else{//初始化xml中的hiscode
							// sharedata.putString("hisCode", hisCode);
							// }
							// sharedata.putString("device_num",
							// configMap.get("DeviceName") + "");
							//
							// String ServerUrl = configMap.get("ServerUrl") +
							// "";
							//
							// if (!ServerUrl.endsWith("/")) {
							// ServerUrl = ServerUrl + "/";
							// }
							// //如果输入ip不为空且不等于xml文件中，则修改
							// if(!modifyIp.equals("")&&!modifyIp.equals(ServerUrl)){
							// sharedata.putString("bgApi", modifyIp);
							// }else{//初始化xml中的ip
							// sharedata.putString("bgApi", ServerUrl);
							// }
							// sharedata.putString("downApi",
							// configMap.get("SyncMoblie") + "");
							// sharedata.putString("upApi",
							// configMap.get("SyncServer") + "");
							// sharedata.putString("orderWay",
							// configMap.get("StatisticsName") + "");
							// sharedata.putString("yueJieWay",
							// configMap.get("YueJie") + "");
							// sharedata.putString("generalFood",
							// configMap.get("PotFoodAdvice") + "");
							// //如果输入CopyRight不为空且不等于xml文件中，则修改
							// if(!modifyCopyRight.equals("")&&!modifyCopyRight.equals(configMap.get("CopyRight")
							// + "")){
							// sharedata.putString("CopyRight",
							// modifyCopyRight);
							// }else{//初始化xml中的CopyRight
							// sharedata.putString("CopyRight",
							// configMap.get("CopyRight") + "");
							// }
							String mode = configMap.get("Mode") + "";
							sharedata.putString("mode", mode);
							if (mode.contains("床位")) {
								sharedata.putInt("checkMode", 0);
								Log.i("mode---------", "--mode-0==" + mode);
							} else {
								Log.i("mode---------", "--mode-1==" + mode);
								sharedata.putInt("checkMode", 1);
							}
							String padID = StringUtils.uid();
							String MemberInfoID = StringUtils.uid();

							Db.insert("insert into PAD (ID,HisCode,PadName) VALUES(?,?,?)", padID, configMap.get("HisCode") + "", configMap.get("DeviceName") + "");
							Db.insert("insert into MemberInfo  (MemberID,HisCode,LoginName,Password ) VALUES(?,?,?,?)", MemberInfoID, configMap.get("HisCode") + "", "InitUser",
									StringUtils.md5("111111"));
							Db.insert("insert into PADMemberInfo (PAD_ID,MemberInfo_MemberID) VALUES(?,?)", padID, MemberInfoID);

							sharedata.commit();

							hand.obtainMessage(1).sendToTarget();

							DeviceActivity.this.finish();

						}
					}).start();

				}
			}
		});

		builder.setNegativeButton("取消", null);
		builder.show();
	}

	/**
	 * 将存在Assets中的配置文件sodexo_config.xml存到本地，如果本地已经存在则返回
	 * 
	 * add：2017-8-8 15:52:49
	 * 
	 * @throws IOException
	 */
	private void copyConfigBase() throws IOException {
		InputStream mInput = this.getAssets().open("sodexo_config.xml");

		File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/");

		if (!filePath.exists()) {
			filePath.mkdirs();
		}

		File configFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/sodexo_config.xml");
		if (configFile.exists()) {
			return;
		}

		String outFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/sodexo_config.xml";
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
	 * 初始化数据库方法
	 * 
	 * @throws IOException
	 */
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

	Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:

				actionTishi.setText(msg.obj + "");
				l_pro.setVisibility(View.VISIBLE);

				break;

			case 1:

				Toast.makeText(DeviceActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}

	};

}
