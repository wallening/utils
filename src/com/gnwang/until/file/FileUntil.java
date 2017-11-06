package com.gnwang.until.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class FileUntil {

	public static byte[] readAllBytes(String fileName, long maxSize) throws IOException {  
	    Path path = Paths.get(fileName);  
	    long size = Files.size(path);  
	    if (size > maxSize) {  
	        throw new IOException("file: " + path + ", size:" + size + "> " + maxSize);  
	    }  
	    return Files.readAllBytes(path);  
	}  
	  
	public static List<String> readAlllines(String fileName, Charset charset, long maxSize) throws IOException {  
	    Path path = Paths.get(fileName);  
	    long size = Files.size(path);  
	    if (size > maxSize) {  
	        throw new IOException("file: " + path + ", size:" + size + "> " + maxSize);  
	    }  
	    return Files.readAllLines(path, charset);  
	}  
	
	
	public static void main(String[] args) throws IOException {
		Path path = Paths.get("/test/a.txt");  
		//Path转换File  
		File file = path.toFile();  
		  
//		Files.readAllBytes(path);  
//		Files.deleteIfExists(path);  
//		Files.size(path);  
//		try (InputStream in = Files.newInputStream(path)) {  
//		    // process  
//		    in.read();  
//		}  
		
		class MyFileVisitor extends SimpleFileVisitor<Path>{  
		    @Override  
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {  
		        System.out.println(file);  
		        return FileVisitResult.CONTINUE;  
		    }  
		      
		}  
		
		  Path path2 = Paths.get("/home/acer/");  
	        
	        System.out.println(Files.walkFileTree(path2, new MyFileVisitor()) );
	}
}
