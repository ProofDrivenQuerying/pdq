package uk.ac.ox.cs.pdq.builder;

import java.util.Properties;

import uk.ac.ox.cs.pdq.db.Schema;

// TODO: Auto-generated Javadoc
/**
 * Common interface to all schema discovered.
 * 
 * @author Julien Leblay
 */
public interface SchemaDiscoverer {
	
	/**
	 * Gets the properties.
	 *
	 * @return the discoverer's properties.
	 */
	Properties getProperties();

	/**
	 * Sets the properties.
	 *
	 * @param p Properties
	 */
	void setProperties(Properties p);

	/**
	 * Discover.
	 *
	 * @return a Schema generated as per the discovery process.
	 * @throws BuilderException the builder exception
	 */
	Schema discover() throws BuilderException;
}
