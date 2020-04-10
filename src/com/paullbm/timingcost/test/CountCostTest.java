package com.paullbm.timingcost.test;

import com.paullbm.timingcost.ICountCost;
import com.paullbm.timingcost.impl.CountCost;


/**
 * @author paullbm
 */
public class CountCostTest {
	public static void main(String[] args) {
		String startTime = "2020-04-01 18:00:01";
		String endTime = "2020-04-03 10:00:00";
		ICountCost cc = new CountCost(startTime, endTime);
		int totalPrice = cc.getTotalPrice();
		System.out.println("总消费金额=" + totalPrice+ "元");
	}
}
