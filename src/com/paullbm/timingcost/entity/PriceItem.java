package com.paullbm.timingcost.entity;

/**
 * @author paullbm
 */
public class PriceItem {
	private int no;
	private int start;
	private int end;
	private int price;

	public PriceItem(int no, int start, int end, int price) {
		this.no = no;
		this.start = start;
		this.end = end;
		this.price = price;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("no=").append(no);
		sb.append(",start=").append(start);
		sb.append(",end=").append(end);
		sb.append(",price=").append(price);
		sb.append("]");
		return sb.toString();
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getPrice() {
		return price;
	}
}