
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
	 * this is the main method executes the whole process.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		BwfProfileLoader loader = new BwfProfileLoader();
		loader.loadAllProfilesAsHTML();
	}
}
