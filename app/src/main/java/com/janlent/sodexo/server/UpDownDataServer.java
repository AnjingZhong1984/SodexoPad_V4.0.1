package com.janlent.sodexo.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;

import com.janlent.utils.CollectionUtils;
import com.janlent.utils.ConvertUtils;
import com.janlent.utils.DataBaseHelper;
import com.janlent.utils.Db;
import com.janlent.utils.DbUtils;
import com.janlent.utils.HttpUtils;
import com.janlent.utils.ImgUtil;
import com.janlent.utils.Log;
import com.janlent.utils.StringUtils;
import com.janlent.utils.Utils;

/**
 * 在线上传和同步数据
 * 
 * @author Administrator
 * 
 */
public class UpDownDataServer {

	/**
	 * 1.传入关联表数据List和主键Key 2.将数据保存在Map<key,list>中
	 * 
	 * @param list
	 *            传入关联表的数据List
	 * @param strKey
	 *            传入关联表的主键key
	 * @return Map 将主键key和数据List以Map的形式保存
	 */
	private static Map<String, List<Map<String, Object>>> list2Map(List<Map<String, Object>> list, String strKey) {
		// TODO
		Map<String, List<Map<String, Object>>> ret;
		if (list == null || list.size() == 0) {
			return new HashMap<String, List<Map<String, Object>>>();
		} else {
			ret = new HashMap<String, List<Map<String, Object>>>(list.size());

			for (Map<String, Object> elem : list) { // 遍历关联表数据List
				Object key = elem.get(strKey);
				List<Map<String, Object>> _list = ret.get(key);
				if (_list == null) { // 判断主键Key对应的value是否为空
					_list = new ArrayList<Map<String, Object>>();
					ret.put(key + "", _list);
				}
				_list.add(elem);
			}

			return ret;
		}
	}

	/**
	 * 1.传入数据Map 2.生成Map数据的md5校验码
	 * 
	 * @param position
	 *            传入要校验的数据项Position
	 * @return String 经过md5后生成的校验码
	 */
	private static String getCheckCode(Map<String, Object> position) {
		StringBuffer stringBuffer = new StringBuffer();
		List<String> keys = new ArrayList<String>();
		for (Entry<String, Object> map : position.entrySet()) { // 遍历Map数据
			if (!"CheckCode".equals(map.getKey()) && !"LastUpdateTime".equals(map.getKey())) { // 判断Key是否等于特定的字符串
				keys.add(map.getKey());
			}
		}
		Collections.sort(keys);
		for (String key : keys) { // 遍历List数据
			Object value = position.get(key);
			if (value == null) {
				value = "";
			}

			stringBuffer.append(key + value);

		}
		return StringUtils.md5(stringBuffer.toString());
	}

	/**
	 * 传入Map数据，将Map中所有数据更新到本地数据库
	 * 
	 * @param sycnmobile
	 *            传入的Map数据
	 * @param hand
	 *            向主线程发送消息
	 */
	public static void downDate(Map<String, Object> sycnmobile, Handler hand, String headerPath) {

		if (sycnmobile == null) {// 判断接口是否为空
			return;
		}
		Map<String, Object> response = (Map<String, Object>) sycnmobile.get("sodex_sync_mobile_response");

		if (response == null) { // 判断接口数据是否为空
			return;
		}
		// 1. Position
		List<Map<String, Object>> positions = (List<Map<String, Object>>) response.get("Positions");
		List<Map<String, Object>> positionMemberInfos = (List<Map<String, Object>>) response.get("PositionMemberInfo");
		// 初始化进度条
		int count = 1;
		// 初始化进度条总进度
		int total = response.get("Positions_TOTAL_COUNT") == null ? 0 : (Integer) response.get("Positions_TOTAL_COUNT");
		// 设置进度条总进度
		hand.obtainMessage(1, total, 0).sendToTarget();

		if (!CollectionUtils.isEmpty(positions)) { // 判断List(positions)是否不为空
			Map<String, List<Map<String, Object>>> positionMemberInfoMap = list2Map(positionMemberInfos, "Position_ID");

			Db.delete("delete from Position");
			Db.delete("delete from PositionMemberInfo");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> position : positions) {

				try {

					// 遍历List(positions)数据
					hand.obtainMessage(-1, count++, 0).sendToTarget();

					List params = new ArrayList();
					Map<String, Object> _position = Db.selectUnique("select * from Position where ID=?", position.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(position);
					if (!cCode.equalsIgnoreCase((position.get("CheckCode")) + "")) {// 判断数据是否完整
						Log.w("数据不完整");
						continue;
					}
					if (_position == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("Position", position, params), params.toArray(new Object[0]));
					} else if (!cCode.equalsIgnoreCase((_position.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("Position", position, params) + " and ID=? ";
						params.add(position.get("ID"));
						Db.update(update, params.toArray(new Object[0]));
					}
					if (!CollectionUtils.isEmpty(positionMemberInfoMap.get(position.get("ID")))) { // 判断关联表是否存在该数据的关联数据
						Db.delete("delete from PositionMemberInfo where Position_ID=?", position.get("ID"));
						for (Map<String, Object> posotionMember : positionMemberInfoMap.get(position.get("ID"))) { // 遍历关联数据
							List _params = new ArrayList();
							Db.insert(DbUtils.insertSQL("PositionMemberInfo", posotionMember, _params), _params.toArray(new Object[0]));
						}
					}

				} catch (Exception e) {
					// TODO: handle exception

					Log.e("同步pasition信息时出错" + "/错误信息是：" + e.getMessage());

					continue;
				}

			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();

		} else { // position为空
			hand.obtainMessage(-1, 0, 0).sendToTarget();
			Db.delete("delete from Position");
			Db.delete("delete from PositionMemberInfo");
		}
		Map<String, Object> postionsNum = Db.selectUnique("select count(*) num from Position");

		if (!((count - 1) + "").equals(postionsNum.get("num"))) { // 判断更新数据的数量与数据库的数量不同
			Log.e("Position/更新数量不匹配");
		}

		// 2.EnumBases
		List<Map<String, Object>> enumBases = (List<Map<String, Object>>) response.get("EnumBase");
		// 初始化总进度
		total = (Integer) response.get("Enumbase_TOTAL_COUNT") + (Integer) response.get("FoodAdvice_TOTAL_COUNT") + (Integer) response.get("PAD_TOTAL_COUNT")
				+ (Integer) response.get("MemberInfo_TOTAL_COUNT");
		// 初始化进度条l
		count = 1;
		// 设置进度条总进度
		hand.obtainMessage(2, total, 0).sendToTarget();

		if (!CollectionUtils.isEmpty(enumBases)) { // 判断List(enumBases)是否不为空

			Db.delete("delete from EnumBase");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> enumBase : enumBases) {

				try {

					// 遍历List(enumBases)的数据
					hand.obtainMessage(-2, count++, 0).sendToTarget();

					List params = new ArrayList();
					Map<String, Object> _enumBase = Db.selectUnique("select * from EnumBase where ID=?", enumBase.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(enumBase);

					if (!cCode.equalsIgnoreCase((enumBase.get("CheckCode")) + "")) { // 判断enumBase的数据是否完整
						Log.w("数据不完整");
						continue;
					}
					if (_enumBase == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("EnumBase", enumBase, params), params.toArray(new Object[0]));
					} else if (!cCode.equalsIgnoreCase((_enumBase.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("EnumBase", enumBase, params) + " and ID=? ";
						params.add(enumBase.get("ID"));
						Db.update(update, params.toArray(new Object[0]));
					}

				} catch (Exception e) {
					// TODO: handle exception

					Log.e("同步enumebase信息时出错" + "/错误信息是：" + e.getMessage());

					continue;
				}

			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();

		} else { // enumBases为空
			Db.delete("delete from EnumBase");
		}
		Map<String, Object> enumBasesNum = Db.selectUnique("select count(*) num from EnumBase");

		if (!((count - 1) + "").equals(enumBasesNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("EnumBases/更新数量不匹配");
		}

		// 3.FoodAdvice
		List<Map<String, Object>> foodAdvices = (List<Map<String, Object>>) response.get("FoodAdvice");
		// 初始化进度
		int cacheCount = count;
		if (!CollectionUtils.isEmpty(foodAdvices)) { // 判断List(foodAdvices)是否不为空

			Db.delete("delete from FoodAdvice");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> foodAdvice : foodAdvices) {
				try {
					// 遍历List(foodAdvices)的数据
					hand.obtainMessage(-2, count++, 0).sendToTarget();

					List params = new ArrayList();
					Map<String, Object> _foodAdvice = Db.selectUnique("select * from FoodAdvice where ID=?", foodAdvice.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(foodAdvice);

					if (!cCode.equalsIgnoreCase((foodAdvice.get("CheckCode")) + "")) { // 判断foodAdvice的数据是否完整
						Log.w("数据不完整");
						continue;
					}
					if (_foodAdvice == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("FoodAdvice", foodAdvice, params), params.toArray(new Object[0]));
					} else if (!cCode.equalsIgnoreCase((_foodAdvice.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("FoodAdvice", foodAdvice, params) + " and ID=? ";
						params.add(foodAdvice.get("ID"));
						Db.update(update, params.toArray(new Object[0]));
					}

				} catch (Exception e) {
					// TODO: handle exception
					Log.e("同步foodadvice信息时出错" + "/错误信息是：" + e.getMessage());
					continue;
				}

			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();

		} else { // foodAdvices为空
			Db.delete("delete from FoodAdvice");
		}
		Map<String, Object> foodAdvicesNum = Db.selectUnique("select count(*) num from FoodAdvice");

		if (!((count - cacheCount) + "").equals(foodAdvicesNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("FoodAdvice/更新数量不匹配");
		}

		// 4. PAD
		List<Map<String, Object>> pads = (List<Map<String, Object>>) response.get("PAD");
		List<Map<String, Object>> padMemberInfos = (List<Map<String, Object>>) response.get("PADMemberInfo");
		Map<String, List<Map<String, Object>>> padMemberInfoMap = list2Map(padMemberInfos, "PAD_ID");
		// 初始化进度
		cacheCount = count;
		if (!CollectionUtils.isEmpty(pads)) { // 判断List(pads)不为空
			List padIds = new ArrayList();

			Db.delete("delete from PAD");
			Db.delete("delete from PADMemberInfo");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> pad : pads) {

				try {

					// 遍历list(pads)的数据
					// 更新进度条进度
					hand.obtainMessage(-2, count++, 0).sendToTarget();

					padIds.add(pad.get("ID"));
					List params = new ArrayList();
					Map<String, Object> _pad = Db.selectUnique("select * from PAD where ID=?", pad.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(pad);
					if (!cCode.equalsIgnoreCase((pad.get("CheckCode")) + "")) { // 判断pad的数据是否完整
						Log.w("数据不完整");
						continue;
					}
					if (_pad == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("PAD", pad, params), params.toArray(new Object[0]));
					} else if (!cCode.equalsIgnoreCase((_pad.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("PAD", pad, params) + " and ID=? ";
						params.add(pad.get("ID"));
						Db.update(update, params.toArray(new Object[0]));
					}
					// 关联更新
					if (!CollectionUtils.isEmpty(padMemberInfoMap) && !CollectionUtils.isEmpty(padMemberInfoMap.get(pad.get("ID")))) { // 判断关联表中是否存在该数据的关联数据
						Db.delete("delete from PADMemberInfo where PAD_ID=?", pad.get("ID"));
						for (Map<String, Object> padMember : padMemberInfoMap.get(pad.get("ID"))) { // 遍历关联数据
							List _params = new ArrayList();
							Db.insert(DbUtils.insertSQL("PADMemberInfo", padMember, _params), _params.toArray(new Object[0]));
						}
					}

				} catch (Exception e) {
					// TODO: handle exception
					Log.e("同步PAD表信息时出错" + "/错误信息是：" + e.getMessage());
					continue;
				}

			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();
		} else { // List(pads)为空
			Db.delete("delete from PAD");
			Db.delete("delete from PADMemberInfo");
		}
		Map<String, Object> padsNum = Db.selectUnique("select count(*) num from PAD");
		if (!((count - cacheCount) + "").equals(padsNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("PAD/更新数量不匹配");
		}

		// 5. MemberInfo
		List<Map<String, Object>> memberInfos = (List<Map<String, Object>>) response.get("MemberInfo");
		// 初始化进度
		cacheCount = count;
		if (!CollectionUtils.isEmpty(memberInfos)) { // 判断List(memberInfos)不为空

			Db.delete("delete from MemberInfo");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> memberInfo : memberInfos) {

				try {

					// 遍历List(memberInfos)的数据
					// 更新进度条
					hand.obtainMessage(-2, count++, 0).sendToTarget();

					List params = new ArrayList();

					Map<String, Object> _memberInfo = Db.selectUnique("select * from MemberInfo where MemberID =?", memberInfo.get("MemberID"));
					// 生成校验码
					String cCode = getCheckCode(memberInfo);
					if (!cCode.equalsIgnoreCase((memberInfo.get("CheckCode")) + "")) { // 判断memberInfo的数据是否完整
						Log.w("数据不完整");
						continue;
					}
					if (_memberInfo == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("MemberInfo", memberInfo, params), params.toArray(new Object[0]));
					} else if (!cCode.equalsIgnoreCase((_memberInfo.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("MemberInfo", memberInfo, params) + " and MemberID=? ";
						params.add(memberInfo.get("MemberID"));
						Db.update(update, params.toArray(new Object[0]));
					}

				} catch (Exception e) {
					// TODO: handle exception

					Log.e("同步memberinfo信息时出错" + "/错误信息是：" + e.getMessage());

					continue;
				}

			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();
		} else { // List(MemberInfo)为空

			hand.obtainMessage(-2, 0, 0).sendToTarget();
			Db.delete("delete from MemberInfo");
		}
		Map<String, Object> memberInfosNum = Db.selectUnique("select count(*) num from MemberInfo");

		if (!((count - cacheCount) + "").equals(memberInfosNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("MemberInfo/更新数量不匹配");
		}

		// 6. FoodMenu
		List<Map<String, Object>> foodMenus = (List<Map<String, Object>>) response.get("FoodMenu");
		// 初始化总进度
		total = (Integer) response.get("FoodMenu_TOTAL_COUNT") + (Integer) response.get("Food_TOTAL_COUNT");
		// 初始化进度
		count = 1;
		// 设置总进度
		hand.obtainMessage(4, total, 0).sendToTarget();

		if (!CollectionUtils.isEmpty(foodMenus)) { // 判断List(foodMenus)不为空
			Db.delete("delete from FoodMenu");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> foodMenu : foodMenus) {

				try {

					// 遍历List(foodMenus)的数据
					// 更新进度条
					hand.obtainMessage(-4, count++, 0).sendToTarget();

					List params = new ArrayList();
					Map<String, Object> _foodMenu = Db.selectUnique("select * from FoodMenu where ID=?", foodMenu.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(foodMenu);
					if (!cCode.equalsIgnoreCase((foodMenu.get("CheckCode")) + "")) { // 判断数据foodMenu的完整性
						Log.w("数据不完整");
						continue;
					}

					if (_foodMenu == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("FoodMenu", foodMenu, params), params.toArray(new Object[0]));
					} else if (!cCode.equalsIgnoreCase((_foodMenu.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("FoodMenu", foodMenu, params) + " and ID=? ";
						params.add(foodMenu.get("ID"));
						Db.update(update, params.toArray(new Object[0]));
					}

				} catch (Exception e) {
					// TODO: handle exception

					Log.e("同步foodmenu信息时出错" + "/错误信息是：" + e.getMessage());

					continue;
				}

			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();

		} else { // List(FoodMenu)为空
			Db.delete("delete from FoodMenu");
		}
		Map<String, Object> foodMenusNum = Db.selectUnique("select count(*) num from FoodMenu");

		if (!((count - 1) + "").equals(foodMenusNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("FoodMenu/更新数量不匹配");
		}

		// 7. Food
		List<Map<String, Object>> foods = (List<Map<String, Object>>) response.get("Food");
		List<Map<String, Object>> foodAdviceFoods = (List<Map<String, Object>>) response.get("FoodAdviceFood");
		List<Map<String, Object>> potFoods = (List<Map<String, Object>>) response.get("PotFood");
		List<Map<String, Object>> foodAllergys = (List<Map<String, Object>>) response.get("FoodAllergy");

		Map<String, List<Map<String, Object>>> foodAdviceMap = list2Map(foodAdviceFoods, "Food_ID");

		Map<String, List<Map<String, Object>>> foodAllergyMap = list2Map(foodAllergys, "Food_ID");

		// 初始化进度
		cacheCount = count;
		if (!CollectionUtils.isEmpty(foods)) { // 判断List(foodMenus)不为空

			Db.delete("delete from Food");
			Db.delete("delete from FoodAdviceFood");
			Db.delete("delete from FoodAllergy");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> food : foods) {

				try {

					// 遍历List(foodMenus)的数据
					// 更新进度条
					hand.obtainMessage(-4, count++, 0).sendToTarget();

					List params = new ArrayList();
					Map<String, Object> _food = Db.selectUnique("select * from Food where ID=?", food.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(food);
					if (!cCode.equalsIgnoreCase((food.get("CheckCode")) + "")) { // 判断数据foodMenu的完整性
						Log.w("数据不完整");
						continue;
					}
					if (_food == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("Food", food, params), params.toArray(new Object[0]));
						Db.delete("delete from FoodAdviceFood where Food_ID=?", food.get("ID"));
						Db.delete("delete from PotFood where Food_ID=?", food.get("ID"));
						Db.delete("delete from FoodAllergy where Food_ID=?", food.get("ID"));
					} else if (!cCode.equalsIgnoreCase((_food.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("Food", food, params) + " and ID=? ";
						params.add(food.get("ID"));
						Db.update(update, params.toArray(new Object[0]));
					}
					if (!CollectionUtils.isEmpty(foodAdviceMap) && !CollectionUtils.isEmpty(foodAdviceMap.get(food.get("ID")))) { // 判断关联表中是否存在该数据的关联数据
						Db.delete("delete from FoodAdviceFood where Food_ID=?", food.get("ID"));
						for (Map<String, Object> adviceFood : foodAdviceMap.get(food.get("ID"))) { // 遍历关联数据
							List _params = new ArrayList();
							Db.insert(DbUtils.insertSQL("FoodAdviceFood", adviceFood, _params), _params.toArray(new Object[0]));
						}
					}
					if (!CollectionUtils.isEmpty(foodAllergyMap) && !CollectionUtils.isEmpty(foodAllergyMap.get(food.get("ID")))) { // 判断关联表中是否存在该数据的关联数据
						Db.delete("delete from FoodAllergy where Food_ID=?", food.get("ID"));
						for (Map<String, Object> foodAllergy : foodAllergyMap.get(food.get("ID"))) { // 遍历关联数据
							List _params = new ArrayList();
							Db.insert(DbUtils.insertSQL("FoodAllergy", foodAllergy, _params), _params.toArray(new Object[0]));
						}
					}
					// 下载食物图片资源
					if (food.get("PicAddress") != null && (food.get("PicAddress") + "").length() >= 1 && headerPath != null) {

						String imgUrl = food.get("PicAddress") + "";

						int index = imgUrl.lastIndexOf("/");
						File imgPath = new File(Environment.getExternalStorageDirectory() + "/Sodexo/images/" + imgUrl.substring(index + 1, imgUrl.length()));
						// 首先判断图片文件是否存在，如果不存在就下载，存在就不用下载
						if (!imgPath.exists()) {
							ImgUtil.loadImageFromUrl(food.get("PicAddress") + "", headerPath);
						}

					}

				} catch (Exception e) {
					// TODO: handle exception
					Log.e("同步food信息时出错" + "/错误信息是：" + e.getMessage());
					continue;
				}

			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();
			// 更新表PotFood的数据
			Db.delete("delete from PotFood");
			for (Map<String, Object> elem : potFoods) { // 遍历List(potFoods)的数据
				List _params = new ArrayList();
				Db.insert(DbUtils.insertSQL("PotFood", elem, _params), _params.toArray(new Object[0]));
			}
		} else { // List(foodMenus)为空
			hand.obtainMessage(-4, 0, 0).sendToTarget();
			Db.delete("delete from Food");
			Db.delete("delete from FoodAdviceFood");
			Db.delete("delete from PotFood");
			Db.delete("delete from FoodAllergy");
		}
		Map<String, Object> foodsNum = Db.selectUnique("select count(*) num from Food");

		if (!((count - cacheCount) + "").equals(foodsNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("FoodMenu/更新数量不匹配");
		}

		// 10.FoodType add：2017-9-20
		List<Map<String, Object>> foodTypes = (List<Map<String, Object>>) response.get("FoodType");
		int potFoodTypeCount = response.get("FoodType_TOTAL_COUNT") == null ? 0 : (Integer) response.get("FoodType_TOTAL_COUNT");
		if (!CollectionUtils.isEmpty(foodTypes)) { // 判断List(FoodType)是否不为空
			Db.delete("delete from FoodType");

			Db.DATABASE.beginTransaction();
			for (Map<String, Object> potFoodType : foodTypes) { // 遍历List(FoodType)的数据
				List params = new ArrayList();

				Map<String, Object> _potFoodType = Db.selectUnique("select * from FoodType where ID=?", potFoodType.get("ID"));
				if (_potFoodType == null) { // 判断数据库中是否存在该条数据
					// insert
					Db.insert(DbUtils.insertSQL("FoodType", potFoodType, params), params.toArray(new Object[0]));
				} else {
					// update
					String update = DbUtils.updateSQL("FoodType", potFoodType, params) + " and ID=? ";
					params.add(potFoodType.get("ID"));
					Db.update(update, params.toArray(new Object[0]));
				}
			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();
		} else { // foodAdvices为空
			Db.delete("delete from FoodType");
		}
		Map<String, Object> potFoodTypeNum = Db.selectUnique("select count(*) num from FoodType");

		if (!(potFoodTypeCount + "").equals(potFoodTypeNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("FoodType/更新数量不匹配");
		}

		// 8. Patient
		List<Map<String, Object>> patients = (List<Map<String, Object>>) response.get("Patients");
		List<Map<String, Object>> patientFoodAdvices = (List<Map<String, Object>>) response.get("PatientFoodAdvice");
		List<Map<String, Object>> patientAllergys = (List<Map<String, Object>>) response.get("PatientAllergy");

		Map<String, List<Map<String, Object>>> paitentFoodAdviceMap = list2Map(patientFoodAdvices, "Patient_ID");
		Map<String, List<Map<String, Object>>> patientAllergyMap = list2Map(patientAllergys, "Patient_ID");

		// 初始化总进度
		total = response.get("Patients_TOTAL_COUNT") == null ? 0 : (Integer) response.get("Patients_TOTAL_COUNT");
		Log.e("total=" + total);
		// 初始化进度
		count = 1;
		// 设置总进度
		hand.obtainMessage(7, total, 0).sendToTarget();

		if (!CollectionUtils.isEmpty(patients)) { // 判断List(patients)不为空

			StringBuffer patpositionid_list = new StringBuffer();
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> patient : patients) { // 遍历List(patients)的数据
				// Log.e("Patient" + patient.get("PatPositionID"));
				// 更新进度
				try {

					patpositionid_list.append("'" + patient.get("ID") + "',");

					hand.obtainMessage(-7, count++, 0).sendToTarget();
					List params = new ArrayList();
					Map<String, Object> _patient = Db.selectUnique("select * from Patient where ID=?", patient.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(patient);
					if (!cCode.equalsIgnoreCase((patient.get("CheckCode")) + "")) { // 判断数据Patient的完整性
						Log.e("数据不完整");
						// continue;
					}
					boolean isUpdate = false;

					if (_patient == null) { // 判断数据库中是否存在该条数据

						Db.update("update Patient set patpositionid=null where patpositionid=?", patient.get("PatPositionID"));
						Db.insert(DbUtils.insertSQL("Patient", patient, params), params.toArray(new Object[0]));

						isUpdate = true;

					} else {
						// update
						Date lastTimeForMobile = ConvertUtils.str2Date((String) _patient.get("LastUpdateTime"), "yyyy-MM-dd HH:mm:ss");
						Date lastTimeForServer = ConvertUtils.str2Date((String) patient.get("LastUpdateTime"), "yyyy-MM-dd HH:mm:ss");
						if (lastTimeForServer == null) { // 判断数据为空
							continue;
						}

						if (lastTimeForMobile == null || lastTimeForMobile.getTime() < lastTimeForServer.getTime()) { // 判断数据的LastUpdateTime是否大与数据库中的LastUpdateTime
							String update = DbUtils.updateSQL("Patient", patient, params) + " and ID=? ";
							params.add(patient.get("ID"));
							Db.update("update Patient set patpositionid=null where patpositionid=?", patient.get("PatPositionID"));
							Db.update(update, params.toArray(new Object[0]));
							isUpdate = true;
						}

					}

					if (isUpdate && !CollectionUtils.isEmpty(paitentFoodAdviceMap) && !CollectionUtils.isEmpty(paitentFoodAdviceMap.get(patient.get("ID")))) { // 判断关联表中是否存在该数据的关联数据并已经更新
						Db.delete("delete from PatientFoodAdvice where Patient_ID=?", patient.get("ID"));
						for (Map<String, Object> patientFoodAdvice : paitentFoodAdviceMap.get(patient.get("ID"))) { // 遍历关联数据

							List _params = new ArrayList();

							Db.insert(DbUtils.insertSQL("PatientFoodAdvice", patientFoodAdvice, _params), _params.toArray(new Object[0]));
						}
					}
					if (isUpdate && !CollectionUtils.isEmpty(patientAllergyMap) && !CollectionUtils.isEmpty(patientAllergyMap.get(patient.get("ID")))) { // 判断关联表中是否存在该数据的关联数据并已经更新
						Db.delete("delete from PatientAllergy where Patient_ID=?", patient.get("ID"));
						for (Map<String, Object> patientAllergy : patientAllergyMap.get(patient.get("ID"))) { // 遍历关联数据

							List _params = new ArrayList();

							Db.insert(DbUtils.insertSQL("PatientAllergy", patientAllergy, _params), _params.toArray(new Object[0]));
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					Log.e("同步病人信息时出错" + "/错误信息是：" + e.getMessage());
					continue;
				}
			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();
			if (patpositionid_list != null) {
				patpositionid_list.deleteCharAt(patpositionid_list.length() - 1);
				Db.update("Update Patient set PatPositionID=NULL where ID not in (" + patpositionid_list.toString() + ")");
			}

		} else {
			hand.obtainMessage(-7, 0, 0).sendToTarget();
		}
		Map<String, Object> patientsNum = Db.selectUnique("select count(*) num from Patient where PatPositionID not null");

		int totalPatientsNum = Integer.parseInt(patientsNum.get("num") + "");
		android.util.Log.e("", "totalPatientsNum=" + totalPatientsNum);
		android.util.Log.e("", "(count - 1)=" + (count - 1));
		if ((count - 1) != totalPatientsNum) { // 判断更新数据数量与数据库的数量不同

			Log.e("Patient/更新数量不匹配");

		}
		int a = Db.update("Update Patient set OutHospitalTime=NULL where OutHospitalTime < '1900-01-02' ");
		// 9.Allergy
		List<Map<String, Object>> allergys = (List<Map<String, Object>>) response.get("Allergy");

		int allCount = response.get("Allergy_TOTAL_COUNT") == null ? 0 : (Integer) response.get("Allergy_TOTAL_COUNT");
		// 初始化进度
		// int cacheCount = count;
		if (!CollectionUtils.isEmpty(allergys)) { // 判断List(allergys)是否不为空

			Db.delete("delete from Allergy");
			Db.DATABASE.beginTransaction();
			for (Map<String, Object> allergy : allergys) { // 遍历List(allergys)的数据
				try {

					List params = new ArrayList();
					Map<String, Object> _allergy = Db.selectUnique("select * from Allergy where ID=?", allergy.get("ID"));
					// 生成校验码
					String cCode = getCheckCode(allergy);

					if (!cCode.equalsIgnoreCase((allergy.get("CheckCode")) + "")) { // 判断allergy的数据是否完整
						Log.w("数据不完整");
						continue;
					}
					if (_allergy == null) { // 判断数据库中是否存在该条数据
						// insert
						Db.insert(DbUtils.insertSQL("Allergy", allergy, params), params.toArray(new Object[0]));
					} else if (!cCode.equalsIgnoreCase((_allergy.get("CheckCode")) + "")) { // 判断该条数据的校验码与数据库中该条数据的校验码不同
						// update
						String update = DbUtils.updateSQL("Allergy", allergy, params) + " and ID=? ";
						params.add(allergy.get("ID"));
						Db.update(update, params.toArray(new Object[0]));
					}
				} catch (Exception e) {
					// TODO: handle exception
					Log.e("同步过敏信息时出错" + "/错误信息是：" + e.getMessage());

					continue;
				}
			}
			Db.DATABASE.setTransactionSuccessful();
			Db.DATABASE.endTransaction();
		} else { // foodAdvices为空
			Db.delete("delete from Allergy");
		}
		Map<String, Object> allergysNum = Db.selectUnique("select count(*) num from Allergy");

		if (!(allCount + "").equals(allergysNum.get("num"))) { // 判断更新数据数量与数据库的数量不同
			Log.e("Allergy/更新数量不匹配");
		}

	}

	/**
	 * 将数据库中的数据取出，返回Json数据
	 * 
	 * @param hand
	 *            向主线程发送消息
	 * @param url
	 *            上传数据的url接口
	 * @return String 生成的Json格式的数据
	 */
	public static String upData(Handler hand, String url, Context context) {
		// Patient表数据Size
		int SHPatientNum = 0;
		// Order表数据Size
		int SHOrderNum = 0;
		// OrderDetail表数据Size
		int SHOrderDetailNum = 0;
		// patientFoodAdviceList表数据Size
		int SHfoodAdviceIDNum = 0;
		int SHAllergyIDNum = 0;
		// 初始化上传进度
		int progressJson = 1;

		if (hand != null) { // 判断hand是否为空
			// 发送更新信息
			hand.obtainMessage(8).sendToTarget();
		}

		// 病员上传信息
		List<Map<String, Object>> patientList = Db
				.select("select PID,PatPositionID,PatientSex,LastUpdateTime,PatientName,WriteTime,HisCode,WriteAdminName,ID,PatientAge,PatientHeight,InHospitalTime,OutHospitalTime from patient where PatPositionID not null order by id");
		if (patientList != null) { // 判断patientList是否为空

			SHPatientNum = patientList.size();

		}
		// 订单上传信息
		List<Map<String, Object>> orderList = Db.select("select ID,Device ,WriteAdminName ,HisCode ,PID ,WriteTime ,LastUpdateAdminName ,LastUpdateTime ,Patient_ID ,PatPositionID,FoodAdviceIDList"
				+ " from [Order] where ID in (select distinct Order_id from OrderDetail where TakeFoodTime >= ?) ", ConvertUtils.date2Str("yyyy-MM-dd"));
		if (orderList != null) { // 判断orderList是否为空

			SHOrderNum = orderList.size();

		}
		// 订单明细上传信息
		List<Map<String, Object>> orderDetailList = Db
				.select("select b.ID,c.HisCode ,b.PID ,b.TakeFoodTime ,b.MealTypeID ,b.PayWayID ,b.Count ,b.Order_ID,b.Food_ID, b.FoodAdviceIDList,a.FoodName ,a.UnitPrice as FoodUnitPrice from food a,orderdetail b, [order] c "
						+ " where a.id=b.food_id and b.Order_ID=c.id " + " and  TakeFoodTime >= ?", ConvertUtils.date2Str("yyyy-MM-dd"));

		if (orderDetailList != null) { // 判断orderDetailList是否为空
			SHOrderDetailNum = orderDetailList.size();
		}

		// 病员医嘱上传信息
		List<Map<String, Object>> patientFoodAdviceList = Db
				.select("select Patient_ID,FoodAdvice_ID from PatientFoodAdvice where Patient_ID in (select id from patient where PatPositionID not null )");

		if (patientFoodAdviceList != null) { // 判断patientFoodAdviceList是否为空
			SHfoodAdviceIDNum = patientFoodAdviceList.size();
		}
		// 病员过敏信息上传信息

		List<Map<String, Object>> patientAllergyList = Db.select("select Patient_ID,Allergy_ID from PatientAllergy where Patient_ID in (select id from patient where PatPositionID not null)");
		if (patientAllergyList != null) {
			SHAllergyIDNum = patientAllergyList.size();
		}
		if (hand != null) { // 判断hand是否为空
			// 发送更新信息
			hand.obtainMessage(-8, progressJson++, 0).sendToTarget();
		}
		try {

			// 用于存放json的jsonObject

			JSONObject jsonObject = new JSONObject();

			// patient表json数据生成

			JSONArray patientItem = new JSONArray();

			if (patientList != null) { // 判断patientList是否非空
				for (int i = 0; i < patientList.size(); i++) { // 遍历patientList数据
					Map<String, Object> patientJsonMap = new HashMap<String, Object>();
					JSONObject json = new JSONObject();
					for (String key : patientList.get(i).keySet()) { // 遍历patientList的i项的key值
						if (patientList.get(i).get(key) == null) { // 判断key为空或者等于特定字段

							if ("PatientAge".equals(key) || "PatientHeight".equals(key)) {
								json.put(key, 0);
							} else {
								json.put(key, "");
							}
						} else { // 判断key非空并不等于特定字段
							json.put(key, patientList.get(i).get(key));
						}
						if (!key.equals("LastUpdateTime")) { // 判断key不等于LastUpdateTime
							if (patientList.get(i).get(key) == null) { // 判断key为空或者等于特定字段

								if ("PatientAge".equals(key) || "PatientHeight".equals(key)) {
									patientJsonMap.put(key, 0);
								} else {
									patientJsonMap.put(key, "");
								}

							} else { // 判断key非空并不等于特定字段
								patientJsonMap.put(key, patientList.get(i).get(key));
							}
						}
					}
					String checkCode = getCheckCode(patientJsonMap);
					json.put("CheckCode", checkCode);
					patientItem.put(json);
				}
			}

			jsonObject.put("Patient", patientItem);
			jsonObject.put("Patient_total_count", SHPatientNum);

			// order表json数据生成
			JSONArray orderItem = new JSONArray();
			if (orderList != null) { // 判断orderList非空
				for (int i = 0; i < orderList.size(); i++) { // 遍历orderList的数据
					JSONObject json = new JSONObject();
					for (String key : orderList.get(i).keySet()) { // 遍历orderList的i项的key值

						if (orderList.get(i).get(key) == null) {
							json.put(key, "");
						} else {
							json.put(key, orderList.get(i).get(key));
						}

					}
					String checkCode = getCheckCode(orderList.get(i));
					json.put("CheckCode", checkCode);
					orderItem.put(json);
				}
			}
			jsonObject.put("Order", orderItem);
			jsonObject.put("Order_total_count", SHOrderNum);
			if (hand != null) { // 判断hand为非空
				// 发送更新信息
				hand.obtainMessage(-8, progressJson++, 0).sendToTarget();
			}
			JSONArray orderDetailItem = new JSONArray();
			if (orderDetailList != null) { // 判断orderDetailList为非空
				for (int i = 0; i < orderDetailList.size(); i++) { // 遍历orderDetailList的数据
					JSONObject json = new JSONObject();
					for (String key : orderDetailList.get(i).keySet()) { // 遍历orderDetailList的i项的key值

						if (orderDetailList.get(i).get(key) == null) {
							json.put(key, "");
						} else {
							json.put(key, orderDetailList.get(i).get(key));
						}

					}
					String checkCode = getCheckCode(orderDetailList.get(i));
					json.put("CheckCode", checkCode);
					orderDetailItem.put(json);
				}
			}

			jsonObject.put("OrderDetail", orderDetailItem);
			jsonObject.put("OrderDetail_total_count", SHOrderDetailNum);

			if (hand != null) { // 判断hand为非空

				// 发送更新信息
				hand.obtainMessage(-8, progressJson++, 0).sendToTarget();

			}
			// 将病员的医嘱信息放进json中
			JSONArray patientFoodAdviceItem = new JSONArray();
			if (patientFoodAdviceList != null) { // 判断patientFoodAdviceList为非空

				// patientFoodAdvice json数据生成

				for (int i = 0; i < patientFoodAdviceList.size(); i++) { // 遍历patientFoodAdviceList的数据

					JSONObject json = new JSONObject();

					for (String key : patientFoodAdviceList.get(i).keySet()) { // 遍历patientFoodAdviceList的i项的key值

						if (patientFoodAdviceList.get(i).get(key) == null) {
							json.put(key, "");
						} else {
							json.put(key, patientFoodAdviceList.get(i).get(key));
						}

					}

					patientFoodAdviceItem.put(json);
				}
			}
			jsonObject.put("PatientFoodAdvice", patientFoodAdviceItem);
			jsonObject.put("PatientFoodAdvice_total_count", SHfoodAdviceIDNum);
			// 将病员的过敏信息放进json中
			JSONArray patientAllergyItem = new JSONArray();
			if (patientAllergyList != null) {

				for (int i = 0; i < patientAllergyList.size(); i++) { // 遍历patientAllergyList的数据

					JSONObject json = new JSONObject();

					for (String key : patientAllergyList.get(i).keySet()) { // 遍历patientAllergyList的i项的key值

						if (patientAllergyList.get(i).get(key) == null) {
							json.put(key, "");
						} else {
							json.put(key, patientAllergyList.get(i).get(key));
						}

					}

					patientAllergyItem.put(json);

				}
			}
			jsonObject.put("PatientAllergy", patientAllergyItem);
			jsonObject.put("PatientAllergy_total_count", SHAllergyIDNum);
			if (url != null) { // 判断传入的url是否为非空

				new HttpUtils().writeStringForPost(url, jsonObject + "");
			}

			if (hand != null) { // 判断hand是否为非空

				// 发送更新信息
				hand.obtainMessage(-8, progressJson++, 0).sendToTarget();
			}
			Utils.writeFileSdcard(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sodexo/Json.txt", jsonObject.toString());
			// 返回用于离线上传文件json字符串

			Log.e("uploadJson--->" + jsonObject);
			return jsonObject + "";

		} catch (JSONException e) {

			e.printStackTrace();
		}
		return null;

	}
}
