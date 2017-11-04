package com.sdzw.wgn;

import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;

public class MyAppender extends RollingFileAppender {

	@Override
	public boolean isAsSevereAsThreshold(Priority priority) {
		return  this.getThreshold().equals(priority); 
	}
}
