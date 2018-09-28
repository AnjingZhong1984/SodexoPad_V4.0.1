package com.janlent.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Environment;

/**
 * 字节流复制
 * 
 * @author Administrator
 * 
 */
public class Utils {

	/**
	 * 复制
	 * 
	 * @param is
	 *            输入流
	 * @param os
	 *            输出流
	 */
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	// 导出订单详情
	public static void ExportOrderdetail(String path, String name) {
		// Cursor
		// cursor=Db.DATABASE.rawQuery("select b.ID,c.HisCode,b.PID
		// ,b.TakeFoodTime ,b.MealTypeID ,b.PayWayID ,b.Count
		// ,b.Order_ID,b.Food_ID,b.FoodAdviceIDList,a.FoodName ,a.UnitPrice as
		// FoodUnitPrice from food a,orderdetail b, [order] c "
		// + " where a.id=b.food_id and b.Order_ID=c.id "
		// + " and TakeFoodTime >= ?",
		// new String[]{ConvertUtils.date2Str("yyyy-MM-dd HH:mm")});
		Cursor cursor = Db.DATABASE.rawQuery(
				"select a.device pad_id,a.writeadminName operation_name,a.pid order_number,c.name position," + "g.pid patientCode,g.patientName,"
						+ "a.foodadviceidlist positionadvice,a.writetime order_time,"
						+ "b.takefoodtime,d.name meal_times,e.foodname,b.[FoodAdviceIDList] foodAdvices, e.[UnitPrice],b.[count] num,f.name pay_method"
						+ " from [Order] a" + " inner join OrderDetail b on a.id=b.order_id" + " left join position c on c.[ID]=a.[PatPositionID]"
						+ " left join enumbase d on d.id=b.[MealTypeID]" + " left join food e on e.id=b.[Food_ID]"
						+ " left join enumbase f on f.id=b.[PayWayID]" + " left join patient g on a.[patient_Id]=g.id" + " and  TakeFoodTime >= ?",
				new String[] { ConvertUtils.date2Str("yyyy-MM-dd") });
		// 判断cursor是否为空
		if (cursor != null) {
			Log.e("ExportToCSV");
			Utils.ExportToCSV(cursor, path, name);
		} else {
			Log.e("cursor=====null");
		}
	}

	// 将医嘱id转中文
	public static String updateString(String foodStr) {
		// 不为空，将，隔开并查询
		StringBuffer foodAdvices = new StringBuffer();
		if (!foodStr.equals("") && !foodStr.equals("null")) {
			String[] strs = foodStr.split(",");
			foodAdvices = new StringBuffer();
			for (int i = 0; i < strs.length; i++) {
				if (strs[i] != null && !strs[i].equals("")) {
					Map<String, Object> map = Db.selectUnique("select Advice from  FoodAdvice	where ID=?", strs[i]);
					foodAdvices.append(map.get("Advice") + ";");
				}
			}
		}
		String resultstr = foodAdvices.toString();
		if (resultstr.endsWith(";")) {
			resultstr = resultstr.substring(0, resultstr.length() - 1);
		}
		return resultstr;
	}

	public static void ExportToCSV(Cursor c, String path, String fileName) {

		int rowCount = 0;
		int colCount = 0;
		BufferedWriter bfw;
		File sdCardDir = new File(path);

		if (!sdCardDir.exists()) {
			sdCardDir.mkdirs();
		}
		// File sdCardDir = Environment.getExternalStorageDirectory();
		File saveFile = new File(sdCardDir, fileName);
		try {

			rowCount = c.getCount();
			colCount = c.getColumnCount();
			FileOutputStream fr = new FileOutputStream(saveFile);
			OutputStreamWriter brs = new OutputStreamWriter(fr, "GB2312");
			bfw = new BufferedWriter(brs);
			if (rowCount > 0) {
				c.moveToFirst();
				int colposition = 0; // 病人医嘱列
				int foodposition = 0; // 饮食医嘱列

				// 写入表头
				for (int i = 0; i < colCount; i++) {
					String colStr = "";
					if (c.getColumnName(i).equals("pad_id")) {
						colStr = "iPad[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("operation_name")) {
						colStr = "点餐员（" + c.getColumnName(i) + "）";
					} else if (c.getColumnName(i).equals("order_number")) {
						colStr = "订单号[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("position")) {
						colStr = "床位号[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("patientCode")) {
						colStr = "住院号[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("PatientName")) {
						colStr = "病人姓名[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("positionadvice")) {
						colStr = "病人医嘱[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("order_time")) {
						colStr = "订餐时间[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("TakeFoodTime")) {
						colStr = "饮食时间[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("meal_times")) {
						colStr = "餐次[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("FoodName")) {
						colStr = "名称[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("foodAdvices")) {
						colStr = "菜谱医嘱[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("UnitPrice")) {
						colStr = "单价[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("num")) {
						colStr = "数量[" + c.getColumnName(i) + "]";
					} else if (c.getColumnName(i).equals("pay_method")) {
						colStr = "付款方式[" + c.getColumnName(i) + "]";
					} else {
						colStr = c.getColumnName(i);
					}
					if (c.getColumnName(i).equals("positionadvice")) {
						colposition = i;
					}
					if (c.getColumnName(i).equals("foodAdvices")) {
						foodposition = i;
					}
					if (i != colCount - 1)
						bfw.write(colStr + ',');
					else
						bfw.write(colStr);
				}
				// 写好表头后换行
				bfw.newLine();

				// 写入数据
				for (int i = 0; i < rowCount; i++) {
					c.moveToPosition(i);
					for (int j = 0; j < colCount; j++) {
						String colStr = "";
						if (j == colposition) {
							colStr = updateString(c.getString(j));
						} else if (j == foodposition) {
							colStr = updateString(c.getString(j));
						} else {
							colStr = c.getString(j);
						}
						if (j != colCount - 1)
							bfw.write(colStr + ',');
						else
							bfw.write(colStr);
					}
					// 写好每条记录后换行
					bfw.newLine();
				}
			}
			// 将缓存数据写入文件
			bfw.flush();
			// 释放缓存
			bfw.close();
			Log.e("导出完毕！");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			c.close();
		}
	}

	// 返回当天日期
	public static String currentDate() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = sdf.format(date);
		return currentDate;
	}

	/**
	 * 字符串转换成date类型
	 * 
	 * @param stringDate
	 *            需要转换的字符串
	 * @param format
	 *            转换的字符串格式
	 * @return
	 */
	// 格式化日期如yyyy-MM-dd转化为yyyy/MM/dd
	public static String formatDate(String date) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(date));
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** 写在/mnt/sdcard/目录下面的文件 */
	public static void writeFileSdcard(String fileName, String message) {
		File F = new File(fileName);
		if (!F.exists())
			try {
				F.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		try {
			// FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
			FileOutputStream fout = new FileOutputStream(fileName);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}