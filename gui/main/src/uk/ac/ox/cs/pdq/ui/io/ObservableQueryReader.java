// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.io;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;

/**
 * Reads queries from XML.
 * 
 * @author Julien LEBLAY
 */
public class ObservableQueryReader {

	/** The description. */
	private String description;

	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 */
	public ObservableQueryReader(Schema schema) {
	}
	
	/**
	 * Read.
	 *
	 * @param in the in
	 * @return a conjunctive query read from the given input stream
	 */
	public ObservableQuery read(File query) {
		try {
			ConjunctiveQuery cq = IOManager.importQuery(query);
			return new ObservableQuery(homepath(query.getPath()), this.description, cq);
		} catch (JAXBException | IOException e) {
			throw new ReaderException("Exception thrown while reading schema ", e);
		}
	}

	
	private String homepath(String path)
	{
		String home = System.getProperty("user.dir");
		return path.replace(home, "");
	}

}
