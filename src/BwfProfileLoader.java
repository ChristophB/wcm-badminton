import io.Directories;
import io.Disciplines;
import io.URLs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

/**
 * this class can load profiles for each discipline from bwf. for this purpose
 * no proxy, proxies from hidemyass or a custom proxy can be used.
 * 
 * @author Marcel
 * 
 */
public class BwfProfileLoader extends WebsiteOperations {

	/**
	 * stores the filenames of all downloaded websites.
	 */
	private HashSet<String> uniqueWebsiteList;

	/**
	 * determines, whether a proxy is used or not.
	 */
	private boolean useProxy;

	/**
	 * proxy for loading the website will be stored here.
	 */
	private Proxy proxy;

	/**
	 * the number of pages for the current discipline will be stored here.
	 */
	private int numberOfPagesForDiscipline;

	/**
	 * default constructor.
	 */
	public BwfProfileLoader() {
		this.useProxy = false;
		this.uniqueWebsiteList = new HashSet<String>();
	}

	/**
	 * all profiles for all disciplines will be loaded.
	 * 
	 * @param useProxy
	 */
	public void loadAllProfilesAsHTML(boolean useProxy) {
		loadAllMensSinglesProfilesAsHTML(useProxy);
		loadAllMensDoublesProfilesAsHTML(useProxy);
		loadAllWomensSinglesProfilesAsHTML(useProxy);
		loadAllWomensDoublesProfilesAsHTML(useProxy);
		loadAllMixedProfilesAsHTML(useProxy);
		logger.info("Unique files whole: " + uniqueWebsiteList.size());
	}

	/**
	 * all profiles for all disciplines will be loaded using the given proxy.
	 * 
	 * @param proxy
	 */
	public void loadAllProfilesAsHTML(Proxy proxy) {
		loadAllMensSinglesProfilesAsHTML(proxy);
		loadAllMensDoublesProfilesAsHTML(proxy);
		loadAllWomensSinglesProfilesAsHTML(proxy);
		loadAllWomensDoublesProfilesAsHTML(proxy);
		loadAllMixedProfilesAsHTML(proxy);
		logger.info("Unique files whole: " + uniqueWebsiteList.size());
	}

	/**
	 * load profiles for all men who play singles.
	 * 
	 * @param useProxy
	 */
	public void loadAllMensSinglesProfilesAsHTML(boolean useProxy) {
		this.useProxy = useProxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.MENS_SINGLES);
	}

	/**
	 * load profiles for all men who play singles.
	 * 
	 * @param proxy
	 */
	public void loadAllMensSinglesProfilesAsHTML(Proxy proxy) {
		this.proxy = proxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.MENS_SINGLES);
	}

	/**
	 * load profiles for all men who play doubles.
	 * 
	 * @param useProxy
	 */
	public void loadAllMensDoublesProfilesAsHTML(boolean useProxy) {
		this.useProxy = useProxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.MENS_DOUBLES);
	}

	/**
	 * load profiles for all men who play doubles.
	 * 
	 * @param proxy
	 */
	public void loadAllMensDoublesProfilesAsHTML(Proxy proxy) {
		this.proxy = proxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.MENS_DOUBLES);
	}

	/**
	 * load profiles for all women who play singles.
	 * 
	 * @param useProxy
	 */
	public void loadAllWomensSinglesProfilesAsHTML(boolean useProxy) {
		this.useProxy = useProxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.WOMENS_SINGLES);
	}

	/**
	 * load profiles for all women who play singles.
	 * 
	 * @param proxy
	 */
	public void loadAllWomensSinglesProfilesAsHTML(Proxy proxy) {
		this.proxy = proxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.WOMENS_SINGLES);
	}

	/**
	 * load profiles for all women who play doubles.
	 * 
	 * @param useProxy
	 */
	public void loadAllWomensDoublesProfilesAsHTML(boolean useProxy) {
		this.useProxy = useProxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.WOMENS_DOUBLES);
	}

	/**
	 * load profiles for all women who play doubles.
	 * 
	 * @param proxy
	 */
	public void loadAllWomensDoublesProfilesAsHTML(Proxy proxy) {
		this.proxy = proxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.WOMENS_DOUBLES);
	}

	/**
	 * load profiles for all men and women who play mixed.
	 * 
	 * @param useProxy
	 */
	public void loadAllMixedProfilesAsHTML(boolean useProxy) {
		this.useProxy = useProxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.MIXED);
	}

	/**
	 * load profiles for all men and women who play mixed.
	 * 
	 * @param proxy
	 */
	public void loadAllMixedProfilesAsHTML(Proxy proxy) {
		this.proxy = proxy;
		loadAllProfilesFromUrlAsHTML(Disciplines.MIXED);
	}

	/**
	 * this is the implemenation for loading all profiles for a certain
	 * discipline.
	 * 
	 * @param discipline
	 */
	public void loadAllProfilesFromUrlAsHTML(String discipline) {
		logger.info("start loading profiles for " + discipline);
		int numberOfPages = 1;
		int currentPage = 0;
		ArrayList<URL> linkList;
		URL currentUrl;
		String filename = null;
		String directory = null;
		while (numberOfPages > 0) {
			currentPage++;
			logger.info("current page:" + currentPage);
			currentUrl = getUrl(discipline, currentPage);
			setWebsiteURL(currentUrl);
			System.out.println(getFilename());
			filename = getFilename();
			directory = getDirectory(discipline);
			System.out.println(directory);
			if (this.proxy == null) {
				saveWebsiteToHTMLFile(useProxy, directory + filename);
			} else {
				saveWebsiteToHTMLFile(this.proxy, directory + filename);
			}
			linkList = createLinklistOfProfiles(discipline);
			logger.info(linkList.size() + " profiles to process");
			if (currentPage == 1) {
				numberOfPages = getNumberOfPagesForDiscipline();
				logger.info(numberOfPages + " pages to process");
			}
			numberOfPages--;
			/*
			for (URL url : linkList) {
				setWebsiteURL(url);
				filename = getFilename();
				if (this.proxy == null) {
					saveWebsiteToHTMLFile(useProxy, directory + filename);
				} else {
					saveWebsiteToHTMLFile(this.proxy, directory + filename);
				}
				uniqueWebsiteList.add(filename);
			}
			*/
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
		String urlAsString = null;
		switch (discipline) {
		case Disciplines.MENS_SINGLES:
			urlAsString = URLs.MENS_SINGLES_URL + String.valueOf(currentPage)
					+ URLs.URL_NUMBER_OF_RESULTS;
			break;
		case Disciplines.WOMENS_SINGLES:
			urlAsString = URLs.WOMENS_SINGLES_URL + String.valueOf(currentPage)
					+ URLs.URL_NUMBER_OF_RESULTS;
			break;
		case Disciplines.MENS_DOUBLES:
			urlAsString = URLs.MENS_DOUBLES_URL + String.valueOf(currentPage)
					+ URLs.URL_NUMBER_OF_RESULTS;
			break;
		case Disciplines.WOMENS_DOUBLES:
			urlAsString = URLs.WOMENS_DOUBLES_URL + String.valueOf(currentPage)
					+ URLs.URL_NUMBER_OF_RESULTS;
			break;
		case Disciplines.MIXED:
			urlAsString = URLs.MIXED_URL + String.valueOf(currentPage)
					+ URLs.URL_NUMBER_OF_RESULTS;
			break;
		default:
			return null;
		}
		try {
			return new URL(urlAsString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * returns the directory where to store the downloaded files.
	 * 
	 * @param discipline
	 * @return String
	 */
	public String getDirectory(String discipline) {
		String directory = null;
		switch (discipline) {
		case Disciplines.MENS_SINGLES:
			directory = Directories.MENS_SINGLES_PROFILES;
			break;
		case Disciplines.WOMENS_SINGLES:
			directory = Directories.WOMENS_SINGLES_PROFILES;
			break;
		case Disciplines.MENS_DOUBLES:
			directory = Directories.MENS_DOUBLES_PROFILES;
			break;
		case Disciplines.WOMENS_DOUBLES:
			directory = Directories.WOMENS_DOUBLES_PROFILES;
			break;
		case Disciplines.MIXED:
			directory = Directories.MIXED_DOUBLES_PROFILES;
			break;
		default:
			return null;
		}
		return directory;
	}

	/**
	 * this creates a link list of profiles, which need to be crawled.
	 */
	public ArrayList<URL> createLinklistOfProfiles(String discipline) {
		ArrayList<URL> linkList = new ArrayList<URL>();
		String line = null;
		boolean start = false;
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					getDestinationOfLastFile()), "UTF-8"));
			while ((line = in.readLine()) != null) {
				if (line.contains("Rank") && line.contains("Country")
						&& line.contains("Player")
						&& line.contains("Member ID")) {
					start = true;
					continue;
				}
				if (start == true) {
					String cleanedLine = Jsoup.parse(String.valueOf(line))
							.text();
					cleanedLine = cleanedLine.trim();
					if (cleanedLine.isEmpty())
						continue;
					else {
						String patternStr = "\\[[A-Z]{3}\\]";
						Pattern pattern = Pattern.compile(patternStr);
						Matcher matcher = pattern.matcher(cleanedLine);
						int count = 0;
						int match1 = 0;
						int match2 = 0;
						while (matcher.find()) {
							count++;
							if (count == 1) {
								match1 = matcher.start();
							} else {
								match2 = matcher.start();
							}
						}

						if (match1 != 0 && match2 != 0) {
							String name1 = cleanedLine.substring(
									match1 + "[XXX] ".length(), match2).trim();
							String name2 = cleanedLine.substring(
									match2 + "[XXX]".length(),
									cleanedLine.indexOf("Profile")).trim();

							name1 = getNewName(name1);
							name2 = getNewName(name2);

							FileUtils.write(new File("doubles.txt"), name1 + ";"
									+ name2 + ";" + discipline + "\n", true);
						}

						String startPattern = "<a href=\"../profile/default.aspx?id=";
						String endPattern = " class=\"icon profile\" title=\"Profile\"";
						String profileUrl = null;
						while (line.contains(startPattern)
								&& line.contains(endPattern)) {

							profileUrl = line.substring(
									line.indexOf(startPattern)
											+ "<a href=\"".length(),
									line.indexOf(endPattern) - 1);
							profileUrl.trim();
							profileUrl = profileUrl.replace("../",
									"http://bwf.tournamentsoftware.com/");
							profileUrl = profileUrl.replace("default",
									"biography");

							linkList.add(new URL(profileUrl));

							line = line.substring(line.indexOf(endPattern)
									+ endPattern.length());
						}
						if (line.contains("Page 1 of ")
								&& line.contains("Results per page")) {
							// last line is for determing the number of results
							// -> number of loops
							line = Jsoup.parse(String.valueOf(line)).text();
							startPattern = "Page 1 of ";
							int index = line.indexOf(startPattern);
							// this parameter will only set once for each
							// discipline
							line = line
									.substring(index + startPattern.length());
							char[] array = line.toCharArray();
							String numberAsString = "";
							for (char c : array) {
								if (Character.isDigit(c)) {
									numberAsString += c;
								} else
									break;
							}
							numberOfPagesForDiscipline = Integer
									.parseInt(numberAsString);
							break;
						}
					}
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return linkList;
	}

	/**
	 * change last and firstname if necessary
	 * 
	 * @param name
	 * @return newName
	 */
	public String getNewName(String name) {
		String[] split = name.split(" ");
		String newName = "";
		String helper = null;
		int counter = 0;
		for (String s : split) {
			counter++;
			if (counter == 1 && StringUtils.isAllUpperCase(s)) {
				helper = s;
				continue;
			}
			if (counter > 1 && helper != null) {
				newName += s + " ";
			}
		}
		if (helper == null) {
			newName = name;
		} else {
			newName = newName + helper;
		}
		return newName.trim();
	}

	/**
	 * calculates a filename for the current url.
	 * 
	 * @return filename
	 */
	public String getFilename() {
		String urlAsString = String.valueOf(getWebsiteURL());
		// int index = urlAsString.indexOf("id=");
		// String filename = urlAsString.substring(index + "id=".length());
		String[] array = urlAsString.split("/");
		String filename = array[array.length - 1];

		filename = filename.replaceAll("[\\?\\.=]", "_") + ".html";
		return filename;
	}

	/**
	 * returns the number of pages for discipline.
	 * 
	 * @return int
	 */
	public int getNumberOfPagesForDiscipline() {
		return numberOfPagesForDiscipline;
	}
}
