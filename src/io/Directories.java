package io;

import java.io.File;

/**
 * This class defines the file paths of all files being used by the program.
 * 
 * @author Marcel
 */
public class Directories {

	/**
	 * Class loader for project-related files.
	 */
	private static ClassLoader classLoader = Thread.currentThread()
			.getContextClassLoader();

	/**
	 * The bin directory of the project.
	 * */
	private static File PATH_TO_BIN_DIRECTORY = new File(classLoader
			.getResource("").getPath());

	/**
	 * the root path of the project.
	 */
	public static final String PROJECT_ROOT_PATH = PATH_TO_BIN_DIRECTORY
			.getParent();
	
	/**
	 * the root path of the project.
	 */
	public static final String PROJECT_ROOT_PATH2 = new File(PROJECT_ROOT_PATH)
			.getParent();

	/**
	 * File path to the crawled urls.
	 */
	public static final String CRAWLED_URL_PATH = PROJECT_ROOT_PATH2 + "/files/origin/";
	
	/**
	 * File path to the cleaned html file.
	 */
	public static final String CLEANED_HTML_FILE_PATH = PROJECT_ROOT_PATH2 + "/files/cleaned/";

}