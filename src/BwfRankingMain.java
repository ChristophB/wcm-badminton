import java.net.URL;
import java.util.ArrayList;

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

		int numberOfPages = 1;
		int currentPage = 0;
		WebsiteOperations mensSinglesRankingWebsite = new WebsiteOperations();
		ArrayList<URL> linkList;
		while (numberOfPages > 0) {
			currentPage++;
			String startUrl = "http://bwf.tournamentsoftware.com/ranking/category.aspx?id=7845&category=472&p="
					+ currentPage + "&ps=100";
			mensSinglesRankingWebsite.setWebsiteURL(startUrl);
			mensSinglesRankingWebsite.saveWebsiteToHTMLFile();
			if (currentPage == 1) {
				numberOfPages = mensSinglesRankingWebsite
						.getNumberOfPagesForDiscipline();
			}
			numberOfPages--;

			linkList = mensSinglesRankingWebsite.createLinklistOfProfiles();
			for (URL url : linkList) {
				mensSinglesRankingWebsite.setWebsiteURL(url);
				mensSinglesRankingWebsite.saveWebsiteToHTMLFile();
				mensSinglesRankingWebsite.createCleanedFile();
				// playerProfile.extractInformation();
				// playerProfile.insertExtractedInformationToDatabase();
			}
		}
		// mens singles completed

	}
}
