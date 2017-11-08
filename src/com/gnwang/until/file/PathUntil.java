package com.gnwang.until.file;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;

import com.gnwang.until.log.LogUntil;
/**
 * 参考
 * http://www.cnblogs.com/leehongee/p/3324062.html
 */
public class PathUntil {
	static final Logger LOGGER = LogUntil.getWorkLog();
	/**
	 * 多个路径拼接
	 * @param first 第一个路径
	 * @param more 更多路径
	 * @return
	 */
	public static Path combine(String first, String... more) {
//		Paths.get(first, more).toUri().toURL();
		return Paths.get(first, more);
	}
	
	/**
	 * 获取用户目录，new File()的默认路径
	 * @return
	 */
	public static String getCurrentUserDir() {
		return System.getProperty("user.dir");
	}
	
	/**
	 * 得到的是当前的classpath的绝对URI路径。
	 * @return
	 */
	public static String getCurrentClasspath() {
		URL url = null;
//		url = ClassLoader.getSystemResource("");
//		url = PathUntil.class.getClassLoader().getResource("");
//		url = PathUntil.class.getResource("/");
		//推荐
		url = Thread.currentThread().getContextClassLoader().getResource("");
		return url.getPath();
	}
	
	/**
	 * 得到的是当前类PathUntil.class文件的URI目录。不包括自己！
	 * @return
	 */
	public static String getCurrentJavapath() {
		return PathUntil.class.getResource("").getPath();
		
	}
	
	public static void main(String[] args) {
		LOGGER.debug(getCurrentClasspath());
		LOGGER.debug(getCurrentJavapath());
		LOGGER.debug(getCurrentUserDir());
	}
	
}
