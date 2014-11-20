package parser.html;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
		toDecode = toDecode.replaceAll("&iexcl;", "Â¡");
		toDecode = toDecode.replaceAll("&#161;", "Â¡");
		toDecode = toDecode.replaceAll("&cent;", "Â¢");
		toDecode = toDecode.replaceAll("&#162;", "Â¢");
		toDecode = toDecode.replaceAll("&pound;", "Â£");
		toDecode = toDecode.replaceAll("&#163;", "Â£");
		toDecode = toDecode.replaceAll("&curren;", "Â¤");
		toDecode = toDecode.replaceAll("&#164;", "Â¤");
		toDecode = toDecode.replaceAll("&yen;", "Â¥");
		toDecode = toDecode.replaceAll("&#165;", "Â¥");
		toDecode = toDecode.replaceAll("&brvbar;", "Â¦");
		toDecode = toDecode.replaceAll("&#166;", "Â¦");
		toDecode = toDecode.replaceAll("&sect;", "Â§");
		toDecode = toDecode.replaceAll("&#167;", "Â§");
		toDecode = toDecode.replaceAll("&uml;", "Â¨");
		toDecode = toDecode.replaceAll("&#168;", "Â¨");
		toDecode = toDecode.replaceAll("&copy;", "Â©");
		toDecode = toDecode.replaceAll("&#169;", "Â©");
		toDecode = toDecode.replaceAll("&ordf;", "Âª");
		toDecode = toDecode.replaceAll("&#170;", "Âª");
		toDecode = toDecode.replaceAll("&laquo;", "Â«");
		toDecode = toDecode.replaceAll("&#171;", "Â«");
		toDecode = toDecode.replaceAll("&not;", "Â¬");
		toDecode = toDecode.replaceAll("&#172;", "Â¬");
		toDecode = toDecode.replaceAll("&shy;", "Â­");
		toDecode = toDecode.replaceAll("&#173;", "Â­");
		toDecode = toDecode.replaceAll("&reg;", "Â®");
		toDecode = toDecode.replaceAll("&#174;", "Â®");
		toDecode = toDecode.replaceAll("&macr;", "Â¯");
		toDecode = toDecode.replaceAll("&#175;", "Â¯");
		toDecode = toDecode.replaceAll("&deg;", "Â°");
		toDecode = toDecode.replaceAll("&#176;", "Â°");
		toDecode = toDecode.replaceAll("&plusmn;", "Â±");
		toDecode = toDecode.replaceAll("&#177;", "Â±");
		toDecode = toDecode.replaceAll("&sup2;", "Â²");
		toDecode = toDecode.replaceAll("&#178;", "Â²");
		toDecode = toDecode.replaceAll("&sup3;", "Â³");
		toDecode = toDecode.replaceAll("&#179;", "Â³");
		toDecode = toDecode.replaceAll("&acute;", "Â´");
		toDecode = toDecode.replaceAll("&#180;", "Â´");
		toDecode = toDecode.replaceAll("&micro;", "Âµ");
		toDecode = toDecode.replaceAll("&#181;", "Âµ");
		toDecode = toDecode.replaceAll("&para;", "Â¶");
		toDecode = toDecode.replaceAll("&#182;", "Â¶");
		toDecode = toDecode.replaceAll("&middot;", "Â·");
		toDecode = toDecode.replaceAll("&#183;", "Â·");
		toDecode = toDecode.replaceAll("&cedil;", "Â¸");
		toDecode = toDecode.replaceAll("&#184;", "Â¸");
		toDecode = toDecode.replaceAll("&sup1;", "Â¹");
		toDecode = toDecode.replaceAll("&#185;", "Â¹");
		toDecode = toDecode.replaceAll("&ordm;", "Âº");
		toDecode = toDecode.replaceAll("&#186;", "Âº");
		toDecode = toDecode.replaceAll("&raquo;", "Â»");
		toDecode = toDecode.replaceAll("&#187;", "Â»");
		toDecode = toDecode.replaceAll("&frac14;", "Â¼");
		toDecode = toDecode.replaceAll("&#188;", "Â¼");
		toDecode = toDecode.replaceAll("&frac12;", "Â½");
		toDecode = toDecode.replaceAll("&#189;", "Â½");
		toDecode = toDecode.replaceAll("&frac34;", "Â¾");
		toDecode = toDecode.replaceAll("&#190;", "Â¾");
		toDecode = toDecode.replaceAll("&iquest;", "Â¿");
		toDecode = toDecode.replaceAll("&#191;", "Â¿");
		toDecode = toDecode.replaceAll("&Agrave;", "Ã€");
		toDecode = toDecode.replaceAll("&#192;", "Ã€");
		toDecode = toDecode.replaceAll("&Aacute;", "Ã�");
		toDecode = toDecode.replaceAll("&#193;", "Ã�");
		toDecode = toDecode.replaceAll("&Acirc;", "Ã‚");
		toDecode = toDecode.replaceAll("&#194;", "Ã‚");
		toDecode = toDecode.replaceAll("&Atilde;", "Ãƒ");
		toDecode = toDecode.replaceAll("&#195;", "Ãƒ");
		toDecode = toDecode.replaceAll("&Auml;", "Ã„");
		toDecode = toDecode.replaceAll("&#196;", "Ã„");
		toDecode = toDecode.replaceAll("&Aring;", "Ã…");
		toDecode = toDecode.replaceAll("&#197;", "Ã…");
		toDecode = toDecode.replaceAll("&AElig;", "Ã†");
		toDecode = toDecode.replaceAll("&#198;", "Ã†");
		toDecode = toDecode.replaceAll("&Ccedil;", "Ã‡");
		toDecode = toDecode.replaceAll("&#199;", "Ã‡");
		toDecode = toDecode.replaceAll("&Egrave;", "Ãˆ");
		toDecode = toDecode.replaceAll("&#200;", "Ãˆ");
		toDecode = toDecode.replaceAll("&Eacute;", "Ã‰");
		toDecode = toDecode.replaceAll("&#201;", "Ã‰");
		toDecode = toDecode.replaceAll("&Ecirc;", "ÃŠ");
		toDecode = toDecode.replaceAll("&#202;", "ÃŠ");
		toDecode = toDecode.replaceAll("&Euml;", "Ã‹");
		toDecode = toDecode.replaceAll("&#203;", "Ã‹");
		toDecode = toDecode.replaceAll("&Igrave;", "ÃŒ");
		toDecode = toDecode.replaceAll("&#204;", "ÃŒ");
		toDecode = toDecode.replaceAll("&Iacute;", "Ã�");
		toDecode = toDecode.replaceAll("&#205;", "Ã�");
		toDecode = toDecode.replaceAll("&Icirc;", "ÃŽ");
		toDecode = toDecode.replaceAll("&#206;", "ÃŽ");
		toDecode = toDecode.replaceAll("&Iuml;", "Ã�");
		toDecode = toDecode.replaceAll("&#207;", "Ã�");
		toDecode = toDecode.replaceAll("&ETH;", "Ã�");
		toDecode = toDecode.replaceAll("&#208;", "Ã�");
		toDecode = toDecode.replaceAll("&Ntilde;", "Ã‘");
		toDecode = toDecode.replaceAll("&#209;", "Ã‘");
		toDecode = toDecode.replaceAll("&Ograve;", "Ã’");
		toDecode = toDecode.replaceAll("&#210;", "Ã’");
		toDecode = toDecode.replaceAll("&Oacute;", "Ã“");
		toDecode = toDecode.replaceAll("&#211;", "Ã“");
		toDecode = toDecode.replaceAll("&Ocirc;", "Ã”");
		toDecode = toDecode.replaceAll("&#212;", "Ã”");
		toDecode = toDecode.replaceAll("&Otilde;", "Ã•");
		toDecode = toDecode.replaceAll("&#213;", "Ã•");
		toDecode = toDecode.replaceAll("&Ouml;", "Ã–");
		toDecode = toDecode.replaceAll("&#214;", "Ã–");
		toDecode = toDecode.replaceAll("&times;", "Ã—");
		toDecode = toDecode.replaceAll("&#215;", "Ã—");
		toDecode = toDecode.replaceAll("&Oslash;", "Ã˜");
		toDecode = toDecode.replaceAll("&#216;", "Ã˜");
		toDecode = toDecode.replaceAll("&Ugrave;", "Ã™");
		toDecode = toDecode.replaceAll("&#217;", "Ã™");
		toDecode = toDecode.replaceAll("&Uacute;", "Ãš");
		toDecode = toDecode.replaceAll("&#218;", "Ãš");
		toDecode = toDecode.replaceAll("&Ucirc;", "Ã›");
		toDecode = toDecode.replaceAll("&#219;", "Ã›");
		toDecode = toDecode.replaceAll("&Uuml;", "Ãœ");
		toDecode = toDecode.replaceAll("&#220;", "Ãœ");
		toDecode = toDecode.replaceAll("&Yacute;", "Ã�");
		toDecode = toDecode.replaceAll("&#221;", "Ã�");
		toDecode = toDecode.replaceAll("&THORN;", "Ãž");
		toDecode = toDecode.replaceAll("&#222;", "Ãž");
		toDecode = toDecode.replaceAll("&szlig;", "ÃŸ");
		toDecode = toDecode.replaceAll("&#223;", "ÃŸ");
		toDecode = toDecode.replaceAll("&agrave;", "Ã ");
		toDecode = toDecode.replaceAll("&#224;", "Ã ");
		toDecode = toDecode.replaceAll("&aacute;", "Ã¡");
		toDecode = toDecode.replaceAll("&#225;", "Ã¡");
		toDecode = toDecode.replaceAll("&acirc;", "Ã¢");
		toDecode = toDecode.replaceAll("&#226;", "Ã¢");
		toDecode = toDecode.replaceAll("&atilde;", "Ã£");
		toDecode = toDecode.replaceAll("&#227;", "Ã£");
		toDecode = toDecode.replaceAll("&auml;", "Ã¤");
		toDecode = toDecode.replaceAll("&#228;", "Ã¤");
		toDecode = toDecode.replaceAll("&aring;", "Ã¥");
		toDecode = toDecode.replaceAll("&#229;", "Ã¥");
		toDecode = toDecode.replaceAll("&aelig;", "Ã¦");
		toDecode = toDecode.replaceAll("&#230;", "Ã¦");
		toDecode = toDecode.replaceAll("&ccedil;", "Ã§");
		toDecode = toDecode.replaceAll("&#231;", "Ã§");
		toDecode = toDecode.replaceAll("&egrave;", "Ã¨");
		toDecode = toDecode.replaceAll("&#232;", "Ã¨");
		toDecode = toDecode.replaceAll("&eacute;", "Ã©");
		toDecode = toDecode.replaceAll("&#233;", "Ã©");
		toDecode = toDecode.replaceAll("&ecirc;", "Ãª");
		toDecode = toDecode.replaceAll("&#234;", "Ãª");
		toDecode = toDecode.replaceAll("&euml;", "Ã«");
		toDecode = toDecode.replaceAll("&#235;", "Ã«");
		toDecode = toDecode.replaceAll("&igrave;", "Ã¬");
		toDecode = toDecode.replaceAll("&#236;", "Ã¬");
		toDecode = toDecode.replaceAll("&iacute;", "Ã­");
		toDecode = toDecode.replaceAll("&#237;", "Ã­");
		toDecode = toDecode.replaceAll("&icirc;", "Ã®");
		toDecode = toDecode.replaceAll("&#238;", "Ã®");
		toDecode = toDecode.replaceAll("&iuml;", "Ã¯");
		toDecode = toDecode.replaceAll("&#239;", "Ã¯");
		toDecode = toDecode.replaceAll("&eth;", "Ã°");
		toDecode = toDecode.replaceAll("&#240;", "Ã°");
		toDecode = toDecode.replaceAll("&ntilde;", "Ã±");
		toDecode = toDecode.replaceAll("&#241;", "Ã±");
		toDecode = toDecode.replaceAll("&ograve;", "Ã²");
		toDecode = toDecode.replaceAll("&#242;", "Ã²");
		toDecode = toDecode.replaceAll("&oacute;", "Ã³");
		toDecode = toDecode.replaceAll("&#243;", "Ã³");
		toDecode = toDecode.replaceAll("&ocirc;", "Ã´");
		toDecode = toDecode.replaceAll("&#244;", "Ã´");
		toDecode = toDecode.replaceAll("&otilde;", "Ãµ");
		toDecode = toDecode.replaceAll("&#245;", "Ãµ");
		toDecode = toDecode.replaceAll("&ouml;", "Ã¶");
		toDecode = toDecode.replaceAll("&#246;", "Ã¶");
		toDecode = toDecode.replaceAll("&divide;", "Ã·");
		toDecode = toDecode.replaceAll("&#247;", "Ã·");
		toDecode = toDecode.replaceAll("&oslash;", "Ã¸");
		toDecode = toDecode.replaceAll("&#248;", "Ã¸");
		toDecode = toDecode.replaceAll("&ugrave;", "Ã¹");
		toDecode = toDecode.replaceAll("&#249;", "Ã¹");
		toDecode = toDecode.replaceAll("&uacute;", "Ãº");
		toDecode = toDecode.replaceAll("&#250;", "Ãº");
		toDecode = toDecode.replaceAll("&ucirc;", "Ã»");
		toDecode = toDecode.replaceAll("&#251;", "Ã»");
		toDecode = toDecode.replaceAll("&uuml;", "Ã¼");
		toDecode = toDecode.replaceAll("&#252;", "Ã¼");
		toDecode = toDecode.replaceAll("&#xfc;", "Ã¼");
		toDecode = toDecode.replaceAll("&yacute;", "Ã½");
		toDecode = toDecode.replaceAll("&#253;", "Ã½");
		toDecode = toDecode.replaceAll("&thorn;", "Ã¾");
		toDecode = toDecode.replaceAll("&#254;", "Ã¾");
		toDecode = toDecode.replaceAll("&yuml;", "Ã¿");
		toDecode = toDecode.replaceAll("&#255;", "Ã¿");
		toDecode = toDecode.replaceAll("&Alpha;", "Î‘");
		toDecode = toDecode.replaceAll("&#913;", "Î‘");
		toDecode = toDecode.replaceAll("&alpha;", "Î±");
		toDecode = toDecode.replaceAll("&#945;", "Î±");
		toDecode = toDecode.replaceAll("&Beta;", "Î’");
		toDecode = toDecode.replaceAll("&#914;", "Î’");
		toDecode = toDecode.replaceAll("&beta;", "Î²");
		toDecode = toDecode.replaceAll("&#946;", "Î²");
		toDecode = toDecode.replaceAll("&Gamma;", "Î“");
		toDecode = toDecode.replaceAll("&#915;", "Î“");
		toDecode = toDecode.replaceAll("&gamma;", "Î³");
		toDecode = toDecode.replaceAll("&#947;", "Î³");
		toDecode = toDecode.replaceAll("&Delta;", "Î”");
		toDecode = toDecode.replaceAll("&#916;", "Î”");
		toDecode = toDecode.replaceAll("&delta;", "Î´");
		toDecode = toDecode.replaceAll("&#948;", "Î´");
		toDecode = toDecode.replaceAll("&Epsilon;", "Î•");
		toDecode = toDecode.replaceAll("&#917;", "Î•");
		toDecode = toDecode.replaceAll("&epsilon;", "Îµ");
		toDecode = toDecode.replaceAll("&#949;", "Îµ");
		toDecode = toDecode.replaceAll("&Zeta;", "Î–");
		toDecode = toDecode.replaceAll("&#918;", "Î–");
		toDecode = toDecode.replaceAll("&zeta;", "Î¶");
		toDecode = toDecode.replaceAll("&#950;", "Î¶");
		toDecode = toDecode.replaceAll("&Eta;", "Î—");
		toDecode = toDecode.replaceAll("&#919;", "Î—");
		toDecode = toDecode.replaceAll("&eta;", "Î·");
		toDecode = toDecode.replaceAll("&#951;", "Î·");
		toDecode = toDecode.replaceAll("&Theta;", "Î˜");
		toDecode = toDecode.replaceAll("&#920;", "Î˜");
		toDecode = toDecode.replaceAll("&theta;", "Î¸");
		toDecode = toDecode.replaceAll("&#952;", "Î¸");
		toDecode = toDecode.replaceAll("&Iota;", "Î™");
		toDecode = toDecode.replaceAll("&#921;", "Î™");
		toDecode = toDecode.replaceAll("&iota;", "Î¹");
		toDecode = toDecode.replaceAll("&#953;", "Î¹");
		toDecode = toDecode.replaceAll("&Kappa;", "Îš");
		toDecode = toDecode.replaceAll("&#922;", "Îš");
		toDecode = toDecode.replaceAll("&kappa;", "Îº");
		toDecode = toDecode.replaceAll("&#954;", "Îº");
		toDecode = toDecode.replaceAll("&Lambda;", "Î›");
		toDecode = toDecode.replaceAll("&#923;", "Î›");
		toDecode = toDecode.replaceAll("&lambda;", "Î»");
		toDecode = toDecode.replaceAll("&#955;", "Î»");
		toDecode = toDecode.replaceAll("&Mu;", "Îœ");
		toDecode = toDecode.replaceAll("&#924;", "Îœ");
		toDecode = toDecode.replaceAll("&mu;", "Î¼");
		toDecode = toDecode.replaceAll("&#956;", "Î¼");
		toDecode = toDecode.replaceAll("&Nu;", "Î�");
		toDecode = toDecode.replaceAll("&#925;", "Î�");
		toDecode = toDecode.replaceAll("&nu;", "Î½");
		toDecode = toDecode.replaceAll("&#957;", "Î½");
		toDecode = toDecode.replaceAll("&Xi;", "Îž");
		toDecode = toDecode.replaceAll("&#926;", "Îž");
		toDecode = toDecode.replaceAll("&xi;", "Î¾");
		toDecode = toDecode.replaceAll("&#958;", "Î¾");
		toDecode = toDecode.replaceAll("&Omicron;", "ÎŸ");
		toDecode = toDecode.replaceAll("&#927;", "ÎŸ");
		toDecode = toDecode.replaceAll("&omicron;", "Î¿");
		toDecode = toDecode.replaceAll("&#959;", "Î¿");
		toDecode = toDecode.replaceAll("&Pi;", "Î ");
		toDecode = toDecode.replaceAll("&#928;", "Î ");
		toDecode = toDecode.replaceAll("&pi;", "Ï€");
		toDecode = toDecode.replaceAll("&#960;", "Ï€");
		toDecode = toDecode.replaceAll("&Rho;", "Î¡");
		toDecode = toDecode.replaceAll("&#929;", "Î¡");
		toDecode = toDecode.replaceAll("&rho;", "Ï�");
		toDecode = toDecode.replaceAll("&#961;", "Ï�");
		toDecode = toDecode.replaceAll("&Sigma;", "Î£");
		toDecode = toDecode.replaceAll("&#931;", "Î£");
		toDecode = toDecode.replaceAll("&sigmaf;", "Ï‚");
		toDecode = toDecode.replaceAll("&#962;", "Ï‚");
		toDecode = toDecode.replaceAll("&sigma;", "Ïƒ");
		toDecode = toDecode.replaceAll("&#963;", "Ïƒ");
		toDecode = toDecode.replaceAll("&Tau;", "Î¤");
		toDecode = toDecode.replaceAll("&#932;", "Î¤");
		toDecode = toDecode.replaceAll("&tau;", "Ï„");
		toDecode = toDecode.replaceAll("&#964;", "Ï„");
		toDecode = toDecode.replaceAll("&Upsilon;", "Î¥");
		toDecode = toDecode.replaceAll("&#933;", "Î¥");
		toDecode = toDecode.replaceAll("&upsilon;", "Ï…");
		toDecode = toDecode.replaceAll("&#965;", "Ï…");
		toDecode = toDecode.replaceAll("&Phi;", "Î¦");
		toDecode = toDecode.replaceAll("&#934;", "Î¦");
		toDecode = toDecode.replaceAll("&phi;", "Ï†");
		toDecode = toDecode.replaceAll("&#966;", "Ï†");
		toDecode = toDecode.replaceAll("&Chi;", "Î§");
		toDecode = toDecode.replaceAll("&#935;", "Î§");
		toDecode = toDecode.replaceAll("&chi;", "Ï‡");
		toDecode = toDecode.replaceAll("&#967;", "Ï‡");
		toDecode = toDecode.replaceAll("&Psi;", "Î¨");
		toDecode = toDecode.replaceAll("&#936;", "Î¨");
		toDecode = toDecode.replaceAll("&psi;", "Ïˆ");
		toDecode = toDecode.replaceAll("&#968;", "Ïˆ");
		toDecode = toDecode.replaceAll("&Omega;", "Î©");
		toDecode = toDecode.replaceAll("&#937;", "Î©");
		toDecode = toDecode.replaceAll("&omega;", "Ï‰");
		toDecode = toDecode.replaceAll("&#969;", "Ï‰");
		toDecode = toDecode.replaceAll("&thetasym;", "Ï‘");
		toDecode = toDecode.replaceAll("&#977;", "Ï‘");
		toDecode = toDecode.replaceAll("&upsih;", "Ï’");
		toDecode = toDecode.replaceAll("&#978;", "Ï’");
		toDecode = toDecode.replaceAll("&piv;", "Ï–");
		toDecode = toDecode.replaceAll("&#982;", "Ï–");
		toDecode = toDecode.replaceAll("&forall;", "âˆ€");
		toDecode = toDecode.replaceAll("&#8704;", "âˆ€");
		toDecode = toDecode.replaceAll("&part;", "âˆ‚");
		toDecode = toDecode.replaceAll("&#8706;", "âˆ‚");
		toDecode = toDecode.replaceAll("&exist;", "âˆƒ");
		toDecode = toDecode.replaceAll("&#8707;", "âˆƒ");
		toDecode = toDecode.replaceAll("&empty;", "âˆ…");
		toDecode = toDecode.replaceAll("&#8709;", "âˆ…");
		toDecode = toDecode.replaceAll("&nabla;", "âˆ‡");
		toDecode = toDecode.replaceAll("&#8711;", "âˆ‡");
		toDecode = toDecode.replaceAll("&isin;", "âˆˆ");
		toDecode = toDecode.replaceAll("&#8712;", "âˆˆ");
		toDecode = toDecode.replaceAll("&notin;", "âˆ‰");
		toDecode = toDecode.replaceAll("&#8713;", "âˆ‰");
		toDecode = toDecode.replaceAll("&ni;", "âˆ‹");
		toDecode = toDecode.replaceAll("&#8715;", "âˆ‹");
		toDecode = toDecode.replaceAll("&prod;", "âˆ�");
		toDecode = toDecode.replaceAll("&#8719;", "âˆ�");
		toDecode = toDecode.replaceAll("&sum;", "âˆ‘");
		toDecode = toDecode.replaceAll("&#8721;", "âˆ‘");
		toDecode = toDecode.replaceAll("&minus;", "âˆ’");
		toDecode = toDecode.replaceAll("&#8722;", "âˆ’");
		toDecode = toDecode.replaceAll("&lowast;", "âˆ—");
		toDecode = toDecode.replaceAll("&#8727;", "âˆ—");
		toDecode = toDecode.replaceAll("&radic;", "âˆš");
		toDecode = toDecode.replaceAll("&#8730;", "âˆš");
		toDecode = toDecode.replaceAll("&prop;", "âˆ�");
		toDecode = toDecode.replaceAll("&#8733;", "âˆ�");
		toDecode = toDecode.replaceAll("&infin;", "âˆž");
		toDecode = toDecode.replaceAll("&#8734;", "âˆž");
		toDecode = toDecode.replaceAll("&ang;", "âˆ ");
		toDecode = toDecode.replaceAll("&#8736;", "âˆ ");
		toDecode = toDecode.replaceAll("&and;", "âˆ§");
		toDecode = toDecode.replaceAll("&#8743;", "âˆ§");
		toDecode = toDecode.replaceAll("&or;", "âˆ¨");
		toDecode = toDecode.replaceAll("&#8744;", "âˆ¨");
		toDecode = toDecode.replaceAll("&cap;", "âˆ©");
		toDecode = toDecode.replaceAll("&#8745;", "âˆ©");
		toDecode = toDecode.replaceAll("&cup;", "âˆª");
		toDecode = toDecode.replaceAll("&#8746;", "âˆª");
		toDecode = toDecode.replaceAll("&int;", "âˆ«");
		toDecode = toDecode.replaceAll("&#8747;", "âˆ«");
		toDecode = toDecode.replaceAll("&there4;", "âˆ´");
		toDecode = toDecode.replaceAll("&#8756;", "âˆ´");
		toDecode = toDecode.replaceAll("&sim;", "âˆ¼");
		toDecode = toDecode.replaceAll("&#8764;", "âˆ¼");
		toDecode = toDecode.replaceAll("&cong;", "â‰…");
		toDecode = toDecode.replaceAll("&#8773;", "â‰…");
		toDecode = toDecode.replaceAll("&asymp;", "â‰ˆ");
		toDecode = toDecode.replaceAll("&#8776;", "â‰ˆ");
		toDecode = toDecode.replaceAll("&ne;", "â‰ ");
		toDecode = toDecode.replaceAll("&#8800;", "â‰ ");
		toDecode = toDecode.replaceAll("&equiv;", "â‰¡");
		toDecode = toDecode.replaceAll("&#8801;", "â‰¡");
		toDecode = toDecode.replaceAll("&le;", "â‰¤");
		toDecode = toDecode.replaceAll("&#8804;", "â‰¤");
		toDecode = toDecode.replaceAll("&ge;", "â‰¥");
		toDecode = toDecode.replaceAll("&#8805;", "â‰¥");
		toDecode = toDecode.replaceAll("&sub;", "âŠ‚");
		toDecode = toDecode.replaceAll("&#8834;", "âŠ‚");
		toDecode = toDecode.replaceAll("&sup;", "âŠƒ");
		toDecode = toDecode.replaceAll("&#8835;", "âŠƒ");
		toDecode = toDecode.replaceAll("&nsub;", "âŠ„");
		toDecode = toDecode.replaceAll("&#8836;", "âŠ„");
		toDecode = toDecode.replaceAll("&sube;", "âŠ†");
		toDecode = toDecode.replaceAll("&#8838;", "âŠ†");
		toDecode = toDecode.replaceAll("&supe;", "âŠ‡");
		toDecode = toDecode.replaceAll("&#8839;", "âŠ‡");
		toDecode = toDecode.replaceAll("&oplus;", "âŠ•");
		toDecode = toDecode.replaceAll("&#8853;", "âŠ•");
		toDecode = toDecode.replaceAll("&otimes;", "âŠ—");
		toDecode = toDecode.replaceAll("&#8855;", "âŠ—");
		toDecode = toDecode.replaceAll("&perp;", "âŠ¥");
		toDecode = toDecode.replaceAll("&#8869;", "âŠ¥");
		toDecode = toDecode.replaceAll("&sdot;", "â‹…");
		toDecode = toDecode.replaceAll("&#8901;", "â‹…");
		toDecode = toDecode.replaceAll("&loz;", "â—Š");
		toDecode = toDecode.replaceAll("&#9674;", "â—Š");
		toDecode = toDecode.replaceAll("&lceil;", "âŒˆ");
		toDecode = toDecode.replaceAll("&#8968;", "âŒˆ");
		toDecode = toDecode.replaceAll("&rceil;", "âŒ‰");
		toDecode = toDecode.replaceAll("&#8969;", "âŒ‰");
		toDecode = toDecode.replaceAll("&lfloor;", "âŒŠ");
		toDecode = toDecode.replaceAll("&#8970;", "âŒŠ");
		toDecode = toDecode.replaceAll("&rfloor;", "âŒ‹");
		toDecode = toDecode.replaceAll("&#8971;", "âŒ‹");
		toDecode = toDecode.replaceAll("&lang;", "âŸ¨");
		toDecode = toDecode.replaceAll("&#9001;", "âŸ¨");
		toDecode = toDecode.replaceAll("&rang;", "âŸ©");
		toDecode = toDecode.replaceAll("&#9002;", "âŸ©");
		toDecode = toDecode.replaceAll("&larr;", "â†�");
		toDecode = toDecode.replaceAll("&#8592;", "â†�");
		toDecode = toDecode.replaceAll("&uarr;", "â†‘");
		toDecode = toDecode.replaceAll("&#8593;", "â†‘");
		toDecode = toDecode.replaceAll("&rarr;", "â†’");
		toDecode = toDecode.replaceAll("&#8594;", "â†’");
		toDecode = toDecode.replaceAll("&darr;", "â†“");
		toDecode = toDecode.replaceAll("&#8595;", "â†“");
		toDecode = toDecode.replaceAll("&harr;", "â†”");
		toDecode = toDecode.replaceAll("&#8596;", "â†”");
		toDecode = toDecode.replaceAll("&crarr;", "â†µ");
		toDecode = toDecode.replaceAll("&#8629;", "â†µ");
		toDecode = toDecode.replaceAll("&lArr;", "â‡�");
		toDecode = toDecode.replaceAll("&#8656;", "â‡�");
		toDecode = toDecode.replaceAll("&uArr;", "â‡‘");
		toDecode = toDecode.replaceAll("&#8657;", "â‡‘");
		toDecode = toDecode.replaceAll("&rArr;", "â‡’");
		toDecode = toDecode.replaceAll("&#8658;", "â‡’");
		toDecode = toDecode.replaceAll("&dArr;", "â‡“");
		toDecode = toDecode.replaceAll("&#8659;", "â‡“");
		toDecode = toDecode.replaceAll("&hArr;", "â‡”");
		toDecode = toDecode.replaceAll("&#8660;", "â‡”");
		toDecode = toDecode.replaceAll("&lsquo;", "â€˜");
		toDecode = toDecode.replaceAll("&rsquo;", "â€™");
		toDecode = toDecode.replaceAll("&#8217;", "â€™");
		toDecode = toDecode.replaceAll("&#8220;", "â€œ");
		toDecode = toDecode.replaceAll("&#8221;", "â€�");
		toDecode = toDecode.replaceAll("&#8211;", "â€“");
		toDecode = toDecode.replaceAll("&#8230;", "â€¦");
		toDecode = toDecode.replaceAll("&#8212;", "â€”");
		toDecode = toDecode.replaceAll("&#8216;", "â€˜");
		toDecode = toDecode.replaceAll("&#8218;", "â€š");
		toDecode = toDecode.replaceAll("&#8222;", "â€ž");
		toDecode = toDecode.replaceAll("&#8224;", "â€ ");
		toDecode = toDecode.replaceAll("&#8225;", "â€¡");
		toDecode = toDecode.replaceAll("&#8226;", "â€¢");
		toDecode = toDecode.replaceAll("&#8240;", "â€°");
		toDecode = toDecode.replaceAll("&#8364;", "â‚¬");
		toDecode = toDecode.replaceAll("&#8482;", "â„¢");
		toDecode = toDecode.replaceAll("&mdash;", "â€”");
		toDecode = toDecode.replaceAll("&ldquo;", "â€œ");
		toDecode = toDecode.replaceAll("&rdquo;", "â€�");
		toDecode = toDecode.replaceAll("&ndash;", "â€“");

		return toDecode;
	}
}