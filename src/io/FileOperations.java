package io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileOperations {

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
}
