package uk.ac.ox.cs.pdq.ui.io;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * Reads queries from XML.
 * 
 * @author Julien LEBLAY
 */
public class ObservableQueryReader {

	/** Logger. */
	private static Logger log = Logger.getLogger(ObservableQueryReader.class);

	/** The name. */
	private String name;

	/** The description. */
	private String description;

	/**  Query builder. */
	private QueryReader queryReader;
	
	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 */
	public ObservableQueryReader(Schema schema) {
		this.queryReader = new QueryReader(schema);
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
		} catch (JAXBException | FileNotFoundException e) {
			throw new ReaderException("Exception thrown while reading schema ", e);
		}
	}

	
	private String homepath(String path)
	{
		String home = System.getProperty("user.dir");
		return path.replace(home, "");
	}

}
