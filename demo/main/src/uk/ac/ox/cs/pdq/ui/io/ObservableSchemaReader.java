package uk.ac.ox.cs.pdq.ui.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.SchemaReader;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.AbstractXMLReader;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QNames;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * Reads schemas from XML.
 * 
 * @author Julien LEBLAY
 * 
 */
public class ObservableSchemaReader extends AbstractXMLReader<ObservableSchema> {

	/** Logger. */
	private static Logger log = Logger.getLogger(ObservableSchemaReader.class);

	/** The name. */
	private String name;

	/** The description. */
	private String description;
	
	/** A conventional schema reader. */
	private SchemaReader schemaReader;

	/**
	 * Default constructor.
	 */
	public ObservableSchemaReader() {
		this.schemaReader = new SchemaReader();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.benchmark.io.AbstractReader#load(java.io.InputStream)
	 */
	@Override
	public ObservableSchema read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return new ObservableSchema(this.name, this.description, this.schemaReader.getSchema());
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new ReaderException("Exception thrown while reading schema ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		switch(QNames.parse(qName)) {
		case SCHEMA:
			this.name = this.getValue(atts, QNames.NAME);
			this.description = this.getValue(atts, QNames.DEPENDENCIES);
		}
		this.schemaReader.startElement(uri, localName, qName, atts);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {
		this.schemaReader.endElement(uri, localName, qName);
	}
	
	/**
	 * For test purpose only.
	 *
	 * @param args the arguments
	 */
	public static void main(String... args) {
		try (InputStream in = new FileInputStream("test/input/schema-mysql-tpch.xml")) {
			ObservableSchema s = new ObservableSchemaReader().read(in);
			new ObservableSchemaWriter().write(System.out, s);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}
}
