package com.paullbm.timingcost.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.paullbm.timingcost.ICountCost;
import com.paullbm.timingcost.entity.PriceItem;


/**
 * @author paullbm
 */
public class CountCost implements ICountCost {
	private int freeTimeSecond = 15 * 60; // 15分钟免费时间(秒数)
	private int oneDayLimitCost = 120; // 1天的封顶费用

	private int oneHourSecond = 60 * 60; // 1小时包含的秒数
	private long oneDaySecond = 24 * oneHourSecond; // =86400毫秒
	private long east8ZoneSecond = 8 * oneHourSecond; // 东八区附加毫秒数

	private int[][] listPrices = {
			{ 1, 0, 5, 2 },
			{ 2, 5, 7, 10 },
			{ 3, 7, 12, 6 },
			{ 4, 12, 14, 10 },
			{ 5, 14, 18, 2 },
			{ 6, 18, 19, 1 },
			{ 7, 19, 21, 20 },
			{ 8, 21, 24, 6 }
	};
	private String fmtDateStr="yyyy-MM-dd HH:mm:ss";
	private ArrayList<PriceItem> itemList = new ArrayList<PriceItem>();
	private Date startDate;
	private Date endDate;

	public CountCost(String startTime, String endTime) {
		Date startDate = null;
		Date endDate = null;
		SimpleDateFormat simdate = new SimpleDateFormat(this.fmtDateStr);
		try {
			startDate = simdate.parse(startTime);
			endDate = simdate.parse(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		this.init(startDate, endDate);
	}

	public CountCost(Date startDate, Date endDate) {
		this.init(startDate, endDate);
	}

	private void init(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;

		for (int i = 0; i < 8; i++) {
			PriceItem struct = new PriceItem(
					this.listPrices[i][0],
					this.listPrices[i][1] * this.oneHourSecond,
					this.listPrices[i][2] * this.oneHourSecond,
					this.listPrices[i][3]);
			this.itemList.add(i, struct);
		}
	}

	@Override
	public int getTotalPrice() {
		long startTimeSecond = this.startDate.getTime() / 1000;
		long endTimeSecond = this.endDate.getTime() / 1000;

		if (isFreeTime(startTimeSecond, endTimeSecond)) // 如果是免费时间内
			return 0;

		int totalPrice = 0;
		int limitCost=getLimitCost(startTimeSecond, endTimeSecond);
		int normalCost = getWithin1DayCost(startTimeSecond, endTimeSecond);
		totalPrice += (limitCost+normalCost);
		System.out.println("封顶消费="+limitCost + "元");
		System.out.println("普通消费="+normalCost + "元");
		return totalPrice;
	}

	// 判断是否是在免费时间范围内
	private boolean isFreeTime(long startTimeSecond, long endTimeSecond) {
		long timeDiff = endTimeSecond - startTimeSecond;
		if (timeDiff <= freeTimeSecond)
			return true;
		return false;
	}

	// 计算封顶费用
	private int getLimitCost(long startTimeSecond, long endTimeSecond) {
		int limitPrice = 0;
		while (true) {
			if (endTimeSecond - startTimeSecond >= oneDaySecond) {
				limitPrice += oneDayLimitCost;
				startTimeSecond += oneDaySecond;
			} else {
				break;
			}
		}
		return limitPrice;
	}

	//计算1天以内的小时数的消费金额(要注意跨天问题)
	private int getWithin1DayCost(long startTimeSecond, long endTimeSecond) {
		int normalPrice = 0;
		long relativeStartTimeSecond = (startTimeSecond + east8ZoneSecond) % oneDaySecond + freeTimeSecond;  //东八区调整再累加免费时长
		long relativeEndTimeSecond = (endTimeSecond + east8ZoneSecond) % oneDaySecond;
		if (relativeEndTimeSecond < relativeStartTimeSecond) {
			//考虑跨天问题，相对结束时间则需要累加1天
			relativeEndTimeSecond += oneDaySecond;
		}

		long offsetTimeSecond = oneHourSecond - (relativeStartTimeSecond % oneHourSecond) + 1; // 计算时间偏移量
		boolean isFirst = true;

		int index = 0;
		int size=itemList.size();
		while (relativeStartTimeSecond < relativeEndTimeSecond) {
			while(index < size) {
				if(relativeStartTimeSecond >= relativeEndTimeSecond)
					break;
				PriceItem item = itemList.get(index);
				if (relativeStartTimeSecond > item.getStart()
						&& relativeStartTimeSecond < item.getEnd()) {
					normalPrice += item.getPrice();
					if (isFirst) { // 首次要添加时间偏移量
						relativeStartTimeSecond += offsetTimeSecond;
						isFirst = false;
					} else { // 之后可进行整小时添加
						relativeStartTimeSecond += oneHourSecond;
					}

					System.out.print(item + ", 阶段性累计消费=" + normalPrice + "元\n");
//					System.out.println(", relativeStartTimeSecond=" + relativeStartTimeSecond
//							+ ", relativeEndTimeSecond=" + relativeEndTimeSecond);
				}else{
					if(relativeStartTimeSecond > item.getEnd())
						index++;
				}
			}

			if (relativeEndTimeSecond > oneDaySecond) {
				//如果按条件迭代完this.itemList还能进入此处
				//说明存在跨天情况，则需要进行相关调整
				relativeStartTimeSecond = 1;
				relativeEndTimeSecond -= oneDaySecond;
				index = 0;
			}
		}

		return normalPrice;
	}
}
