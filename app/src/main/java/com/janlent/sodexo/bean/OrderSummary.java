package com.janlent.sodexo.bean;

import java.io.Serializable;

public class OrderSummary implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String orderTime;  //订单时间
	public String bedNum;  //床位数
	public String orderCount;  //份数
	public String cashMoney;  //现金
	public String monthMoney;  //月结
	public String allMoney;  //金额
}
