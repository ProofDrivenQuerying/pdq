package uk.ac.ox.cs.pdq.ui.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.AbstractXMLReader;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QNames;
//import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QueryReader;
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
// MR	private QueryReader queryReader;
	
	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 */
	public ObservableQueryReader(String schema) {
// MR		this.queryReader = new QueryReader(schema);
	}
	
	/**
	 * Read.
	 *
	 * @param in the in
	 * @return a conjunctive query read from the given input stream
	 */
// MR	@Override
	public ObservableQuery read(File in) {
		try {
		ConjunctiveQuery cq = IOManager.importQuery(in);
/* MR			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);*/
			return new ObservableQuery(in.getPath(), this.description, cq);
		} catch (JAXBException | FileNotFoundException e) {
			throw new ReaderException("Exception thrown while reading schema ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
/* MR	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		switch(QNames.parse(qName)) {
		case QUERY:
			this.name = this.getValue(atts, QNames.NAME);
			this.description = this.getValue(atts, QNames.DEPENDENCIES);
			break;
		}
		this.queryReader.startElement(uri, localName, qName, atts);
	}*/

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
/* MR	@Override
	public void endElement(String uri, String localName, String qName) {
		this.queryReader.endElement(uri, localName, qName);
	}*/
	
	/**
	 * For test purpose only.
	 *
	 * @param args the arguments
	 */
	public static void main(String... args) {
		try (
				InputStream sin = new FileInputStream("test/input/minimal-schema.xml");
				InputStream qin = new FileInputStream("test/input/query.xml")) {
/* MR			ObservableSchema s = new ObservableSchemaReader().read(sin);
			ObservableQuery q = new ObservableQueryReader(s.getSchema()).read(qin);
			new ObservableQueryWriter().write(System.out, q);*/
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}
}
