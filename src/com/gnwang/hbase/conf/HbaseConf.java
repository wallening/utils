package com.gnwang.hbase.conf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gnwang.hbase.bean.HbaseCreateTableBean;
import com.gnwang.hbase.until.FileUntil;
import com.gnwang.hbase.until.LogUntil;

public class HbaseConf {
	
	public static final String relativePath = "hbase_create.json";
	
	public static List<HbaseCreateTableBean> getTasks(String relativePath) throws Exception {
		List<HbaseCreateTableBean> rtn = new ArrayList<>();
		InputStream in = HbaseConf.class.getResourceAsStream("/" + relativePath);
		String confStr  = FileUntil.getFileStr(in);
		JSONObject obj = JSONObject.parseObject(confStr);
		String zookeerquorum = obj.getString("hbase.zookeeper.quorum");
		
		LogUntil.getWorkLog().debug("ok  zookeerquorum: {} ", zookeerquorum);
		
		JSONArray tables = obj.getJSONArray("tables");
		for (int j = 0; j < tables.size(); j++) {
			JSONObject table = tables.getJSONObject(j);
			HbaseCreateTableBean createBean = new HbaseCreateTableBean();
			createBean.setFamilys(table.getString("familys"));
			createBean.setQuorum(zookeerquorum);
			createBean.setSpiltKeys(table.getString("spiltKeys"));
			createBean.setTableName(table.getString("tableName"));
			System.out.println(createBean.toString());
			rtn.add(createBean);
		}
		return rtn;
	}
	
	public static void main(String[] args) throws Exception {
		getTasks(relativePath);
	}
}
