package com.gnwang.hbase.until;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUntil {
	static Logger logger = LoggerFactory.getLogger("work");
	
	public static Logger getWorkLog() {
		return logger;
	}
}
