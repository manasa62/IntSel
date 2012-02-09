package com.finalMS.intSel;

public class InfoElement {
	private String netType;
	private long value;
	private String ssid;
	private int netID;
	
	public String getNetType() {
		return netType;
	}
	public void setNetType(String string) {
		this.netType = string;
	}
	public long getValue() {
		return value;
	}
	public int getNetID() {
		return netID;
	}
	public void setNetID(int netID) {
		this.netID = netID;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	
	public String toString(){
		return ("Net Type = "+this.netType+"  Net Subtype = "+this.ssid+"  Net ID = "+this.netID+"  sValue = "+this.value);
	}
}
