package com.gnwang.hbase.dao;

import java.util.List;

import com.gnwang.hbase.bean.HbaseCreateTableBean;
import com.gnwang.hbase.conf.HbaseConf;
import com.gnwang.hbase.until.HbaseUntil;

public class HbaseDao {
	static HbaseUntil hbaseUntil;
	
	//根据配置文件建表
	public static void checkTable() throws Exception {
		List<HbaseCreateTableBean> tasks = HbaseConf.getTasks(HbaseConf.relativePath);
		for(HbaseCreateTableBean task: tasks) {
			hbaseUntil = HbaseUntil.getInstance(task.getQuorum());
			if (!hbaseUntil.existTable(task.getTableName())) {
				hbaseUntil.createTableWithRegion(task.getTableName(), task.getSpiltKeys(), task.getFamilys().split(","));
			}
			hbaseUntil.existTable(task.getTableName());
			hbaseUntil.deleteTable(task.getTableName());
		}
	}
	public static void main(String[] args) throws Exception {
		System.setProperty("hadoop.home.dir", "/home/acer/log");
		checkTable();
	}
}
