package uk.ac.ox.cs.pdq.ui.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.AbstractXMLWriter;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QNames;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
//import uk.ac.ox.cs.pdq.io.xml.SchemaWriter;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * Writes an observable schemas to XML.
 * 
 * @author Julien LEBLAY
 * 
 */
public class ObservableSchemaWriter extends AbstractXMLWriter<ObservableSchema> {

	/** Logger. */
	private static Logger log = Logger.getLogger(ObservableSchemaWriter.class);
	
	/**
	 * Default constructor.
	 */
	public ObservableSchemaWriter() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.benchmark.io.AbstractWriter#save(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, ObservableSchema o) {
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}
	
	
	public void write(File file, ObservableSchema o) {
		try
		{
			PrintStream out = new PrintStream(file);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			this.writeSchema(file, o);
		}
		catch(FileNotFoundException e)
		{
		}
	}

	/**
	 * Writes the given schema to the given output.
	 *
	 * @param out the out
	 * @param s the s
	 */
	private void writeSchema(File file, ObservableSchema s) {
		try
		{
			IOManager.exportSchemaToXml(s.getSchema(), file);
		}
		catch(JAXBException e)
		{
		}
	}
}
