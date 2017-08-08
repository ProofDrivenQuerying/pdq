package uk.ac.ox.cs.pdq.datasources.builder;

import java.util.Properties;

import uk.ac.ox.cs.pdq.db.Schema;

/**
 * Common interface to all schema discoverers.
 * Creates a schema based on a database instance. Have to be implemented for each database manager like postgres, mysql and so on.
 * 
 * @author Gabor
 * @author Julien Leblay
 */
public interface SchemaDiscoverer {
	
	/**
	 * Gets the properties of the schema discoverer.
	 *
	 * @return the discoverer's properties.
	 */
	Properties getProperties();

	/**
	 * Sets the properties of the schema discoverer.
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
