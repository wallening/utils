package com.gnwang.until.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;

import com.gnwang.until.log.LogUntil;

public class FileUntil {
	public static final long TFILE_SIZE = 1024*1024*50;//50M
	static final Logger LOGGER = LogUntil.getWorkLog();
	
	public static Path findPath(String filePath) throws IOException {
		Path rtn = Paths.get(filePath);
		if (!rtn.toFile().exists()) {
			rtn = Paths.get(PathUntil.getCurr+entClasspath(), filePath);
		}
		return rtn;
	}
	
	public static File findFile(String filePath) throws IOException {
		return findPath(filePath).toFile();
	}
	  
	public static String readFile(String filePath) throws Exception {
		return new String(readAllBytes(filePath, TFILE_SIZE), "utf-8");
	}
	
	public static String readFile(File file) throws Exception {
		return new String(Files.readAllBytes(file.toPath()), "utf-8");
	}
	
	public static byte[] readAllBytes(String filePath) throws IOException {  
		 Path path = findPath(filePath);
		return Files.readAllBytes(path);  
	}
	
	public static byte[] readAllBytes(String filePath, long maxSize) throws IOException {  
		Path path = findPath(filePath);
	    long size = Files.size(path);  
	    if (size > maxSize) {  
	        throw new IOException("file: " + path + ", size:" + size + "> " + maxSize);  
	    }  
	    return Files.readAllBytes(path);  
	}  
	
	public static List<String> readAlllines(String filePath, Charset charset) throws IOException {  
		 Path path = findPath(filePath);
	    return Files.readAllLines(path, charset);  
	}  
	
	public static List<String> readAlllines(String filePath, Charset charset, long maxSize) throws IOException {  
	    Path path = findPath(filePath);  
	    long size = Files.size(path);  
	    if (size > maxSize) {  
	        throw new IOException("file: " + path + ", size:" + size + "> " + maxSize);  
	    }  
	    return Files.readAllLines(path, charset);  
	}  
	
	public static void main(String[] args) throws Exception {
		String relativePath = "hbase_create.json";
		Path path = findFile(relativePath).toPath();
		LOGGER.debug("path: {}",path.toString());
		LOGGER.debug("exists: {}",path.toFile().exists());
		//Path转换File  
		  
		Files.readAllBytes(path);  
		Files.size(path);  
		try (InputStream in = Files.newInputStream(path)) {  
		    // process  
		    in.read();  
		}  
//		Files.deleteIfExists(path);  
	}
}
