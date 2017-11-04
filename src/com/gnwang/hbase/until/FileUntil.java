package com.gnwang.hbase.until;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUntil {

	public static String getFileStr(InputStream in) throws Exception {
		String rtn = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		if (sb.length() > 0) {
			rtn = sb.toString();
		}
		return rtn;
	}
	public static void main(String[] args) throws Exception {
	}
}
