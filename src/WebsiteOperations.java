import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;

import parser.Parser;
import parser.ProxyManager;
import parser.html.HTMLParser;
import parser.html.sites.HideMyAssHTMLParser;

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
	 * this is the hide my ass proxy parser.
	 */
	private HideMyAssHTMLParser hideMyAss;
	
	/**
	 * stores the destination of the last successfully downloaded url.
	 */
	private String destinationOfLastFile;

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
	public void saveWebsiteToHTMLFile(String filename) {
		boolean completed = false;
		while(completed == false){
			try {
				ReadableByteChannel rbc = Channels.newChannel(getWebsiteURL().openStream());
				FileOutputStream fos = new FileOutputStream(new File(filename));
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				completed = true;
			} catch (IOException e) {
				System.out.println("Could not download website.");
			}
		}
		numberOfCrawledURLs++;
		destinationOfLastFile = filename;
	}

	/**
	 * saves the website to a html file, using a proxy. if an error occures, the
	 * next proxy will be taken for loading the content from the url.
	 */
	public void saveWebsiteToHTMLFile(boolean useProxy, String filename) {
		if (useProxy == true) {
			ProxyManager proxyManager = ProxyManager.getInstance();
			if (hideMyAss == null) {
				hideMyAss = new HideMyAssHTMLParser();
				try {
					proxyManager.setProxyParser(hideMyAss);
					proxyManager.loadProxies();
					proxyManager.switchProxy();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			boolean completed = false;
			boolean firstTry = true;
			while (completed == false) {
				try {
					if (firstTry != true) {
						proxyManager.switchProxy();
					}
					String code = hideMyAss.fetch(String.valueOf(getWebsiteURL()),
							proxyManager.getCurrentProxy());
					if(code != null){
						FileUtils.writeStringToFile(new File(filename), code);
						completed = true;
					}
					else {
						firstTry = false;
						continue;
					}
				} catch (Exception e1) {
					System.out.println("Could not download website.");
					firstTry = false;
					continue;
				}
			}
			numberOfCrawledURLs++;
			destinationOfLastFile = filename;
		} else {
			saveWebsiteToHTMLFile(filename);
		}
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
	public void setWebsiteURL(String websiteURLAsString) {
		try {
			this.websiteURL = new URL(websiteURLAsString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * returns the current website url.
	 * 
	 * @return url
	 */
	public URL getWebsiteURL() {
		return websiteURL;
	}

	/**
	 * returns the current number of successfully crawled urls.
	 * 
	 * @return int
	 */
	public int getNumberOfCrawledURLs() {
		return numberOfCrawledURLs;
	}

	/**
	 * transfers the html text into plain text.
	 */
	public void createCleanedFile(String inputFilename, String outputFilename,
			boolean removeEmptyLines) {
		String line = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFilename), "UTF-8"));
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFilename), "UTF-8"));
			while ((line = in.readLine()) != null) {

				String cleanedLine = Jsoup.parse(String.valueOf(line)).text();
				cleanedLine = cleanedLine.trim();
				if (removeEmptyLines == true && cleanedLine.equals("")) {
					continue;
				}
				out.write(cleanedLine + "\n");
			}
			in.close();
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * returns the destination of the last successfully downloaded url.
	 * @return String
	 */
	public String getDestinationOfLastFile() {
		return destinationOfLastFile;
	}
}
