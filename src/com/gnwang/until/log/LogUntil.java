package com.gnwang.until.log;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUntil {
	private static String workLogName = "RollingFile2";
	private static String rootLogName = "RollingFile";
	private static Logger workLogger = LoggerFactory.getLogger(workLogName);
	private static Logger rootLogger = LoggerFactory.getLogger(rootLogName);

	public static Logger getWorkLog() {
		return workLogger;
	}
	
	public static Logger getRootLog() {
		return rootLogger;
	}
	
	public static void main(String[] args) {
		test1();
	}
	
	private static void test1() {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LogUntil.class);
		org.apache.log4j.Logger logger2 = org.apache.log4j.Logger.getLogger("CONSOLE");
		org.slf4j.Logger logger3 = LogUntil.getWorkLog();
		
		long start = System.currentTimeMillis();
		logger.debug("debug---");
		logger.info("info---");
		logger.error("error---");
		
		logger2.debug("debug2---");
		logger2.info("info2---");
		logger2.error("error2---");
		
		logger3.debug("debug3---");
		logger3.info("info3---");
		logger3.error("error3---");
		
		long end = System.currentTimeMillis();
		long millis = end-start;
		logger2.debug(millis+"");
		logger2.debug(end-start);
	}
	
	private static String propertyFile = "log4j.properties";
	private static String domFile = "log4j.xml";
	
	public void init1() {
		//1.xml文件
		DOMConfigurator.configure(domFile);
		//2.properties文件
		PropertyConfigurator.configure(propertyFile);
		//3.缺省Log4j环境
		BasicConfigurator.configure();
	}
	
	
	public void init2() {
		PropertyConfigurator.configure(getLogProperties());
	}
	
	private Properties getLogProperties() {
		Properties properties = new Properties();
		properties.put("log4j.rootLogger", "DEBUG , RollingFile,CONSOLE");
		
		properties.put("log4j.logger.com.neusoft", "OFF");
		
		properties.put("log4j.logger.RollingFile2", "DEBUG,RollingFile2");
		properties.put("log4j.additivity.RollingFile2", "false");
		
		properties.put("log4j.appender.RollingFile", "org.apache.log4j.RollingFileAppender");
		properties.put("log4j.appender.RollingFile.Threshold", "DEBUG");
		properties.put("log4j.appender.RollingFile.ImmediateFlush", "false");
		properties.put("log4j.appender.RollingFile.File", "/var/log/sdzw/demo/all.log");
		properties.put("log4j.appender.RollingFile.Append", "true");
		properties.put("log4j.appender.RollingFile.MaxFileSize", "50MB");
		properties.put("log4j.appender.RollingFile.MaxBackupIndex", "10");
		properties.put("log4j.appender.RollingFile.layout", "org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.RollingFile.layout.ConversionPattern", "%40d{[yyyy-MM-dd HH:mm:ss-S]} [%-5p] [ %-3.3r] [%-10.10c] %l  [%x] --> %n %m %n");
		
		properties.put("log4j.appender.RollingFile2", "org.apache.log4j.RollingFile2Appender");
		properties.put("log4j.appender.RollingFile2.Threshold", "DEBUG");
		properties.put("log4j.appender.RollingFile2.ImmediateFlush", "false");
		properties.put("log4j.appender.RollingFile2.File", "/var/log/sdzw/demo/work.log");
		properties.put("log4j.appender.RollingFile2.Append", "true");
		properties.put("log4j.appender.RollingFile2.MaxFileSize", "50MB");
		properties.put("log4j.appender.RollingFile2.MaxBackupIndex", "10");
		properties.put("log4j.appender.RollingFile2.layout", "org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.RollingFile2.layout.ConversionPattern", "%40d{[yyyy-MM-dd HH:mm:ss-S]} [%-5p] [ %-3.3r] [%-10.10c] %l  [%x] --> %n %m %n");
		
		properties.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
		properties.put("log4j.appender.CONSOLE.Threshold", "DEBUG");
		properties.put("log4j.appender.CONSOLE.ImmediateFlush", "true");
		properties.put("log4j.appender.CONSOLE.Target", "System.err");
		properties.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%40d{[yyyy-MM-dd HH:mm:ss-S]} [%-5p] [ %-3.3r] [%-10.10c] %l  [%x] --> %n %m %n");
		
		return properties;
	}
}
