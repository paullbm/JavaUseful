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
//		String startTime = "2020-04-01 10:15:01";
//		String endTime = "2020-04-01 17:00:02";
		ICountCost cc = new CountCost(startTime, endTime);
		int totalPrice = cc.getTotalPrice();
		System.out.println("总消费金额=" + totalPrice+ "元");
	}
}
