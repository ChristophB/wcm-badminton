import io.Directories;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import org.jsoup.Jsoup;

/**
 * this class can execute different kind of operations on a website.
 * 
 * @author Marcel
 */
public class WebsiteOperations {

	/**
	 * number of crawled urls will be stores here.
	 */
	private int numberOfCrawledURLs;

	/**
	 * the url of the website will be stored here.
	 */
	private URL websiteURL;
	
	/**
	 * the number of pages for the current discipline will be stored here.
	 */
	private int numberOfPagesForDiscipline;
	
	/**
	 * default constructor
	 */
	public WebsiteOperations() {
		
	}

	/**
	 * constructor for the website class.
	 * 
	 * @param url
	 */
	public WebsiteOperations(String url) {
		try {
			this.websiteURL = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * constructor for the website class.
	 * 
	 * @param url
	 */
	public WebsiteOperations(URL url) {
		this.websiteURL = url;
	}

	/**
	 * saves the website in a file.
	 */
	public void saveWebsiteToHTMLFile() {
		ReadableByteChannel rbc;
		try {
			rbc = Channels.newChannel(websiteURL.openStream());
			//filenameOfLastCrawledWebsite = getCurrentFilename();
			numberOfCrawledURLs++;
			FileOutputStream fos = new FileOutputStream(
					Directories.CRAWLED_URL_PATH + numberOfCrawledURLs + ".html");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * this creates a link list of profiles, which need to be crawled.
	 */
	public ArrayList<URL> createLinklistOfProfiles() {
		ArrayList<URL> linkList = new ArrayList<URL>();
		String line = null;
		boolean start = false;
		int counter = 101;
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(Directories.CRAWLED_URL_PATH
							+ numberOfCrawledURLs + ".html"), "UTF-8"));
			while ((line = in.readLine()) != null) {
				if (start == true && counter == 0)
					break;
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
						if(counter > 1){
							String startPattern = "<a href=\"../profile/default.aspx?id=";
							String endPattern = " class=\"icon profile\" title=\"Profile\"";
							line = line.substring(line.indexOf(startPattern) + "<a href=\"".length(),
									line.indexOf(endPattern) - 1);
							line.trim();
							line = line.replace("../", "http://bwf.tournamentsoftware.com/");
						}
						else{
							//last line is for determing the number of results -> number of loops
							line = Jsoup.parse(String.valueOf(line)).text();
							String startPattern = "Page 1 of ";
							if (line.contains(startPattern)){
								int index = line.indexOf(startPattern);
								//this parameter will only set once for each discipline
								line = line.substring(index + startPattern.length());
								char[] array = line.toCharArray();
								String numberAsString = "";
								for(char c: array){
									if(Character.isDigit(c)){
										numberAsString+= c;
									}
									else break;
								}
								numberOfPagesForDiscipline = Integer.parseInt(numberAsString);
							}
						}
						
						//last line is no link and should not be added
						if(counter != 1){
							linkList.add(new URL(line));
						}
						counter--;
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
	 * returns all whitespace characters.
	 * 
	 * @return String
	 */
	public String getWhiteSpaceChars() {
		String whitespaceChars = "" /* dummy empty string for homogeneity */
				+ "\\u0009" // CHARACTER TABULATION
				+ "\\u000A" // LINE FEED (LF)
				+ "\\u000B" // LINE TABULATION
				+ "\\u000C" // FORM FEED (FF)
				+ "\\u000D" // CARRIAGE RETURN (CR)
				+ "\\u0020" // SPACE
				+ "\\u0085" // NEXT LINE (NEL)
				+ "\\u00A0" // NO-BREAK SPACE
				+ "\\u1680" // OGHAM SPACE MARK
				+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
				+ "\\u2000" // EN QUAD
				+ "\\u2001" // EM QUAD
				+ "\\u2002" // EN SPACE
				+ "\\u2003" // EM SPACE
				+ "\\u2004" // THREE-PER-EM SPACE
				+ "\\u2005" // FOUR-PER-EM SPACE
				+ "\\u2006" // SIX-PER-EM SPACE
				+ "\\u2007" // FIGURE SPACE
				+ "\\u2008" // PUNCTUATION SPACE
				+ "\\u2009" // THIN SPACE
				+ "\\u200A" // HAIR SPACE
				+ "\\u2028" // LINE SEPARATOR
				+ "\\u2029" // PARAGRAPH SEPARATOR
				+ "\\u202F" // NARROW NO-BREAK SPACE
				+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
				+ "\\u3000"; // IDEOGRAPHIC SPACE
		return whitespaceChars;
	}
	
	/**
	 * returns the number of pages for discipline.
	 * @return int
	 */
	public int getNumberOfPagesForDiscipline() {
		return numberOfPagesForDiscipline;
	}
	
	/**
	 * sets the url.
	 * 
	 * @param websiteURL
	 */
	public void setWebsiteURL(URL websiteURL) {
		this.websiteURL = websiteURL;
	}
	
	/**
	 * sets the url after converting the string in an url.
	 * 
	 * @param websiteURLAsString
	 */
	public void setWebsiteURL(String websiteURLAsString){
		try {
			this.websiteURL = new URL(websiteURLAsString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * returns the current website url.
	 * @return url
	 */
	public URL getWebsiteURL() {
		return websiteURL;
	}
}
