package com.gnwang.hbase.dao;

import java.util.List;

import com.gnwang.hbase.bean.HbaseCreateTableBean;
import com.gnwang.hbase.conf.HbaseConf;
import com.gnwang.until.hbase.HbaseUntil;

public class HbaseDao {
	static HbaseUntil hbaseUntil;
	
	//根据配置文件建表
	public static void checkTable() throws Exception {
		List<HbaseCreateTableBean> tasks = HbaseConf.getTasks(HbaseConf.relativePath);
		System.out.println(tasks.get(0).getHbaseQuorum());
	}
	public static void main(String[] args) throws Exception {
		System.setProperty("hadoop.home.dir", "/home/acer/log");
		checkTable();
	}
}
