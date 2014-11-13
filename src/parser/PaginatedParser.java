package parser;

import java.util.List;

import javax.xml.transform.Result;

/**
 *
 * @author jnphilipp
 * @version 0.0.2
 */
public interface PaginatedParser extends Parser {
	/**
	 * @return the meta
	 */
	public abstract Meta getMeta();

	/**
	 * @return the results
	 */
	public abstract List<Result> getResults();

	/**
	 * The first page.
	 */
	public abstract void firstPage();

	/**
	 * Checks if there is a next page.
	 * @return <code>True</code> if a next page exists else <code>false</code>.
	 */
	public abstract boolean hasNextPage();

	/**
	 * Iterates to the next page.
	 */
	public abstract void nextPage();

	/**
	 * Resets the results.
	 */
	public abstract void resetResults();
}