package com.gnwang.hbase.bean;

public class HbaseTable {
	private String tableName;
	private String familys;
	private String spiltKeys;
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFamilys() {
		return familys;
	}
	public void setFamilys(String familys) {
		this.familys = familys;
	}
	public String getSpiltKeys() {
		return spiltKeys;
	}
	public void setSpiltKeys(String spiltKeys) {
		this.spiltKeys = spiltKeys;
	}
}
