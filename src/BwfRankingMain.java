import io.FileSystemOperations;

import java.io.File;
import java.util.logging.Logger;


/**
 * this class do all the work for setting up our mysql database. this includes
 * crawling the urls, cleaning the files, extracting the relevant information
 * and finally storing it in the database.
 * 
 * @author Marcel
 * 
 */
public class BwfRankingMain {
	
	/**
	 * logging some intermediate states could be helpful.
	 */
	public static final Logger logger = Logger.getLogger( BwfProfileLoader.class.getName() );

	/**
	 * this is the main method executes the whole process.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info(String.valueOf(FileSystemOperations.getFilesCount(new File(System.getProperty("user.dir") + "/crawler/data/"), false)));
		logger.info(String.valueOf(FileSystemOperations.getFilesCount(new File(System.getProperty("user.dir") + "/crawler/data/"), true)));

		//FileOperations.cleanCrawledDirectories();
		//BwfProfileLoader loader = new BwfProfileLoader();
		//loader.loadAllProfilesAsHTML(false);
	}
}
