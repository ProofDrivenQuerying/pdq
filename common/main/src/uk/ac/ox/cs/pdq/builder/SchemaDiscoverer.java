package uk.ac.ox.cs.pdq.builder;

import java.util.Properties;

import uk.ac.ox.cs.pdq.db.Schema;

/**
 * Common interface to all schema discovered.
 * 
 * @author Julien Leblay
 */
public interface SchemaDiscoverer {
	/**
	 * @return the discoverer's properties.
	 */
	Properties getProperties();

	/**
	 * @param p Properties
	 */
	void setProperties(Properties p);

	/**
	 * @return a Schema generated as per the discovery process.
	 * @throws BuilderException
	 */
	Schema discover() throws BuilderException;
}
