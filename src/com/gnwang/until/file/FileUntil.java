package com.gnwang.until.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

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
	
	@SuppressWarnings("resource")
	public static String getFileStr(String path) throws Exception {
		String rtn = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
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
	
	public Properties getProperties(String path) throws Exception {
		Properties rtn = new Properties();
		FileInputStream in = new FileInputStream(path);
		rtn.load(in);
		in.close();
		return rtn;
	}
	
	public static void main(String[] args) throws Exception {
	}
}
