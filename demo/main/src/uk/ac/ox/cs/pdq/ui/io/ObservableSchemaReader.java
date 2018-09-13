package uk.ac.ox.cs.pdq.ui.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

//import uk.ac.ox.cs.pdq.SchemaReader;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.AbstractXMLReader;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QNames;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.ServiceGroup;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * Reads schemas from XML.
 * 
 * @author Julien LEBLAY
 * 
 */
public class ObservableSchemaReader {

	/** Logger. */
	private static Logger log = Logger.getLogger(ObservableSchemaReader.class);

	/** The name. */
	private String name;

	/** The description. */
	private String description;
	
	private Schema schema;
	
	/** A conventional schema reader, service group. */
	private ServiceGroup sgr;
	
	/** A conventional schema reader, service. */
	private Service sr;

	/**
	 * Default constructor.
	 */
	public ObservableSchemaReader() {
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.benchmark.io.AbstractReader#load(java.io.InputStream)
	 */
	public ObservableSchema read(File file) {
		try {
/* MR		JAXBContext jaxbContext1 = JAXBContext.newInstance(ServiceGroup.class);
			Unmarshaller jaxbUnmarshaller1 = jaxbContext1.createUnmarshaller();
			this.sgr = (ServiceGroup) jaxbUnmarshaller1.unmarshal(in1);
			JAXBContext jaxbContext2 = JAXBContext.newInstance(Service.class);
			Unmarshaller jaxbUnmarshaller2 = jaxbContext2.createUnmarshaller();
			this.sr = (Service) jaxbUnmarshaller2.unmarshal(in2);
			this.name = sr.getUrl();
			this.description = sr.getDocumentation();*/
			this.schema = IOManager.importSchema(file);
			this.name = file.getPath();
			return new ObservableSchema(this.name, this.description, this.schema);
		} catch (JAXBException | FileNotFoundException e) {
			throw new ReaderException("Exception thrown while reading schema ", e);
		}
	}

	/**
	 * For test purpose only.
	 *
	 * @param args the arguments
	 */
	public static void main(String... args) {
		try (InputStream in = new FileInputStream("test/input/schema-mysql-tpch.xml")) {
/* MR			ObservableSchema s = new ObservableSchemaReader().read(in);
			new ObservableSchemaWriter().write(System.out, s); */
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}
}
