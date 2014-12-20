package parser.html;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parser.Parser;

/**
 *
 * @author jnphilipp
 * @version 1.7.5
 */
public class HTMLParser implements Parser {
	
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0";
	public static final int DEFAULT_TIMEOUT = 10000;
	/**
	 * response code
	 */
	protected int responseCode = 0;
	/**
	 * Code of the web page.
	 */
	protected String code = "";
	/**
	 * User-Agent which is used when connecting to the page.
	 */
	protected String userAgent = "";
	/**
	 * content type
	 */
	protected String contentType = "";
	/**
	 * timeout
	 */
	protected int timeout;

	/**
	 * Default constructor.
	 */
	public HTMLParser() {
		this.setDefaultUserAgent();
		this.setDefaultTimeout();
	}

	/**
	 * Creates a new HTMLParser and fetches the given site.
	 * @param url URL which will be fetched
	 */
	public HTMLParser(String url) throws Exception {
		this.setDefaultUserAgent();
		this.setDefaultTimeout();
		this.fetch(url);
	}

	/**
	 * Creates a new HTMLParser and fetches the given site.
	 * @param url URL which will be fetched
	 * @param decodeHTML if <code>true</code> HTML encoded characters will be decoded.
	 */
	public HTMLParser(String url, boolean decodeHTML) throws Exception {
		this.setDefaultUserAgent();
		this.setDefaultTimeout();
		this.fetch(url, decodeHTML);
	}

	/**
	 * Sets the User-Agent to the given agent.
	 * @param agent New User-Agent to use.
	 */
	public void setUserAgent(String agent) {
		this.userAgent = agent;
	}

	/**
	 * Sets the User-Agent to the default User-Agent.
	 */
	public void setDefaultUserAgent() {
		this.userAgent = HTMLParser.DEFAULT_USER_AGENT;
	}

	/**
	 * Sets the User-Agent to the given agent.
	 * @param agent New User-Agent to use.
	 */
	public void setTimeout(String agent) {
		this.userAgent = agent;
	}

	/**
	 * Sets the timeout to the default timeout.
	 */
	public void setDefaultTimeout() {
		this.timeout = HTMLParser.DEFAULT_TIMEOUT;
	}

	/**
	 * Returns the web pages code.
	 * @return code
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * Returns the content type.
	 * @return content type
	 */
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public int getResponseCode() {
		return this.responseCode;
	}

	@Override
	public boolean isResponseCodeOK() {
		return this.responseCode == HttpURLConnection.HTTP_OK;
	}

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @throws Exception 
	 */
	@Override
	public String fetch(String url) throws Exception {
		return this.fetch(url, false, Proxy.NO_PROXY);
	}

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param docodeHTML if <code>true</code> HTML encoded characters will be decoded.
	 * @param proxy proxy
	 * @throws Exception 
	 */
	public String fetch(String url, boolean decodeHTML) throws Exception {
		return this.fetch(url, decodeHTML, Proxy.NO_PROXY);
	}

	/**
	 * Builds a connection to the given URL using the given proxy and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param proxy proxy
	 * @throws Exception 
	 */
	@Override
	public String fetch(String url, Proxy proxy) throws Exception {
		return this.fetch(url, false, proxy);
	}

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param docodeHTML if <code>true</code> HTML encoded characters will be decoded.
	 * @param proxy proxy
	 * @throws Exception 
	 */
	public String fetch(String url, boolean decodeHTML, Proxy proxy) throws Exception {
		this.code = null;
		System.out.println(url + " "+ proxy.toString());
		if ( proxy == null )
			proxy = Proxy.NO_PROXY;

		URL u = new URL(url);
		HttpURLConnection con = (HttpURLConnection)u.openConnection(proxy);
		con.setConnectTimeout(this.timeout);
		con.setReadTimeout(this.timeout);

		if ( !this.userAgent.equals("") )
			con.setRequestProperty("User-Agent", this.userAgent);

		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.connect();
		String header = con.getHeaderField("Content-Type");
		String charset = "utf-8";

		if ( header != null ) {
			if ( header.contains("ISO-8859-15") )
  			charset = "ISO-8859-15";
			else if ( header.contains("ISO-8859-1") )
				charset = "ISO-8859-1";
		}

		this.contentType = header;
		this.responseCode = con.getResponseCode();

		if ( this.responseCode == HttpURLConnection.HTTP_OK ) {
			InputStreamReader in = new InputStreamReader(con.getInputStream(), charset);
			BufferedReader buff = new BufferedReader(in);

			String line;
			StringBuilder text = new StringBuilder();

			while ( (line = buff.readLine()) != null ) {
				text.append(line);
				text.append("\n");
			}

			buff.close();
			in.close();

			this.code = text.toString().replace("\0", " ").replace("\u2028", "\n").replace(String.valueOf((char)160), " ");
		}
		con.disconnect();

		if ( decodeHTML )
			this.code = this.decode();
		if(this.code == null) return this.code;
		else return String.valueOf(this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @return list of tags
	 */
	public List<String> getTags(String tag) {
		return getTags(tag, "", false, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag) {
		List<String> tags = getTags(tag, "", false, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return list of tags
	 */
	public List<String> getTags(String tag, boolean clean) {
		return getTags(tag, "", clean, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag, boolean clean) {
		List<String> tags = getTags(tag, "", clean, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return list of tags
	 */
	public List<String> getTags(String tag, String param) {
		return getTags(tag, param, false, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag, String param) {
		List<String> tags = getTags(tag, param, false, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return list of tags
	 */
	public List<String> getTags(String tag, String param, boolean clean) {
		return getTags(tag, param, clean, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag, String param, boolean clean) {
		List<String> tags = getTags(tag, param, clean, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, String param, String code) {
		return getTags(tag, param, false, code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public static String getFirstTag(String tag, String param, String code) {
		List<String> tags = getTags(tag, param, false, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, boolean clean, String code) {
		return getTags(tag, "", clean, code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param codes The HTML-codes which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, boolean clean, Collection<String> codes) {
		List<String> tags = new ArrayList<>();
		for ( String code : codes )
			tags.addAll(getTags(tag, "", clean, code));

		return tags;
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public static String getFirstTag(String tag, boolean clean, String code) {
		List<String> tags = getTags(tag, "", clean, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, String param, boolean clean, String code) {
		ArrayList<String> l = new ArrayList<>();

		int i = -1;

		while ( (i = code.indexOf("<" + tag, i + 1)) != -1 ) {
			if ( !param.equals("") ) {
				if ( !code.substring(i, code.indexOf(">", i)).contains(param) ) {
					i = code.indexOf(">", i);
					continue;
				}
			}

			int j = code.indexOf("</" + tag, i);
			String s = code.substring(i, code.indexOf(">", j) + 1);

			int k = tag.length();

			while ( (k = s.indexOf("<" + tag, k)) != -1 ) {
				k = s.indexOf(">", k);
				j = code.indexOf("</" + tag, j + tag.length());

				if ( j < 0 )
					break;

				s = code.substring(i, code.indexOf(">", j) + 1);
			}

			if ( j < 0 ) {
				i += tag.length() + param.length();
				continue;
			}

			if ( clean )
				s = s.replaceAll("<.*?>", "");

			if ( !s.equals("") )
				l.add(s);

			i = j;
		}

		return l;
	}

	/**
	 * 
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public static String getFirstTag(String tag, String param, boolean clean, String code) {
		List<String> tags = getTags(tag, param, clean, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @return list of tags
	 */
	public List<String> getOnlyTags(String tag) {
		return getOnlyTags(tag, false, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag) {
		List<String> tags = getOnlyTags(tag, false, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return list of tags
	 */
	public List<String> getOnlyTags(String tag, boolean clean) {
		return getOnlyTags(tag, clean, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag, boolean clean) {
		List<String> tags = getOnlyTags(tag, clean, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getOnlyTags(String tag, String code) {
		return getOnlyTags(tag, false, code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag, String code) {
		List<String> tags = getOnlyTags(tag, false, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getOnlyTags(String tag, boolean clean, String code) {
		ArrayList<String> l = new ArrayList<>();

		int i = -1;

		while ( (i = code.indexOf("<" + tag + ">", i + 1)) != -1 ) {
			int j = code.indexOf("</" + tag + ">", i);
			String s = code.substring(i, code.indexOf(">", j) + 1);

			int k = tag.length();

			while ( (k = s.indexOf("<" + tag + ">", k)) != -1 ) {
				k = s.indexOf(">", k);
				j = code.indexOf("</" + tag + ">", j + tag.length());

				if ( j < 0 )
					break;

				s = code.substring(i, code.indexOf(">", j) + 1);
			}

			if ( clean )
				s = s.replaceAll("<.*?>", "");

			if ( !s.equals("") )
				l.add(s);

			i = j;
		}

		return l;
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag, boolean clean, String code) {
		List<String> tags = getOnlyTags(tag, clean, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @return list of tag occurrences
	 */
	public List<String> getTagsWithoutEnd(String tag) {
		return getTagsWithoutEnd(tag, "", "", this.code);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @return first occurrences of given tag
	 */
	public String getFirstTagWithoutEnd(String tag) {
		List<String> tags = getTagsWithoutEnd(tag, "", "", this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return list of tag occurrences
	 */
	public List<String> getTagsWithoutEnd(String tag, String param) {
		return getTagsWithoutEnd(tag, param, "", this.code);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return first occurrences of given tag
	 */
	public String getFirstTagWithoutEnd(String tag, String param) {
		List<String> tags = getTagsWithoutEnd(tag, param, "", this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @return list of tag occurrences
	 */
	public List<String> getTagsWithoutEnd(String tag, String param, String returnContent) {
		return getTagsWithoutEnd(tag, param, returnContent, this.code);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @return first occurrences of given tag
	 */
	public String getFirstTagWithoutEnd(String tag, String param, String returnContent) {
		List<String> tags = getTagsWithoutEnd(tag, param, returnContent, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @param code The HTML-code which will be searched.
	 * @return list of tag occurrences
	 */
	public static List<String> getTagsWithoutEnd(String tag, String param, String returnContent, String code) {
		List<String> l = new ArrayList<>();

		Pattern p = Pattern.compile("<" + Pattern.quote(tag) + "[^>]*?" + Pattern.quote(param) + "[^>]*?>");
		Matcher m = p.matcher(code);
		while ( m.find() ) {
			if ( returnContent.isEmpty() )
				l.add(m.group());
			else {
				p = Pattern.compile(returnContent);
				Matcher mrc = p.matcher(m.group());
				while ( mrc.find() )
					l.add(mrc.group(mrc.groupCount()));
			}
		}

		return l;
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @param code The HTML-code which will be searched.
	 * @return first occurrences of given tag
	 */
	public static String getFirstTagWithoutEnd(String tag, String param, String returnContent, String code) {
		List<String> tags = getTagsWithoutEnd(tag, param, returnContent, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Removes all tags from the parsed code.
	 * @return clean code
	 */
	public String removeAllTags() {
		return removeAllTags(this.code);
	}

	/**
	 * Removes all tags from the given code.
	 * @param code code
	 * @return clean code
	 */
	public static String removeAllTags(String code) {
		return code.replaceAll("<[^>]*>", "");
	}

	/**
	 * Remove all occurrences of the given tag in the code.
	 * @param tag HTML-tag like b, i, div
	 * @return cleaned code
	 */
	public String replaceTagContent(String tag) {
		return this.replaceTagContent(tag, "", this.code);
	}

	/**
	 * Remove all occurrences of the given tag in the code.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return cleaned code
	 */
	public String replaceTagContent(String tag, String param) {
		return this.replaceTagContent(tag, param, this.code);
	}

	/**
	 * Remove all occurrences of the given tag in the code.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param code The HTML-code which will be searched.
	 * @return cleaned code
	 */
	public String replaceTagContent(String tag, String param, String code) {
		String returnCode = code;
		List<String> tags = getTags(tag, param, code);
		for ( String r : tags )
			returnCode = returnCode.replaceAll(Pattern.quote(r), "");

		return returnCode;
	}

	/**
	 * Returns all links this site contains.
	 * @return list of all links
	 */
	public List<String> getLinkURLs() {
		return getLinkURLs("", this.code);
	}

	/**
	 * Returns the first link this site contains.
	 * @return first link
	 */
	public String getFirstLinkURL() {
		List<String> links = getLinkURLs("", this.code);

		if ( links.isEmpty() )
			return "";

		return links.get(0);
	}

	/**
	 * Returns all links this site contains.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @return list of all links
	 */
	public List<String> getLinkURLs(String base) {
		return getLinkURLs(base, this.code);
	}

	/**
	 * Returns first link this site contains.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @return first link
	 */
	public String getFirstLinkURL(String base) {
		List<String> links = getLinkURLs(base, this.code);

		if ( links.isEmpty() )
			return "";

		return links.get(0);
	}

	/**
	 * Returns all links in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param code the HTML-code which will be searched
	 * @return list of all links
	 */
	public static List<String> getLinkURLs(String base, String code) {
		List<String> a = new ArrayList<>();

		Pattern p = Pattern.compile("href=\"?.[^\" >]*(.*?)\"?");
		Matcher m = p.matcher(code);

		while ( m.find() ) {
			//a.add(this.code.substring(m.start() + 6, m.end()-1).startsWith("?") || this.code.substring(m.start() + 6, m.end()-1).startsWith("/") ? base + this.code.substring(m.start() + 6, m.end()-1) : this.code.substring(m.start() + 6, m.end()-1));
			String s = code.substring(m.start() + (m.group().startsWith("href=\"") ? 6 : 5), m.end() - (m.group().endsWith("\"") ? 1 : 0));
			if ( s.contains("javascript") || s.equals("#") )
				continue;
			else if ( s.startsWith("?") || s.startsWith("/") )
				s = base + s.replaceAll(" ", "");

			a.add(s);
		}

		return a;
	}

	/**
	 * Returns first link in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param code the HTML-code which will be searched
	 * @return first link
	 */
	public static String getFirstLinkURL(String base, String code) {
		List<String> links = getLinkURLs(base, code);

		if ( links.isEmpty() )
			return "";

		return links.get(0);
	}

	/**
	 * Returns all links in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param codes multiple HTML-codes which will be searched
	 * @return list of all links
	 */
	public static Collection<String> getLinkURLs(String base, List<String> codes) {
		Collection<String> links = new ArrayList<>();
		for ( String code : codes )
			links.addAll(getLinkURLs(base, code));

		return links;
	}

	/**
	 * Returns all links and link texts in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param code the HTML-code which will be searched
	 * @return list of all links and link texts
	 */
	public static Collection<String[]> getLinkURLsAndTexts(String base, String code) {
		Collection<String[]> links = new ArrayList<>();

		Matcher matcher = Pattern.compile("<a[^>]*href=\"([^\"]+)\"[^>]*>(.+)</a>").matcher(code);

		while ( matcher.find() ) {
			String[] s = {matcher.group(1), matcher.group(2)};

			if ( s[0].contains("javascript") || s[0].equals("#") )
				continue;
			else if ( s[0].startsWith("?") || s[0].startsWith("/") )
				s[0] = base + s[0].replaceAll(" ", "");

			links.add(s);
		}

		return links;
	}

	/**
	 * Returns all links and link texts in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param codes multiple HTML-codes which will be searched
	 * @return list of all links and link texts
	 */
	public static Collection<String[]> getLinkURLsAndTexts(String base, Collection<String> codes) {
		Collection<String[]> links = new ArrayList<>();

		for ( String code : codes )
			links.addAll(getLinkURLsAndTexts(base, code));

		return links;
	}

	/**
	 * Returns the base URL of the given URL.
	 * @param url URL
	 * @return base URL or an empty string
	 */
	public static String getBaseURL(String url) {
		Pattern p = Pattern.compile("(^https?://(www\\.)?[^/]+\\.[^/\\.]+)/?.*$");
		Matcher m = p.matcher(url);
		if ( m.find() )
			return m.group(1);
		else
			return "";
	}

	/**
	 * Check if the given URL is a base URL.
	 * @param url URL
	 * @return <code>true</code> if given URL is a base URL
	 */
	public static boolean isBaseURL(String url) {
		return Pattern.compile("^https?://(www\\.)?[^/]+\\.[^/\\.]+/?$").matcher(url).find();
	}

	/**
	 * Replaces all HTML special characters in the page code with the original character.
	 * @return replace String
	 */
	public String decode() {
		return decode(this.code);
	}

	/**
	 * Replaces all HTML special characters in the given String with the original character.
	 * @param toDecode List of String with HTML characters to replace
	 * @return replace List of String
	 */
	public static List<String> decode(List<String> toDecode) {
		List<String> decoded = new ArrayList<>();

		for ( String s : toDecode )
			decoded.add(decode(s));

		return decoded;
	}

	/**
	 * Replaces all HTML special characters in the given String with the original character.
	 * @param toDecode String with HTML characters to replace
	 * @return replace String
	 */
	public static String decode(String toDecode) {
		toDecode = toDecode.replaceAll("&quot;", "\"");
		toDecode = toDecode.replaceAll("&#34;", "\"");
		toDecode = toDecode.replaceAll("&amp;", "&");
		toDecode = toDecode.replaceAll("&#38;", "&");
		toDecode = toDecode.replaceAll("&#39;", "'");
		toDecode = toDecode.replaceAll("&#039;", "'");
		toDecode = toDecode.replaceAll("&lt;", "<");
		toDecode = toDecode.replaceAll("&#60;", "<");
		toDecode = toDecode.replaceAll("&gt;", ">");
		toDecode = toDecode.replaceAll("&#62;", ">");
		toDecode = toDecode.replaceAll("&nbsp;", " ");
		toDecode = toDecode.replaceAll("&#160;", " ");
		toDecode = toDecode.replaceAll("&#xa0;", " ");
		toDecode = toDecode.replaceAll("&iexcl;", "Ã‚Â¡");
		toDecode = toDecode.replaceAll("&#161;", "Ã‚Â¡");
		toDecode = toDecode.replaceAll("&cent;", "Ã‚Â¢");
		toDecode = toDecode.replaceAll("&#162;", "Ã‚Â¢");
		toDecode = toDecode.replaceAll("&pound;", "Ã‚Â£");
		toDecode = toDecode.replaceAll("&#163;", "Ã‚Â£");
		toDecode = toDecode.replaceAll("&curren;", "Ã‚Â¤");
		toDecode = toDecode.replaceAll("&#164;", "Ã‚Â¤");
		toDecode = toDecode.replaceAll("&yen;", "Ã‚Â¥");
		toDecode = toDecode.replaceAll("&#165;", "Ã‚Â¥");
		toDecode = toDecode.replaceAll("&brvbar;", "Ã‚Â¦");
		toDecode = toDecode.replaceAll("&#166;", "Ã‚Â¦");
		toDecode = toDecode.replaceAll("&sect;", "Ã‚Â§");
		toDecode = toDecode.replaceAll("&#167;", "Ã‚Â§");
		toDecode = toDecode.replaceAll("&uml;", "Ã‚Â¨");
		toDecode = toDecode.replaceAll("&#168;", "Ã‚Â¨");
		toDecode = toDecode.replaceAll("&copy;", "Ã‚Â©");
		toDecode = toDecode.replaceAll("&#169;", "Ã‚Â©");
		toDecode = toDecode.replaceAll("&ordf;", "Ã‚Âª");
		toDecode = toDecode.replaceAll("&#170;", "Ã‚Âª");
		toDecode = toDecode.replaceAll("&laquo;", "Ã‚Â«");
		toDecode = toDecode.replaceAll("&#171;", "Ã‚Â«");
		toDecode = toDecode.replaceAll("&not;", "Ã‚Â¬");
		toDecode = toDecode.replaceAll("&#172;", "Ã‚Â¬");
		toDecode = toDecode.replaceAll("&shy;", "Ã‚Â­");
		toDecode = toDecode.replaceAll("&#173;", "Ã‚Â­");
		toDecode = toDecode.replaceAll("&reg;", "Ã‚Â®");
		toDecode = toDecode.replaceAll("&#174;", "Ã‚Â®");
		toDecode = toDecode.replaceAll("&macr;", "Ã‚Â¯");
		toDecode = toDecode.replaceAll("&#175;", "Ã‚Â¯");
		toDecode = toDecode.replaceAll("&deg;", "Ã‚Â°");
		toDecode = toDecode.replaceAll("&#176;", "Ã‚Â°");
		toDecode = toDecode.replaceAll("&plusmn;", "Ã‚Â±");
		toDecode = toDecode.replaceAll("&#177;", "Ã‚Â±");
		toDecode = toDecode.replaceAll("&sup2;", "Ã‚Â²");
		toDecode = toDecode.replaceAll("&#178;", "Ã‚Â²");
		toDecode = toDecode.replaceAll("&sup3;", "Ã‚Â³");
		toDecode = toDecode.replaceAll("&#179;", "Ã‚Â³");
		toDecode = toDecode.replaceAll("&acute;", "Ã‚Â´");
		toDecode = toDecode.replaceAll("&#180;", "Ã‚Â´");
		toDecode = toDecode.replaceAll("&micro;", "Ã‚Âµ");
		toDecode = toDecode.replaceAll("&#181;", "Ã‚Âµ");
		toDecode = toDecode.replaceAll("&para;", "Ã‚Â¶");
		toDecode = toDecode.replaceAll("&#182;", "Ã‚Â¶");
		toDecode = toDecode.replaceAll("&middot;", "Ã‚Â·");
		toDecode = toDecode.replaceAll("&#183;", "Ã‚Â·");
		toDecode = toDecode.replaceAll("&cedil;", "Ã‚Â¸");
		toDecode = toDecode.replaceAll("&#184;", "Ã‚Â¸");
		toDecode = toDecode.replaceAll("&sup1;", "Ã‚Â¹");
		toDecode = toDecode.replaceAll("&#185;", "Ã‚Â¹");
		toDecode = toDecode.replaceAll("&ordm;", "Ã‚Âº");
		toDecode = toDecode.replaceAll("&#186;", "Ã‚Âº");
		toDecode = toDecode.replaceAll("&raquo;", "Ã‚Â»");
		toDecode = toDecode.replaceAll("&#187;", "Ã‚Â»");
		toDecode = toDecode.replaceAll("&frac14;", "Ã‚Â¼");
		toDecode = toDecode.replaceAll("&#188;", "Ã‚Â¼");
		toDecode = toDecode.replaceAll("&frac12;", "Ã‚Â½");
		toDecode = toDecode.replaceAll("&#189;", "Ã‚Â½");
		toDecode = toDecode.replaceAll("&frac34;", "Ã‚Â¾");
		toDecode = toDecode.replaceAll("&#190;", "Ã‚Â¾");
		toDecode = toDecode.replaceAll("&iquest;", "Ã‚Â¿");
		toDecode = toDecode.replaceAll("&#191;", "Ã‚Â¿");
		toDecode = toDecode.replaceAll("&Agrave;", "Ãƒâ‚¬");
		toDecode = toDecode.replaceAll("&#192;", "Ãƒâ‚¬");
		toDecode = toDecode.replaceAll("&Aacute;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&#193;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&Acirc;", "Ãƒâ€š");
		toDecode = toDecode.replaceAll("&#194;", "Ãƒâ€š");
		toDecode = toDecode.replaceAll("&Atilde;", "ÃƒÆ’");
		toDecode = toDecode.replaceAll("&#195;", "ÃƒÆ’");
		toDecode = toDecode.replaceAll("&Auml;", "Ãƒâ€ž");
		toDecode = toDecode.replaceAll("&#196;", "Ãƒâ€ž");
		toDecode = toDecode.replaceAll("&Aring;", "Ãƒâ€¦");
		toDecode = toDecode.replaceAll("&#197;", "Ãƒâ€¦");
		toDecode = toDecode.replaceAll("&AElig;", "Ãƒâ€ ");
		toDecode = toDecode.replaceAll("&#198;", "Ãƒâ€ ");
		toDecode = toDecode.replaceAll("&Ccedil;", "Ãƒâ€¡");
		toDecode = toDecode.replaceAll("&#199;", "Ãƒâ€¡");
		toDecode = toDecode.replaceAll("&Egrave;", "ÃƒË†");
		toDecode = toDecode.replaceAll("&#200;", "ÃƒË†");
		toDecode = toDecode.replaceAll("&Eacute;", "Ãƒâ€°");
		toDecode = toDecode.replaceAll("&#201;", "Ãƒâ€°");
		toDecode = toDecode.replaceAll("&Ecirc;", "ÃƒÅ ");
		toDecode = toDecode.replaceAll("&#202;", "ÃƒÅ ");
		toDecode = toDecode.replaceAll("&Euml;", "Ãƒâ€¹");
		toDecode = toDecode.replaceAll("&#203;", "Ãƒâ€¹");
		toDecode = toDecode.replaceAll("&Igrave;", "ÃƒÅ’");
		toDecode = toDecode.replaceAll("&#204;", "ÃƒÅ’");
		toDecode = toDecode.replaceAll("&Iacute;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&#205;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&Icirc;", "ÃƒÅ½");
		toDecode = toDecode.replaceAll("&#206;", "ÃƒÅ½");
		toDecode = toDecode.replaceAll("&Iuml;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&#207;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&ETH;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&#208;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&Ntilde;", "Ãƒâ€˜");
		toDecode = toDecode.replaceAll("&#209;", "Ãƒâ€˜");
		toDecode = toDecode.replaceAll("&Ograve;", "Ãƒâ€™");
		toDecode = toDecode.replaceAll("&#210;", "Ãƒâ€™");
		toDecode = toDecode.replaceAll("&Oacute;", "Ãƒâ€œ");
		toDecode = toDecode.replaceAll("&#211;", "Ãƒâ€œ");
		toDecode = toDecode.replaceAll("&Ocirc;", "Ãƒâ€�");
		toDecode = toDecode.replaceAll("&#212;", "Ãƒâ€�");
		toDecode = toDecode.replaceAll("&Otilde;", "Ãƒâ€¢");
		toDecode = toDecode.replaceAll("&#213;", "Ãƒâ€¢");
		toDecode = toDecode.replaceAll("&Ouml;", "Ãƒâ€“");
		toDecode = toDecode.replaceAll("&#214;", "Ãƒâ€“");
		toDecode = toDecode.replaceAll("&times;", "Ãƒâ€”");
		toDecode = toDecode.replaceAll("&#215;", "Ãƒâ€”");
		toDecode = toDecode.replaceAll("&Oslash;", "ÃƒËœ");
		toDecode = toDecode.replaceAll("&#216;", "ÃƒËœ");
		toDecode = toDecode.replaceAll("&Ugrave;", "Ãƒâ„¢");
		toDecode = toDecode.replaceAll("&#217;", "Ãƒâ„¢");
		toDecode = toDecode.replaceAll("&Uacute;", "ÃƒÅ¡");
		toDecode = toDecode.replaceAll("&#218;", "ÃƒÅ¡");
		toDecode = toDecode.replaceAll("&Ucirc;", "Ãƒâ€º");
		toDecode = toDecode.replaceAll("&#219;", "Ãƒâ€º");
		toDecode = toDecode.replaceAll("&Uuml;", "ÃƒÅ“");
		toDecode = toDecode.replaceAll("&#220;", "ÃƒÅ“");
		toDecode = toDecode.replaceAll("&Yacute;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&#221;", "Ãƒï¿½");
		toDecode = toDecode.replaceAll("&THORN;", "ÃƒÅ¾");
		toDecode = toDecode.replaceAll("&#222;", "ÃƒÅ¾");
		toDecode = toDecode.replaceAll("&szlig;", "ÃƒÅ¸");
		toDecode = toDecode.replaceAll("&#223;", "ÃƒÅ¸");
		toDecode = toDecode.replaceAll("&agrave;", "ÃƒÂ ");
		toDecode = toDecode.replaceAll("&#224;", "ÃƒÂ ");
		toDecode = toDecode.replaceAll("&aacute;", "ÃƒÂ¡");
		toDecode = toDecode.replaceAll("&#225;", "ÃƒÂ¡");
		toDecode = toDecode.replaceAll("&acirc;", "ÃƒÂ¢");
		toDecode = toDecode.replaceAll("&#226;", "ÃƒÂ¢");
		toDecode = toDecode.replaceAll("&atilde;", "ÃƒÂ£");
		toDecode = toDecode.replaceAll("&#227;", "ÃƒÂ£");
		toDecode = toDecode.replaceAll("&auml;", "ÃƒÂ¤");
		toDecode = toDecode.replaceAll("&#228;", "ÃƒÂ¤");
		toDecode = toDecode.replaceAll("&aring;", "ÃƒÂ¥");
		toDecode = toDecode.replaceAll("&#229;", "ÃƒÂ¥");
		toDecode = toDecode.replaceAll("&aelig;", "ÃƒÂ¦");
		toDecode = toDecode.replaceAll("&#230;", "ÃƒÂ¦");
		toDecode = toDecode.replaceAll("&ccedil;", "ÃƒÂ§");
		toDecode = toDecode.replaceAll("&#231;", "ÃƒÂ§");
		toDecode = toDecode.replaceAll("&egrave;", "ÃƒÂ¨");
		toDecode = toDecode.replaceAll("&#232;", "ÃƒÂ¨");
		toDecode = toDecode.replaceAll("&eacute;", "ÃƒÂ©");
		toDecode = toDecode.replaceAll("&#233;", "ÃƒÂ©");
		toDecode = toDecode.replaceAll("&ecirc;", "ÃƒÂª");
		toDecode = toDecode.replaceAll("&#234;", "ÃƒÂª");
		toDecode = toDecode.replaceAll("&euml;", "ÃƒÂ«");
		toDecode = toDecode.replaceAll("&#235;", "ÃƒÂ«");
		toDecode = toDecode.replaceAll("&igrave;", "ÃƒÂ¬");
		toDecode = toDecode.replaceAll("&#236;", "ÃƒÂ¬");
		toDecode = toDecode.replaceAll("&iacute;", "ÃƒÂ­");
		toDecode = toDecode.replaceAll("&#237;", "ÃƒÂ­");
		toDecode = toDecode.replaceAll("&icirc;", "ÃƒÂ®");
		toDecode = toDecode.replaceAll("&#238;", "ÃƒÂ®");
		toDecode = toDecode.replaceAll("&iuml;", "ÃƒÂ¯");
		toDecode = toDecode.replaceAll("&#239;", "ÃƒÂ¯");
		toDecode = toDecode.replaceAll("&eth;", "ÃƒÂ°");
		toDecode = toDecode.replaceAll("&#240;", "ÃƒÂ°");
		toDecode = toDecode.replaceAll("&ntilde;", "ÃƒÂ±");
		toDecode = toDecode.replaceAll("&#241;", "ÃƒÂ±");
		toDecode = toDecode.replaceAll("&ograve;", "ÃƒÂ²");
		toDecode = toDecode.replaceAll("&#242;", "ÃƒÂ²");
		toDecode = toDecode.replaceAll("&oacute;", "ÃƒÂ³");
		toDecode = toDecode.replaceAll("&#243;", "ÃƒÂ³");
		toDecode = toDecode.replaceAll("&ocirc;", "ÃƒÂ´");
		toDecode = toDecode.replaceAll("&#244;", "ÃƒÂ´");
		toDecode = toDecode.replaceAll("&otilde;", "ÃƒÂµ");
		toDecode = toDecode.replaceAll("&#245;", "ÃƒÂµ");
		toDecode = toDecode.replaceAll("&ouml;", "ÃƒÂ¶");
		toDecode = toDecode.replaceAll("&#246;", "ÃƒÂ¶");
		toDecode = toDecode.replaceAll("&divide;", "ÃƒÂ·");
		toDecode = toDecode.replaceAll("&#247;", "ÃƒÂ·");
		toDecode = toDecode.replaceAll("&oslash;", "ÃƒÂ¸");
		toDecode = toDecode.replaceAll("&#248;", "ÃƒÂ¸");
		toDecode = toDecode.replaceAll("&ugrave;", "ÃƒÂ¹");
		toDecode = toDecode.replaceAll("&#249;", "ÃƒÂ¹");
		toDecode = toDecode.replaceAll("&uacute;", "ÃƒÂº");
		toDecode = toDecode.replaceAll("&#250;", "ÃƒÂº");
		toDecode = toDecode.replaceAll("&ucirc;", "ÃƒÂ»");
		toDecode = toDecode.replaceAll("&#251;", "ÃƒÂ»");
		toDecode = toDecode.replaceAll("&uuml;", "ÃƒÂ¼");
		toDecode = toDecode.replaceAll("&#252;", "ÃƒÂ¼");
		toDecode = toDecode.replaceAll("&#xfc;", "ÃƒÂ¼");
		toDecode = toDecode.replaceAll("&yacute;", "ÃƒÂ½");
		toDecode = toDecode.replaceAll("&#253;", "ÃƒÂ½");
		toDecode = toDecode.replaceAll("&thorn;", "ÃƒÂ¾");
		toDecode = toDecode.replaceAll("&#254;", "ÃƒÂ¾");
		toDecode = toDecode.replaceAll("&yuml;", "ÃƒÂ¿");
		toDecode = toDecode.replaceAll("&#255;", "ÃƒÂ¿");
		toDecode = toDecode.replaceAll("&Alpha;", "ÃŽâ€˜");
		toDecode = toDecode.replaceAll("&#913;", "ÃŽâ€˜");
		toDecode = toDecode.replaceAll("&alpha;", "ÃŽÂ±");
		toDecode = toDecode.replaceAll("&#945;", "ÃŽÂ±");
		toDecode = toDecode.replaceAll("&Beta;", "ÃŽâ€™");
		toDecode = toDecode.replaceAll("&#914;", "ÃŽâ€™");
		toDecode = toDecode.replaceAll("&beta;", "ÃŽÂ²");
		toDecode = toDecode.replaceAll("&#946;", "ÃŽÂ²");
		toDecode = toDecode.replaceAll("&Gamma;", "ÃŽâ€œ");
		toDecode = toDecode.replaceAll("&#915;", "ÃŽâ€œ");
		toDecode = toDecode.replaceAll("&gamma;", "ÃŽÂ³");
		toDecode = toDecode.replaceAll("&#947;", "ÃŽÂ³");
		toDecode = toDecode.replaceAll("&Delta;", "ÃŽâ€�");
		toDecode = toDecode.replaceAll("&#916;", "ÃŽâ€�");
		toDecode = toDecode.replaceAll("&delta;", "ÃŽÂ´");
		toDecode = toDecode.replaceAll("&#948;", "ÃŽÂ´");
		toDecode = toDecode.replaceAll("&Epsilon;", "ÃŽâ€¢");
		toDecode = toDecode.replaceAll("&#917;", "ÃŽâ€¢");
		toDecode = toDecode.replaceAll("&epsilon;", "ÃŽÂµ");
		toDecode = toDecode.replaceAll("&#949;", "ÃŽÂµ");
		toDecode = toDecode.replaceAll("&Zeta;", "ÃŽâ€“");
		toDecode = toDecode.replaceAll("&#918;", "ÃŽâ€“");
		toDecode = toDecode.replaceAll("&zeta;", "ÃŽÂ¶");
		toDecode = toDecode.replaceAll("&#950;", "ÃŽÂ¶");
		toDecode = toDecode.replaceAll("&Eta;", "ÃŽâ€”");
		toDecode = toDecode.replaceAll("&#919;", "ÃŽâ€”");
		toDecode = toDecode.replaceAll("&eta;", "ÃŽÂ·");
		toDecode = toDecode.replaceAll("&#951;", "ÃŽÂ·");
		toDecode = toDecode.replaceAll("&Theta;", "ÃŽËœ");
		toDecode = toDecode.replaceAll("&#920;", "ÃŽËœ");
		toDecode = toDecode.replaceAll("&theta;", "ÃŽÂ¸");
		toDecode = toDecode.replaceAll("&#952;", "ÃŽÂ¸");
		toDecode = toDecode.replaceAll("&Iota;", "ÃŽâ„¢");
		toDecode = toDecode.replaceAll("&#921;", "ÃŽâ„¢");
		toDecode = toDecode.replaceAll("&iota;", "ÃŽÂ¹");
		toDecode = toDecode.replaceAll("&#953;", "ÃŽÂ¹");
		toDecode = toDecode.replaceAll("&Kappa;", "ÃŽÅ¡");
		toDecode = toDecode.replaceAll("&#922;", "ÃŽÅ¡");
		toDecode = toDecode.replaceAll("&kappa;", "ÃŽÂº");
		toDecode = toDecode.replaceAll("&#954;", "ÃŽÂº");
		toDecode = toDecode.replaceAll("&Lambda;", "ÃŽâ€º");
		toDecode = toDecode.replaceAll("&#923;", "ÃŽâ€º");
		toDecode = toDecode.replaceAll("&lambda;", "ÃŽÂ»");
		toDecode = toDecode.replaceAll("&#955;", "ÃŽÂ»");
		toDecode = toDecode.replaceAll("&Mu;", "ÃŽÅ“");
		toDecode = toDecode.replaceAll("&#924;", "ÃŽÅ“");
		toDecode = toDecode.replaceAll("&mu;", "ÃŽÂ¼");
		toDecode = toDecode.replaceAll("&#956;", "ÃŽÂ¼");
		toDecode = toDecode.replaceAll("&Nu;", "ÃŽï¿½");
		toDecode = toDecode.replaceAll("&#925;", "ÃŽï¿½");
		toDecode = toDecode.replaceAll("&nu;", "ÃŽÂ½");
		toDecode = toDecode.replaceAll("&#957;", "ÃŽÂ½");
		toDecode = toDecode.replaceAll("&Xi;", "ÃŽÅ¾");
		toDecode = toDecode.replaceAll("&#926;", "ÃŽÅ¾");
		toDecode = toDecode.replaceAll("&xi;", "ÃŽÂ¾");
		toDecode = toDecode.replaceAll("&#958;", "ÃŽÂ¾");
		toDecode = toDecode.replaceAll("&Omicron;", "ÃŽÅ¸");
		toDecode = toDecode.replaceAll("&#927;", "ÃŽÅ¸");
		toDecode = toDecode.replaceAll("&omicron;", "ÃŽÂ¿");
		toDecode = toDecode.replaceAll("&#959;", "ÃŽÂ¿");
		toDecode = toDecode.replaceAll("&Pi;", "ÃŽÂ ");
		toDecode = toDecode.replaceAll("&#928;", "ÃŽÂ ");
		toDecode = toDecode.replaceAll("&pi;", "Ã�â‚¬");
		toDecode = toDecode.replaceAll("&#960;", "Ã�â‚¬");
		toDecode = toDecode.replaceAll("&Rho;", "ÃŽÂ¡");
		toDecode = toDecode.replaceAll("&#929;", "ÃŽÂ¡");
		toDecode = toDecode.replaceAll("&rho;", "Ã�ï¿½");
		toDecode = toDecode.replaceAll("&#961;", "Ã�ï¿½");
		toDecode = toDecode.replaceAll("&Sigma;", "ÃŽÂ£");
		toDecode = toDecode.replaceAll("&#931;", "ÃŽÂ£");
		toDecode = toDecode.replaceAll("&sigmaf;", "Ã�â€š");
		toDecode = toDecode.replaceAll("&#962;", "Ã�â€š");
		toDecode = toDecode.replaceAll("&sigma;", "Ã�Æ’");
		toDecode = toDecode.replaceAll("&#963;", "Ã�Æ’");
		toDecode = toDecode.replaceAll("&Tau;", "ÃŽÂ¤");
		toDecode = toDecode.replaceAll("&#932;", "ÃŽÂ¤");
		toDecode = toDecode.replaceAll("&tau;", "Ã�â€ž");
		toDecode = toDecode.replaceAll("&#964;", "Ã�â€ž");
		toDecode = toDecode.replaceAll("&Upsilon;", "ÃŽÂ¥");
		toDecode = toDecode.replaceAll("&#933;", "ÃŽÂ¥");
		toDecode = toDecode.replaceAll("&upsilon;", "Ã�â€¦");
		toDecode = toDecode.replaceAll("&#965;", "Ã�â€¦");
		toDecode = toDecode.replaceAll("&Phi;", "ÃŽÂ¦");
		toDecode = toDecode.replaceAll("&#934;", "ÃŽÂ¦");
		toDecode = toDecode.replaceAll("&phi;", "Ã�â€ ");
		toDecode = toDecode.replaceAll("&#966;", "Ã�â€ ");
		toDecode = toDecode.replaceAll("&Chi;", "ÃŽÂ§");
		toDecode = toDecode.replaceAll("&#935;", "ÃŽÂ§");
		toDecode = toDecode.replaceAll("&chi;", "Ã�â€¡");
		toDecode = toDecode.replaceAll("&#967;", "Ã�â€¡");
		toDecode = toDecode.replaceAll("&Psi;", "ÃŽÂ¨");
		toDecode = toDecode.replaceAll("&#936;", "ÃŽÂ¨");
		toDecode = toDecode.replaceAll("&psi;", "Ã�Ë†");
		toDecode = toDecode.replaceAll("&#968;", "Ã�Ë†");
		toDecode = toDecode.replaceAll("&Omega;", "ÃŽÂ©");
		toDecode = toDecode.replaceAll("&#937;", "ÃŽÂ©");
		toDecode = toDecode.replaceAll("&omega;", "Ã�â€°");
		toDecode = toDecode.replaceAll("&#969;", "Ã�â€°");
		toDecode = toDecode.replaceAll("&thetasym;", "Ã�â€˜");
		toDecode = toDecode.replaceAll("&#977;", "Ã�â€˜");
		toDecode = toDecode.replaceAll("&upsih;", "Ã�â€™");
		toDecode = toDecode.replaceAll("&#978;", "Ã�â€™");
		toDecode = toDecode.replaceAll("&piv;", "Ã�â€“");
		toDecode = toDecode.replaceAll("&#982;", "Ã�â€“");
		toDecode = toDecode.replaceAll("&forall;", "Ã¢Ë†â‚¬");
		toDecode = toDecode.replaceAll("&#8704;", "Ã¢Ë†â‚¬");
		toDecode = toDecode.replaceAll("&part;", "Ã¢Ë†â€š");
		toDecode = toDecode.replaceAll("&#8706;", "Ã¢Ë†â€š");
		toDecode = toDecode.replaceAll("&exist;", "Ã¢Ë†Æ’");
		toDecode = toDecode.replaceAll("&#8707;", "Ã¢Ë†Æ’");
		toDecode = toDecode.replaceAll("&empty;", "Ã¢Ë†â€¦");
		toDecode = toDecode.replaceAll("&#8709;", "Ã¢Ë†â€¦");
		toDecode = toDecode.replaceAll("&nabla;", "Ã¢Ë†â€¡");
		toDecode = toDecode.replaceAll("&#8711;", "Ã¢Ë†â€¡");
		toDecode = toDecode.replaceAll("&isin;", "Ã¢Ë†Ë†");
		toDecode = toDecode.replaceAll("&#8712;", "Ã¢Ë†Ë†");
		toDecode = toDecode.replaceAll("&notin;", "Ã¢Ë†â€°");
		toDecode = toDecode.replaceAll("&#8713;", "Ã¢Ë†â€°");
		toDecode = toDecode.replaceAll("&ni;", "Ã¢Ë†â€¹");
		toDecode = toDecode.replaceAll("&#8715;", "Ã¢Ë†â€¹");
		toDecode = toDecode.replaceAll("&prod;", "Ã¢Ë†ï¿½");
		toDecode = toDecode.replaceAll("&#8719;", "Ã¢Ë†ï¿½");
		toDecode = toDecode.replaceAll("&sum;", "Ã¢Ë†â€˜");
		toDecode = toDecode.replaceAll("&#8721;", "Ã¢Ë†â€˜");
		toDecode = toDecode.replaceAll("&minus;", "Ã¢Ë†â€™");
		toDecode = toDecode.replaceAll("&#8722;", "Ã¢Ë†â€™");
		toDecode = toDecode.replaceAll("&lowast;", "Ã¢Ë†â€”");
		toDecode = toDecode.replaceAll("&#8727;", "Ã¢Ë†â€”");
		toDecode = toDecode.replaceAll("&radic;", "Ã¢Ë†Å¡");
		toDecode = toDecode.replaceAll("&#8730;", "Ã¢Ë†Å¡");
		toDecode = toDecode.replaceAll("&prop;", "Ã¢Ë†ï¿½");
		toDecode = toDecode.replaceAll("&#8733;", "Ã¢Ë†ï¿½");
		toDecode = toDecode.replaceAll("&infin;", "Ã¢Ë†Å¾");
		toDecode = toDecode.replaceAll("&#8734;", "Ã¢Ë†Å¾");
		toDecode = toDecode.replaceAll("&ang;", "Ã¢Ë†Â ");
		toDecode = toDecode.replaceAll("&#8736;", "Ã¢Ë†Â ");
		toDecode = toDecode.replaceAll("&and;", "Ã¢Ë†Â§");
		toDecode = toDecode.replaceAll("&#8743;", "Ã¢Ë†Â§");
		toDecode = toDecode.replaceAll("&or;", "Ã¢Ë†Â¨");
		toDecode = toDecode.replaceAll("&#8744;", "Ã¢Ë†Â¨");
		toDecode = toDecode.replaceAll("&cap;", "Ã¢Ë†Â©");
		toDecode = toDecode.replaceAll("&#8745;", "Ã¢Ë†Â©");
		toDecode = toDecode.replaceAll("&cup;", "Ã¢Ë†Âª");
		toDecode = toDecode.replaceAll("&#8746;", "Ã¢Ë†Âª");
		toDecode = toDecode.replaceAll("&int;", "Ã¢Ë†Â«");
		toDecode = toDecode.replaceAll("&#8747;", "Ã¢Ë†Â«");
		toDecode = toDecode.replaceAll("&there4;", "Ã¢Ë†Â´");
		toDecode = toDecode.replaceAll("&#8756;", "Ã¢Ë†Â´");
		toDecode = toDecode.replaceAll("&sim;", "Ã¢Ë†Â¼");
		toDecode = toDecode.replaceAll("&#8764;", "Ã¢Ë†Â¼");
		toDecode = toDecode.replaceAll("&cong;", "Ã¢â€°â€¦");
		toDecode = toDecode.replaceAll("&#8773;", "Ã¢â€°â€¦");
		toDecode = toDecode.replaceAll("&asymp;", "Ã¢â€°Ë†");
		toDecode = toDecode.replaceAll("&#8776;", "Ã¢â€°Ë†");
		toDecode = toDecode.replaceAll("&ne;", "Ã¢â€°Â ");
		toDecode = toDecode.replaceAll("&#8800;", "Ã¢â€°Â ");
		toDecode = toDecode.replaceAll("&equiv;", "Ã¢â€°Â¡");
		toDecode = toDecode.replaceAll("&#8801;", "Ã¢â€°Â¡");
		toDecode = toDecode.replaceAll("&le;", "Ã¢â€°Â¤");
		toDecode = toDecode.replaceAll("&#8804;", "Ã¢â€°Â¤");
		toDecode = toDecode.replaceAll("&ge;", "Ã¢â€°Â¥");
		toDecode = toDecode.replaceAll("&#8805;", "Ã¢â€°Â¥");
		toDecode = toDecode.replaceAll("&sub;", "Ã¢Å â€š");
		toDecode = toDecode.replaceAll("&#8834;", "Ã¢Å â€š");
		toDecode = toDecode.replaceAll("&sup;", "Ã¢Å Æ’");
		toDecode = toDecode.replaceAll("&#8835;", "Ã¢Å Æ’");
		toDecode = toDecode.replaceAll("&nsub;", "Ã¢Å â€ž");
		toDecode = toDecode.replaceAll("&#8836;", "Ã¢Å â€ž");
		toDecode = toDecode.replaceAll("&sube;", "Ã¢Å â€ ");
		toDecode = toDecode.replaceAll("&#8838;", "Ã¢Å â€ ");
		toDecode = toDecode.replaceAll("&supe;", "Ã¢Å â€¡");
		toDecode = toDecode.replaceAll("&#8839;", "Ã¢Å â€¡");
		toDecode = toDecode.replaceAll("&oplus;", "Ã¢Å â€¢");
		toDecode = toDecode.replaceAll("&#8853;", "Ã¢Å â€¢");
		toDecode = toDecode.replaceAll("&otimes;", "Ã¢Å â€”");
		toDecode = toDecode.replaceAll("&#8855;", "Ã¢Å â€”");
		toDecode = toDecode.replaceAll("&perp;", "Ã¢Å Â¥");
		toDecode = toDecode.replaceAll("&#8869;", "Ã¢Å Â¥");
		toDecode = toDecode.replaceAll("&sdot;", "Ã¢â€¹â€¦");
		toDecode = toDecode.replaceAll("&#8901;", "Ã¢â€¹â€¦");
		toDecode = toDecode.replaceAll("&loz;", "Ã¢â€”Å ");
		toDecode = toDecode.replaceAll("&#9674;", "Ã¢â€”Å ");
		toDecode = toDecode.replaceAll("&lceil;", "Ã¢Å’Ë†");
		toDecode = toDecode.replaceAll("&#8968;", "Ã¢Å’Ë†");
		toDecode = toDecode.replaceAll("&rceil;", "Ã¢Å’â€°");
		toDecode = toDecode.replaceAll("&#8969;", "Ã¢Å’â€°");
		toDecode = toDecode.replaceAll("&lfloor;", "Ã¢Å’Å ");
		toDecode = toDecode.replaceAll("&#8970;", "Ã¢Å’Å ");
		toDecode = toDecode.replaceAll("&rfloor;", "Ã¢Å’â€¹");
		toDecode = toDecode.replaceAll("&#8971;", "Ã¢Å’â€¹");
		toDecode = toDecode.replaceAll("&lang;", "Ã¢Å¸Â¨");
		toDecode = toDecode.replaceAll("&#9001;", "Ã¢Å¸Â¨");
		toDecode = toDecode.replaceAll("&rang;", "Ã¢Å¸Â©");
		toDecode = toDecode.replaceAll("&#9002;", "Ã¢Å¸Â©");
		toDecode = toDecode.replaceAll("&larr;", "Ã¢â€ ï¿½");
		toDecode = toDecode.replaceAll("&#8592;", "Ã¢â€ ï¿½");
		toDecode = toDecode.replaceAll("&uarr;", "Ã¢â€ â€˜");
		toDecode = toDecode.replaceAll("&#8593;", "Ã¢â€ â€˜");
		toDecode = toDecode.replaceAll("&rarr;", "Ã¢â€ â€™");
		toDecode = toDecode.replaceAll("&#8594;", "Ã¢â€ â€™");
		toDecode = toDecode.replaceAll("&darr;", "Ã¢â€ â€œ");
		toDecode = toDecode.replaceAll("&#8595;", "Ã¢â€ â€œ");
		toDecode = toDecode.replaceAll("&harr;", "Ã¢â€ â€�");
		toDecode = toDecode.replaceAll("&#8596;", "Ã¢â€ â€�");
		toDecode = toDecode.replaceAll("&crarr;", "Ã¢â€ Âµ");
		toDecode = toDecode.replaceAll("&#8629;", "Ã¢â€ Âµ");
		toDecode = toDecode.replaceAll("&lArr;", "Ã¢â€¡ï¿½");
		toDecode = toDecode.replaceAll("&#8656;", "Ã¢â€¡ï¿½");
		toDecode = toDecode.replaceAll("&uArr;", "Ã¢â€¡â€˜");
		toDecode = toDecode.replaceAll("&#8657;", "Ã¢â€¡â€˜");
		toDecode = toDecode.replaceAll("&rArr;", "Ã¢â€¡â€™");
		toDecode = toDecode.replaceAll("&#8658;", "Ã¢â€¡â€™");
		toDecode = toDecode.replaceAll("&dArr;", "Ã¢â€¡â€œ");
		toDecode = toDecode.replaceAll("&#8659;", "Ã¢â€¡â€œ");
		toDecode = toDecode.replaceAll("&hArr;", "Ã¢â€¡â€�");
		toDecode = toDecode.replaceAll("&#8660;", "Ã¢â€¡â€�");
		toDecode = toDecode.replaceAll("&lsquo;", "Ã¢â‚¬Ëœ");
		toDecode = toDecode.replaceAll("&rsquo;", "Ã¢â‚¬â„¢");
		toDecode = toDecode.replaceAll("&#8217;", "Ã¢â‚¬â„¢");
		toDecode = toDecode.replaceAll("&#8220;", "Ã¢â‚¬Å“");
		toDecode = toDecode.replaceAll("&#8221;", "Ã¢â‚¬ï¿½");
		toDecode = toDecode.replaceAll("&#8211;", "Ã¢â‚¬â€œ");
		toDecode = toDecode.replaceAll("&#8230;", "Ã¢â‚¬Â¦");
		toDecode = toDecode.replaceAll("&#8212;", "Ã¢â‚¬â€�");
		toDecode = toDecode.replaceAll("&#8216;", "Ã¢â‚¬Ëœ");
		toDecode = toDecode.replaceAll("&#8218;", "Ã¢â‚¬Å¡");
		toDecode = toDecode.replaceAll("&#8222;", "Ã¢â‚¬Å¾");
		toDecode = toDecode.replaceAll("&#8224;", "Ã¢â‚¬Â ");
		toDecode = toDecode.replaceAll("&#8225;", "Ã¢â‚¬Â¡");
		toDecode = toDecode.replaceAll("&#8226;", "Ã¢â‚¬Â¢");
		toDecode = toDecode.replaceAll("&#8240;", "Ã¢â‚¬Â°");
		toDecode = toDecode.replaceAll("&#8364;", "Ã¢â€šÂ¬");
		toDecode = toDecode.replaceAll("&#8482;", "Ã¢â€žÂ¢");
		toDecode = toDecode.replaceAll("&mdash;", "Ã¢â‚¬â€�");
		toDecode = toDecode.replaceAll("&ldquo;", "Ã¢â‚¬Å“");
		toDecode = toDecode.replaceAll("&rdquo;", "Ã¢â‚¬ï¿½");
		toDecode = toDecode.replaceAll("&ndash;", "Ã¢â‚¬â€œ");

		return toDecode;
	}
}