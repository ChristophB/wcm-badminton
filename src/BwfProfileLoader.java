import io.Disciplines;
import io.URLs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * this class can load profiles for each discipline.
 * 
 * @author Marcel
 *
 */
public class BwfProfileLoader {

	public BwfProfileLoader() {

	}

	/**
	 * all profiles for all disciplines will be loaded.
	 */
	public void loadAllProfilesAsHTML() {
		loadAllMensSinglesProfilesAsHTML();
		loadAllMensDoublesProfilesAsHTML();
		loadAllWomensSinglesProfilesAsHTML();
		loadAllWomensDoublesProfilesAsHTML();
		loadAllMixedProfilesAsHTML();
	}

	public void loadAllMensSinglesProfilesAsHTML() {
		loadAllProfilesFromUrlAsHTML(Disciplines.MENS_SINGLES);
	}

	public void loadAllMensDoublesProfilesAsHTML() {
		loadAllProfilesFromUrlAsHTML(Disciplines.MENS_DOUBLES);
	}

	public void loadAllWomensSinglesProfilesAsHTML() {
		loadAllProfilesFromUrlAsHTML(Disciplines.WOMENS_SINGLES);
	}

	public void loadAllWomensDoublesProfilesAsHTML() {
		loadAllProfilesFromUrlAsHTML(Disciplines.WOMENS_DOUBLES);
	}

	public void loadAllMixedProfilesAsHTML() {
		loadAllProfilesFromUrlAsHTML(Disciplines.MIXED);
	}

	/**
	 * this is the implemenation for loading all profiles for a certain discipline.
	 * 
	 * @param discipline
	 */
	public void loadAllProfilesFromUrlAsHTML(String discipline) {
		int numberOfPages = 1;
		int currentPage = 0;
		WebsiteOperations mensSinglesRankingWebsite = new WebsiteOperations();
		ArrayList<URL> linkList;
		URL currentUrl;
		while (numberOfPages > 0) {
			currentPage++;
			currentUrl = getUrl(discipline, currentPage);
			mensSinglesRankingWebsite.setWebsiteURL(currentUrl);
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
			}
		}
	}

	/**
	 * returns the url for the discipline and the currentpage.
	 * 
	 * @param discipline
	 * @param currentPage
	 * @return url
	 */
	public URL getUrl(String discipline, int currentPage) {
		try {
			switch (discipline) {
			case Disciplines.MENS_SINGLES:
				return new URL(URLs.MENS_SINGLES_URL
						+ String.valueOf(currentPage)
						+ URLs.URL_NUMBER_OF_RESULTS);
			case Disciplines.WOMENS_SINGLES:
				return new URL(URLs.WOMENS_SINGLES_URL
						+ String.valueOf(currentPage)
						+ URLs.URL_NUMBER_OF_RESULTS);
			case Disciplines.MENS_DOUBLES:
				return new URL(URLs.MENS_DOUBLES_URL
						+ String.valueOf(currentPage)
						+ URLs.URL_NUMBER_OF_RESULTS);
			case Disciplines.WOMENS_DOUBLES:
				return new URL(URLs.WOMENS_DOUBLES_URL
						+ String.valueOf(currentPage)
						+ URLs.URL_NUMBER_OF_RESULTS);
			case Disciplines.MIXED:
				return new URL(URLs.MIXED_URL + String.valueOf(currentPage)
						+ URLs.URL_NUMBER_OF_RESULTS);
			default:
				return null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
