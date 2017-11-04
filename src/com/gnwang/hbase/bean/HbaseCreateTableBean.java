package com.gnwang.hbase.bean;

public class HbaseCreateTableBean {
	private String quorum;
	private String tableName;
	private String familys;
	private String spiltKeys;
	public String getQuorum() {
		return quorum;
	}
	public void setQuorum(String quorum) {
		this.quorum = quorum;
	}
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
	@Override
	public String toString() {
		return "HaaseCreateTableBean [quorum=" + quorum + ", tableName=" + tableName + ", familys=" + familys
				+ ", spiltKeys=" + spiltKeys + "]";
	}
	
	
}
