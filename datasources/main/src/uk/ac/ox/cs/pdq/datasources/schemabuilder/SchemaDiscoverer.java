// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.schemabuilder;

import java.util.Properties;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.builder.BuilderException;

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
	 *
	 * @return a Schema generated as per the discovery process.
	 * @throws BuilderException the builder exception
	 */
	Schema discover() throws BuilderException;
}
