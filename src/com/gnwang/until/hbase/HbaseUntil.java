package com.gnwang.until.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.gnwang.until.log.LogUntil;

public class HbaseUntil {
	private static HbaseUntil hbaseUntil;
	private static Connection connection;

	private HbaseUntil() {

	}

	public synchronized static HbaseUntil getInstance(String quorum) throws IOException {
		if (hbaseUntil == null) {
			hbaseUntil = new HbaseUntil();
			connection = getConnection(quorum);
		}
		return hbaseUntil;
	}

	public static Connection getConnection(String quorum) throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", quorum);
		// conf.set("hbase.zookeeper.property.clientport", "2181");
		conf.set("hbase.client.retries.number", "3");// 重试次数，默认为 10，可配置为 3
		conf.set("hbase.client.pause", "100");// 重试的休眠时间，默认为 1s，可减少，比如 100ms

		Connection connection = ConnectionFactory.createConnection(conf);
		return connection;
	}

	public void createNamespace(String namespace) throws IOException {
		Admin admin = connection.getAdmin();
		admin.createNamespace(NamespaceDescriptor.create(namespace).build());
		LogUntil.getWorkLog().debug("ok  创建空间: {}", namespace);
	}

	public void createTableWithRegion(String tableName, String spiltKeys, String... familys) throws IOException {
		Admin admin = connection.getAdmin();
		HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(tableName));
		for (String family : familys) {
			descriptor.addFamily(new HColumnDescriptor(family));
		}

		String[] spiltKeys1 = spiltKeys.split(",");
		byte[][] spiltKeys2 = new byte[spiltKeys1.length][];
		for (int i = 0; i < spiltKeys2.length; i++) {
			spiltKeys2[i] = spiltKeys1[i].getBytes("utf-8");
		}
		admin.createTable(descriptor, spiltKeys2);
		admin.close();
		LogUntil.getWorkLog().debug("ok  创建表并建立分区: {}", tableName);
	}


	public boolean existTable(String tableName) throws IOException {
		boolean rtn = false;
		Admin admin = connection.getAdmin();
		rtn = admin.tableExists(TableName.valueOf(tableName));
		LogUntil.getWorkLog().debug("ok  表: {} 是否存在: {}", tableName, rtn);
		return rtn;
	}
	
	public void createTable(String tableName, String... familys) throws IOException {
		Admin admin = connection.getAdmin();
		HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(tableName));
		for (String family : familys) {
			descriptor.addFamily(new HColumnDescriptor(family));
		}

		admin.createTable(descriptor);
		admin.close();
		LogUntil.getWorkLog().debug("ok  创建表: {}", tableName);
	}

	public void deleteTable(String tableName) throws IOException {
		Admin admin = connection.getAdmin();
		if (admin.tableExists(TableName.valueOf(tableName))) {
			admin.disableTable(TableName.valueOf(tableName));
			admin.deleteTable(TableName.valueOf(tableName));
			LogUntil.getWorkLog().debug("ok  删除表: {}", tableName);
		} else {
			LogUntil.getWorkLog().debug("ok  删除表: {} 表不存在", tableName);
		}
	}

	public void insertRow(String tableName, Put put) throws IOException {
		long start = System.currentTimeMillis();
		Table table = connection.getTable(TableName.valueOf(tableName));
		table.put(put);
		table.close();
		long end = System.currentTimeMillis();
		LogUntil.getWorkLog().debug("ok  插入一条数据: {} TimeMillis: {}", put.getId(), end - start);

	}

	public void insertRows(String tableName, List<Put> puts) throws IOException {
		long start = System.currentTimeMillis();
		HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
		/*
		 * table的参数是否每次调用都要设置，这个要测试
		 */
		table.setAutoFlushTo(false);// AutoFlush指的是在每次调用HBase的Put操作，是否提交到HBase Server。
									// 默认是true,每次会提交。如果此时是单条插入，就会有更多的IO,从而降低性能
		table.setWriteBufferSize(1024 * 1024 * 24);// Write Buffer
													// Size在AutoFlush为false的时候起作用，默认是2MB,也就是当插入数据超过2MB,就会自动提交到Server
		// put.setWriteToWAL(wal);
		// WAL是Write Ahead
		// Log的缩写，指的是HBase在插入操作前是否写Log。默认是打开，关掉会提高性能，但是如果系统出现故障(负责插入的Region
		// Server挂掉)，数据可能会丢失。
		table.put(puts);
		table.close();
		long end = System.currentTimeMillis();
		LogUntil.getWorkLog().debug("ok  批量插入数据: {} 条 TimeMillis: {}", puts.size(), end - start);
	}

	public void deleteRow(String tableName, Delete del) throws IOException {
		// Delete d1 = new Delete(Bytes.toBytes(rowkey));
		long start = System.currentTimeMillis();
		HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
		table.delete(del);// d1.addColumn(family, qualifier);d1.addFamily(family);
		table.close();
		long end = System.currentTimeMillis();
		LogUntil.getWorkLog().debug("ok  删除一条数据: {} TimeMillis: {}", del.getId(), end - start);
	}

	public void deleteRows(String tableName, List<Delete> dels) throws IOException {
		// Delete d1 = new Delete(Bytes.toBytes(rowkey));
		long start = System.currentTimeMillis();
		HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
		table.delete(dels);// d1.addCo	lumn(family, qualifier);d1.addFamily(family);
		table.close();
		long end = System.currentTimeMillis();
		LogUntil.getWorkLog().debug("ok  批量删除数据: {} 条 TimeMillis: {}", dels.size(), end - start);
	}

	public List<Map<String, String>> queryAll(String tableName) throws Exception {
		List<Map<String, String>> rtn = new ArrayList<>();
		HTable table = (HTable) connection.getTable(TableName.valueOf(tableName));
		ResultScanner rs = table.getScanner(new Scan());
		for (Result r : rs) {
			Map<String, String> map = new HashMap<>();
			map.put("rowkey", new String(r.getRow()));
			for (Cell keyValue : r.rawCells()) {
				map.put("family", new String(CellUtil.cloneFamily(keyValue)));
				map.put(new String(CellUtil.cloneQualifier(keyValue)), new String(CellUtil.cloneValue(keyValue)));
			}
			rtn.add(map);
		}
		rs.close();
		table.close();
		return rtn;
	}

	public Map<String, String> queryByRowkey(String tableName, String rowkey) throws Exception {
		Map<String, String> rtn = new HashMap<>();
		rtn.put("rowkey", rowkey);
		Table table = connection.getTable(TableName.valueOf(tableName));
		Get get = new Get(rowkey.getBytes());// 根据rowkey查询
		Result r = table.get(get);
		for (Cell keyValue : r.rawCells()) {
			rtn.put(new String(CellUtil.cloneQualifier(keyValue)), new String(CellUtil.cloneValue(keyValue)));
		}
		table.close();
		return rtn;
	}

	public List<Map<String, String>> queryByCondition(String tableName, String familyName, String qualifier,
			String value) throws IOException {
		List<Map<String, String>> rtn = new ArrayList<>();
		Table table = connection.getTable(TableName.valueOf(tableName));
		Filter filter = new SingleColumnValueFilter(Bytes.toBytes(familyName), Bytes.toBytes(qualifier),
				CompareOp.EQUAL, Bytes.toBytes(value)); // 当列familyName的值为value时进行查询
		Scan s = new Scan();
		s.setFilter(filter);
		ResultScanner rs = table.getScanner(s);
		for (Result r : rs) {
			Map<String, String> map = new HashMap<>();
			map.put("rowkey", new String(r.getRow()));
			for (Cell keyValue : r.rawCells()) {
				map.put("family", new String(CellUtil.cloneFamily(keyValue)));
				map.put(new String(CellUtil.cloneQualifier(keyValue)), new String(CellUtil.cloneValue(keyValue)));
			}
			rtn.add(map);
		}
		rs.close();
		table.close();
		return rtn;
	}

	public List<Map<String, String>> queryByConditions(String tableName, String[] familyNames, String[] qualifiers,
			String[] values) throws IOException {
		List<Map<String, String>> rtn = new ArrayList<>();
		Table table = connection.getTable(TableName.valueOf(tableName));
		List<Filter> filters = new ArrayList<Filter>();
		if (familyNames != null && familyNames.length > 0) {
			int i = 0;
			for (String familyName : familyNames) {
				Filter filter = new SingleColumnValueFilter(Bytes.toBytes(familyName), Bytes.toBytes(qualifiers[i]),
						CompareOp.EQUAL, Bytes.toBytes(values[i]));
				filters.add(filter);
				i++;
			}
		}

		FilterList filterList = new FilterList(filters);
		Scan scan = new Scan();
		scan.setFilter(filterList);
		ResultScanner rs = table.getScanner(scan);
		for (Result r : rs) {
			Map<String, String> map = new HashMap<>();
			map.put("rowkey", new String(r.getRow()));
			System.out.println("获得到rowkey:" + new String(r.getRow()));
			for (Cell keyValue : r.rawCells()) {
				map.put("family", new String(CellUtil.cloneFamily(keyValue)));
				map.put(new String(CellUtil.cloneQualifier(keyValue)), new String(CellUtil.cloneValue(keyValue)));
			}
			rtn.add(map);
		}
		rs.close();
		table.close();
		return rtn;
	}
	static int num=0;
	public static Put createPut() {
		long millons = System.currentTimeMillis();
		Put put = new Put((millons + "_" + num).getBytes());
		for (int i = 0; i < 10; i++) {
			
			String col = "col_" + i;
			String value = millons + "_" + i + "_" + num;
			put.addColumn("cf01".getBytes(), col.getBytes(), value.getBytes());
		}
		num++;
		return put;
	}

	public static Delete createDelte(String rowkey) {
		Delete del = new Delete(rowkey.getBytes());
		return del;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("hadoop.home.dir", "/home/acer/log");
		String quorum = "hadoop1:2181,hadoop2:2181,hadoop3:2181";
		String tableName = "gnwang_hbase_test_01";
		String familyName = "cf01";
		HbaseUntil hbaseUntil = HbaseUntil.getInstance(quorum);
		if (!hbaseUntil.existTable(tableName)) {
			hbaseUntil.createTable(tableName, familyName);
		}
		
		hbaseUntil.insertRow(tableName, createPut());
		
		List<Put> puts = new ArrayList<>();
		puts.add(createPut());
		puts.add(createPut());
		puts.add(createPut());
		puts.add(createPut());
		hbaseUntil.insertRows(tableName, puts);
		
		List<Map<String, String>> list2 = hbaseUntil.queryAll(tableName);
		System.out.println(list2.size());
		System.out.println(list2.toString());
		
		List<Map<String, String>> list3 = hbaseUntil.queryByCondition(tableName, familyName, "col_5", "1509625302272_5_4");
		System.out.println(list3.toString());
		hbaseUntil.deleteTable(tableName);
	}
}
