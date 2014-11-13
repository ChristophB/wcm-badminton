package parser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.net.ssl.SSLException;

/**
 * this class provides methods for proxies.
 *
 * @author jnphilipp, marcel
 * @version 0.1.4
 */
public class ProxyManager {
	
	/**
	 * proxymanager is a singleton.
	 */
	private static ProxyManager instance;
	
	/**
	 * the proxyparser will be stored here (for example the hidemyass proxyparser)
	 */
	private ProxyParser proxyParser;
	
	/**
	 * all proxies will be stored here.
	 */
	private Set<Proxy> proxies;
	
	/**
	 * the number of the proxy in the list.
	 */
	private int useProxyNumber;
	
	/**
	 * the currently used proxy.
	 */
	private Proxy currentProxy;

	/**
	 * default constructor.
	 */
	private ProxyManager() {
		this.proxies = new LinkedHashSet<>();
	}

	/**
	 * singleton method. returns the instance for the proxymanager class.
	 * @return ProxyManager
	 */
	public static synchronized ProxyManager getInstance() {
		if ( instance == null)
			instance = new ProxyManager();
		return instance;
	}

	/**
	 * @return the proxyParser
	 */
	public ProxyParser getProxyParser() {
		return this.proxyParser;
	}

	/**
	 * @param proxyParser the proxyParser to set
	 */
	public void setProxyParser(ProxyParser proxyParser) {
		this.proxyParser = proxyParser;
	}

	/**
	 * proxies will be loaded with the specified proxyparser.
	 * @throws Exception
	 */
	public void loadProxies() throws Exception {
		if ( this.proxyParser == null )
			this.proxies.add(Proxy.NO_PROXY);
		else 
			this.proxies = this.proxyParser.getProxies();
	}

	public void fetch(String url, Parser parser) throws Exception {
		if ( this.proxies.isEmpty() )
			this.loadProxies();

		if ( parser instanceof PaginatedParser ) {
			while ( ((PaginatedParser)parser).hasNextPage() ) {
				this.paginatedFetch(url, parser);

				if ( this.proxies.isEmpty() )
					this.loadProxies();
			}
		}
		else
			this.singleFetch(url, parser);
		useProxyNumber = 1;
	}

	protected void singleFetch(String url, Parser parser) throws Exception {
		Proxy proxy = this.proxies.iterator().next();

		try {
			parser.fetch(url, proxy);
			if ( parser.getResponseCode() != HttpURLConnection.HTTP_OK ) {

				if ( proxy == Proxy.NO_PROXY )
					throw new IOException("Response code ist: " + parser.getResponseCode() + " without any proxy.");

				this.proxies.remove(proxy);
				this.singleFetch(url, parser);
			}

			this.proxies.add(proxy);
		}
		catch ( SSLException e ) {
			e.printStackTrace();
			this.proxies.remove(proxy);
			this.singleFetch(url, parser);
		}
		catch ( IOException e ) {
			e.printStackTrace();
			this.proxies.remove(proxy);
			this.singleFetch(url, parser);
		}
	}

	protected void paginatedFetch(String url, Parser parser) throws Exception {
		this.singleFetch(url, parser);
		((PaginatedParser)parser).nextPage();
	}
	
	/**
	 * switches to the next proxy in the list, which was not tested yet.
	 * 
	 * @return Proxy
	 * @throws Exception
	 */
	public Proxy switchProxy() throws Exception {
		useProxyNumber++;
		Proxy proxy = null;
		if(proxies.isEmpty()) {
			proxies = proxyParser.getProxies();
		}
		Iterator<Proxy> iterator = proxies.iterator();
		if (iterator.hasNext()) {
			proxy = iterator.next();
		}		
		for (int i = 1; i < useProxyNumber; i++) {
			if (iterator.hasNext()) {
				proxy = iterator.next();
			} else {
				try {
					proxies = proxyParser.getProxies();
				} catch (Exception e) {
					e.printStackTrace();
				}
				useProxyNumber = 1;
				switchProxy();
			}
		}
		currentProxy = proxy;
		return proxy;
	}
	
	/**
	 * returns the currently used proxy.
	 * @return Proxy
	 */
	public Proxy getCurrentProxy() {
		return currentProxy;
	}
	
	/**
	 * returns the list of proxies.
	 * @return Set<Proxy>
	 */
	public Set<Proxy> getProxies() {
		return proxies;
	}
}