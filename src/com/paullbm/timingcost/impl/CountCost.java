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
	private int freeTime = 15 * 60 * 1000; // 15分钟免费时间(毫秒数)
	private int oneDayLimitCost = 120; // 1天的封顶费用

	private int oneHourMsec = 60 * 60 * 1000; // 1小时包含的毫秒数
	private long oneDayMsec = 24 * oneHourMsec; // =86400000毫秒
	private long east8ZoneMsec = 8 * oneHourMsec; // 东八区附加毫秒数

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
					this.listPrices[i][1] * this.oneHourMsec,
					this.listPrices[i][2] * this.oneHourMsec,
					this.listPrices[i][3]);
			this.itemList.add(i, struct);
		}
	}

	@Override
	public int getTotalPrice() {
		long startTimeMsec = this.startDate.getTime();
		long endTimeMsec = this.endDate.getTime();

		if (isFreeTime(startTimeMsec, endTimeMsec)) // 如果是免费时间内
			return 0;

		int totalPrice = 0;
		int limitCost=getLimitCost(startTimeMsec, endTimeMsec);
		int normalCost = getWithin1DayCost(startTimeMsec, endTimeMsec);
		totalPrice += (limitCost+normalCost);
		System.out.println("封顶消费="+limitCost + "元");
		System.out.println("普通消费="+normalCost + "元");
		return totalPrice;
	}

	// 判断是否是在免费时间范围内
	private boolean isFreeTime(long startTimeMsec, long endTimeMsec) {
		long timeDiff = endTimeMsec - startTimeMsec;
		if (timeDiff <= freeTime)
			return true;
		return false;
	}

	// 计算封顶费用
	private int getLimitCost(long startTimeMsec, long endTimeMsec) {
		int limitPrice = 0;
		while (true) {
			if (endTimeMsec - startTimeMsec >= oneDayMsec) {
				limitPrice += oneDayLimitCost;
				startTimeMsec += oneDayMsec;
			} else {
				break;
			}
		}
		return limitPrice;
	}

	//计算1天以内的小时数的消费金额(要注意跨天问题)
	private int getWithin1DayCost(long startTimeMsec, long endTimeMsec) {
		int normalPrice = 0;
		long relativeStartTimeMsec = startTimeMsec % oneDayMsec
				+ east8ZoneMsec + freeTime;  //东八区调整再累加免费时长
		long relativeEndTimeMsec = endTimeMsec % oneDayMsec + east8ZoneMsec;
		if (relativeEndTimeMsec < relativeStartTimeMsec) {
			//考虑跨天问题，相对结束时间则需要累加1天
			relativeEndTimeMsec += oneDayMsec;
		}

		long offsetTimeMsec = oneHourMsec - freeTime; // 计算时间偏移量
		boolean isFirst = true;
		int index = 0;
		while (relativeStartTimeMsec < relativeEndTimeMsec) {
			for (; index < itemList.size(); index++) {
				PriceItem item = itemList.get(index);
				while (relativeStartTimeMsec > item.getStart()
						&& relativeStartTimeMsec <= item.getEnd()) {
					if (relativeStartTimeMsec >= relativeEndTimeMsec)
						break;
					if (isFirst) { // 首次要添加时间偏移量
						relativeStartTimeMsec += offsetTimeMsec;
						isFirst = false;
					} else { // 之后可进行整小时添加
						relativeStartTimeMsec += oneHourMsec;
					}
					normalPrice += item.getPrice();
					System.out.print(item);
					System.out.println(", 阶段性累计消费=" + normalPrice + "元");
				}
			}

			if(relativeStartTimeMsec < relativeEndTimeMsec) {
				//如果按条件迭代完this.itemList还能进入此处
				//说明存在跨天情况，则需要进行相关调整
				relativeStartTimeMsec -= oneDayMsec;
				relativeEndTimeMsec -= oneDayMsec;
				index = 0;
			}
		}

		return normalPrice;
	}
}
