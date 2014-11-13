package parser;

import java.net.Proxy;

/**
 *
 * @author jnphilipp
 * @version 0.0.2
 */
public interface Parser {
	
	/**
	 * Returns the response code of the request.
	 * @return response code
	 */
	public abstract int getResponseCode();

	/**
	 * returns whether the response code is ok or not.
	 * @return true / false
	 */
	public abstract boolean isResponseCodeOK();

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @throws Exception 
	 */
	public abstract String fetch(String url) throws Exception;

	/**
	 * Builds a connection to the given URL using the given proxy and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param proxy proxy
	 * @throws Exception 
	 */
	public abstract String fetch(String url, Proxy proxy) throws Exception;
}