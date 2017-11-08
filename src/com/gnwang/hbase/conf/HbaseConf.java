package com.gnwang.hbase.conf;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.gnwang.hbase.bean.HbaseCreateTableBean;
import com.gnwang.until.file.FileUntil;

public class HbaseConf {
	
	public static final String relativePath = "hbase_create.json";
	
	public static List<HbaseCreateTableBean> getTasks(String relativePath) throws Exception {
		List<HbaseCreateTableBean> rtn = new ArrayList<>();
		rtn = JSON.parseArray(FileUntil.readFile(relativePath), HbaseCreateTableBean.class);
		return rtn;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(getTasks(relativePath).get(0).getHbaseQuorum());
	}
}
