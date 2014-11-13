package parser;

import java.net.Proxy;
import java.util.Set;

/**
 *
 * @author jnphilipp
 * @version 0.0.3
 */
public interface ProxyParser extends Parser {
	/**
	 * all proxies will be returned.
	 * @return the proxies
	 * @throws Exception
	 */
	public abstract Set<Proxy> getProxies() throws Exception;
	
	/**
	 * all proxies will be loaded.
	 * @throws Exception
	 */
	public abstract void fetch() throws Exception;
}