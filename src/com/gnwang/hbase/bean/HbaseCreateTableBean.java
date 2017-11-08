package com.gnwang.hbase.bean;

import java.util.List;

public class HbaseCreateTableBean {
	private String hbaseQuorum;
	private List<HbaseTable> hbaseTables;
	public String getHbaseQuorum() {
		return hbaseQuorum;
	}
	public void setHbaseQuorum(String hbaseQuorum) {
		this.hbaseQuorum = hbaseQuorum;
	}
	public List<HbaseTable> getHbaseTables() {
		return hbaseTables;
	}
	public void setHbaseTables(List<HbaseTable> hbaseTables) {
		this.hbaseTables = hbaseTables;
	}
	
	
}
