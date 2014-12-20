package io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

public class FileSystemOperations {
	
	private static HashSet<String> uniqueFilenames = new HashSet<String>();
	
	public static void cleanCrawledDirectories(){
		try {
			FileUtils.cleanDirectory(new File(Directories.MENS_DOUBLES_PROFILES));
			FileUtils.cleanDirectory(new File(Directories.WOMENS_DOUBLES_PROFILES));
			FileUtils.cleanDirectory(new File(Directories.MIXED_DOUBLES_PROFILES));
			FileUtils.cleanDirectory(new File(Directories.MENS_SINGLES_PROFILES));
			FileUtils.cleanDirectory(new File(Directories.WOMENS_SINGLES_PROFILES));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getFilesCount(File file, boolean unique) {
		File[] files = file.listFiles();
		  int count = 0;
		  for (File f : files){
		    if (f.isDirectory()){
		      count += getFilesCount(f, unique);
		    }
		    else{
		    	if(unique == true){
		    		uniqueFilenames.add(f.getName());
		    	}
		    	count++;
		    }    
		  }
		  if(unique == true) return uniqueFilenames.size();
		  else return count;
	}	
}
